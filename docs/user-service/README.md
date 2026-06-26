# user-service

**Port:** 8091 · **Context-path:** `/users` · **Gateway prefix:** `/api/users/v1/...`
**Stack:** Java 17, Spring Boot 4.0.0 (Spring 7 / Hibernate 7 / Tomcat 11), PostgreSQL, Flyway.

Owns user **identity, authentication, authorization and account lifecycle**. It sits behind
`gateway-service`, which marks each request `PUBLIC`/`PROTECTED` (`X-Request-Type`) and injects
`X-User-Email` / `X-User-Username` / `X-User-Role` for validated tokens.

See also: [changelog](./changelog.md) · [project docs](../README.md).

---

## Capabilities

- Registration, login, JWT access/refresh, logout
- **Refresh-token rotation** with reuse detection (revokes all sessions on replay)
- Password reset & email verification (single-use tokens; profile-switched email delivery)
- Optional **email-verification gate** at login (`app.auth.require-verified-email`)
- Profile CRUD, admin user listing (paginated), soft delete
- Role-based access control (RBAC)
- Admin account ops: unlock, enable/disable; **bootstrap admin** seeding
- Account security: BCrypt, login-attempt lockout, SHA-256 hashed token storage, config-driven rate limiting
- Scheduled credential cleanup; security audit trail; Caffeine caching; OpenAPI/Swagger

---

## Architecture

```mermaid
flowchart TB
    subgraph web["web layer"]
        RLF["RateLimitFilter<br/>(config-driven paths)"]
        AC["AuthenticationController"]
        UC["UserController"]
        AUC["AdminUserController"]
        AZC["Authorization controllers"]
    end
    subgraph svc["service layer"]
        AS["AuthenticationServiceImpl"]
        US["UserServiceImpl<br/>(UserService + AdminUserService)"]
        JS["JwtServiceImpl"]
        PS["PasswordService"]
        ES["EmailService<br/>(profile-switched)"]
        AZS["AuthorizationServiceImpl"]
        AUD["AuditService (REQUIRES_NEW)"]
        CCJ["CredentialCleanupJob @Scheduled"]
        BA["AdminBootstrapper (startup)"]
    end
    subgraph data["persistence"]
        UR["UserRepository"]
        UCR["UserCredentialRepository"]
        AER["AuditEventRepository"]
        DB[("PostgreSQL<br/>Flyway-managed")]
    end
    RLF --> AC & UC & AUC & AZC
    AC --> AS
    UC --> US
    AUC --> US
    AZC --> AZS
    AS --> US & JS & PS & ES & AUD
    US --> UR
    AS --> UCR
    CCJ --> UCR
    BA --> UR
    AUD --> AER
    UR & UCR & AER --> DB
```

Cross-cutting: `@ConfigurationPropertiesScan` binds all `app.*` records; `@EnableScheduling`
drives cleanup; `@EnableCaching` (Caffeine `users` cache); springdoc serves
`/users/swagger-ui.html`.

---

## Data model (ERD)

```mermaid
erDiagram
    users ||--|| user_profiles : "profile_id (unique)"
    users ||--|| user_account_status : "account_status_id (unique)"
    users ||--o{ user_credentials : "user_id"
    users ||..o{ audit_events : "user_id (loose ref)"

    users {
        bigint id PK
        varchar(100) email UK
        varchar(50) username UK
        varchar(20) role "USER|ADMIN|MODERATOR|GUEST"
        boolean oauth_user
        varchar(255) password "BCrypt hash"
        bigint profile_id FK
        bigint account_status_id FK
        timestamp deleted_at "soft-delete (@SQLRestriction)"
        timestamp created_at
        timestamp updated_at
    }
    user_profiles {
        bigint id PK
        varchar(50) first_name
        varchar(50) middle_name
        varchar(50) last_name
        varchar(15) phone_number
        timestamp created_at
        timestamp updated_at
    }
    user_account_status {
        bigint id PK
        boolean enabled
        boolean email_verified
        boolean phone_verified
        int failed_login_attempts
        timestamp account_locked_until
        timestamp last_login_at
        timestamp created_at
        timestamp updated_at
    }
    user_credentials {
        bigint id PK
        bigint user_id FK
        varchar(64) token "SHA-256 hex of JWT"
        varchar(20) token_type "ACCESS|REFRESH|PASSWORD_RESET|EMAIL_VERIFICATION"
        boolean active "false once rotated"
        varchar(100) issuer
        timestamp issued_at
        timestamp expires_at
        timestamp created_at
        timestamp updated_at
    }
    audit_events {
        bigint id PK
        bigint user_id
        varchar(40) event_type
        varchar(64) ip_address
        varchar(512) user_agent
        varchar(512) details
        timestamp created_at
    }
```

