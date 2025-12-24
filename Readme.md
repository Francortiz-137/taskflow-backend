# Taskflow Backend 
### Task & User Management API

Backend REST API built with **Spring Boot 3**, following **clean architecture**, **test-driven development**, and **production-ready practices**.

The project currently provides:
- Task management (CRUD, filtering, pagination, sorting)
- User management (CRUD, password hashing, change password)
- Global error handling with i18n support
- Strong validation and defensive JSON parsing
- High test coverage (service + controller layers)
- Security foundation ready for JWT authentication (next milestone)

---

## 🧱 Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Spring Web**
- **Spring Validation (Jakarta)**
- **Spring Security (foundation)**
- **JPA / Hibernate**
- **JUnit 5**
- **Mockito**
- **MockMvc**
- **JaCoCo** (code coverage)
- **OpenAPI / Swagger**
- **Gradle**

---

## 📂 Project Structure
```
src/main/java/com/franco/backend
├── api # Global exception handling
├── config # Security, Jackson, i18n, CORS, OpenAPI
├── controller # REST controllers
├── dto
│ ├── common # Shared DTOs (errors, pagination)
│ ├── task # Task-related DTOs
│ └── user # User-related DTOs
├── entity # JPA entities & enums
├── exception # Domain & API exceptions
├── mapper # Entity ↔ DTO mappers
├── repository
│ └── specification # Dynamic query specifications
├── security # Password hashing & security utilities
└── service
├── interfaces
└── impl 
```
# Business logic

Tests are located under:
```
src/test/java/com/franco/backend
├── controller
├── service
└── testutil
```
---

## ✅ Implemented Features

### 📝 Tasks
- Create, read, update, delete tasks
- Pagination & sorting
- Filtering by status and title
- Validation for query params and body
- Enum-safe deserialization
- Idempotent updates
- Full controller & service test coverage

### 👤 Users
- Create users with hashed passwords
- Retrieve users (single & list)
- Update user profile
- Change password with:
  - current password validation
  - hash comparison protection
- Strong validation via DTOs
- Defensive error handling
- Full service & controller test coverage

---

## 🔐 Security (Current State)

- Password hashing via `PasswordService`
- Pluggable hashing strategies (BCrypt / Plain for tests)
- Validation errors do **not leak sensitive information**
- Authentication groundwork completed

➡️ **JWT authentication & authorization will be added next**

---

## 🌍 Error Handling & Validation

All errors follow a **consistent API format**:

```json
{
  "timestamp": "2025-01-01T12:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "email: must be a valid email",
  "path": "/api/users"
}
```
Handled cases include:

DTO validation errors

Invalid query/path parameters

Unknown JSON fields

Invalid enum values

Domain-specific exceptions

Optimistic locking conflicts

Controlled 500 errors

All messages support internationalization (i18n).

## 🧪 Testing & Coverage

Service layer tested with Mockito

Controller layer tested with MockMvc

Security filters disabled in controller tests

Edge cases covered (invalid input, empty results, conflicts)

## 📊 Coverage (JaCoCo)

Current overall coverage:

Instructions: ~74%

Controllers: ~94%

Services: ~95%

Coverage report:
```
./gradlew test jacocoTestReport
```
Open:
```
build/reports/jacoco/test/html/index.html
```
📖 API Documentation

Swagger UI available at:
```
/swagger-ui.html
```

OpenAPI docs:
```
/v3/api-docs
```
🚀 Running the Project
Run tests
```
./gradlew clean test
```
Run application
```
./gradlew bootRun
```
## 🛣️ Roadmap

Next milestones:

 JWT Authentication (login, token validation)

 Role-based authorization

 Security integration tests

 Refresh tokens

 Docker support

 CI pipeline

## 🧠 Design Principles

- Clean separation of concerns
  
- DTO-driven validation
  
- Fail-fast input validation

- No sensitive data leakage

- Test-first mindset

- Production-oriented error handling

### ✍️ Author

Franco Ortiz