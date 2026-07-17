# user-service — Changelog

Design rationale lives in [`README.md`](./README.md). Branch: `user-service-refactor`
(Java 17, Spring Boot 4.0.0).

---

## Production-readiness follow-up (2026-07) — commit `7789065`

Resolved the remaining MED/LOW items from the production-readiness audit.

### HikariCP pool tuning
Added explicit pool configuration to `application-prod.yml` (previously running on HikariCP
defaults). Pool size is env-var overridable (`HIKARI_MAX_POOL_SIZE` / `HIKARI_MIN_IDLE`, defaults
10/5). `keepalive-time: 60s` prevents cloud DB (Supabase) from dropping idle connections.
`leak-detection-threshold: 60s` logs a warning if a connection is held longer than expected.

### API-docs lockdown
`springdoc.swagger-ui` and `springdoc.api-docs` are now **disabled in the base `application.yml`**
as a secure default (dev profile explicitly re-enables `api-docs`). The gateway prod profile
disables its own springdoc and removes the `/api/users/v1/api-docs` and `/api/pdf/v1/api-docs`
paths from `public-routes`, so accidental re-enablement on a downstream service would land behind
JWT rather than being publicly accessible.

### OSIV eliminated + N+1 fixed
Set `spring.jpa.open-in-view: false` globally. Every controller→mapper path that accessed lazy
`profile` or `accountStatus` associations outside a transaction was identified and fixed:

| Changed method | Was | Now |
|---|---|---|
| `UserServiceImpl.getUserById` | `findById` | `findByIdWithDetails` (JOIN FETCH) |
| `UserServiceImpl.updateUser` | `findById` | `findByIdWithDetails` |
| `UserServiceImpl.updatePassword` | `findUserByUsername` | `findByUsernameWithDetails` |
| `UserServiceImpl.getAllUsers` | `findAll(pageable)` | `findAllWithDetails` (JOIN FETCH + countQuery) |
| `UserController.getCurrentUser` | `getUserByEmail` | `getUserByEmailWithDetails` |

`findAllWithDetails` also resolves the N+1 on the admin paginated user listing (profile and account
status were previously lazy-loaded per row).

### AdminBootstrapper soft-delete blind spot
`existsByRole(UserRole.ADMIN)` is a derived query filtered by `@SQLRestriction("deleted_at IS NULL")`,
so a soft-deleted admin was invisible — the bootstrapper would re-seed on the next restart.
Replaced with `existsByRoleIncludingDeleted(String role)` — a native SQL `EXISTS` query that
bypasses the restriction and counts all rows regardless of `deleted_at`.

### `failed_login_attempts` schema fix (`V4` migration)
The V1 baseline declared `failed_login_attempts integer` (nullable, no default), while the
`UserAccountStatus` entity maps it to a primitive `int`. Any row with a `NULL` value would cause
Hibernate to NPE on read. `V4__fix_failed_login_attempts_not_null.sql` backfills `NULL → 0`,
then adds `NOT NULL DEFAULT 0`. The `@Column` annotation on `UserAccountStatus` is aligned to
`nullable = false`.

---

## Production hardening & single-VM deployment (2026-06)

A production-readiness review drove a batch of go-live fixes. The deployment target is a **single
low-powered VM** (no horizontal scaling), which shaped several decisions.

### Go-live blockers
- **Actuator** added — `health` (+ liveness/readiness probes) and `info` on the app port for
  container/load-balancer health checks.
- **JWT secret** now `${JWT_SECRET_KEY}` with **no fallback** in prod (fails fast if unset);
  **CORS** allowed-origins externalized to `${CORS_ALLOWED_ORIGINS}`.
- **Profile footgun closed** — `spring.profiles.active` is no longer pinned in `application.yml`.
  The `spring-boot-maven-plugin` activates `dev` for local runs; a prod deploy that forgets
  `SPRING_PROFILES_ACTIVE=prod` now fails fast on a missing datasource instead of silently booting
  dev with its weak fallback secret and seeded admin.

### Redis: prototyped, then removed
A Redis-backed rate limiter and distributed cache were added for horizontal scaling, then
**reverted** — the single-VM target makes them unnecessary, and the Redis limiter introduced a
fail-closed dependency on the auth path (a Redis blip would 500 every login). Back to the in-process
`FixedWindowRateLimiter` and Caffeine cache. Dead deps `sqlite-jdbc` and `hibernate-community-dialects`
were dropped at the same time (Postgres only).

