# Resume Builder Backend - Roadmap

## Overview
Multi-module Spring Boot microservices architecture for resume building and PDF generation.

---

## 📦 Module Structure

### **1. Common Module** *(Completed)*
Shared utilities and models across all services.

**Status:** ✅ Production Ready

**Components:**
- Base request/response models
- Response processing status enums
- Utility classes (Base64, sanitization, string manipulation)

---

### **2. Gateway Service** *(Completed)*
Spring Cloud Gateway — single entry point, JWT validation, request routing, and aggregated API docs.

**Status:** ✅ Production Ready

**Components:**
- Route configuration for user-service and pdf-service
- Global JWT authentication filter with public-route bypass
- Fixed-window rate limiting propagation
- Aggregated Swagger UI at `/swagger-ui.html` (springdoc-openapi-starter-webflux-ui)

---

### **3. PDF Service** 

**Status:** 🟡 In Progress (70% Complete)

#### **Phase 1: Core Functionality** ✅
- [x] PDF generation from HTML using wkhtmltopdf
- [x] Base64 encoding/decoding
- [x] Save generated PDFs to database
- [x] HTML sanitization for security
- [x] Configurable layout parameters (page size, margins, orientation)
- [x] Generator abstraction layer (PDFGenerator interface)
- [x] Global exception handling
- [x] REST API endpoints (generate, generateAndSave)

#### **Phase 2: CRUD Operations** 🔄
- [ ] Get PDF by ID
- [ ] Get all PDFs by user ID
- [ ] Delete PDF by ID
- [ ] Update/regenerate existing PDF
- [ ] Get PDF metadata (pages, size, created date)
- [ ] Download PDF as raw bytes

#### **Phase 3: Production Readiness** ⏳
- [ ] Add `@Transactional` to save operations
- [ ] Capture and log wkhtmltopdf error output
- [ ] Add process timeout configuration
- [ ] Externalize configuration to application.properties
  - [ ] wkhtmltopdf path
  - [ ] Process timeout
  - [ ] Default layout parameters
- [ ] Add charset specification to encoding/decoding
- [ ] Validate PDFGeneratorConfiguration inputs
- [ ] Add timestamp fields to GeneratedPDFEntity (createdAt, updatedAt)

#### **Phase 4: Monitoring & Health** ⏳
- [ ] Spring Boot Actuator integration
- [ ] Custom health check for wkhtmltopdf availability
- [ ] Add metrics for PDF generation (duration, size, success/failure rates)
- [x] Add API documentation (Swagger/OpenAPI) — aggregated at gateway `/swagger-ui.html`
- [ ] Implement rate limiting for resource-intensive operations

#### **Phase 5: Advanced Features** 📋
- [ ] Async PDF generation with `@Async`
- [ ] Batch PDF generation
- [ ] Template-based PDF generation
- [ ] PDF watermarking
- [ ] PDF compression
- [ ] PDF merging/splitting
- [ ] Password-protected PDFs
- [ ] Digital signatures
- [ ] PDF to image conversion

#### **Phase 6: Testing & Quality** ⏳
- [ ] Unit tests for services
- [ ] Integration tests for PDF generation
- [ ] Security tests for HTML sanitization
- [ ] Performance tests for concurrent generation
- [ ] Test coverage > 80%

#### **Phase 7: Database Migration** 📋
- [ ] Switch from SQLite to PostgreSQL/MySQL
- [ ] Add Flyway/Liquibase for schema migrations
- [ ] Add database indexes for performance
- [ ] Implement soft delete for PDFs

#### **Phase 8: Scalability** 📋
- [ ] Implement caching layer (Redis)
- [ ] Message queue for async processing (RabbitMQ/Kafka)
- [ ] File storage abstraction (S3/Azure Blob)
- [ ] Horizontal scaling considerations

---

### **4. User Service** 

**Status:** ✅ Feature-complete (auth hardened, RBAC, Flyway, bootstrap admin, unit-tested)

#### **Phase 1: Foundation** ✅
- [x] Create user-service module
- [x] Add Spring Security dependency
- [x] Design user data model
  - [x] User entity (id, email, password, roles, status)
  - [x] UserProfile entity (firstName, lastName, phone, etc.)
  - [x] Audit fields (createdAt, updatedAt)
- [x] PostgreSQL database integration
- [x] Flyway migrations (V1 baseline; `ddl-auto: validate`)

#### **Phase 2: Authentication** ✅
- [x] User registration endpoint
- [x] Password encryption (BCrypt)
- [x] Login endpoint with JWT token generation
- [x] Refresh token mechanism
- [x] Logout functionality
- [x] Password reset flow
- [x] Change password endpoint
- [x] Email verification (single-use token; profile-switched email delivery)
- [x] Refresh-token rotation + reuse detection

#### **Phase 3: Authorization** ✅
- [x] Role-based access control (RBAC)
- [x] User roles: ADMIN, USER, MODERATOR, GUEST
- [x] JWT token validation at gateway
- [x] Method-level security with `@PreAuthorize`
- [x] Rate limiting (fixed-window filter)

#### **Phase 4: User Management** ✅
- [x] Get user profile
- [x] Update user profile
- [x] Delete user account (soft delete)
- [ ] Upload profile picture
- [ ] Get user activity history
- [ ] User preferences management

