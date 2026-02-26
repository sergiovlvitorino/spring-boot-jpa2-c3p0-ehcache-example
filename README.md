# Spring Boot 3 + JPA + JWT + Cache Example

Example project demonstrating Spring Boot 3.2, Java 21, JWT authentication, JPA with HikariCP,
and Spring Cache — using only Spring Boot starters and JDK built-ins (no external libraries).

## Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.2.3 |
| Persistence | Spring Data JPA + Hibernate 6 + HikariCP |
| Database | H2 (in-memory) |
| Cache | Spring Cache (`ConcurrentMapCacheManager`) |
| Security | Spring Security 6 + JWT (spring-security-oauth2-jose / NimbusJWT HS256) |
| Validation | Jakarta Bean Validation 3 |

## Requirements

- Java 21+
- Maven 3.8+

## Running

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

## Configuration

All sensitive values are read from environment variables.
Copy the defaults below and set real values for production:

```bash
# Required in production — generate with: openssl rand -hex 32
export APP_JWT_SECRET=your-secret-here

# Admin credentials (defaults: admin / changeme)
export APP_ADMIN_USERNAME=admin
export APP_ADMIN_PASSWORD=changeme
```

Defaults (dev only) are defined in [application.properties](src/main/resources/application.properties).
**Never use the default JWT secret in production.**

## API

### Authentication

```
POST /auth/login
Content-Type: application/json

{ "username": "admin", "password": "changeme" }
```

Response:
```json
{ "token": "<jwt>", "type": "Bearer" }
```

### Person

All endpoints require `Authorization: Bearer <token>`.

| Method | Path | Description | Body |
|---|---|---|---|
| `POST` | `/api/person` | Create a person | `{ "name": "...", "job": "..." }` |
| `GET` | `/api/person/{id}` | Find by ID | — |

#### Example

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"changeme"}' | jq -r .token)

# 2. Create
curl -s -X POST http://localhost:8080/api/person \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","job":"Engineer"}' | jq

# 3. Find by ID
curl -s http://localhost:8080/api/person/<uuid> \
  -H "Authorization: Bearer $TOKEN" | jq
```

## HTTP Status Codes

| Status | Meaning |
|---|---|
| 200 | OK |
| 201 | Person created |
| 400 | Validation error (response body lists fields) |
| 401 | Missing or invalid token |
| 404 | Person not found |
| 500 | Unexpected server error |

## Project Structure

```
src/main/java/…/
├── Start.java                          # @SpringBootApplication entry point
├── application/
│   ├── command/
│   │   ├── auth/LoginCommand.java      # Login DTO
│   │   └── person/                     # SaveCommand, FindByIdCommand
│   └── service/PersonService.java      # @Cacheable / @CachePut
├── domain/
│   ├── model/Person.java               # JPA entity (UUID PK)
│   └── repository/PersonRepository.java
├── infrastructure/
│   ├── exception/
│   │   ├── EntityNotFoundException.java
│   │   └── GlobalExceptionHandler.java # @RestControllerAdvice
│   └── security/
│       ├── JwtConfig.java              # JwtEncoder / JwtDecoder beans
│       ├── JwtUtil.java                # Token generation and validation
│       ├── JwtAuthenticationFilter.java
│       └── SecurityConfig.java         # SecurityFilterChain, CORS, @EnableCaching
└── ui/rest/controller/
    ├── AuthController.java
    └── PersonController.java
```

## Running Tests

```bash
mvn clean test
```

All 3 integration tests run against an embedded server with an in-memory H2 database.

## Author

[Sergio Vitorino](https://github.com/sergiovlvitorino)