### Security / correctness
- **Trusted-proxy client IP** — extracted a shared `ClientIpResolver` used by both `RateLimitFilter`
  and `AuditService`; `X-Forwarded-For` is honoured only from `app.rate-limit.trusted-proxies`,
  closing an IP-spoofing vector in both rate limiting and audit logs.
- **`ChangePasswordRequest`** now validates both fields as Base64 (the service decodes Base64; the
  old plain-text complexity regex made compliant requests fail to decode).
- **`PASSWORD_CHANGED` audit event** added (+ `V2__add_password_changed_audit_event.sql` extends the
  `audit_events.event_type` CHECK constraint) and emitted on self-service password change.
- **`assignRole`** made `@Transactional` (the load-modify-save was non-atomic).

### Observability / ops
- **MDC correlation id** — `CorrelationIdFilter` reads/generates `X-Correlation-Id`, stores it as
  `requestId` in MDC, and `logging.pattern.level` emits it on every log line. Previously the id was
  set but never logged.
- **Graceful shutdown** (`server.shutdown: graceful`) drains in-flight requests on rolling restarts.
- **PII** — login no longer logs the user's email.

### API docs path fix
SpringDoc's default `/v3/api-docs` returned **400** under path-segment versioning (`v3` → unsupported
version `3`), and a non-version path like `/openapi` also fails (not parseable as a version). The
OpenAPI path is now **`/v1/api-docs`** in user-service and pdf-service, with the gateway's
public-routes allowlist and swagger aggregator URLs updated to match. Actuator is unaffected (served
by a separate `WebMvcEndpointHandlerMapping`).

> **Still open before go-live:** no integration/context-load test exists (nothing verifies the app
> boots with Flyway + the full filter chain — the class of failure that a stale `common` artifact
> caused at runtime). `rememberMe` remains a no-op; `getUserByUsername` cache has no live caller.

---

## Native API versioning (2026-06)

Adopted **Spring Framework 7 / Spring Boot 4 native API versioning** in place of the hardcoded
`/v1` URL prefixes.

- **Strategy:** path segment — the `/v1` URL is kept but is now framework-managed. A global
  `/{version}` path prefix (scoped to the controller package) lets patterns consume the segment;
  `usePathSegment(0)` resolves the version. ⚠️ Note that version *validation* still applies to every
  request through `RequestMappingHandlerMapping` (including SpringDoc); the OpenAPI path is therefore
  `/v1/api-docs`, not `/v3/api-docs` — see the production-hardening section below.
- **Parser:** shared `common` `PrefixedSemanticApiVersionParser` strips a leading `v`/`V` then
  delegates to `SemanticApiVersionParser`, so `v1` compares semantically against `version = "1+"`.
