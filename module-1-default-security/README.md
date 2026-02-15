# 🔐 Module 1 - Default Security

> **Understanding Spring Security Auto-Configuration**

---

## 📚 Table of Contents

- [Overview](#overview)
- [Theory & Concepts](#theory--concepts)
- [What's Different Here](#whats-different-here)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Testing the Security](#testing-the-security)
- [API Examples](#api-examples)
- [Common Pitfalls](#common-pitfalls)
- [Key Takeaways](#key-takeaways)
- [Further Reading](#further-reading)
- [Practice Exercise](#practice-exercises)
- [What's next](#whats-next)

---

## 🎯Overview

This module demonstrates **Spring Security's default auto-configuration**. By simply adding `spring-boot-starter-security` to your dependencies, Spring Boot automatically configures a complete security setup without any custom code.

**Learning Goals:**
- ✅ Understand what Spring Security auto-configures
- ✅ Learn about HTTP Basic Authentication
- ✅ Explore the default security filter chain
- ✅ Understand 401 vs 403 status codes
- ✅ See how all endpoints are protected by default

---

## 📖Theory & Concepts

### What Happens When You Add Spring Security?

When `spring-boot-starter-security` is on the classpath, Spring Boot automatically:

1. **Enables HTTP Basic Authentication**
    - Browsers prompt for username/password
    - Credentials sent in `Authorization` header
    - Format: `Basic base64(username:password)`

2. **Protects ALL Endpoints**
    - Every URL requires authentication
    - No endpoint is public by default
    - Even `/error` requires auth

3. **Creates a Default User**
    - Username: `user`
    - Password: Generated at startup (printed in console)
    - Role: `USER`

4. **Enables CSRF Protection**
    - POST, PUT, DELETE require CSRF token
    - Important for form-based applications
    - Can cause issues with REST APIs

5. **Adds Security Headers**
    - X-Content-Type-Options
    - X-Frame-Options
    - X-XSS-Protection
    - Cache-Control

6. **Sets Up Filter Chain**
    - 15+ security filters process each request
    - Authentication, authorization, CSRF, headers, etc.

### The Security Filter Chain

Every HTTP request passes through a chain of filters:

```
Request → SecurityContextPersistenceFilter
       → LogoutFilter
       → UsernamePasswordAuthenticationFilter
       → BasicAuthenticationFilter ← We use this!
       → RequestCacheAwareFilter
       → SecurityContextHolderAwareRequestFilter
       → AnonymousAuthenticationFilter
       → SessionManagementFilter
       → ExceptionTranslationFilter
       → FilterSecurityInterceptor
       → Your Controller
```

**Key Filter for This Module:** `BasicAuthenticationFilter`
- Looks for `Authorization: Basic ...` header
- Decodes credentials
- Authenticates user
- Sets SecurityContext

### HTTP Basic Authentication Flow

```
┌─────────┐                  ┌─────────┐
│ Client  │                  │ Server  │
└────┬────┘                  └────┬────┘
     │                            │
     │  GET /api/products         │
     │ ─────────────────────────> │
     │                            │
     │  401 Unauthorized          │
     │  WWW-Authenticate: Basic   │
     │ <───────────────────────── │
     │                            │
     │  GET /api/products         │
     │  Authorization: Basic XXX  │
     │ ─────────────────────────> │
     │                            │
     │  200 OK + Data             │
     │ <───────────────────────── │
     │                            │
```

### Authentication vs Authorization

| **Authentication** | **Authorization** |
|-------------------|-------------------|
| "Who are you?" | "What can you do?" |
| Verifying identity | Checking permissions |
| Username + Password | Roles + Authorities |
| Happens first | Happens after authentication |
| Example: Login | Example: Admin-only endpoints |

**In this module:** We only have authentication, no role-based authorization yet.

### 401 vs 403 Status Codes

| Status | Meaning | When Used |
|--------|---------|-----------|
| **401 Unauthorized** | Authentication required or failed | No credentials OR wrong credentials |
| **403 Forbidden** | Authenticated but not authorized | Correct credentials but insufficient permissions |

**In default config:** You'll only see 401, not 403 (no role restrictions yet).

### Stateful vs Stateless (Preview)

**This module is STATEFUL:**
- Uses HTTP sessions (JSESSIONID cookie)
- Server stores authentication state
- Subsequent requests use session cookie

**Later modules will be STATELESS:**
- No sessions
- Every request must authenticate
- Token-based (JWT)

---

## 🆕What's Different Here

**Compared to no security:**
- ✅ All endpoints now require authentication
- ✅ Default user created automatically
- ✅ Security headers added to responses
- ✅ CSRF protection enabled

**Compared to later modules:**
- ❌ No custom SecurityConfig class
- ❌ No custom UserDetailsService
- ❌ No database-backed users
- ❌ No role-based access control
- ❌ No JWT tokens
- ❌ No custom login endpoint

---

## 🗂️Project Structure

```
module-1-default-security/
│
├── src/main/java/com/vbforge/security/defaultsec/
│   ├── DefaultSecurityApplication.java     # Main class
│   ├── controller/
│   │   ├── ProductController.java          # REST endpoints
│   │   └── TagController.java
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── TagService.java
│   │   └── impl/
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   └── TagRepository.java
│   ├── entity/
│   │   ├── Product.java
│   │   └── Tag.java
│   ├── dto/
│   │   ├── ProductDTO.java
│   │   └── TagDTO.java
│   ├── mapper/
│   │   ├── ProductMapper.java
│   │   └── TagMapper.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ResourceNotFoundException.java
│       └── DuplicateResourceException.java
│
├── src/main/resources/
│   ├── application.properties                  # Controls active profile (dev/test/prod)
│   ├── application-dev.properties              # Development profile configuration, local database connection settings
│   └── application-dev.properties.example      # Template configuration file, copy this to create local config
│
└── src/test/
    ├── java/.../
    │   ├── repository/                     # @DataJpaTest
    │   ├── service/                        # @ExtendWith(MockitoExtension)
    │   └── security/                       # Security integration tests
    └── resources/
        └── application-test.properties     # H2 config
```

**Note:** No `security/` package with custom config!

---

## 🚀How to Run

### 1️⃣ Prerequisites

Ensure you have:
- ✅ JDK 17+
- ✅ Maven 3.8+
- ✅ MySQL 8+ running on `localhost:3306`
- ✅ Database `security_playground_db` created
- ✅ User `dev_user` with password `dev_password`

### 2️⃣ Setup MySQL

```bash
mysql -u root -p

CREATE DATABASE security_playground_db;
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON security_playground_db.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3️⃣ Build the Module

```bash
cd module-1-default-security
mvn clean install
```

### 4️⃣ Run the Application

```bash
mvn spring-boot:run
```

### 5️⃣ Find the Generated Password

Look for this in the console output:

```
Using generated security password: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Save this password!** You'll need it for authentication.

### 6️⃣ Access the Application

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Docs:** http://localhost:8081/api-docs
- **Products API:** http://localhost:8081/api/products

All URLs will prompt for authentication:
- **Username:** `user`
- **Password:** (the generated password from console)

---

## 🧪Testing the Security

### Run All Tests

```bash
mvn test
```

### Test Categories

1. **Repository Tests** (`@DataJpaTest`)
    - Test JPA queries
    - H2 in-memory database
    - No security involved

2. **Service Tests** (`@ExtendWith(MockitoExtension)`)
    - Test business logic
    - Mocked dependencies
    - No security involved

3. **Security Integration Tests** (`@SpringBootTest`)
    - Test authentication behavior
    - Test 401 responses
    - Test with/without credentials
    - Uses `@WithMockUser` for authenticated tests

### Key Security Tests

```java
// Test 1: No auth = 401
@Test
void whenNoAuthentication_thenUnauthorized() {
    // Expects 401
}

// Test 2: Valid auth = 200
@Test
@WithMockUser
void whenMockUser_thenCanAccessEndpoint() {
    // Expects 200
}

// Test 3: Wrong credentials = 401
@Test
void whenInvalidBasicAuth_thenUnauthorized() {
    // Expects 401
}
```

---

## 📸API Examples

### Using cURL

#### 1. Try without authentication (will fail with 401)

```bash
curl -X GET http://localhost:8081/api/products
```

**Response:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

#### 2. Authenticate with Basic Auth

```bash
curl -X GET http://localhost:8081/api/products \
  -u user:a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response:**
```json
[]
```

#### 3. Create a Product

```bash
curl -X POST http://localhost:8081/api/products \
  -u user:a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "Gaming laptop",
    "price": 1299.99
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "Gaming laptop",
  "price": 1299.99,
  "createdAt": "2026-02-12T10:30:00",
  "tags": []
}
```

#### 4. Get All Products

```bash
curl -X GET http://localhost:8081/api/products \
  -u user:your-generated-password
```

#### 5. Create a Tag

```bash
curl -X POST http://localhost:8081/api/tags \
  -u user:your-generated-password \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics"
  }'
```

#### 6. Add Tag to Product

```bash
curl -X POST http://localhost:8081/api/products/1/tags/1 \
  -u user:your-generated-password
```

### Using Postman

1. **Create a new request**
2. **Set Authorization:**
    - Type: `Basic Auth`
    - Username: `user`
    - Password: (your generated password)
3. **Make requests** to any endpoint

### Using Browser

1. Navigate to http://localhost:8081/api/products
2. Browser will prompt for credentials
3. Enter `user` and generated password
4. Subsequent requests in same session won't prompt (session cookie)

---

## ⚠️Common Pitfalls

### 1. Lost the Generated Password?

**Solution:** Restart the application and look for it in console logs.

**Alternative:** Configure a fixed password in `application.properties`:

```properties
spring.security.user.name=user
spring.security.user.password=mypassword
```

### 2. CSRF Errors on POST/PUT/DELETE

**Symptom:** 403 Forbidden when trying to POST

**Cause:** CSRF protection is enabled by default

**Solution for development:**
```properties
# application.properties (NOT recommended for production)
spring.security.csrf.enabled=false
```

**Better solution:** We'll handle CSRF properly in Module 2

### 3. Can't Access Swagger UI

**Cause:** Swagger endpoints also require authentication

**Solution:** Authenticate first, then access Swagger

### 4. 401 vs 403 Confusion

**In this module:** You'll ONLY see 401
- Wrong credentials = 401
- No credentials = 401
- We don't have role-based restrictions yet

**403 comes later** when we add role-based access control

### 5. Session Cookie Issues

**Symptom:** Keep getting prompted for credentials

**Cause:** Browser not storing JSESSIONID cookie

**Solution:** Check browser cookie settings, or use Postman

### 6. Port Already in Use

**Symptom:** `Port 8081 already in use`

**Solution:** Change port in `application.properties`:
```properties
server.port=8082
```

---

## 🎯Key Takeaways

After completing this module, you should understand:

✅ **Auto-configuration Magic**
- Spring Security works with zero configuration
- Just add dependency and you get full security

✅ **HTTP Basic Authentication**
- Simple but effective for testing
- Credentials in every request
- Base64 encoded (not encrypted!)

✅ **Default Behavior**
- All endpoints protected
- Single user with generated password
- Stateful (uses sessions)

✅ **Security Filter Chain**
- Every request goes through filters
- Authentication happens before reaching controller

✅ **Testing Security**
- `@WithMockUser` for authenticated tests
- Test both authenticated and unauthenticated scenarios

✅ **Limitations**
- Single user (not practical)
- Can't customize authentication
- No role-based access control
- Password generated each startup

**Next Step:** Module 2 will show how to customize this with form login and session management!

---

## 🤔Reflection Questions

1. Why does Spring Security protect ALL endpoints by default?
2. What happens if you remove `spring-boot-starter-security` dependency?
3. How is the password transmitted in HTTP Basic Auth?
4. Is HTTP Basic Auth secure over HTTP (not HTTPS)?
5. When would you use default security vs custom security?

---

## 🐛Troubleshooting

### MySQL Connection Failed

```bash
# Check MySQL is running
sudo systemctl status mysql

# Verify connection
mysql -u dev_user -p -h localhost
```

### Tests Failing

```bash
# Make sure test profile uses H2
# Check application-test.properties exists
# Verify @ActiveProfiles("test") in test classes
```

### Can't Build Project

```bash
# Clean and rebuild
mvn clean install -U
```

---

## 📚Further Reading

### Official Documentation
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Boot Security Auto-Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.security)
- [HTTP Basic Authentication RFC](https://tools.ietf.org/html/rfc7617)

### Recommended Articles
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
- [Understanding the Security Filter Chain](https://www.baeldung.com/spring-security-filter-chain)
- [Basic Authentication in Spring Boot](https://www.baeldung.com/spring-security-basic-authentication)

### Videos
- Spring Security Fundamentals (YouTube)
- Understanding Spring Boot Auto-Configuration

---

## 🎓Practice Exercises

1. **Add a custom user** in `application.properties`
2. **Disable security temporarily** to see the difference
3. **Add logging** to see which security filters are applied
4. **Try different HTTP methods** (GET, POST, PUT, DELETE) with/without auth
5. **Inspect network traffic** in browser dev tools to see `Authorization` header
6. **Create a simple HTML form** that submits to your API with Basic Auth

---

## 📊What's Next?

**Module 2 - Web Security** will introduce:
- Form-based login (login page)
- Custom UserDetailsService
- In-memory user storage
- Logout functionality
- Remember-me feature
- Session management

---

## 🔒 Security Configuration

**DO NOT use the example credentials in production!**

1. Copy `application-dev.properties.example` to `application-dev.properties`
2. Update all credentials with secure values
3. Never commit `application.properties` to Git
4. Use environment variables for production deployment

---

**Happy Learning! 🚀**

*Understanding defaults is the foundation for mastering Spring Security!*
