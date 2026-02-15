# 🔐 Module 4 - REST + JWT

> **Token-Based Authentication - Industry Standard for Production APIs**

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

This module demonstrates **JWT (JSON Web Token) authentication** - the industry standard for securing modern REST APIs consumed by mobile apps, SPAs, and microservices.

**Learning Goals:**
- ✅ Understand JWT structure and lifecycle
- ✅ Implement token-based authentication
- ✅ Create login endpoint that returns JWT
- ✅ Validate JWT tokens on every request
- ✅ Extract user information from tokens
- ✅ Understand JWT advantages over HTTP Basic
- ✅ Handle token expiration

---

## 📖 Theory & Concepts

### What is JWT?

**JWT (JSON Web Token)** is a compact, URL-safe token format for securely transmitting information between parties as a JSON object.

#### JWT Structure

A JWT consists of **three parts** separated by dots (`.`):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImlhdCI6MTcwOTQ3MjAwMCwiZXhwIjoxNzA5NTU4NDAwfQ.4Hb-8xj9k2L5mN6pQ7rS8tU9vW0xY1zA2BC3dE4fG5H

       Header                                                 Payload                                                                    Signature
```

#### 1. Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
- **alg**: Algorithm used (HS256 = HMAC SHA-256)
- **typ**: Token type (JWT)

#### 2. Payload (Claims)
```json
{
  "sub": "user",
  "roles": ["ROLE_USER"],
  "iat": 1709472000,
  "exp": 1709558400
}
```
- **sub**: Subject (username)
- **roles**: User authorities
- **iat**: Issued at (timestamp)
- **exp**: Expiration (timestamp)

#### 3. Signature
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```
- Ensures token hasn't been tampered with
- Only server with secret key can create valid signature

### JWT Authentication Flow

```
┌──────────┐                          ┌──────────┐
│  Client  │                          │  Server  │
└─────┬────┘                          └─────┬────┘
      │                                     │
      │ 1. POST /auth/login                 │
      │    {username, password}             │
      │ ─────────────────────────────────>  │
      │                                     │
      │                                     │ 2. Validate credentials
      │                                     │ 3. Generate JWT token
      │                                     │
      │ 4. Return token                     │
      │    {token: "eyJhbG..."}             │
      │ <─────────────────────────────────  │
      │                                     │
      │ [Client stores token]               │
      │                                     │
      │ 5. GET /api/products                │
      │    Authorization: Bearer eyJhbG...  │
      │ ─────────────────────────────────>  │
      │                                     │
      │                                     │ 6. Extract token
      │                                     │ 7. Verify signature
      │                                     │ 8. Check expiration
      │                                     │ 9. Extract user info
      │                                     │ 10. Set SecurityContext
      │                                     │
      │ 11. Return data                     │
      │ <─────────────────────────────────  │
      │                                     │
```

**Key Points:**
1. ✅ User logs in **once**, gets token
2. ✅ Token contains user info (no DB lookup needed!)
3. ✅ Token sent with every request
4. ✅ Server validates token, not credentials
5. ✅ No server-side session storage

### JWT vs HTTP Basic Auth (Module 3)

| Feature | HTTP Basic (Module 3) | JWT (Module 4) |
|---------|----------------------|----------------|
| **Credentials Sent** | Every request | Only at login |
| **Token/Auth** | `Basic base64(user:pass)` | `Bearer <jwt-token>` |
| **DB Lookup** | Every request | Only at login |
| **Contains User Info** | No | Yes (in payload) |
| **Expiration** | No | Yes (built-in) |
| **Stateless** | Yes | Yes |
| **Security** | Credentials always in transit | Token in transit (safer) |
| **Performance** | Slower (DB lookup each time) | Faster (no DB lookup) |
| **Best For** | Internal APIs, testing | Production APIs |

### JWT Components in Our Implementation

#### 1. JwtUtil
```java
@Component
public class JwtUtil {
    // Generate token
    public String generateToken(UserDetails userDetails)
    
    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails)
    
    // Extract username
    public String extractUsername(String token)
    
    // Extract claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
}
```