- **Controllers:** the five API interfaces drop the literal `/v1` and declare `version = "1+"` at
  the type level (Spring's `VersionRequestCondition.combine` propagates it to every handler method).
- **Lenient resolution:** missing version defaults to `1` (`setVersionRequired(false)`); an unknown
  version (e.g. `/v2/...`) now returns **400** — `GlobalExceptionHandler` gained a
  `ResponseStatusException` handler so Spring's `InvalidApiVersionException` is no longer masked as 500.
- **No external contract change:** user-service URLs are byte-for-byte identical, so the gateway
  routes, public-routes, email links, rate-limiter and existing tests are unaffected.
- **Tests:** `PrefixedSemanticApiVersionParserTest` (common) and `ApiVersioningTest` (drives
  `RequestMappingHandlerMapping` directly) verify prefixing, type-level version propagation, and the
  400-on-unknown-version path.

---

## Hardening, completion & migrations (2026-06)

A fresh review of the (by then feature-complete) service found real bugs and gaps; all were
fixed, the larger infra items were added, and a broad test suite was introduced
(`src/test` was previously empty).

### Bug fixes
| Area | Fix |
|------|-----|
| Profile update / mapping | Self-service update was non-functional and admin update NPE'd; profile fields were dropped. `UserController.updateCurrentUser` now resolves the user by email; `UserMapper` flattens `profile.*`, ignores `role`/`email`, and uses `NullValuePropertyMappingStrategy.IGNORE`. Added `middleName`. |
| Account lockout | Use the configured lock duration (not the remember-me window); reset the failed-attempt counter when an expired lock is re-evaluated (`UserAccountStatus.clearExpiredLock`). |
| Email verification | Now enforceable at login via `app.auth.require-verified-email` (default false); `emailVerified` surfaced on `AuthResponse`. |
| HTTP status | `UserException` carries an `HttpStatus`; added `UserNotFoundException` (404) / `DuplicateUserException` (409) — business errors no longer return 200. |
| Enumeration | `forgot-password` (and resend-verification) return a uniform response for unknown emails. |
| Tokens | Refresh-token **rotation** with reuse detection (revokes all sessions); `verifyEmail` consumes its one-time token; `JwtService.validateToken` returns `false` on expired/invalid tokens instead of throwing 401. |

### Features / infrastructure
- **Config-driven rate limiting** — limited paths, capacity, window and cleanup interval are all
  in `app.rate-limit.*`; added a scheduled stale-window eviction (bounded memory) and extended
  coverage to reset-password / register / refresh.
- **Credential lifecycle** — `UserCredentialRepository` + scheduled `CredentialCleanupJob`
  (`@EnableScheduling`) purge expired credentials; the `active` flag is now meaningful.
- **Admin/account endpoints** — `POST /admin/users/{id}/unlock`, `PATCH /admin/users/{id}/enabled`,
  `POST /auth/resend-verification`.
- **Bootstrap admin** — `AdminBootstrapper` idempotently seeds an `ADMIN` from
  `app.bootstrap-admin.*` when none exists (solves the first-admin chicken-and-egg).
- **Flyway / Postgres** — introduced Flyway with `V1__baseline.sql`; `ddl-auto` set to `validate`
  in dev and prod; dev standardised on Postgres. The migration notes below are now obsolete.
- **Tests** — broad unit/regression suite (services, mapper, JWT, rate-limiter, exception handler,
  controllers, bootstrapper).

---

## Refactor → feature-complete (earlier on `user-service-refactor`)

| # | Area | Change |
|---|------|--------|
| 1 | Token storage | Stored tokens SHA-256 hashed (`token varchar(64)`); raw JWTs returned via `@Transient` carriers |
| 2 | Transactions | `@Transactional`(+`readOnly`) on `UserServiceImpl`; `noRollbackFor` so lockout persists |
| 3 | Soft delete | `@SQLRestriction("deleted_at IS NULL")` on `UserEntity`; marker on `users` |
| 4 | Token purpose | `validateToken` enforces token *type* (reset/verify can't accept an access token) |
| 5 | Email | Profile-switched `LoggingEmailService` (!prod) / `SmtpEmailService` (prod) |
| 6 | Authorization | RBAC (`Permission`, `RolePermissions`, service/impl + controllers) |
| 7 | Rate limiting | Dependency-free fixed-window limiter + filter |
| 8 | Pagination | Admin paginated user listing |
| 9 | Versioning | `/v1` on all controllers + gateway public-routes |
| 10 | Audit logging | `audit_events` + `AuditService` (`REQUIRES_NEW`) |
| 11 | Caching | Caffeine `users` cache + eviction on mutations |
| 12 | OpenAPI | springdoc 3.0.0 + bearer-JWT scheme + `@Tag`s |
| 13 | Containerization | Root `compose.yaml` + `spring-boot-docker-compose` dev auto-start |

The "god object" `UserEntity` had already been split into `UserProfile` / `UserAccountStatus` /
`UserCredential`; N+1 solved with LAZY associations + tiered fetch-join repository methods; auth
config externalized into `@ConfigurationProperties` records.

> Historical note: schema migration was previously managed by Hibernate `ddl-auto` and an earlier
> review tracker (`REVIEW_REMARKS.md`) is no longer maintained — its items are all resolved and
> superseded by this changelog. Schema is now owned by Flyway, so the old manual prod-migration
> steps (add `users.deleted_at`, resize `user_credentials.token`, create `audit_events`, …) are
> captured in `V1__baseline.sql`.