#### **Phase 5: Integration** 🔄
- [ ] Integrate with PDF service (user ownership of PDFs, quota)
- [ ] Service-to-service authentication
- [x] API Gateway configuration
- [x] CORS configuration for frontend

#### **Phase 6: Advanced Features** 📋
- [ ] OAuth2 integration (Google, GitHub, LinkedIn)
- [ ] Two-factor authentication (2FA)
- [ ] User session management
- [x] Account lockout after failed attempts
- [ ] Email notifications (stub in place)
- [ ] User analytics and reporting

#### **Phase 7: Monitoring & Security** 📋
- [ ] Actuator endpoints
- [ ] Security audit logging
- [x] Failed login attempt tracking
- [ ] GDPR compliance features
- [ ] Data export functionality

#### **Phase 8: Testing** 📋
- [ ] Unit tests for all services
- [ ] Integration tests
- [ ] Security tests
- [ ] Test coverage > 80%

---

## 🎯 Priority Roadmap

### **Sprint 1–4: Core Services** (Completed)
- Gateway service with JWT auth and routing
- User service: registration, login, JWT, RBAC, rate limiting
- PDF service: sync/async generation, DB persistence
- Aggregated API documentation at gateway

### **Sprint 5: User Service Completion** (Current)
1. Email service implementation (verification + password reset)
2. Flyway database migrations
3. User profile picture upload

### **Sprint 6: Service Integration**
1. User ownership of PDFs
2. User quota management (free vs premium)
3. Service-to-service authentication

### **Sprint 7: Production Hardening**
1. Comprehensive test suite (target > 80% coverage)
2. Actuator + health checks
3. Process timeouts for wkhtmltopdf
4. Redis caching layer

### **Sprint 8: Advanced Features**
1. OAuth2 integration (Google, GitHub)
2. Async PDF generation improvements
3. Template-based PDF generation
4. RabbitMQ/Kafka for async processing

---

## 🏗️ Architecture Decisions

### **Multi-Module vs Microservices**
- **Current:** Multi-module monorepo with parent POM
- **Future:** Consider splitting into independent repositories as services scale
- **Reason:** Easier dependency management initially, maintain flexibility for future

### **Database Strategy**
- **PDF Service:** Move to PostgreSQL for production
- **User Service:** PostgreSQL from start
- **Shared:** Consider separate databases per service (database-per-service pattern)

### **Authentication**
- **Approach:** JWT-based stateless authentication
- **Storage:** Redis for refresh tokens and session management
- **Security:** OAuth2 for third-party providers

### **File Storage**
- **Phase 1:** Local file system or database (current)
- **Phase 2:** Cloud storage (S3, Azure Blob, Google Cloud Storage)
- **Reason:** Scalability and cost-effectiveness

### **API Gateway**
- **Tool:** Spring Cloud Gateway (WebFlux) — implemented
- **Purpose:** Single entry point, JWT validation, routing, aggregated Swagger UI

---

## 📊 Success Metrics

### **PDF Service**
- PDF generation success rate > 99%
- Average generation time < 3 seconds
- Support 100+ concurrent generations
- API response time < 500ms (excluding PDF generation)
- Test coverage > 80%

### **User Service**
- Authentication latency < 100ms
- Support 10,000+ active users
- Zero security vulnerabilities
- Test coverage > 80%
- GDPR compliance

---

## 🔄 Version History

- **v0.1.0** (Nov 2025) - Initial PDF service with basic generation
- **v0.2.0** (Dec 2025) - PDF CRUD operations, PostgreSQL migration
- **v0.3.0** (Jan 2026) - User service: auth, JWT, RBAC, rate limiting
- **v0.4.0** (Jun 2026) - API Gateway, versioned routes (/v1), aggregated Swagger UI
- **v0.5.0** (Jun 2026) - user-service hardening (auth fixes, refresh rotation, RBAC, audit), Flyway, bootstrap admin, config-driven rate limiting, unit tests
- **v1.0.0** (Target: Q3 2026) - gateway/pdf test suites, Actuator, pdf-service production hardening

---

## 📝 Notes

### **Known Technical Debt**
1. Test coverage: `user-service` has a unit/regression suite; `gateway-service` and `pdf-service` still lack tests
2. Flyway is in place for `user-service` only; `pdf-service` still uses Hibernate `ddl-auto`
3. No monitoring/alerting system (Actuator not configured)
4. wkhtmltopdf process has no timeout — long-running HTML can hang
5. `pdf-service` layout params and `wkhtmltopdf` path are hardcoded (not externalized); async has no status column
6. `pdf-service` `PDFException` maps to HTTP 200 (should be 4xx/5xx)

### **Security Considerations**
1. HTML sanitization implemented but consider using libraries like OWASP Java HTML Sanitizer
2. wkhtmltopdf has inherent security risks - consider pure Java alternatives
3. Rate limiting required to prevent abuse
4. Input validation needs to be comprehensive
5. Audit logging for sensitive operations

### **Performance Optimization**
1. Implement caching for frequently generated PDFs
2. Consider CDN for static content
3. Database query optimization and indexing
4. Connection pooling tuning
5. Async processing for heavy operations

---

**Last Updated:** June 2026
**Maintained By:** Development Team
**Status Legend:** ✅ Complete | 🔄 In Progress | ⏳ Planned Soon | 📋 Backlog | 🔴 Not Started