**Responsibilities:**
- Generate JWT tokens with user claims
- Validate token signature and expiration
- Extract information from tokens

#### 2. JwtAuthenticationFilter
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. Extract token from Authorization header
        // 2. Validate token
        // 3. Set authentication in SecurityContext
        // 4. Continue filter chain
    }
}
```

**Runs on every request:**
1. Extracts JWT from `Authorization: Bearer <token>` header
2. Validates token (signature, expiration)
3. Extracts user details from token
4. Sets authentication in SecurityContext
5. Allows request to proceed

#### 3. AuthController
```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request)
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth)
}
```

**Endpoints:**
- `POST /auth/login` - Authenticate and return JWT
- `GET /auth/me` - Get current user info (requires JWT)

### Token Storage (Client-Side)

**Where clients store JWT:**

| Storage | Pros | Cons | Use Case |
|---------|------|------|----------|
| **localStorage** | Persistent, easy to use | Vulnerable to XSS | SPAs with XSS protection |
| **sessionStorage** | Cleared on tab close | Not persistent | Temporary sessions |
| **Memory (variable)** | Most secure | Lost on refresh | High security apps |
| **httpOnly Cookie** | Safe from XSS | Vulnerable to CSRF | Need CSRF protection |

**Best Practice for SPAs:**
```javascript
// Login
const response = await fetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
});

const { token } = await response.json();

// Store token (choose based on requirements)
localStorage.setItem('token', token); // or sessionStorage, or in-memory

// Use token
const data = await fetch('/api/products', {
    headers: { 'Authorization': `Bearer ${token}` }
});
```

### Token Expiration & Refresh

**Our Implementation:**
- Token expires after **24 hours** (configurable)
- Client must re-login after expiration
- No refresh tokens (simpler for learning)

**Production Enhancement (Optional):**
```
Access Token (short-lived: 15 min)
  ↓
Expires → Use Refresh Token
  ↓
Get New Access Token (without re-login)
  ↓
Refresh Token expires (7 days) → Re-login required
```

### Security Considerations

#### ✅ Do's
- ✅ **Always use HTTPS** in production
- ✅ **Set reasonable expiration** (not too long)
- ✅ **Use strong secret key** (at least 256 bits)
- ✅ **Validate signature** on every request
- ✅ **Check expiration** on every request
- ✅ **Use HS256 or RS256** algorithm

#### ❌ Don'ts
- ❌ **Don't store sensitive data** in JWT payload (it's base64, not encrypted!)
- ❌ **Don't use weak secret keys**
- ❌ **Don't skip signature validation**
- ❌ **Don't use HTTP** (tokens visible in transit)
- ❌ **Don't hardcode secret** in code (use environment variables)

### Advantages of JWT

1. **Stateless** - No server-side session storage
2. **Scalable** - Works across multiple servers
3. **Self-contained** - Token contains all user info
4. **Performance** - No database lookup on every request
5. **Mobile-friendly** - Works perfectly with mobile apps
6. **Cross-domain** - Can be used across different domains
7. **Expiration built-in** - Automatic token expiry

### When to Use JWT

✅ **Use JWT for:**
- REST APIs
- Mobile apps
- Single Page Applications (SPAs)
- Microservices
- Third-party API integrations
- Cross-domain authentication

❌ **Don't use JWT for:**
- Traditional server-rendered web apps (use sessions instead)
- When you need instant token revocation (JWT can't be invalidated until expiry)
- When payload size matters (JWT larger than session cookie)

---

## 🆕 What's Different Here

### Compared to Module 3 (HTTP Basic):

| Feature | Module 3 | Module 4 |
|---------|----------|----------|
| **Authentication** | Every request with credentials | Login once, use token |
| **Credentials** | Sent every time | Sent only at login |
| **Database Lookup** | Every request | Only at login |
| **Token Type** | None | JWT |
| **Header Format** | `Basic base64(user:pass)` | `Bearer <jwt-token>` |
| **User Info** | Not in request | In token payload |
| **Expiration** | No | Yes (built-in) |
| **Performance** | Slower | Faster |
| **Security** | Less secure | More secure |

### What's New:

- ✅ **JWT token generation** - `/auth/login` endpoint
- ✅ **JwtUtil** - Token creation and validation
- ✅ **JwtAuthenticationFilter** - Validates tokens on every request
- ✅ **Token-based authentication** - No credentials after login
- ✅ **Claims** - User info stored in token
- ✅ **Expiration handling** - Tokens expire automatically

### What's the Same:

- ✅ Same domain model (Product, Tag)
- ✅ Same 3-layer architecture
- ✅ STATELESS session management
- ✅ CSRF disabled
- ✅ Role-based access control
- ✅ Custom 401/403 JSON responses

---

## 🗂️ Project Structure

```
module-4-rest-jwt/
│
├── src/main/java/com/vbforge/security/restjwt/
│   ├── RestJwtApplication.java              # Main class
│   │
│   ├── controller/
│   │   ├── AuthController.java              # ⭐ Login endpoint
│   │   ├── ProductController.java           # REST API
│   │   ├── TagController.java               # REST API
│   │   └── AdminController.java             # ADMIN-only
│   │
│   ├── security/
│   │   ├── JwtUtil.java                     # ⭐ JWT token utility
│   │   ├── JwtAuthenticationFilter.java     # ⭐ JWT validation filter
│   │   ├── JwtSecurityConfig.java           # ⭐ Security config
│   │   └── UserDetailsServiceConfig.java    # User management
│   │
│   ├── dto/
│   │   ├── LoginRequest.java                # ⭐ Login credentials
│   │   ├── AuthResponse.java                # ⭐ JWT token response
│   │   ├── ProductDTO.java
│   │   └── TagDTO.java
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
│   ├── application-dev.properties            # Development profile settings (e.g., local DB connection, debug options) ⭐ JWT config
│   └── application-dev.properties.example    # Configuration template; copy & rename for local setup
│
└── src/test/
    ├── java/.../
    │   ├── repository/                      # @DataJpaTest
    │   ├── service/                         # Mockito tests
    │   └── security/
    │       └── JwtSecurityIntegrationTest.java  # ⭐ JWT tests
    └── resources/
        └── application-test.properties
