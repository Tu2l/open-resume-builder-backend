# user-service — Changelog

Design rationale lives in [`README.md`](./README.md). Branch: `user-service-refactor`
(Java 17, Spring Boot 4.0.0).

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
