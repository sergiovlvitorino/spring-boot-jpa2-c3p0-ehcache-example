# Spring Boot 4 + JPA + JWT + Cache Example

Example project demonstrating Spring Boot 4.0, Java 21, JWT authentication, JPA with HikariCP,
and Spring Cache with Caffeine — using only Spring Boot starters and JDK built-ins.

## Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Persistence | Spring Data JPA + Hibernate 6 + HikariCP |
| Database | H2 (in-memory) |
| Cache | Spring Cache + Caffeine (TTL 10min, max 1000 entries) |
| Security | Spring Security 7 + JWT (spring-security-oauth2-jose / NimbusJWT HS256) |
| Monitoring | Spring Boot Actuator (health, info, metrics) |
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

# CORS origins (comma-separated, default: http://localhost:3000)
export APP_CORS_ORIGINS=https://myapp.com,https://admin.myapp.com
```

Defaults (dev only) are defined in [application.properties](src/main/resources/application.properties).
**Never use the default JWT secret or admin password in production** — the application will
fail to start if defaults are detected in a production profile.

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

Rate limited to 5 attempts per minute per IP. Returns `429 Too Many Requests` when exceeded.

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
| 429 | Too many login attempts |
| 500 | Unexpected server error |

## Security

- **JWT** via Spring Security OAuth2 JOSE (NimbusJWT, HS256)
- **Stateless sessions** — no server-side session storage
- **Security headers** — `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, HSTS
- **Rate limiting** — login endpoint limited to 5 attempts/min per IP
- **Startup validation** — rejects default credentials in production profiles
- **CORS** — configurable via `APP_CORS_ORIGINS` environment variable
- **BCrypt** password encoding
- **JWT claims** — includes `iss` and `aud` to prevent cross-service token reuse
- **Logging** — authentication events, errors, and JWT failures logged via SLF4J

## Project Structure

```
src/main/java/.../
├── Start.java                          # @SpringBootApplication entry point
├── application/
│   ├── command/
│   │   ├── auth/LoginCommand.java      # Login DTO
│   │   └── person/SaveCommand.java     # Create person DTO
│   └── service/PersonService.java      # @Cacheable / @CachePut
├── domain/
│   ├── model/Person.java               # JPA entity (UUID PK)
│   └── repository/PersonRepository.java
├── infrastructure/
│   ├── CacheConfig.java                # @EnableCaching (Caffeine)
│   ├── SchedulingConfig.java           # @EnableScheduling (rate limiter cleanup)
│   ├── exception/
│   │   ├── EntityNotFoundException.java
│   │   ├── TooManyRequestsException.java
│   │   └── GlobalExceptionHandler.java # @RestControllerAdvice
│   └── security/
│       ├── JwtConfig.java              # JwtEncoder / JwtDecoder beans
│       ├── JwtUtil.java                # Token generation and validation
│       ├── JwtAuthenticationFilter.java
│       ├── LoginRateLimiter.java       # IP-based sliding window rate limiter
│       ├── SecurityConfig.java         # SecurityFilterChain, CORS, headers
│       └── SecurityPropertiesValidator.java # Startup credential validation
└── ui/rest/controller/
    ├── AuthController.java
    └── PersonController.java
```

## Running Tests

```bash
mvn clean test
```

96 tests covering unit, integration, security, and architecture layers:

| Suite | Tests | Scope |
|---|---|---|
| CommandValidationTest | 10 | Jakarta Bean Validation |
| PersonServiceTest | 5 | Service layer (Mockito) |
| PersonServiceTransactionalTest | 2 | @Transactional annotations (reflection) |
| PersonRepositoryTest | 5 | JPA repository (@DataJpaTest) |
| PersonControllerMvcTest | 6 | REST endpoints (MockMvc) |
| PersonControllerTest | 3 | Full integration (TestRestTemplate) |
| AuthControllerMvcTest | 5 | Login endpoint (MockMvc) |
| AuthControllerRateLimitMvcTest | 1 | Rate limiting → 429 |
| RateLimiterBehaviorTest | 3 | Valid logins don't consume rate limit |
| JwtUtilTest | 7 | Token generation/validation |
| JwtClaimsTest | 5 | JWT issuer/audience/subject/exp claims |
| JwtAuthenticationFilterTest | 5 | Bearer token filter |
| JwtSecretValidationTest | 5 | JWT secret length validation |
| LoginRateLimiterTest | 6 | Sliding window rate limiter |
| SecurityPropertiesValidatorTest | 7 | Startup credential validation |
| SecurityArchitectureTest | 7 | No @Autowired fields, records immutability |
| SecurityHeadersTest | 3 | Security response headers |
| GlobalExceptionHandlerTest | 3 | Exception → HTTP status mapping |
| GlobalExceptionHandlerLoggingTest | 5 | Error logging, no data leakage |
| ActuatorEndpointsTest | 2 | Health/info endpoints public |
| ApplicationContextTest | 1 | Context loads |

## Author

[Sergio Vitorino](https://github.com/sergiovlvitorino)