```

**Key Additions:**
- ⭐ `JwtUtil` - Token generation and validation
- ⭐ `JwtAuthenticationFilter` - Intercepts and validates tokens
- ⭐ `AuthController` - Login endpoint
- ⭐ `LoginRequest` & `AuthResponse` DTOs

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
cd module-4-rest-jwt
mvn clean install
```

### 4️⃣ Run the Application

```bash
mvn spring-boot:run
```

**Console Output:**
```
=================================================================
  Module 4 - REST + JWT is running!
=================================================================
  🌐 Base URL:        http://localhost:8084
  🔐 Login:           POST http://localhost:8084/auth/login
  👤 Current User:    GET  http://localhost:8084/auth/me
  📡 API Products:    GET  http://localhost:8084/api/products
  📡 API Tags:        GET  http://localhost:8084/api/tags
  👑 Admin API:       GET  http://localhost:8084/api/admin/stats
  📚 Swagger UI:      http://localhost:8084/swagger-ui.html

  ⚡ Mode: STATELESS with JWT tokens
  🎫 Auth: JWT Bearer token
  🚫 CSRF: Disabled (not needed)
  ⏰ Token Expiry: 24 hours

  Test Credentials:
  📝 Regular User:    user / password
  👑 Admin User:      admin / admin
=================================================================
```

### 5️⃣ Test JWT Authentication

**Step 1: Login**
```bash
curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiUk9MRV9VU0VSIn1dLCJzdWIiOiJ1c2VyIiwiaWF0IjoxNzA5NDcyMDAwLCJleHAiOjE3MDk1NTg0MDB9.4Hb-8xj9k2L5mN6pQ7rS8tU9vW0xY1zA2BC3dE4fG5H",
  "type": "Bearer",
  "username": "user",
  "roles": ["ROLE_USER"],
  "expiresIn": 86400000
}
```

