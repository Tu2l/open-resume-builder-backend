# Agent Knowledge Base

Each `.agent-memory.b64` file in this directory is a Base64-encoded Markdown document.
Decode with `base64 -d <file>` to read.

| File | Contents |
|---|---|
| `kb-01-architecture.agent-memory.b64` | Module layout, tech stack, request flow, URL versioning |
| `kb-02-common.agent-memory.b64` | CommonConstants, JwtUtil API, CommonUtil, shared models |
| `kb-03-gateway.agent-memory.b64` | Routes, filter chain (GlobalRequestFilter → AuthGatewayFilter), public vs protected |
| `kb-04-user-entities.agent-memory.b64` | UserEntity / UserCredential / UserProfile / UserAccountStatus, repositories, Flyway, soft-delete |
| `kb-05-user-auth.agent-memory.b64` | All auth flows (register/login/logout/refresh/forgot-pwd/verify-email), RBAC matrix, audit events |
| `kb-06-user-config.agent-memory.b64` | All config properties (auth/rate-limit/mail/cors), rate limiting, required env vars |

## Quick reference

- **Port map:** gateway `:8080`, user-service `:8091`, pdf-service `:8090`
- **Security model:** gateway validates JWT and injects `X-User-{Username,Email,Role}` headers; downstream services trust these headers
- **Tokens stored as:** SHA-256 hex in `user_credentials.token` — never plaintext
- **Passwords transmitted as:** Base64-encoded; BCrypt-hashed at rest
- **Soft delete:** `@SQLRestriction("deleted_at IS NULL")` on UserEntity; partial unique indexes on username/email
