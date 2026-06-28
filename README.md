# Resume Builder Backend

A modern, microservices-based backend system for building and managing resumes with PDF generation capabilities.

## AI Agent Knowledge Base

Structured context for AI agents is in `.agent-memory/`. Each file is Base64-encoded Markdown (`base64 -d <file>` to read):

- `kb-01-architecture` — module layout, request flow, URL versioning
- `kb-02-common` — CommonConstants, JwtUtil, shared models
- `kb-03-gateway` — routes, filter chain, public vs protected routes
- `kb-04-user-entities` — entities, repos, Flyway, soft-delete
- `kb-05-user-auth` — all auth flows, RBAC matrix, audit events
- `kb-06-user-config` — config properties, rate limiting, required env vars

See `.agent-memory/README.md` for the full index.

## Architecture

This project follows a **multi-module microservices architecture** with centralized dependency management:

```
resume-builder-backend/
├── common/              # Shared utilities and models
├── gateway-service/     # API Gateway (entry point)
├── pdf-service/         # PDF generation service
├── user-service/        # User management and authentication
└── pom.xml             # Parent POM with centralized dependencies
```

All external traffic flows through the gateway at port `8080`. Services are not exposed directly in production.

## Technology Stack

### Core Technologies

- **Java 17** - LTS version with modern language features
- **Spring Boot 4.0.0** - Latest Spring Boot framework
- **Spring Cloud 2025.1.0** - Microservices toolkit
- **Maven** - Dependency management and build automation
- **PostgreSQL** (dev & production) - Primary database

### Key Libraries

- **Spring Cloud Gateway (WebFlux)** - Reactive API gateway
- **Spring Security (crypto)** - BCrypt password hashing
- **springdoc-openapi 3.0.0** - OpenAPI 3 / Swagger UI
- **Lombok 1.18.42** - Reduce boilerplate code with annotations
- **MapStruct 1.6.3** - Type-safe bean mapping
- **JJWT 0.13.0** - JWT token generation and validation
- **Flyway** - Database migrations (user-service)
- **Caffeine** - In-process caching (user-service)

> 📚 **Detailed, service-wise documentation** (architecture, flows, UML, ERDs) lives in
> [`docs/`](./docs/README.md).

### External Tools

- **wkhtmltopdf** - HTML to PDF conversion engine

## Services

### 1. Common Module

Shared library containing:

- Base request/response models
- JWT utilities
- HTML sanitization (XSS protection)
- Common enums and constants
- Response factory

---

### 2. Gateway Service

**Port:** 8080  
**Status:** Implemented

Single entry point for all API traffic. Handles JWT validation and request routing.

#### Routes

| Incoming Path | Forwards To | Auth |
|--------------|-------------|------|
| `/api/users/**` | `user-service:8091/users/**` | JWT required |
| `/api/pdf/**` | `pdf-service:8090/pdf/**` | Public |
| `/api/users/v1/auth/**` | `user-service:8091/users/v1/auth/**` | Public |

#### API Documentation

The gateway aggregates OpenAPI specs from all services into a single Swagger UI:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- Tabs: **User Service**, **PDF Service**

Individual service `/v3/api-docs` endpoints are accessible via the gateway at:
- `http://localhost:8080/api/users/v3/api-docs`
- `http://localhost:8080/api/pdf/v3/api-docs`

---

### 3. PDF Service

**Port:** 8090  
**Context path:** `/pdf`  
**Status:** Implemented

Generate PDFs from HTML content with comprehensive configuration options.

#### Features

- Synchronous PDF generation
- Asynchronous PDF generation with background processing
- PDF persistence to database
- 33+ page size configurations (A0–A9, B0–B10, Letter, Legal, etc.)
- Custom margins and orientation settings
- HTML sanitization (XSS protection)

#### API Endpoints

All endpoints are accessed via the gateway at `/api/pdf/...`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/generate` | Generate PDF synchronously (returns base64) |
| POST | `/v1/generate/save` | Generate and persist PDF to DB |
| POST | `/v1/generate/async` | Start async PDF generation |
| GET | `/v1/{id}` | Retrieve saved PDF by ID |

---

### 4. User Service

**Port:** 8091  
**Context path:** `/users`  
**Status:** Implemented

User management, authentication, and authorization service.

#### Features

- JWT-based authentication (access + refresh tokens) with **refresh-token rotation + reuse detection**
- BCrypt password hashing; SHA-256 hashed token storage
- Role-based access control (RBAC) — USER, ADMIN, MODERATOR, GUEST
- **Config-driven** fixed-window rate limiting (paths/capacity/window in `app.rate-limit.*`)
- Email verification (single-use, optionally enforced at login) and password reset flows
- Account lockout after failed login attempts; admin unlock / enable-disable
- **Bootstrap admin** seeding (`app.bootstrap-admin.*`)
- Soft delete / account deactivation; scheduled credential cleanup
- Flyway-managed schema (`ddl-auto: validate`)

#### API Endpoints

All endpoints are accessed via the gateway at `/api/users/...`

**Authentication** (public)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/auth/register` | Register new user |
| POST | `/v1/auth/authenticate` | Login |
| POST | `/v1/auth/refresh-token` | Refresh access token |
| POST | `/v1/auth/logout` | Invalidate refresh token |
| POST | `/v1/auth/forgot-password` | Request password reset email (enumeration-safe) |
| POST | `/v1/auth/reset-password` | Reset password with token |
| GET | `/v1/auth/verify-email?token=` | Verify email address (single-use) |
| POST | `/v1/auth/resend-verification` | Resend verification email (enumeration-safe) |