**Step 2: Use Token**
```bash
curl http://localhost:8084/api/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

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

3. **JWT Security Integration Tests** (`@SpringBootTest`)
   - **Login and token generation** ⭐
   - **Token validation** ⭐
   - **Stateless behavior** ⭐
   - Role-based access control
   - Token expiration handling
   - Complete CRUD with JWT

### Key JWT Tests

```java
// ✅ Test 1: Login returns JWT token
@Test
void whenValidCredentials_thenReturnJwtToken() {
    // Login
    // Verify token structure (3 parts: header.payload.signature)
}

// ✅ Test 2: Use JWT to access protected endpoint
@Test
void whenValidToken_thenAccessProtectedEndpoint() {
    String token = loginAndGetToken("user", "password");
    // Use token to access /api/products
}

// ✅ Test 3: No session created (stateless)
@Test
void whenUsingJwt_thenNoSessionCreated() {
    // Verify no JSESSIONID cookie
}

// ✅ Test 4: Token works for multiple requests
@Test
void whenTokenGenerated_thenWorksForMultipleRequests() {
    String token = loginAndGetToken("user", "password");
    // Use same token for multiple requests
}

// ✅ Test 5: Invalid token returns 401
@Test
void whenInvalidToken_then401() {
    // Try with invalid token
}
```

---

## 📸 API Examples

### Complete Authentication Flow

#### 1. Login (Get JWT Token)

**Request:**
```bash
curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiUk9MRV9VU0VSIn1dLCJzdWIiOiJ1c2VyIiwiaWF0IjoxNzA5NDcyMDAwLCJleHAiOjE3MDk1NTg0MDB9.4Hb-8xj9k2L5mN6pQ7rS8tU9vW0xY1zA2BC3dE4fG5H",
  "type": "Bearer",
  "username": "user",
  "roles": ["ROLE_USER"],
  "expiresIn": 86400000
}
```

**Save the token!** You'll use it for all subsequent requests.

#### 2. Get Current User Info

```bash
TOKEN="your-jwt-token-here"

curl http://localhost:8084/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "username": "user",
  "roles": ["ROLE_USER"]
}
```

#### 3. Get All Products

```bash
curl http://localhost:8084/api/products \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
[]
```

#### 4. Create a Product

```bash
curl -X POST http://localhost:8084/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 1499.99
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop",
  "price": 1499.99,
  "createdAt": "2026-02-14T10:30:00",
  "tags": []
}
```

#### 5. Update a Product

```bash
curl -X PUT http://localhost:8084/api/products/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Gaming Laptop",
    "price": 1299.99
  }'
```

#### 6. Delete a Product

```bash
curl -X DELETE http://localhost:8084/api/products/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### 7. Try Admin Endpoint as USER (403)

```bash
curl http://localhost:8084/api/admin/stats \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "timestamp": "2026-02-14T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. You don't have sufficient permissions.",
  "path": "/api/admin/stats"
}
```

#### 8. Login as Admin

```bash
curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

**Use admin token to access admin endpoint:**
```bash
ADMIN_TOKEN="admin-jwt-token-here"

curl http://localhost:8084/api/admin/stats \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Response:**
```json
{
  "timestamp": "2026-02-14T10:30:00",
  "admin": "admin",
  "totalProducts": 0,
  "totalTags": 0,
  "totalUsers": 2,
  "message": "Admin-only statistics endpoint"
}
```

### Using Postman

1. **Login:**
   - Method: POST
   - URL: `http://localhost:8084/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "user",
       "password": "password"
     }
     ```
   - Copy the `token` from response

2. **Use Token:**
   - Method: GET (or any)
   - URL: `http://localhost:8084/api/products`
   - Authorization tab:
     - Type: `Bearer Token`
     - Token: (paste your token)
   - Send request ✅

### JavaScript (Fetch API)

```javascript
// Login
async function login(username, password) {
    const response = await fetch('http://localhost:8084/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    
    const data = await response.json();
    localStorage.setItem('token', data.token);
    return data.token;
}

// Use token
async function getProducts() {
    const token = localStorage.getItem('token');
    
    const response = await fetch('http://localhost:8084/api/products', {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    
    return await response.json();
}

// Create product
async function createProduct(product) {
    const token = localStorage.getItem('token');
    
    const response = await fetch('http://localhost:8084/api/products', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(product)
    });
    
    return await response.json();
}

// Usage
const token = await login('user', 'password');
const products = await getProducts();
const newProduct = await createProduct({
    name: 'Laptop',
    description: 'Gaming laptop',
    price: 1299.99
});
```

