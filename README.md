# Resume Builder Backend

A modern, microservices-based backend system for building and managing resumes with PDF generation capabilities.

## 🏗️ Architecture

This project follows a **multi-module microservices architecture** with centralized dependency management:

```
resume-builder-backend/
├── common/              # Shared utilities and models
├── pdf-service/         # PDF generation service
├── user-service/        # User management and authentication
└── pom.xml             # Parent POM with centralized dependencies
```

## 🚀 Technology Stack

### Core Technologies

- **Java 17** - LTS version with modern language features
- **Spring Boot 3.4.0** - Latest Spring Boot framework
- **Maven** - Dependency management and build automation
- **SQLite** (dev) / **PostgreSQL** (production) - Database options

### Key Libraries

- **Lombok 1.18.36** - Reduce boilerplate code with annotations
- **Hibernate 6.6.3** - ORM with community dialects support
- **MapStruct 1.5.5** - Type-safe bean mapping
- **JJWT 0.12.6** - JWT token generation and validation
- **SLF4J/Logback** - Logging framework

### External Tools

- **wkhtmltopdf** - HTML to PDF conversion engine

## 📦 Services

### 1. Common Module

Shared library containing:

- Base request/response models
- Utility classes (Base64, HTML sanitization)
- Common enums and constants
- Response processing status

**Key Features:**

- HTML sanitization with XSS protection
- Base64 encoding/decoding utilities
- Marker interfaces for type safety

---

### 2. PDF Service (`/pdf`)

**Port:** 8090  
**Status:** ✅ Fully Implemented

Generate PDFs from HTML content with comprehensive configuration options.

#### Features

- ✅ Synchronous PDF generation
- ✅ Asynchronous PDF generation with background processing
- ✅ PDF persistence to database
- ✅ 33+ page size configurations (A0-A9, B0-B10, Letter, Legal, etc.)
- ✅ Custom margins and orientation settings
- ✅ HTML sanitization for security
- ✅ Temporary file cleanup
- ✅ Global exception handling
- ✅ Request validation

#### API Endpoints

| Method | Endpoint              | Description                          |
|--------|-----------------------|--------------------------------------|
| POST   | `/pdf/generate`       | Generate PDF (sync, returns base64)  |
| POST   | `/pdf/generate/save`  | Generate and save PDF to DB          |
| POST   | `/pdf/generate/async` | Generate and save PDF asynchronously |
| GET    | `/pdf/{id}`           | Retrieve generated PDF by ID         |

#### Technical Implementation

- **Async Processing:** Separate `AsyncPDFService` class for proper Spring `@Async` proxy support
- **Functional Interface:** `PDFGenerationFunction` for flexible async operations
- **Custom Exception:** `PDFException` for domain-specific error handling
- **Entity Mapping:** Clean separation with `EntityMapper` utility
- **Logging:** `@Slf4j` for consistent logging across all components

#### Configuration

```properties
server.port=8090
server.servlet.context-path=/pdf
spring.datasource.url=jdbc:sqlite:databases/generated_pdfs.db
logging.file.name=logs/pdf-service/app.log
```

---

### 3. User Service (`/user`)

**Port:** 8091  
**Status:** 🚧 In Progress (API structure complete, implementation pending)

Comprehensive user management, authentication, and authorization service.

#### Features

- ✅ RESTful API structure with industry-standard endpoints
- ✅ Complete request/response DTOs with validation
- ✅ User entity with security features
- 🚧 JWT-based authentication (pending)
- 🚧 Role-based access control (pending)
- 🚧 Password reset flow (pending)
- 🚧 Email verification (pending)

#### API Endpoints