**User Management** (requires Bearer token)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/me` | Get current user profile |
| PUT | `/v1/me` | Update current user profile |
| PUT | `/v1/me/password` | Change password |
| DELETE | `/v1/me` | Deactivate current account |
| GET | `/v1/admin/users` | List all users — paginated (Admin) |
| GET | `/v1/admin/users/{id}` | Get user by ID (Admin) |
| PUT | `/v1/admin/users/{id}` | Update user by ID (Admin) |
| DELETE | `/v1/admin/users/{username}` | Delete user (Admin) |
| POST | `/v1/admin/users/{id}/unlock` | Unlock account (Admin) |
| PATCH | `/v1/admin/users/{id}/enabled?enabled=` | Enable/disable account (Admin) |

**Authorization** (requires Bearer token)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/authorize/check` | Check a permission |
| GET | `/v1/authorize/roles` | List all roles (Admin) |
| GET | `/v1/authorize/roles/{userId}` | Get user's role (Admin) |
| POST | `/v1/authorize/roles/{userId}` | Assign role (Admin) |
| GET | `/v1/authorize/permissions` | List all permissions (Admin) |
| GET | `/v1/authorize/permissions/{userId}` | Get user permissions (Admin) |
| GET | `/v1/authorize/me/permissions` | Get current user's permissions |

---

## Setup & Installation

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker (for local PostgreSQL via Docker Compose)
- wkhtmltopdf (for PDF service)

### Installation

```bash
git clone https://github.com/Tu2l/open-resume-builder-backend.git
cd resume-builder-backend
mvn clean install -DskipTests
```

### Running (Development)

Each service starts Docker Compose automatically to spin up PostgreSQL.

```bash
# Terminal 1 — User Service
mvn spring-boot:run -pl user-service

# Terminal 2 — PDF Service
mvn spring-boot:run -pl pdf-service

# Terminal 3 — Gateway
mvn spring-boot:run -pl gateway-service
```

### Running (Production)

```bash
mvn clean package -DskipTests

java -Dspring.profiles.active=prod \
     -DJWT_SECRET_KEY=<secret> \
     -DDATABASE_URL=<url> \
     -jar user-service/target/user-service-1.0-SNAPSHOT.jar

java -Dspring.profiles.active=prod \
     -DDATABASE_URL=<url> \
     -jar pdf-service/target/pdf-service-1.0-SNAPSHOT.jar

java -Dspring.profiles.active=prod \
     -DJWT_SECRET_KEY=<secret> \
     -jar gateway-service/target/gateway-service-1.0-SNAPSHOT.jar
```

## Package Structure

All services follow the same package layout:

```
com.tu2l.{service}/
├── controller/        # REST controllers
├── service/           # Business logic
│   └── impl/
├── repository/        # Data access layer
├── entity/            # JPA entities
├── model/
│   ├── request/
│   └── response/
├── config/            # Spring configuration (security, OpenAPI, etc.)
├── exception/         # Custom exceptions + global handler
└── util/              # Utility classes
```

## Configuration

### Environment Profiles

| Profile | Database | Notes |
|---------|----------|-------|
| `dev` (default) | Local PostgreSQL via Docker Compose | Auto-started by Spring |
| `prod` | PostgreSQL via env vars | All secrets from environment |

### Key Environment Variables (prod)

| Variable | Service | Description |
|----------|---------|-------------|
| `JWT_SECRET_KEY` | gateway, user-service | Shared JWT signing secret (256-bit min) |
| `DATABASE_URL` | user-service, pdf-service | JDBC connection string |
| `DATABASE_USERNAME` | user-service, pdf-service | DB username |
| `DATABASE_PASSWORD` | user-service, pdf-service | DB password |
| `MAIL_HOST` | user-service | SMTP host |
| `MAIL_USERNAME` | user-service | SMTP username |
| `MAIL_PASSWORD` | user-service | SMTP password |
| `FRONTEND_BASE_URL` | user-service | Base URL for email links |

## Security

- JWT access tokens (15 min) + refresh tokens (7 days); refresh-token rotation with reuse detection
- BCrypt password hashing; tokens stored as SHA-256 hashes (never plaintext)
- Gateway-level token validation — services trust `X-User-Email` / `X-User-Username` / `X-User-Role` headers
- Config-driven fixed-window rate limiting on user-service (5 req/60s dev, 10 req/60s prod)
- HTML sanitization (XSS protection) on all HTML input
- CORS configured for frontend origin
- Account lockout after 5 failed login attempts (15 min lock); admin unlock available

## Testing

`user-service` ships a unit/regression suite (services, mapper, JWT, rate-limiter, exception
handler, controllers, bootstrapper). The other modules have minimal coverage — see the
[roadmap](./ROADMAP.md).

```bash
mvn test                      # all modules
mvn -pl user-service -am test # user-service (add -o to run offline)
mvn clean verify              # with coverage
```

## Quick API Test

```bash
# Register
curl -X POST http://localhost:8080/api/users/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"<base64-encoded>"}'

# Login
curl -X POST http://localhost:8080/api/users/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"<base64-encoded>"}'

# Generate PDF (authenticated)
curl -X POST http://localhost:8080/api/pdf/v1/generate \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"htmlContent":"<html><body><h1>Resume</h1></body></html>","fileName":"resume.pdf"}'
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes
4. Push and open a Pull Request

## License

MIT License — see the LICENSE file for details.

## Author

**Tu2l** — [@Tu2l](https://github.com/Tu2l)

---

**Last Updated:** June 2026  
**Version:** 1.0-SNAPSHOT  
**Status:** Active Development