The schema is owned by **Flyway** (`db/migration/V1__baseline.sql`); `ddl-auto` is `validate`
in dev and prod. `users.deleted_at` drives soft delete via `@SQLRestriction("deleted_at IS NULL")`.

### Entity relationships (UML)

```mermaid
classDiagram
    class UserEntity {
        +Long id
        +String email
        +String username
        +UserRole role
        +String password
        +LocalDateTime deletedAt
        ~String plainAccessToken
        ~String plainRefreshToken
        +addUserCredential(c)
        +getCredentialByTokenTypeAndToken(type, hash)
        +removeCredentialByToken(hash)
        +clearSensitiveTokens()
    }
    class UserProfile {
        +String firstName
        +String middleName
        +String lastName
        +String phoneNumber
        +getFullName()
    }
    class UserAccountStatus {
        +boolean enabled
        +boolean emailVerified
        +int failedLoginAttempts
        +LocalDateTime accountLockedUntil
        +isAccountLocked()
        +isLockExpired()
        +lockAccount(min)
        +clearExpiredLock()
        +unlockAccount()
    }
    class UserCredential {
        +String token
        +JwtTokenType tokenType
        +Boolean active
        +LocalDateTime expiresAt
        +isTokenExpired()
    }
    UserEntity "1" *-- "1" UserProfile : profile
    UserEntity "1" *-- "1" UserAccountStatus : accountStatus
    UserEntity "1" *-- "0..*" UserCredential : credentials
```

---

## API surface

All paths are versioned under `/v1` and reached through the gateway as `/api/users/v1/...`.
Only `/api/users/v1/auth/**` is public; everything else is `PROTECTED`. Admin endpoints
additionally enforce `ADMIN` (header role check / `AuthTokenService.verifyRole`).

> **API versioning is framework-managed (Spring Framework 7 native).** The `/v1` segment is no
> longer hardcoded into each `@RequestMapping`; instead `ApiVersioningConfiguration` adds a global
> `/{version}` path prefix and resolves the version from path-segment 0 (`usePathSegment(0)`),
> while controllers declare `version = "1+"` (a baseline that keeps serving future versions until
> explicitly overridden). The shared `common` `PrefixedSemanticApiVersionParser` strips the leading
> `v` so `v1` is compared semantically. Resolution is lenient — a missing version defaults to `1`;
> an unknown version (e.g. `/v2/...`) returns **400** via `GlobalExceptionHandler`'s
> `ResponseStatusException` handler. URLs are unchanged from the previous hardcoded scheme.