---

## ⚠️ Common Pitfalls

### 1. Token Not Included in Request

**Symptom:** Always getting 401 even after login

**Cause:** Forgot to include Authorization header

**Solution:**
```bash
# ❌ Wrong - No Authorization header
curl http://localhost:8084/api/products

# ✅ Correct
curl http://localhost:8084/api/products \
  -H "Authorization: Bearer your-token-here"
```

### 2. Missing "Bearer " Prefix

**Symptom:** 401 even with token in header

**Cause:** Token sent without "Bearer " prefix

**Solution:**
```bash
# ❌ Wrong
Authorization: eyJhbGciOiJIUzI1NiJ9...

# ✅ Correct
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 3. Token Expired

**Symptom:** Token worked before, now returns 401

**Cause:** Token expired (24 hours default)

**Solution:** Login again to get new token
```bash
curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'
```

### 4. Wrong Secret Key in Production

**Symptom:** Tokens don't validate after deployment

**Cause:** Different secret key in different environments

**Solution:**
```properties
# Use environment variable
jwt.secret=${JWT_SECRET:default-secret-for-dev}

# Or set in deployment
export JWT_SECRET=your-production-secret-base64-encoded
```

### 5. Storing Sensitive Data in JWT

**Issue:** JWT payload is base64 encoded, NOT encrypted!

**Never store:**
- ❌ Passwords
- ❌ Credit card numbers
- ❌ Social security numbers
- ❌ Private keys

**Safe to store:**
- ✅ Username
- ✅ User ID
- ✅ Roles/authorities
- ✅ Non-sensitive preferences

### 6. Not Using HTTPS in Production

**Issue:** JWT tokens visible in network traffic over HTTP

**Solution:**
```
❌ http://api.example.com (token visible!)
✅ https://api.example.com (token encrypted by TLS)
```

### 7. Weak Secret Key

**Issue:** Short secret keys can be brute-forced

**Solution:**
```bash
# Generate strong secret (256 bits)
openssl rand -base64 32

# Use in application-dev.properties
jwt.secret=your-generated-secret-here
```

### 8. No Token Revocation Strategy

**Issue:** Can't invalidate tokens before expiration

**Solutions:**
1. **Short expiration time** (e.g., 15 minutes)
2. **Refresh tokens** (separate token for renewing)
3. **Token blacklist** (store revoked tokens in Redis)
4. **Token versioning** (increment version on password change)

---

## 🎯 Key Takeaways

After completing this module, you should understand:

✅ **JWT Structure**
- Three parts: Header.Payload.Signature
- Base64 encoded, not encrypted
- Self-contained with user information

✅ **JWT Authentication Flow**
- Login once, get token
- Token sent with every request
- Server validates token, not credentials

✅ **JWT Advantages**
- No credentials after login
- Faster (no DB lookup every request)
- Stateless (scales horizontally)
- Mobile-friendly

✅ **JWT Components**
- JwtUtil: Generate and validate tokens
- JwtAuthenticationFilter: Intercept and validate
- AuthController: Login endpoint

✅ **Token Security**
- Always use HTTPS
- Strong secret key (256+ bits)
- Reasonable expiration time
- Don't store sensitive data in payload

✅ **Stateless Authentication**
- No sessions created
- No cookies
- Every request independent
- Perfect for microservices

✅ **Performance Benefits**
- Token contains user info
- No database lookup per request
- Only validate signature
- Much faster than Basic Auth

---

## 🤔 Reflection Questions

1. How is JWT different from HTTP Basic Auth?
2. Why is JWT payload base64 encoded but not encrypted?
3. What happens when a JWT token expires?
4. Can you revoke a JWT token before expiration?
5. Why don't we need CSRF protection with JWT?
6. Where should clients store JWT tokens?
7. What's the advantage of no database lookup on every request?
8. How does JWT enable horizontal scalability?

---

## 🐛 Troubleshooting

### Token Validation Fails

**Check:**
1. Secret key matches in JwtUtil
2. Token not expired
3. Token format is correct (3 parts)
4. Signature algorithm matches (HS256)

**Debug:**
```bash
# Decode JWT payload (base64)
echo "payload-part" | base64 -d

