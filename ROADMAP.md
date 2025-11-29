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

### **2. PDF Service** 

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
- [ ] Add API documentation (Swagger/OpenAPI)
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

### **3. User Service** 

**Status:** 🔴 Not Started (0% Complete)

#### **Phase 1: Foundation** 📋
- [ ] Create user-service module
- [ ] Add Spring Security dependency
- [ ] Design user data model
  - [ ] User entity (id, email, password, roles, status)
  - [ ] UserProfile entity (firstName, lastName, phone, etc.)
  - [ ] Audit fields (createdAt, updatedAt, createdBy, modifiedBy)
- [ ] PostgreSQL database integration
- [ ] Flyway migrations setup

#### **Phase 2: Authentication** 📋
- [ ] User registration endpoint
- [ ] Email verification
- [ ] Password encryption (BCrypt)
- [ ] Login endpoint with JWT token generation
- [ ] Refresh token mechanism
- [ ] Logout functionality
- [ ] Password reset flow
- [ ] Change password endpoint

#### **Phase 3: Authorization** 📋
- [ ] Role-based access control (RBAC)
- [ ] User roles: ADMIN, USER, PREMIUM_USER
- [ ] JWT token validation filter
- [ ] Method-level security with `@PreAuthorize`
- [ ] Rate limiting per user tier

#### **Phase 4: User Management** 📋
- [ ] Get user profile
- [ ] Update user profile
- [ ] Upload profile picture
- [ ] Delete user account (soft delete)
- [ ] Get user activity history
- [ ] User preferences management

#### **Phase 5: Integration** 📋
- [ ] Integrate with PDF service
  - [ ] Verify user ownership of PDFs
  - [ ] User quota management (free vs premium)
  - [ ] Track user's PDF generation count
- [ ] Service-to-service authentication
- [ ] API Gateway configuration
- [ ] CORS configuration for frontend

#### **Phase 6: Advanced Features** 📋
- [ ] OAuth2 integration (Google, GitHub, LinkedIn)
- [ ] Two-factor authentication (2FA)
- [ ] User session management
- [ ] Account lockout after failed attempts
- [ ] Email notifications
- [ ] User analytics and reporting

#### **Phase 7: Monitoring & Security** 📋
- [ ] Actuator endpoints
- [ ] Security audit logging
- [ ] Failed login attempt tracking
- [ ] GDPR compliance features
- [ ] Data export functionality

#### **Phase 8: Testing** 📋
- [ ] Unit tests for all services
- [ ] Integration tests
- [ ] Security tests
- [ ] Test coverage > 80%

---

## 🎯 Priority Roadmap

### **Sprint 1: PDF Service Stabilization** (Current)
1. Complete CRUD operations for PDFs
2. Add transaction management
3. Externalize configuration
4. Add health checks and monitoring

### **Sprint 2: PDF Service Production Ready**
1. Implement comprehensive testing
2. Add process timeouts and error handling
3. Database migration to PostgreSQL
4. API documentation

### **Sprint 3: User Service Foundation**
1. Set up user-service module
2. Design and implement user data model
3. Basic authentication (register, login, JWT)
4. Database migrations

### **Sprint 4: User Service Auth & Authorization**
1. Implement RBAC
2. JWT validation and security
3. Password reset flow
4. User profile management

### **Sprint 5: Service Integration**
1. Integrate user-service with pdf-service
2. Service-to-service authentication
3. User quota management
4. API Gateway setup

### **Sprint 6: Advanced Features**
1. Async PDF generation
2. Template system
3. OAuth2 integration
4. Caching layer

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
- **Tool:** Spring Cloud Gateway or Kong
- **Purpose:** Single entry point, routing, rate limiting, authentication

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
- **v0.2.0** (Planned) - PDF CRUD operations, production hardening
- **v0.3.0** (Planned) - User service foundation
- **v1.0.0** (Target: Q2 2026) - Production-ready with both services integrated

---

## 📝 Notes

### **Known Technical Debt**
1. SQLite not suitable for production (PDF Service)
2. Missing comprehensive test suite
3. No async processing for long-running operations
4. Hard-coded configuration values
5. Missing API documentation
6. No monitoring/alerting system

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

**Last Updated:** 29 November 2025
**Maintained By:** Development Team
**Status Legend:** ✅ Complete | 🔄 In Progress | ⏳ Planned Soon | 📋 Backlog | 🔴 Not Started