| Method | Gateway path | Access | Purpose |
|--------|--------------|--------|---------|
| POST | `/api/users/v1/auth/register` | public · rate-limited | Register (issues tokens + sends verification email) |
| POST | `/api/users/v1/auth/authenticate` | public · rate-limited | Login |
| POST | `/api/users/v1/auth/refresh-token` | public · rate-limited | Rotate tokens |
| POST | `/api/users/v1/auth/logout` | public | Invalidate token |
| POST | `/api/users/v1/auth/forgot-password` | public · rate-limited | Send reset link (enumeration-safe) |
| POST | `/api/users/v1/auth/reset-password` | public · rate-limited | Complete reset |
| GET | `/api/users/v1/auth/verify-email?token=` | public | Verify email (single-use) |
| POST | `/api/users/v1/auth/resend-verification` | public | Resend verification (enumeration-safe) |
| GET | `/api/users/v1/me` | protected | Current profile |
| PUT | `/api/users/v1/me` · `/me/password` | protected | Update profile / password |
| DELETE | `/api/users/v1/me` | protected | Soft-delete self |
| GET | `/api/users/v1/admin/users?page&size` | admin | Paginated user list |
| GET/PUT/DELETE | `/api/users/v1/admin/users/{id\|username}` | admin | Admin user ops |
| POST | `/api/users/v1/admin/users/{id}/unlock` | admin | Clear lock + reset attempts |
| PATCH | `/api/users/v1/admin/users/{id}/enabled?enabled=` | admin | Enable/disable account |
| GET/POST | `/api/users/v1/authorize/...` | protected/admin | RBAC introspection & role assignment |

---

## Key flows

### Registration → email verification

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant A as AuthenticationServiceImpl
    participant DB as DB
    participant E as EmailService
    C->>A: register(username, email, base64 password)
    A->>A: reject if username/email exists (409 DuplicateUserException)
    A->>A: hash password, mint ACCESS+REFRESH+EMAIL_VERIFICATION JWTs
    A->>DB: save user (+profile +status) and sha256(token) credentials
    A->>E: sendVerificationEmail(link)
    A-->>C: AuthResponse { accessToken, refreshToken, emailVerified=false }
    C->>A: GET verify-email?token=...
    A->>A: validateToken(type=EMAIL_VERIFICATION) + match stored hash
    A->>DB: set email_verified=true, remove the verification credential (single-use)
    A-->>C: 200 verified
```

### Login: lockout + optional verification gate

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant A as AuthenticationServiceImpl
    participant S as UserAccountStatus
    C->>A: authenticate(email, password, rememberMe)
    A->>S: if lock expired → clearExpiredLock() (reset counter)
    A->>A: reject if locked or disabled (401)
    alt wrong password
        A->>S: incrementFailedLoginAttempts()
        A->>S: at max → lockAccount(accountLockDurationMinutes)
        A-->>C: 401 (audit LOGIN_FAILED / ACCOUNT_LOCKED)
    else correct password
        opt require-verified-email = true
            A->>A: reject if !emailVerified (401)
        end
        A->>S: unlockAccount() + lastLoginAt
        A->>A: mint + store tokens (sha256)
        A-->>C: 200 AuthResponse
    end
```

> The lockout transaction is annotated `@Transactional(noRollbackFor = AuthenticationException.class)`
> so the failed-attempt increment persists despite the thrown exception.

### Refresh-token rotation + reuse detection

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant A as AuthenticationServiceImpl
    participant DB as DB
    C->>A: refresh(refreshToken, username)
    A->>DB: find REFRESH credential where token = sha256(refreshToken)
    alt not found / expired
        A-->>C: 401 Invalid refresh token
    else found but active = false (already rotated)
        A->>DB: clearSensitiveTokens() (revoke ALL sessions)
        A-->>C: 401 Refresh token reuse detected (audit TOKEN_REUSE_DETECTED)
    else valid & active
        A->>DB: mark old refresh inactive, store new REFRESH + ACCESS (sha256)
        A-->>C: 200 AuthResponse { new accessToken, new refreshToken }
    end