# Check expiration timestamp
# Convert Unix timestamp to date
date -d @1709558400
```

### Login Returns 401

**Check:**
1. Credentials are correct (user/password or admin/admin)
2. PasswordEncoder matches (BCrypt)
3. UserDetailsService returns user

### Can't Create/Update Resources

**Check:**
1. Token included in Authorization header
2. "Bearer " prefix present
3. Token not expired
4. User has correct role

### Tests Failing

```bash
# Run specific test
mvn test -Dtest=JwtSecurityIntegrationTest#whenValidCredentials_thenReturnJwtToken

# Check logs
mvn test -X
```

---

## 📚 Further Reading

### Official Documentation
- [JWT.io - Introduction](https://jwt.io/introduction)
- [RFC 7519 - JWT Standard](https://datatracker.ietf.org/doc/html/rfc7519)
- [JJWT Library Documentation](https://github.com/jwtk/jjwt)
- [Spring Security - JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

### Recommended Articles
- [Understanding JWT](https://jwt.io/introduction)
- [JWT Best Practices](https://curity.io/resources/learn/jwt-best-practices/)
- [JWT vs Session Cookies](https://ponyfoo.com/articles/json-web-tokens-vs-session-cookies)
- [Where to Store JWT](https://auth0.com/docs/secure/security-guidance/data-security/token-storage)

### Security
- [JWT Security Best Practices](https://tools.ietf.org/html/rfc8725)
- [Common JWT Vulnerabilities](https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/)
- [OWASP - JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)

### Videos
- JWT Authentication Explained
- Spring Security with JWT
- JWT vs OAuth2

---

## 🎓 Practice Exercises

1. **Add token refresh endpoint** - `/auth/refresh` that returns new token
2. **Implement logout** - Blacklist tokens on logout
3. **Add custom claims** - Include user email, full name in token
4. **Token versioning** - Invalidate all tokens on password change
5. **Rate limiting** - Limit login attempts per IP
6. **Remember-me with JWT** - Longer expiration for remember-me
7. **Multiple device support** - Track tokens per device
8. **Admin dashboard** - Show active tokens, revoke tokens

---

## 📊 Comparison: All Modules So Far

| Feature | Module 1 | Module 2 | Module 3 | Module 4 |
|---------|----------|----------|----------|----------|
| **Auth Type** | HTTP Basic | Form Login | HTTP Basic | **JWT** |
| **Session** | Stateful | Stateful | Stateless | Stateless |
| **Credentials** | Every request | Once (login) | Every request | Once (login) |
| **Token** | None | Cookie | None | **JWT** |
| **DB Lookup** | Default | On login | Every request | **On login only** |
| **Scalability** | Poor | Poor | Good | **Excellent** |
| **Mobile** | Poor | No | OK | **Perfect** |
| **Performance** | Slow | Medium | Slow | **Fast** |
| **Production** | No | Web apps | Internal | **✅ Yes!** |

---

## 🎯 What's Next?

**Module 5 - OAuth2 Resource Server** will introduce:
- External identity provider (Keycloak)
- OAuth2 standard
- Social login (Google, GitHub, etc.)
- Enterprise authentication
- Token introspection
- Microservice security

**Key Difference:** Authentication handled by external provider, not our app!

---

## 🔒 Security Configuration

**DO NOT use the example credentials in production!**

1. Copy `application-dev.properties.example` to `application-dev.properties`
2. Update all credentials with secure values
3. Never commit `application.properties` to Git
4. Use environment variables for production deployment

---

**Congratulations! 🎉**

**You've mastered JWT authentication - the industry standard for modern APIs!**

This is production-ready knowledge that you'll use in real-world applications!

---

*Last Updated: February 2026*
