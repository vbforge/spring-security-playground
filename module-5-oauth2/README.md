# 🔐 Module 5 - OAuth2 Resource Server

> **Enterprise-Grade Authentication with External Identity Provider (Keycloak)**

---

## 📚 Table of Contents

- [Overview](#-overview)
- [Theory & Concepts](#-theory--concepts)
- [What's Different Here](#-whats-different-here)
- [Project Structure](#-project-structure)
- [How to Run](#-how-to-run)
- [Keycloak Setup](#-keycloak-setup)
- [Testing the Security](#-testing-the-security)
- [API Examples](#-api-examples)
- [Common Pitfalls](#-common-pitfalls)
- [Key Takeaways](#-key-takeaways)
- [Further Reading](#-further-reading)

---

## 🎯 Overview

This module demonstrates **OAuth2 Resource Server** with **Keycloak** as an external identity provider - the enterprise-standard approach for modern authentication in microservices and cloud-native applications.

**Learning Goals:**
- ✅ Understand OAuth2 Resource Server pattern
- ✅ Integrate with external identity provider (Keycloak)
- ✅ Validate JWT tokens from external issuer
- ✅ Map external roles to Spring Security authorities
- ✅ Implement enterprise SSO (Single Sign-On)
- ✅ Understand separation of concerns (Auth Server vs Resource Server)
- ✅ Learn industry-standard authentication patterns

---

## 📖 Theory & Concepts

### What is OAuth2?

**OAuth2** is an authorization framework that enables applications to obtain limited access to user accounts on an HTTP service.

**Key Roles in OAuth2:**

```
┌─────────────────┐
│  Resource Owner │  (User - owns the data)
└────────┬────────┘
         │
         │ 1. Authorize
         ▼
┌─────────────────┐
│ Authorization   │  (Keycloak - authenticates user, issues tokens)
│    Server       │
└────────┬────────┘
         │
         │ 2. Access Token
         ▼
┌─────────────────┐
│     Client      │  (Browser, Mobile App - uses the token)
└────────┬────────┘
         │
         │ 3. API Request + Token
         ▼
┌─────────────────┐
│   Resource      │  (Our Spring App - validates token, serves data)
│    Server       │
└─────────────────┘
```

### OAuth2 vs Our JWT (Module 4)

| Aspect | Module 4 (Our JWT) | Module 5 (OAuth2) |
|--------|-------------------|-------------------|
| **Token Generation** | Our application | Keycloak (external) |
| **User Management** | In-memory in our app | Keycloak database |
| **Authentication** | Our `/auth/login` | Keycloak endpoint |
| **Token Validation** | Our `JwtUtil` | Spring OAuth2 + Keycloak public key |
| **User Database** | Our in-memory list | Keycloak's database |
| **SSO Support** | No | Yes ✅ |
| **Social Login** | No | Yes ✅ (configurable) |
| **Enterprise Ready** | No | Yes ✅ |
| **Separation of Concerns** | No (all-in-one) | Yes ✅ (delegated) |

### Resource Server Pattern

**Resource Server** = API that validates tokens but doesn't issue them.

**Key Principle:** *Separate authentication from resource management*

```
┌──────────────────────────────────────────────────────┐
│              BEFORE (Module 4)                       │
│                                                      │
│  ┌────────────────────────────────────────────┐      │
│  │         Our Spring Application             │      │
│  │                                            │      │
│  │  ┌──────────────┐  ┌──────────────────┐    │      │
│  │  │ Login        │  │ Product API      │    │      │
│  │  │ /auth/login  │  │ /api/products    │    │      │
│  │  │              │  │                  │    │      │
│  │  │ - Validate   │  │ - Validate JWT   │    │      │
│  │  │ - Generate   │  │ - Serve data     │    │      │
│  │  │   JWT        │  │                  │    │      │
│  │  └──────────────┘  └──────────────────┘    │      │
│  └────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│              AFTER (Module 5)                        │
│                                                      │
│  ┌──────────────────┐      ┌──────────────────────┐  │
│  │   Keycloak       │      │  Our Spring App      │  │
│  │  (Auth Server)   │      │ (Resource Server)    │  │
│  │                  │      │                      │  │
│  │  - User DB       │      │  ┌────────────────┐  │  │
│  │  - Login UI      │      │  │ Product API    │  │  │
│  │  - Issue tokens  │      │  │ /api/products  │  │  │
│  │  - Manage users  │      │  │                │  │  │
│  │  - Roles/perms   │      │  │ - Validate JWT │  │  │
│  └──────────────────┘      │  │ - Serve data   │  │  │
│                            │  └────────────────┘  │  │
│                            └──────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

### Why Separate Auth Server and Resource Server?

**Advantages:**

1. **Single Sign-On (SSO)**
   - One login for multiple applications
   - User logs in once, accesses many apps
   - Common in enterprises

2. **Centralized User Management**
   - One place to manage all users
   - Consistent policies across apps
   - Easier auditing and compliance

3. **Scalability**
   - Auth server and resource servers scale independently
   - Add more resource servers without changing auth

4. **Security**
   - Specialized auth server (Keycloak is battle-tested)
   - Resource servers don't handle passwords
   - Reduced attack surface

5. **Flexibility**
   - Easy to add social logins (Google, GitHub, etc.)
   - Support multiple authentication methods
   - Can replace auth server without changing apps

6. **Microservices**
   - Each microservice is a resource server
   - All validate tokens from same auth server
   - No duplicate auth logic

### Keycloak - What Is It?

**Keycloak** is an open-source Identity and Access Management (IAM) solution.

**Features:**
- 🔐 User authentication and authorization
- 👥 User management (create, update, delete)
- 🎭 Role-based access control (RBAC)
- 🌐 Single Sign-On (SSO)
- 🔗 Social login (Google, Facebook, GitHub, etc.)
- 🎫 Token issuance (JWT, SAML)
- 🔒 Multi-factor authentication (MFA)
- 📊 Admin console
- 🔌 LDAP/Active Directory integration
- 🌍 Multi-tenancy support

**In Our Setup:**
- Keycloak = Authorization Server (issues tokens)
- Our Spring App = Resource Server (validates tokens)

### OAuth2 Resource Server Flow

```
┌──────────┐                    ┌──────────┐                    ┌────────────────┐
│  Client  │                    │ Keycloak │                    │ Resource Server│
│ (Browser)│                    │ (Auth)   │                    │ (Our App)      │
└─────┬────┘                    └─────┬────┘                    └────────┬───────┘
      │                               │                                  │
      │ 1. User wants to access API   │                                  │
      │ ──────────────────────────────────────────────────────────────>  │
      │                               │                                  │
      │ 2. No token → 401             │                                  │
      │ <──────────────────────────────────────────────────────────────  │
      │                               │                                  │
      │ 3. Redirect to Keycloak login │                                  │
      │ ─────────────────────────────>│                                  │
      │                               │                                  │
      │ 4. Show login form            │                                  │
      │ <─────────────────────────────│                                  │
      │                               │                                  │
      │ 5. Submit credentials         │                                  │
      │ ─────────────────────────────>│                                  │
      │                               │ 6. Validate credentials          │
      │                               │ 7. Generate JWT token            │
      │ 8. Return token               │                                  │
      │ <─────────────────────────────│                                  │
      │                               │                                  │
      │ 9. API Request + token        │                                  │
      │ ──────────────────────────────────────────────────────────────>  │
      │                               │                                  │
      │                               │ 10. Fetch public key (cached)    │
      │                               │ <─────────────────────────────── │
      │                               │ 11. Return public key            │
      │                               │ ──────────────────────────────>  │
      │                               │                                  │ 12. Verify signature
      │                               │                                  │ 13. Check expiration
      │                               │                                  │ 14. Extract roles
      │                               │                                  │ 15. Authorize
      │ 16. API Response              │                                  │
      │ <──────────────────────────────────────────────────────────────  │
```

### JWT Token Validation

**How Spring validates Keycloak's JWT:**

1. **Extract token** from `Authorization: Bearer <token>` header
2. **Parse JWT** (decode header and payload)
3. **Fetch public key** from Keycloak (cached after first fetch)
   - URL: `http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/certs`
4. **Verify signature** using public key (RSA256)
5. **Check expiration** (`exp` claim)
6. **Extract claims** (username, roles, etc.)
7. **Map roles** to Spring Security authorities
8. **Set authentication** in SecurityContext

**Key Point:** Our app NEVER sees user passwords! Only validates tokens.

### Keycloak JWT Structure

**Keycloak JWT Example:**

```json
{
  "exp": 1709558400,
  "iat": 1709558100,
  "jti": "d5e6f7g8-h9i0-1234-5678-9abcdefghijk",
  "iss": "http://localhost:8080/realms/spring-security-playground",
  "sub": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "typ": "Bearer",
  "azp": "spring-security-playground-client",
  "session_state": "12345678-90ab-cdef-1234-567890abcdef",
  "acr": "1",
  "realm_access": {
    "roles": [
      "user",
      "admin"
    ]
  },
  "resource_access": {
    "spring-security-playground-client": {
      "roles": [
        "client-admin"
      ]
    }
  },
  "scope": "email profile",
  "email_verified": true,
  "name": "Admin User",
  "preferred_username": "admin",
  "given_name": "Admin",
  "family_name": "User",
  "email": "admin@example.com"
}
```

**Important Claims:**
- `iss`: Issuer (Keycloak URL) - used for validation
- `sub`: Subject (user ID)
- `exp`: Expiration timestamp
- `realm_access.roles`: User's realm roles
- `preferred_username`: Username
- `email`: User's email

### Role Mapping

**Keycloak → Spring Security:**

```java
// Keycloak JWT
{
  "realm_access": {
    "roles": ["user", "admin"]
  }
}

// Our converter extracts and maps
roles.stream()
  .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
  .collect(Collectors.toList());

// Spring Security sees
[ROLE_USER, ROLE_ADMIN]

// Can now use
@PreAuthorize("hasRole('ADMIN')")
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

### Public Key Infrastructure (PKI)

**Keycloak uses RSA (asymmetric encryption):**

```
┌──────────────────────────────────────────────────────┐
│               Keycloak (Auth Server)                 │
│                                                      │
│  ┌──────────────┐         ┌──────────────┐           │
│  │ Private Key  │────────>│ Sign JWT     │           │
│  │ (secret)     │         │              │           │
│  └──────────────┘         └──────┬───────┘           │
│                                  │                   │
│                                  │ Signed JWT        │
│                                  ▼                   │
│                           Client receives token      │
└──────────────────────────────────────────────────────┘
                                   │
                                   │ Send to API
                                   ▼
┌──────────────────────────────────────────────────────┐
│           Resource Server (Our App)                  │
│                                                      │
│  ┌──────────────┐         ┌──────────────┐           │
│  │ Public Key   │────────>│ Verify JWT   │           │
│  │ (from Keycloak)        │              │           │
│  └──────────────┘         └──────────────┘           │
│                                                      │
│  Token valid? → Process request                      │
│  Token invalid? → 401 Unauthorized                   │
└──────────────────────────────────────────────────────┘
```

**Key Benefits:**
- ✅ Private key stays on Keycloak (secure!)
- ✅ Public key can be shared (safe!)
- ✅ Multiple resource servers use same public key
- ✅ Keycloak can rotate keys without changing apps

### Single Sign-On (SSO)

**One login, many applications:**

```
User logs in to App 1 (Keycloak)
  ↓
Gets JWT token
  ↓
Can now access:
  → App 1 (Resource Server 1)
  → App 2 (Resource Server 2)
  → App 3 (Resource Server 3)
  ...all without logging in again!
```

**How it works:**
1. User logs into Keycloak once
2. Keycloak creates session and issues token
3. User accesses App 1 → validates token ✅
4. User accesses App 2 → validates same token ✅
5. User accesses App 3 → validates same token ✅

**No separate logins needed!**

---

## 🆕 What's Different Here

### Compared to Module 4 (Our JWT):

| Feature | Module 4 | Module 5 |
|---------|----------|----------|
| **Authentication** | Internal (`/auth/login`) | External (Keycloak) |
| **Token Issuer** | Our application | Keycloak |
| **Token Validation** | `JwtUtil` class | Spring OAuth2 |
| **User Storage** | In-memory | Keycloak database |
| **Password Handling** | We hash with BCrypt | Keycloak handles |
| **Public Key** | Symmetric (shared secret) | Asymmetric (RSA public key) |
| **Role Source** | Our code | Keycloak realm roles |
| **User Management** | Code changes | Keycloak admin UI |
| **SSO** | No | Yes ✅ |
| **Social Login** | No | Yes ✅ (configurable) |
| **MFA** | No | Yes ✅ (configurable) |
| **Admin UI** | No | Yes ✅ (Keycloak console) |
| **Production Ready** | Learning only | Yes ✅ |

### What's New:

- ✅ **OAuth2 Resource Server** - Spring validates external JWT
- ✅ **Keycloak Integration** - External identity provider
- ✅ **Public Key Validation** - RSA signature verification
- ✅ **Role Mapping** - Extract roles from Keycloak JWT
- ✅ **No Login Endpoint** - Authentication delegated to Keycloak
- ✅ **Docker Compose** - Easy Keycloak setup
- ✅ **Enterprise Patterns** - Production-ready architecture

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
module-5-oauth2/
│
├── docker-compose.yml                       # ⭐ Keycloak setup
├── KEYCLOAK-SETUP.md                        # ⭐ Step-by-step guide
│
├── src/main/java/com/vbforge/security/oauth2/
│   ├── OAuth2Application.java               # Main class
│   │
│   ├── controller/
│   │   ├── ProductController.java           # REST API
│   │   ├── TagController.java               # REST API
│   │   └── AdminController.java             # ADMIN-only
│   │
│   ├── security/
│   │   └── OAuth2ResourceServerConfig.java  # ⭐ OAuth2 config
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
│   ├── application-dev.properties            # Development profile settings (e.g., local DB connection, debug options) ⭐ OAuth2 config
│   └── application-dev.properties.example    # Configuration template; copy & rename for local setup              
│
└── src/test/
    ├── java/.../
    │   ├── repository/                      # @DataJpaTest
    │   ├── service/                         # Mockito tests
    │   └── security/
    │       └── OAuth2SecurityIntegrationTest.java  # ⭐ OAuth2 tests
    └── resources/
        └── application-test.properties
```

**Key Additions:**
- ⭐ `docker-compose.yml` - Keycloak setup
- ⭐ `KEYCLOAK-SETUP.md` - Configuration guide
- ⭐ `OAuth2ResourceServerConfig` - OAuth2 Resource Server
- ⭐ No `AuthController` - Authentication delegated to Keycloak

---

## 🚀 How to Run

### Prerequisites

- ✅ JDK 17+
- ✅ Maven 3.8+
- ✅ **Docker** and **Docker Compose** (for Keycloak)
- ✅ MySQL 8+ (for application data)

### Quick Start

#### 1️⃣ Start Keycloak

```bash
cd module-5-oauth2

# Start Keycloak with Docker Compose
docker-compose up -d

# Verify Keycloak is running
docker-compose ps

# Check logs (wait for startup - takes 30-60 seconds)
docker-compose logs -f keycloak
```

**Keycloak will be available at:** http://localhost:8080

#### 2️⃣ Configure Keycloak

Follow the detailed guide: **[KEYCLOAK-SETUP.md](./KEYCLOAK-SETUP.md)**

**Quick Summary:**
1. Access Keycloak admin console (admin/admin)
2. Create realm: `spring-security-playground`
3. Create roles: `user`, `admin`
4. Create client: `spring-security-playground-client`
5. Get client secret
6. Create users:
   - `user` / `password` (role: user)
   - `admin` / `admin` (roles: user, admin)

#### 3️⃣ Setup MySQL

```bash
mysql -u root -p

CREATE DATABASE security_playground_db;
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON security_playground_db.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 4️⃣ Build and Run Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

**Application will start on:** http://localhost:8085

---

## 🔧 Keycloak Setup

See **[KEYCLOAK-SETUP.md](./KEYCLOAK-SETUP.md)** for complete step-by-step instructions.

**Quick Reference:**

### Get Token from Keycloak

```bash
# Replace YOUR_CLIENT_SECRET with actual secret from Keycloak
curl -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-security-playground-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=user" \
  -d "password=password"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "...",
  "scope": "email profile"
}
```

### Use Token with API

```bash
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

curl http://localhost:8085/api/products \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🧪 Testing the Security

### Run All Tests

```bash
mvn test
```

**All tests run WITHOUT Keycloak!** We use Spring Security Test's `jwt()` mock.

### Test Categories

1. **Repository Tests** - JPA with H2
2. **Service Tests** - Mockito
3. **OAuth2 Security Tests** - Mock JWT validation

### Key OAuth2 Tests

```java
// ✅ Test 1: Mock JWT with role
@Test
void whenValidJwt_thenAccessGranted() {
    mockMvc.perform(get("/api/products")
        .with(jwt()
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isOk());
}

// ✅ Test 2: No JWT returns 401
@Test
void whenNoJwt_then401() {
    mockMvc.perform(get("/api/products"))
        .andExpect(status().isUnauthorized());
}

// ✅ Test 3: Wrong role returns 403
@Test
void whenUserRole_thenCannotAccessAdmin() {
    mockMvc.perform(get("/api/admin/stats")
        .with(jwt()
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
}
```

---

## 📸 API Examples

### Complete OAuth2 Flow

#### 1. Get Token from Keycloak

**For regular user:**
```bash
curl -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-security-playground-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=user" \
  -d "password=password" \
  | jq -r '.access_token'
```

**For admin:**
```bash
curl -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-security-playground-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=admin" \
  | jq -r '.access_token'
```

#### 2. Use Token with Resource Server

```bash
# Save token
TOKEN=$(curl -s -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-security-playground-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=user" \
  -d "password=password" | jq -r '.access_token')

# Get products
curl http://localhost:8085/api/products \
  -H "Authorization: Bearer $TOKEN"

# Create product
curl -X POST http://localhost:8085/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "OAuth2 Product",
    "description": "Created with Keycloak token",
    "price": 499.99
  }'
```

#### 3. Try Admin Endpoint

```bash
# As user (403)
curl http://localhost:8085/api/admin/stats \
  -H "Authorization: Bearer $USER_TOKEN"

# As admin (200)
curl http://localhost:8085/api/admin/stats \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Decode JWT Token

**Online:** https://jwt.io

**Command line:**
```bash
# Get token payload (part between first and second dot)
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq
```

### Refresh Token

```bash
# Use refresh token to get new access token
curl -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-security-playground-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=$REFRESH_TOKEN"
```

---

## ⚠️ Common Pitfalls

### 1. Keycloak Not Running

**Symptom:** "Connection refused" or "Unable to connect to issuer"

**Solution:**
```bash
# Check if Keycloak is running
docker-compose ps

# Start Keycloak
docker-compose up -d

# Check logs
docker-compose logs keycloak
```

### 2. Wrong Issuer URI

**Symptom:** "Invalid issuer" or token validation fails

**Check:**
```properties
# In application-dev.properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/spring-security-playground
```

**Verify:**
```bash
# Should return realm info
curl http://localhost:8080/realms/spring-security-playground
```

### 3. Token Expired

**Symptom:** 401 after token was working

**Cause:** Keycloak tokens expire (default 5 minutes)

**Solution:** Get a fresh token

### 4. Wrong Client Secret

**Symptom:** "Invalid client credentials" when getting token

**Solution:**
1. Go to Keycloak admin console
2. Clients → spring-security-playground-client
3. Credentials tab
4. Copy the correct client secret

### 5. User Missing Roles

**Symptom:** 403 even with valid token

**Solution:**
1. Go to Keycloak admin console
2. Users → Select user
3. Role mapping tab
4. Assign required role

### 6. Realm Not Found

**Symptom:** "Realm does not exist"

**Solution:**
1. Verify realm name: `spring-security-playground`
2. Check dropdown in Keycloak admin console
3. Create realm if missing

### 7. Can't Get Token

**Common Issues:**
```bash
# Wrong username/password
"error": "invalid_grant", "error_description": "Invalid user credentials"
→ Check user credentials in Keycloak

# Wrong client ID
"error": "invalid_client"
→ Verify client ID: spring-security-playground-client

# Wrong client secret
"error": "unauthorized_client"
→ Get correct secret from Keycloak Credentials tab
```

---

## 🎯 Key Takeaways

After completing this module, you should understand:

✅ **OAuth2 Resource Server Pattern**
- Separate authentication from resource management
- Validate tokens from external issuer
- Don't handle passwords directly

✅ **External Identity Provider**
- Keycloak handles authentication
- Our app only validates tokens
- Centralized user management

✅ **Public Key Validation**
- Keycloak uses RSA (asymmetric)
- Private key signs (stays on Keycloak)
- Public key verifies (shared with resource servers)

✅ **Role Mapping**
- Extract roles from JWT claims
- Map to Spring Security authorities
- `realm_access.roles` → `ROLE_USER`, `ROLE_ADMIN`

✅ **Enterprise Benefits**
- Single Sign-On (SSO)
- Social login capability
- MFA support
- Admin UI for user management
- Scalability (independent scaling)

✅ **Production Readiness**
- Battle-tested auth server (Keycloak)
- Industry-standard patterns
- Microservice-friendly
- Cloud-native architecture

✅ **Separation of Concerns**
- Auth Server: Authentication & token issuance
- Resource Server: Token validation & resource serving
- Clear boundaries and responsibilities

---

## 🤔 Reflection Questions

1. Why separate authentication (Keycloak) from resource serving (our app)?
2. How does public/private key cryptography enable distributed validation?
3. What's the difference between `realm_access` and `resource_access` roles?
4. How does SSO work across multiple applications?
5. Why can't we invalidate JWT tokens before expiration?
6. What happens if Keycloak goes down temporarily?
7. How would you add Google login to this setup?
8. What's the advantage of refresh tokens over long-lived access tokens?

---

## 🐛 Troubleshooting

### Keycloak Issues

```bash
# Restart Keycloak
docker-compose down
docker-compose up -d

# Remove all data and start fresh
docker-compose down -v
docker-compose up -d

# Check Keycloak logs
docker-compose logs -f keycloak
```

### Application Issues

```bash
# Enable debug logging
# In application-dev.properties
logging.level.org.springframework.security.oauth2=TRACE

# Test connection to Keycloak
curl http://localhost:8080/realms/spring-security-playground/.well-known/openid-configuration
```

### Token Issues

```bash
# Decode token to see contents
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq

# Check token expiration
# exp claim is Unix timestamp
date -d @<exp-value>

# Verify token signature manually (advanced)
# Use jwt.io with Keycloak's public key
```

---

## 📚 Further Reading

### Official Documentation
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OAuth 2.0 RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749)
- [JWT RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519)

### Keycloak Guides
- [Getting Started with Keycloak](https://www.keycloak.org/getting-started)
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/)
- [Keycloak Themes](https://www.keycloak.org/docs/latest/server_development/#_themes)
- [Keycloak with Spring Boot](https://www.baeldung.com/spring-boot-keycloak)

### OAuth2 & Security
- [OAuth 2.0 Simplified](https://www.oauth.com/)
- [Understanding OAuth2](https://auth0.com/docs/get-started/authentication-and-authorization-flow)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [Microservices Security Patterns](https://microservices.io/patterns/security/)

### Videos
- OAuth 2.0 and OpenID Connect (in plain English)
- Keycloak Tutorial for Beginners
- Spring Security OAuth2 Resource Server

---

## 🎓 Practice Exercises

1. **Add social login** - Configure Google/GitHub login in Keycloak
2. **Multi-tenancy** - Create multiple realms for different organizations
3. **Custom roles** - Create fine-grained roles and permissions
4. **Admin API** - Use Keycloak Admin REST API to manage users
5. **Token introspection** - Implement token introspection endpoint
6. **Logout** - Implement logout with token revocation
7. **Remember-me** - Configure longer-lived refresh tokens
8. **MFA** - Enable multi-factor authentication in Keycloak
9. **LDAP integration** - Connect Keycloak to Active Directory
10. **Multiple clients** - Create separate clients for web/mobile

---

## 📊 Complete Comparison: All 5 Modules

| Feature | Module 1 | Module 2 | Module 3 | Module 4 | Module 5 |
|---------|----------|----------|----------|----------|----------|
| **Auth Type** | HTTP Basic | Form Login | HTTP Basic | JWT | **OAuth2** |
| **Session** | Stateful | Stateful | Stateless | Stateless | Stateless |
| **Token** | None | Cookie | None | JWT | **Keycloak JWT** |
| **Login** | Auto | Form page | Every req | Once | **Keycloak** |
| **User Mgmt** | Generated | In-memory | In-memory | In-memory | **Keycloak** |
| **Scalability** | Poor | Poor | Good | Excellent | **Excellent** |
| **SSO** | No | No | No | No | **Yes ✅** |
| **Social Login** | No | No | No | No | **Yes ✅** |
| **Enterprise** | No | No | No | No | **Yes ✅** |
| **Production** | No | Web apps | Simple APIs | APIs | **All ✅** |

---

## 🎊 Congratulations!

**You've completed ALL 5 modules!** 🎉

You now understand:
- ✅ HTTP Basic authentication
- ✅ Form-based login with sessions
- ✅ Stateless REST APIs
- ✅ JWT token generation and validation
- ✅ **OAuth2 Resource Server with external provider**
- ✅ **Enterprise authentication patterns**
- ✅ **Microservices security**

**This is production-grade knowledge!** You're ready to build secure, scalable applications! 🚀

---

## 🌟 What's Next?

**Optional Advanced Topics:**
- OAuth2 Authorization Code Flow (for web apps)
- PKCE (Proof Key for Code Exchange) for SPAs
- OpenID Connect (OIDC) protocol details
- Token introspection and revocation
- Dynamic client registration
- Custom Keycloak providers
- Keycloak clustering and high availability
- Integration with cloud providers (AWS Cognito, Azure AD, etc.)

**You've mastered Spring Security!** 🏆

---

## 🔒 Security Configuration

**DO NOT use the example credentials in production!**

1. Copy `application-dev.properties.example` to `application-dev.properties`
2. Update all credentials with secure values
3. Never commit `application.properties` to Git
4. Use environment variables for production deployment

---

*Last Updated: February 2026*

*Thank you for completing this comprehensive Spring Security journey!*
