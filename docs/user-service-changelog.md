# User Service — Completion Changelog

Changes that took `user-service` to feature-complete, addressing the open items in
[`../user-service/REVIEW_REMARKS.md`](../user-service/REVIEW_REMARKS.md) plus the Authorization
controller and database containerization. Design rationale lives in
[`user-service-design.md`](./user-service-design.md).

Branch: `user-service-refactor` · Build: Java 17, Spring Boot 4.0.0.

---

## Summary by area

| # | Area | Change | Review item |
|---|------|--------|-------------|
| 1 | Token storage | Stored tokens SHA-256 hashed (`token varchar(64)`); raw JWTs returned via `@Transient` carriers | #3 |
| 2 | Transactions | `@Transactional`(+`readOnly`) on `UserServiceImpl`; `noRollbackFor` so lockout persists | #4 |
| 3 | Soft delete | `@SQLRestriction("deleted_at IS NULL")` on `UserEntity`; marker moved to `users` | #8 |
| 4 | Token purpose | `validateToken` enforces token *type* (reset/verify can't accept an access token) | #1/#3 |
| 5 | Email | Profile-switched `LoggingEmailService` (!prod) / `SmtpEmailService` (prod); flows wired to work | #1 |
| 6 | Authorization | Enabled controller + RBAC (`Permission`, `RolePermissions`, service/impl) | controllers |
| 7 | Rate limiting | Dependency-free fixed-window limiter + filter on auth endpoints (429) | #5 |
| 8 | Pagination | `GET /v1/all` admin paginated listing | #9 |
| 9 | Versioning | `/v1` on all controllers + gateway public-routes (dev+prod) + `.http` | #7 |
| 10 | Audit logging | `audit_events` + `AuditService` (`REQUIRES_NEW`) across all flows | #13 |
| 11 | Caching | Caffeine `users` cache + eviction on mutations | #14 |
| 12 | OpenAPI | springdoc 3.0.0 + bearer-JWT scheme + `@Tag`s | #15 |
| 13 | Containerization | Root `compose.yaml` + `spring-boot-docker-compose` dev auto-start | add-on |

Beyond the review: the account-lockout was silently broken (the failed-attempt increment rolled back
with the thrown exception); fixed via `noRollbackFor` + reset-on-success. The password-reset /
email-verification flows were also completed (reset credential is now persisted; registration emails
a verification token; verification flips `emailVerified`).

---

## File-by-file

**New files (user-service)**
- `audit/AuditEvent.java`, `audit/AuditEventType.java`, `audit/AuditEventRepository.java`, `audit/AuditService.java`
- `authorization/Permission.java`, `authorization/RolePermissions.java`
- `config/MailProperties.java`, `config/RateLimitProperties.java`, `config/CacheConfig.java`, `config/OpenAPIConfig.java`
- `model/response/AuthorizationResponse.java`
- `ratelimit/FixedWindowRateLimiter.java`, `ratelimit/RateLimitFilter.java`
- `service/impl/AuthorizationServiceImpl.java`, `service/impl/LoggingEmailService.java`, `service/impl/SmtpEmailService.java`

**Modified**
- `common/.../util/CommonUtil.java` — `sha256Hex(...)`
- `entity/UserEntity.java` — `deletedAt` + `@SQLRestriction`; `@Transient plainAccessToken/plainRefreshToken`
- `entity/UserCredential.java` — `token` → `varchar(64)` (hash); `entity/UserAccountStatus.java` — removed `deletedAt`
- `service/impl/AuthenticationServiceImpl.java` — hashing, lockout fix, audit, email flow completion
- `service/impl/UserServiceImpl.java` — transactions, soft-delete write, pagination, caching
- `service/impl/JwtServiceImpl.java` — token-type validation
- `service/AuthorizationService.java`, `service/UserService.java` — new methods
- `controller/AuthorizationController.java` + `controller/api/AuthorizationApi.java` — enabled & implemented
- `controller/UserController.java` (`/v1`, `/all`, audit), `controller/api/AuthenticationApi.java` (`/v1/auth`)
- `utils/AuthResponseBuilder.java` — reads raw tokens from transient fields
- `pom.xml` — `spring-boot-starter-mail`, `-cache`, caffeine, springdoc 3.0.0, `spring-boot-docker-compose`
- `resources/application-{dev,prod}.yml` — `app.mail.*`, `app.rate-limit.*`, `spring.docker.compose.*`, `spring.mail.*`

**Removed**
- `service/impl/EmailServiceImpl.java` (stub returning `false`)

**Other modules**
- `compose.yaml` (root, new); `pdf-service/pom.xml` + `application-dev.yml` (docker-compose dev)
- `gateway-service/.../application-{dev,prod}.yml` — public route `/api/users/auth/**` → `/api/users/v1/auth/**`
- `etc/requests/user-service.http` — updated to `/v1` + new flows

---

## ⚠️ Migration notes (production)

Dev uses `spring.jpa.hibernate.ddl-auto: create`, so the schema changes below are applied
automatically. **Prod uses `validate`** and will fail to start until a migration is applied:

1. `ALTER TABLE users ADD COLUMN deleted_at timestamp;`
2. `ALTER TABLE user_credentials ALTER COLUMN token TYPE varchar(64);` — **destructive**: existing
   plaintext tokens are now invalid (they aren't SHA-256 hashes). Plan to clear `user_credentials`
   (forces re-login) as part of the rollout.
3. `ALTER TABLE user_account_status DROP COLUMN deleted_at;`
4. Create `audit_events` (+ indexes on `user_id`, `event_type`, `created_at`).

Recommend introducing Flyway/Liquibase to manage these. New prod env vars: `MAIL_HOST`,
`MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM_ADDRESS`, `FRONTEND_BASE_URL`.

---

## Verification

`mvn clean install` green; context boots (Hibernate 7 / Tomcat 11); schema correct; rate-limit 429
and Swagger confirmed live; register persistence confirmed. Full HTTP flows that hit the
`REQUIRES_NEW` audit path require PostgreSQL (`docker compose up -d`) — the SQLite smoke harness is
single-writer and can't model the second connection.
