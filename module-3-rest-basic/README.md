# 🔐 Module 3 - REST + Basic Auth (Stateless)

> **Stateless REST API Security - Industry Standard**

---

## 📚 Table of Contents

- [Overview](#-overview)
- [Theory & Concepts](#-theory--concepts)
- [What's Different Here](#-whats-different-here)
- [Project Structure](#-project-structure)
- [How to Run](#-how-to-run)
- [Testing the Security](#-testing-the-security)
- [API Examples](#-api-examples)
- [Common Pitfalls](#-common-pitfalls)
- [Key Takeaways](#-key-takeaways)
- [Further Reading](#-further-reading)

---

## 🎯 Overview

This module demonstrates **stateless REST API security** - the industry standard for modern APIs consumed by mobile apps, SPAs, and microservices.

**Learning Goals:**
- ✅ Understand stateless vs stateful authentication
- ✅ Implement stateless session management
- ✅ Configure proper REST API error responses (401/403 JSON)
- ✅ Learn when to disable CSRF
- ✅ Master HTTP Basic Auth for REST APIs
- ✅ Understand why sessions don't work for APIs

---

## 📖 Theory & Concepts

### Stateless vs Stateful - The Critical Difference

This is the **MOST IMPORTANT** concept in this module!

#### Stateful (Modules 1 & 2)

```
┌─────────────────────────────────────────────┐
│          SERVER MEMORY                      │
│                                             │
│  Session Store:                             │
│  ┌─────────────────────────────────────┐   │
│  │ JSESSIONID: abc123                  │   │
│  │ - User: john                        │   │
│  │ - Roles: [USER]                     │   │
│  │ - Login time: 10:00 AM              │   │
│  └─────────────────────────────────────┘   │
│                                             │
└─────────────────────────────────────────────┘
        ↑
        │ Cookie: JSESSIONID=abc123
        │
   ┌────────┐
   │ Client │
   └────────┘
```

**Characteristics:**
- ✅ Server stores session data
- ✅ Client sends cookie (JSESSIONID)
- ✅ Login once, subsequent requests use cookie
- ❌ Doesn't scale horizontally easily
- ❌ Requires session replication in clusters
- ❌ Not suitable for mobile apps
- ❌ Not suitable for microservices

#### Stateless (Module 3)

```
┌─────────────────────────────────────────────┐
│          SERVER MEMORY                      │
│                                             │
│  No Session Store!                          │
│  Every request is independent               │
│                                             │
└─────────────────────────────────────────────┘
        ↑
        │ Authorization: Basic dXNlcjpwYXNz
        │ (Every request includes credentials)
        │
   ┌────────┐
   │ Client │
   └────────┘
```

**Characteristics:**
- ✅ Server stores NO state
- ✅ Every request includes credentials
- ✅ Scales horizontally easily
- ✅ Perfect for microservices
- ✅ Works with mobile apps
- ❌ Credentials sent every request (security consideration)

### SessionCreationPolicy.STATELESS

This is configured in security:

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

**What it does:**
1. **Prevents** session creation
2. **Disables** JSESSIONID cookie
3. **Forces** authentication on every request
4. **Removes** session-based security context storage

**Policies Available:**

| Policy | Behavior | Use Case |
|--------|----------|----------|
| **ALWAYS** | Always create session | Traditional web apps |
| **IF_REQUIRED** | Create only if needed | Default Spring Security |
| **NEVER** | Don't create, but use existing | Rare |
| **STATELESS** | Never create or use | REST APIs ✅ |

### HTTP Basic Authentication for REST APIs

**Format:**
```
Authorization: Basic base64(username:password)
```

**Example:**
```
user:password → base64 → dXNlcjpwYXNzd29yZA==
Authorization: Basic dXNlcjpwYXNzd29yZA==
```

**Flow:**

```
Request 1:
┌─────────┐                           ┌─────────┐
│ Client  │  GET /api/products        │ Server  │
│         │  Auth: Basic dXNlcjpw... │         │
│         │ ───────────────────────>  │         │
│         │                           │ Decode  │
│         │                           │ Verify  │
│         │  200 OK + Data            │         │
│         │ <─────────────────────── │         │
└─────────┘                           └─────────┘

Request 2:
┌─────────┐                           ┌─────────┐
│ Client  │  GET /api/tags            │ Server  │
│         │  Auth: Basic dXNlcjpw... │         │
│         │ ───────────────────────>  │         │
│         │                           │ Decode  │
│         │                           │ Verify  │
│         │  200 OK + Data            │         │
│         │ <─────────────────────── │         │
└─────────┘                           └─────────┘

Every request is independent!
```

**Security Considerations:**

⚠️ **CRITICAL:** HTTP Basic sends credentials with EVERY request!
- ✅ **MUST use HTTPS** in production
- ✅ Credentials are base64 encoded (NOT encrypted)
- ✅ Anyone intercepting traffic can decode credentials
- ✅ This is why HTTPS is mandatory

**When to Use:**
- ✅ Internal microservices (behind firewall)
- ✅ Development/testing
- ✅ Simple APIs with HTTPS
- ❌ Production APIs with high security needs (use JWT instead - Module 4)

### Why Disable CSRF for Stateless APIs?

**CSRF Protection Requires:**
1. Sessions (to store CSRF token)
2. Cookies (to automatically send with requests)

**Stateless APIs Have:**
1. ❌ No sessions
2. ❌ No cookies

**Therefore:** CSRF protection is **not applicable** to stateless APIs!

```java
.csrf(csrf -> csrf.disable())  // Safe for stateless APIs
```

**CSRF Attack Scenario (doesn't apply here):**

```
1. User logs into bank.com (gets session cookie)
2. User visits evil.com
3. evil.com has: <form action="bank.com/transfer">
4. Browser AUTOMATICALLY sends bank.com cookie
5. Transfer executes without user knowing
```

**Why it doesn't work against stateless APIs:**
- No cookies to automatically send!
- Client must explicitly add Authorization header
- Evil site can't add Authorization header to user's requests

### Custom 401/403 JSON Responses

**Default Spring Security behavior:**
- Returns HTML error pages
- Not REST-friendly

**Our custom handlers:**

```java
// 401 - Not authenticated
{
    "timestamp": "2026-02-13T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Authentication required. Please provide valid credentials.",
    "path": "/api/products"
}

// 403 - Not authorized
{
    "timestamp": "2026-02-13T10:30:00",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. You don't have sufficient permissions.",
    "path": "/api/admin/stats"
}
```

**Implementation:**

```java
@Bean
public AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> {
        response.setContentType("application/json");
        response.setStatus(401);
        response.getWriter().write("{...}");
    };
}

@Bean
public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
        response.setContentType("application/json");
        response.setStatus(403);
        response.getWriter().write("{...}");
    };
}
```

### Horizontal Scalability

**Stateful (Modules 1-2):**

```
   Client
     │
     ├──> Server 1 (has session)
     │
     └──> Server 2 (no session - ERROR!)
     
Problem: Session only exists on Server 1
Solution: Session replication (complex, expensive)
```

**Stateless (Module 3):**

```
   Client
     │
     ├──> Server 1 ✅ (authenticates request)
     │
     └──> Server 2 ✅ (authenticates request)
     
Solution: Load balancer can send to any server!
```

**Why Stateless Scales Better:**
- No shared state between servers
- No session replication needed
- Any server can handle any request
- Easy to add/remove servers
- Perfect for cloud/containers

### When to Use Stateless vs Stateful

| Use Case | Stateless | Stateful |
|----------|-----------|----------|
| **Mobile Apps** | ✅ Perfect | ❌ Cookies don't work well |
| **SPAs (React, Angular)** | ✅ Perfect | ⚠️ Possible but tricky |
| **Microservices** | ✅ Essential | ❌ Doesn't scale |
| **Traditional Web Apps** | ⚠️ Overkill | ✅ Perfect |
| **Third-party APIs** | ✅ Required | ❌ Can't use cookies |
| **High Scalability** | ✅ Required | ❌ Complex |
| **Server-to-Server** | ✅ Perfect | ❌ No browsers |

---

## 🆕 What's Different Here

### Compared to Module 1 (Default Security):
- ✅ Custom security configuration (not auto-config)
- ✅ **STATELESS** session policy (KEY DIFFERENCE)
- ✅ Custom 401/403 JSON responses
- ✅ CSRF explicitly disabled
- ✅ No sessions created
- ✅ No cookies used

### Compared to Module 2 (Web Security):
- ✅ **STATELESS** instead of stateful (BIGGEST CHANGE)
- ✅ HTTP Basic instead of form login
- ✅ Returns 401 JSON, not redirect to login
- ✅ No CSRF protection
- ✅ No sessions, no cookies
- ✅ No web pages (pure REST API)
- ✅ Every request must authenticate

### What's the Same:
- ✅ Same domain model (Product, Tag)
- ✅ Same 3-layer architecture
- ✅ Same in-memory users (user/password, admin/admin)
- ✅ Same role-based access control

---

## 🗂️ Project Structure

```
module-3-rest-basic/
│
├── src/main/java/com/vbforge/security/restbasic/
│   ├── RestBasicApplication.java            # Main class
│   │
│   ├── controller/
│   │   ├── ProductController.java           # REST API
│   │   ├── TagController.java               # REST API
│   │   └── AdminController.java             # ⭐ ADMIN-only endpoints
│   │
│   ├── security/
│   │   └── RestSecurityConfig.java          # ⭐ STATELESS configuration
│   │
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── TagService.java
│   │   └── impl/
│   │
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   └── TagRepository.java
│   │
│   ├── entity/
│   │   ├── Product.java
│   │   └── Tag.java
│   │
│   ├── dto/
│   │   ├── ProductDTO.java
│   │   └── TagDTO.java
│   │
│   ├── mapper/
│   │   ├── ProductMapper.java
│   │   └── TagMapper.java
│   │
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ResourceNotFoundException.java
│       └── DuplicateResourceException.java
│
├── src/main/resources/
│   ├── application.properties                # Base configuration; defines active Spring profile (dev/test/prod)
│   ├── application-dev.properties            # Development profile settings (e.g., local DB connection, debug options)
│   ├── application-dev.properties.example    # Configuration template; copy & rename for local setup
│
└── src/test/
    ├── java/.../
    │   ├── repository/                      # @DataJpaTest
    │   ├── service/                         # Mockito tests
    │   └── security/
    │       └── RestSecurityIntegrationTest.java  # ⭐ Stateless tests
    └── resources/
        └── application-test.properties
```

**Key Differences from Previous Modules:**
- ✅ No `templates/` folder (no web pages)
- ✅ `AdminController` for testing 403
- ✅ `RestSecurityConfig` with STATELESS policy

---

## 🚀 How to Run

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
cd module-3-rest-basic
mvn clean install
```

### 4️⃣ Run the Application

```bash
mvn spring-boot:run
```

**Console Output:**
```
=================================================================
  Module 3 - REST + Basic Auth (Stateless) is running!
=================================================================
  🌐 Base URL:        http://localhost:8083
  📡 API Products:    http://localhost:8083/api/products
  📡 API Tags:        http://localhost:8083/api/tags
  👑 Admin API:       http://localhost:8083/api/admin/stats
  📚 Swagger UI:      http://localhost:8083/swagger-ui.html

  ⚡ Mode: STATELESS (no sessions, no cookies)
  🔐 Auth: HTTP Basic (every request)
  🚫 CSRF: Disabled (not needed)

  Test Credentials:
  📝 Regular User:    user / password
  👑 Admin User:      admin / admin

  Example cURL:
  curl -u user:password http://localhost:8083/api/products
=================================================================
```

### 5️⃣ Access the Application

**Swagger UI:** http://localhost:8083/swagger-ui.html
- Click "Authorize" button
- Enter credentials: user / password
- Try the endpoints!

---

## 🧪 Testing the Security

### Run All Tests

```bash
mvn test
```

### Test Categories

1. **Repository Tests** (`@DataJpaTest`)
   - JPA queries
   - H2 in-memory database

2. **Service Tests** (`@ExtendWith(MockitoExtension)`)
   - Business logic
   - Mocked dependencies

3. **Security Integration Tests** (`@SpringBootTest`)
   - **Stateless behavior verification** ⭐
   - No session creation
   - Every request requires auth
   - 401/403 JSON responses
   - CSRF disabled
   - Role-based access

### Key Stateless Tests

```java
// ✅ Test 1: No session cookie created
@Test
void whenAuthenticating_thenNoSessionCreated() {
    MvcResult result = mockMvc.perform(get("/api/products")
        .with(httpBasic("user", "password")))
        .andReturn();
    
    assertThat(result.getResponse().getCookie("JSESSIONID")).isNull();
}

// ✅ Test 2: Every request must authenticate
@Test
void whenSecondRequestWithoutAuth_thenUnauthorized() {
    // First request - succeeds
    mockMvc.perform(get("/api/products")
        .with(httpBasic("user", "password")))
        .andExpect(status().isOk());
    
    // Second request WITHOUT auth - fails (no session!)
    mockMvc.perform(get("/api/products"))
        .andExpect(status().isUnauthorized());
}

// ✅ Test 3: 401 returns JSON
@Test
void whenNoCredentials_then401WithJson() {
    mockMvc.perform(get("/api/products"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("Unauthorized"));
}

// ✅ Test 4: CSRF not needed
@Test
void whenPostWithoutCsrf_thenSuccess() {
    mockMvc.perform(post("/api/products")
        .with(httpBasic("user", "password"))
        .content(json))
        .andExpect(status().isCreated());
}
```

---

## 📸 API Examples

### Using cURL

#### 1. Get all products (requires auth)

```bash
curl -u user:password http://localhost:8083/api/products
```

**Response:**
```json
[]
```

#### 2. Try without credentials (401)

```bash
curl http://localhost:8083/api/products
```

**Response:**
```json
{
  "timestamp": "2026-02-13T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required. Please provide valid credentials.",
  "path": "/api/products"
}
```

#### 3. Create a product

```bash
curl -u user:password \
  -X POST http://localhost:8083/api/products \
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
  "createdAt": "2026-02-13T10:30:00",
  "tags": []
}
```

#### 4. Get product by ID

```bash
curl -u user:password http://localhost:8083/api/products/1
```

#### 5. Update a product

```bash
curl -u user:password \
  -X PUT http://localhost:8083/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Laptop",
    "price": 1499.99
  }'
```

#### 6. Delete a product

```bash
curl -u user:password \
  -X DELETE http://localhost:8083/api/products/1
```

#### 7. Try admin endpoint as USER (403)

```bash
curl -u user:password http://localhost:8083/api/admin/stats
```

**Response:**
```json
{
  "timestamp": "2026-02-13T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. You don't have sufficient permissions to access this resource.",
  "path": "/api/admin/stats"
}
```

#### 8. Admin endpoint as ADMIN (200)

```bash
curl -u admin:admin http://localhost:8083/api/admin/stats
```

**Response:**
```json
{
  "timestamp": "2026-02-13T10:30:00",
  "admin": "admin",
  "totalProducts": 0,
  "totalTags": 0,
  "totalUsers": 2,
  "message": "Admin-only statistics endpoint"
}
```

### Using Authorization Header Directly

```bash
# Encode credentials: user:password → base64
echo -n "user:password" | base64
# Output: dXNlcjpwYXNzd29yZA==

# Use in Authorization header
curl -H "Authorization: Basic dXNlcjpwYXNzd29yZA==" \
  http://localhost:8083/api/products
```

### Using Postman

1. **Create new request**
2. **Select Authorization:**
   - Type: `Basic Auth`
   - Username: `user`
   - Password: `password`
3. **Make request** to any endpoint
4. **Notice:** No cookies are stored!

### Using HTTPie (Alternative to cURL)

```bash
# Install: pip install httpie

# GET with auth
http GET :8083/api/products -a user:password

# POST with auth
http POST :8083/api/products -a user:password \
  name="Laptop" \
  description="Gaming laptop" \
  price:=1299.99
```

---

## ⚠️ Common Pitfalls

### 1. Expecting Sessions to Work

**Symptom:** Thinking first request stores authentication for subsequent requests

**Reality:** Every request MUST include credentials!

```bash
# ❌ WRONG - Second request will fail
curl -u user:password http://localhost:8083/api/products
curl http://localhost:8083/api/products  # 401 Unauthorized!

# ✅ CORRECT - Include credentials every time
curl -u user:password http://localhost:8083/api/products
curl -u user:password http://localhost:8083/api/tags
```

### 2. Using HTTP Instead of HTTPS in Production

**Issue:** HTTP Basic sends credentials with every request

**Risk:** Credentials visible in network traffic (base64 is NOT encryption!)

**Solution:**
```
❌ http://api.example.com  (credentials visible)
✅ https://api.example.com (credentials encrypted by TLS)
```

### 3. Trying to Use CSRF Tokens

**Symptom:** Looking for CSRF token in stateless API

**Reality:** CSRF is disabled - not needed!

```bash
# ✅ This works (no CSRF token needed)
curl -u user:password \
  -X POST http://localhost:8083/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Product"}'
```

### 4. Wrong Authorization Header Format

**Common Mistakes:**

```bash
# ❌ Wrong - Missing "Basic"
curl -H "Authorization: dXNlcjpwYXNzd29yZA==" ...

# ❌ Wrong - Using Bearer (that's for JWT)
curl -H "Authorization: Bearer user:password" ...

# ✅ Correct
curl -H "Authorization: Basic dXNlcjpwYXNzd29yZA==" ...

# ✅ Or use -u flag (easier)
curl -u user:password ...
```

### 5. Expecting Different Behavior After Login

**Misconception:** "If I login, subsequent requests should work without auth"

**Reality:** There is no "login" in stateless APIs!
- Every request is independent
- No login endpoint needed
- Just include credentials every time

### 6. Testing Stateless Behavior

**How to verify it's really stateless:**

```bash
# Method 1: Check for JSESSIONID cookie (should be none)
curl -v -u user:password http://localhost:8083/api/products | grep JSESSIONID
# Should return nothing

# Method 2: Make two requests
curl -u user:password http://localhost:8083/api/products  # ✅ Works
curl http://localhost:8083/api/products  # ❌ 401 - No session!
```

---

## 🎯 Key Takeaways

After completing this module, you should understand:

✅ **Stateless Architecture**
- No server-side session storage
- Every request is independent
- Scales horizontally easily

✅ **SessionCreationPolicy.STATELESS**
- Prevents session creation
- No JSESSIONID cookie
- Authenticate every request

✅ **HTTP Basic for REST**
- Credentials in Authorization header
- Base64 encoded (not encrypted!)
- HTTPS required in production

✅ **CSRF Not Needed**
- Only needed for cookie-based auth
- Stateless APIs use headers
- Safe to disable

✅ **Custom Error Responses**
- 401 JSON for not authenticated
- 403 JSON for not authorized
- REST-friendly format

✅ **Stateless Benefits**
- Horizontal scalability
- No session replication
- Perfect for microservices
- Works with mobile apps

✅ **Stateless Limitations**
- Credentials sent every request
- HTTPS is mandatory
- Can't easily "logout"
- More bandwidth (auth header every request)

---

## 🤔 Reflection Questions

1. What happens if you make a request without credentials?
2. Why don't we need CSRF protection in stateless APIs?
3. How is stateless different from Module 2's session-based auth?
4. What security risk exists with HTTP Basic over HTTP (not HTTPS)?
5. How would you implement "logout" in a stateless API?
6. Why is stateless better for horizontal scaling?
7. When would you prefer stateful over stateless?
8. How many times is the database queried for user authentication in 10 requests?

---

## 🐛 Troubleshooting

### Every Request Returns 401

**Check:**
1. Credentials are correct: `user / password` or `admin / admin`
2. Authorization header format: `Authorization: Basic ...`
3. Base64 encoding is correct
4. No typos in username/password

**Test:**
```bash
# Test with verbose output
curl -v -u user:password http://localhost:8083/api/products

# Check for "401 Unauthorized" in response
```

### 403 Forbidden on Admin Endpoints

**Expected!** USER role cannot access `/api/admin/**`

**Solution:** Use admin credentials:
```bash
curl -u admin:admin http://localhost:8083/api/admin/stats
```

### Can't Create/Update/Delete (405 or 403)

**Check:**
1. Using correct HTTP method (POST, PUT, DELETE)
2. Content-Type header: `application/json`
3. Request body is valid JSON

### Tests Failing

```bash
# Verify stateless behavior test
# Should show no JSESSIONID cookie created
mvn test -Dtest=RestSecurityIntegrationTest#whenAuthenticating_thenNoSessionCreated
```

---

## 📚 Further Reading

### Official Documentation
- [Spring Security - Stateless Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html)
- [HTTP Basic Authentication](https://datatracker.ietf.org/doc/html/rfc7617)
- [RESTful API Security Best Practices](https://restfulapi.net/security-essentials/)

### Recommended Articles
- [Stateless vs Stateful Authentication](https://sherryhsu.medium.com/session-vs-token-based-authentication-11a6c5ac45e4)
- [Why HTTPS Matters for APIs](https://www.ssl.com/article/why-https-matters/)
- [Understanding Base64 Encoding](https://developer.mozilla.org/en-US/docs/Glossary/Base64)
- [Horizontal vs Vertical Scaling](https://www.section.io/blog/scaling-horizontally-vs-vertically/)

### Videos
- REST API Security Fundamentals
- Stateless Authentication Explained
- HTTP Basic vs JWT

---

## 🎓 Practice Exercises

1. **Add rate limiting** to prevent brute force attacks
2. **Implement API key authentication** instead of Basic Auth
3. **Add request logging** to track all authentication attempts
4. **Create health check endpoint** that doesn't require authentication
5. **Add API versioning** (e.g., /api/v1/products)
6. **Implement pagination** for product listings
7. **Add filtering** by price range, tags, etc.
8. **Create an admin dashboard** showing recent API calls

---

## 📊 Comparison: Module 2 vs Module 3

| Feature | Module 2 (Stateful) | Module 3 (Stateless) |
|---------|---------------------|----------------------|
| **Session Policy** | Default (creates sessions) | STATELESS |
| **Authentication** | Form login (once) | HTTP Basic (every request) |
| **Session Cookie** | JSESSIONID created | No cookies |
| **CSRF** | Enabled | Disabled |
| **Scalability** | Requires session replication | Scales easily |
| **401 Response** | Redirect to login | JSON error |
| **403 Response** | HTML page | JSON error |
| **Use Case** | Traditional web apps | REST APIs |
| **Mobile Friendly** | No | Yes |
| **Logout** | Invalidate session | N/A (stateless) |

---

## 🎯 What's Next?

**Module 4 - REST + JWT** will introduce:
- Token-based authentication (better than Basic Auth)
- JWT (JSON Web Tokens)
- Login endpoint that returns token
- Token validation on every request
- Refresh tokens
- Claims and custom user data

**Key Improvement:** No credentials sent with every request - just a token!

---

## 🔒 Security Configuration

**DO NOT use the example credentials in production!**

1. Copy `application-dev.properties.example` to `application-dev.properties`
2. Update all credentials with secure values
3. Never commit `application.properties` to Git
4. Use environment variables for production deployment

---

**Happy Learning! 🚀**

*Stateless is the foundation of modern API security!*