```

### Token storage (hashing)

Tokens are stored as a **SHA-256 hex digest** (`user_credentials.token varchar(64)`), chosen
over BCrypt because lookups are deterministic ("hash the incoming token and compare") and BCrypt
truncates >72 bytes. The raw JWT for the current operation rides on two `@Transient` fields of
`UserEntity` (`plainAccessToken`/`plainRefreshToken`), read by `AuthResponseBuilder`; both are
excluded from `@ToString`/`@EqualsAndHashCode` so tokens never reach logs.

### Bootstrap admin (startup)

```mermaid
sequenceDiagram
    autonumber
    participant App as Startup (ApplicationRunner)
    participant AB as AdminBootstrapper
    participant DB as DB
    App->>AB: run()
    alt app.bootstrap-admin.enabled = false
        AB-->>App: skip
    else enabled
        AB->>DB: existsByRole(ADMIN)?
        alt admin exists / name taken / config incomplete
            AB-->>App: skip (idempotent)
        else none yet
            AB->>DB: save ADMIN (enabled, emailVerified, BCrypt password)
        end
    end
```

---

## Subsystem notes

- **RBAC** — static in-code model: `Permission` enum + `RolePermissions` (ADMIN=all,
  MODERATOR/USER subsets, GUEST=`PDF_READ`). `(resource, action)` → `Permission.from(...)`.
- **Rate limiting** — dependency-free fixed-window limiter (`FixedWindowRateLimiter`) applied by
  `RateLimitFilter`; **paths, capacity, window and cleanup interval are all config**
  (`app.rate-limit.*`). A `@Scheduled` sweep evicts stale windows (bounded memory). Per-instance;
  back with Redis for multi-node.
- **Credential lifecycle** — `CredentialCleanupJob` (`@Scheduled`) purges expired credentials via
  `UserCredentialRepository.deleteByExpiresAtBefore`; rotated-but-unexpired refresh tokens are kept
  (`active=false`) so reuse detection still works.
- **Errors** — `UserException` carries an `HttpStatus`; `UserNotFoundException` → 404,
  `DuplicateUserException` → 409, others → 400 (`GlobalExceptionHandler`).
- **Email** — `LoggingEmailService` (`!prod`) logs links; `SmtpEmailService` (`prod`) sends via SMTP.
- **Audit** — `AuditService.log` runs `REQUIRES_NEW` so events persist even when the business
  transaction rolls back (e.g. `LOGIN_FAILED`).
- **Caching** — Caffeine `users` cache (5-min TTL); mutations evict.

---

## Configuration reference (`app.*`)

```yaml
app:
  auth:
    max-failed-login-attempts: 5
    account-lock-duration-minutes: 15
    remember-me-token-validity-minutes: 14
    require-verified-email: false        # gate login on verified email
    jwt:
      secret-key: ${JWT_SECRET_KEY}
      access-token-expiration-minutes: 15
      refresh-token-expiration-days: 7
      issuer: resume-builder-app
  rate-limit:
    enabled: true
    capacity: 5                          # dev=5, prod=10
    window-seconds: 60
    cleanup-interval-ms: 300000
    limited-paths:                       # config-driven; no hardcoded suffixes
      - /auth/authenticate
      - /auth/forgot-password
      - /auth/reset-password
      - /auth/register
      - /auth/refresh
  mail:                                  # MailProperties (link building)
    from-address: ...
    frontend-base-url: ...
    verification-path: /verify-email?token=
    password-reset-path: /reset-password?token=
  bootstrap-admin:                       # seed first admin if none exists
    enabled: true                        # dev=true; prod gated on BOOTSTRAP_ADMIN_ENABLED
    username: admin
    email: admin@resume-builder.local
    password: Admin@12345
spring:
  jpa.hibernate.ddl-auto: validate       # Flyway owns the schema
  flyway: { enabled: true, baseline-on-migrate: true }
  mail: { host, port, username, password }   # prod SMTP transport (env-driven)
```

---

## Build, run & test

```bash
docker compose up -d                       # Postgres (postgres:17-alpine)
mvn -pl user-service -am test              # unit/regression suite (offline-friendly: add -o)
mvn -pl user-service spring-boot:run       # :8091  (run gateway for :8080)
```

Swagger UI: `http://localhost:8091/users/swagger-ui.html`. Exercise flows with
`etc/requests/user-service.http`. Tests are pure unit/Mockito (services, mapper, JWT, rate-limiter,
exception handler, controllers, bootstrapper).