**Authentication (`/user/auth`)**
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/auth/register` | Register new user | 🚧 TODO |
| POST | `/auth/login` | Login with credentials | 🚧 TODO |
| POST | `/auth/refresh` | Refresh access token | 🚧 TODO |
| POST | `/auth/logout` | Logout and invalidate token | 🚧 TODO |
| POST | `/auth/forgot-password` | Request password reset | 🚧 TODO |
| POST | `/auth/reset-password` | Reset password with token | 🚧 TODO |
| POST | `/auth/verify-email` | Verify email address | 🚧 TODO |

**User Management (`/user/users`)**
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/users/me` | Get current user profile | 🚧 TODO |
| GET | `/users/{id}` | Get user by ID (Admin) | 🚧 TODO |
| PUT | `/users/me` | Update current user | 🚧 TODO |
| PUT | `/users/{id}` | Update user by ID (Admin) | 🚧 TODO |
| PUT | `/users/me/password` | Change password | 🚧 TODO |
| DELETE | `/users/me` | Delete/deactivate account | 🚧 TODO |
| DELETE | `/users/{id}` | Delete user (Admin) | 🚧 TODO |

**Authorization (`/user/authorize`)**
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/authorize/check` | Check user permission | 🚧 TODO |
| GET | `/authorize/roles` | Get all roles | 🚧 TODO |
| GET | `/authorize/roles/{userId}` | Get user roles | 🚧 TODO |
| POST | `/authorize/roles/{userId}` | Assign role to user | 🚧 TODO |
| GET | `/authorize/permissions` | Get all permissions | 🚧 TODO |
| GET | `/authorize/permissions/{userId}` | Get user permissions | 🚧 TODO |
| GET | `/authorize/me/permissions` | Get current user permissions | 🚧 TODO |

#### User Entity

Comprehensive user model with:

- **Authentication:** Username, email, hashed password, refresh tokens
- **Profile:** First name, last name, phone number
- **Security:** Email verification, password reset tokens, account locking, failed login tracking
- **Roles:** USER, ADMIN, MODERATOR, GUEST
- **Audit:** Created/updated timestamps, soft delete support
- **Helper Methods:** Account status checks, token validation, full name generation

#### Configuration

```properties
server.port=8091
server.servlet.context-path=/user
spring.datasource.url=jdbc:sqlite:databases/users.db
logging.file.name=logs/user-service/app.log
```

---

## 🛠️ Setup & Installation

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- wkhtmltopdf (for PDF service)
- Git

### Installation Steps

1. **Clone the repository**

```bash
git clone https://github.com/Tu2l/open-resume-builder-backend.git
cd resume-builder-backend
```

2. **Build the project**

```bash
mvn clean install
```

3. **Run services individually**

```bash
# PDF Service
cd pdf-service
mvn spring-boot:run

# User Service (in another terminal)
cd user-service
mvn spring-boot:run
```

## 🏃 Running the Application

### Development Mode

Each service can be run independently:

```bash
# From project root
mvn spring-boot:run -pl pdf-service
mvn spring-boot:run -pl user-service
```

### Production Mode

Build executable JARs:

```bash
mvn clean package -DskipTests

# Run services
java -jar pdf-service/target/pdf-service-1.0-SNAPSHOT.jar
java -jar user-service/target/user-service-1.0-SNAPSHOT.jar
```

## 📝 Project Structure

### Package Organization

All services follow singular package naming convention:

```
com.tu2l.{service}/
├── controller/          # REST controllers
├── service/            # Business logic
│   └── impl/           # Service implementations
├── repository/         # Data access layer
├── entity/             # JPA entities
├── model/              # DTOs
│   ├── request/        # Request models
│   └── response/       # Response models
├── config/             # Configuration classes
├── exception/          # Custom exceptions
├── commonUtil/               # Utility classes
└── generator/          # Generators (PDF service)
```

### Key Design Patterns

- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - Separation of domain and API models
- **Builder Pattern** - Fluent object construction (Lombok)
- **Service Layer Pattern** - Business logic encapsulation
- **Async Pattern** - Non-blocking operations with Spring `@Async`
- **Functional Interface** - Flexible async processing

## 🔧 Configuration

### Application Properties

**Common Settings (both services):**

```properties
# JSON serialization
spring.jackson.default-property-inclusion=non_null

# Database (SQLite for development)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect

# Logging
logging.level.org.springframework=INFO
logging.level.com.tu2l.*=DEBUG
logging.file.max-size=10MB
logging.file.max-history=30
```

### Database Configuration

**Development (SQLite):**

```properties
spring.datasource.url=jdbc:sqlite:databases/{service}.db
spring.datasource.driver-class-name=org.sqlite.JDBC
```

**Production (PostgreSQL - Recommended):**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/{database}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Environment Profiles

The project supports multiple profiles:

- `dev` - Development with SQLite
- `prod` - Production with PostgreSQL
- `test` - Testing environment

Activate profiles:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🧪 Testing

### Run Tests

```bash
# All modules
mvn test

# Specific module
mvn test -pl pdf-service
mvn test -pl user-service
```

### Test Coverage

```bash
mvn clean verify
```

## 📊 Current Status

### ✅ Completed Features

- [x] Multi-module Maven project structure
- [x] Parent POM with centralized dependency management
- [x] Common utilities module
- [x] PDF Service - Full implementation
    - [x] Sync/Async PDF generation
    - [x] Database persistence
    - [x] 33+ page size configurations
    - [x] HTML sanitization
    - [x] Exception handling
    - [x] Request validation
- [x] User Service - API structure
    - [x] REST endpoints definition
    - [x] Complete DTOs with validation
    - [x] User entity with security features
- [x] Logging with @Slf4j
- [x] Code refactoring (singular package names)
- [x] Database schemas

### 🚧 In Progress

- [ ] User Service - Implementation
    - [ ] JWT token generation/validation
    - [ ] Password hashing with BCrypt
    - [ ] Authentication service logic
    - [ ] Authorization/RBAC implementation
    - [ ] Email service integration
    - [ ] Password reset flow
    - [ ] Email verification

### 📋 Planned Features

- [ ] API Gateway (optional)
- [ ] Service Discovery (Eureka)
- [ ] Distributed tracing
- [ ] Redis caching
- [ ] RabbitMQ/Kafka message broker
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Integration tests
- [ ] Performance testing
- [ ] CI/CD pipeline

## 🔒 Security

### Current Implementation

- ✅ HTML sanitization (XSS protection)
- ✅ Request validation with Bean Validation
- ✅ Global exception handling
- ✅ Prepared statements (SQL injection protection)

### Pending Implementation

- 🚧 JWT authentication
- 🚧 Password hashing (BCrypt)
- 🚧 CORS configuration
- 🚧 Rate limiting
- 🚧 SQL injection prevention (parameterized queries)
- 🚧 HTTPS enforcement
- 🚧 Security headers

## 📖 API Documentation

### PDF Service Example

**Generate PDF (Sync)**

```bash
curl -X POST http://localhost:8090/pdf/generate \
  -H "Content-Type: application/json" \
  -d '{
    "htmlContent": "<html><body><h1>Resume</h1></body></html>",
    "fileName": "my-resume.pdf",
    "pageSize": "LETTER",
    "orientation": "PORTRAIT",
    "marginTop": 10,
    "marginBottom": 10,
    "marginLeft": 10,
    "marginRight": 10
  }'
```

**Response**

```json
{
  "message": "PDF generated successfully",
  "status": "SUCCESS",
  "pdfBase64": "JVBERi0xLjQKJeLjz9MK...",
  "fileName": "my-resume.pdf",
  "fileSize": 15234
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use singular package names
- Add `@Slf4j` for logging
- Write comprehensive JavaDocs
- Add validation annotations
- Keep methods focused and small
- Write unit tests for new features

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

**Tu2l**

- GitHub: [@Tu2l](https://github.com/Tu2l)
- Repository: [open-resume-builder-backend](https://github.com/Tu2l/open-resume-builder-backend)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- wkhtmltopdf for PDF generation capabilities
- Lombok project for reducing boilerplate
- Hibernate team for ORM support

## 📞 Support

For issues and questions:

- Create an issue in the GitHub repository
- Check existing issues for solutions
- Review the [ROADMAP.md](ROADMAP.md) for planned features

---

**Last Updated:** November 29, 2025  
**Version:** 1.0-SNAPSHOT  
**Status:** Active Development 🚀
