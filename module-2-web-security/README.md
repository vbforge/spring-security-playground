# 🔐 Module 2 - Web Security

> **Session-Based Authentication with Form Login**

---

## 📚 Table of Contents

- [Overview](#-overview)
- [Theory & Concepts](#-theory--concepts)
- [What's Different Here](#-whats-different-here)
- [Project Structure](#-project-structure)
- [How to Run](#-how-to-run)
- [Testing the Security](#-testing-the-security)
- [User Interface Guide](#-user-interface-guide)
- [API Examples](#-api-examples)
- [Common Pitfalls](#-common-pitfalls)
- [Key Takeaways](#-key-takeaways)
- [Further Reading](#-further-reading)

---

## 🎯 Overview

This module demonstrates **traditional web application security** with form-based login, session management, and role-based access control. It's how most MVC web applications handle authentication.

**Learning Goals:**
- ✅ Implement custom form login with Thymeleaf
- ✅ Configure in-memory users with roles
- ✅ Understand session-based (stateful) authentication
- ✅ Implement role-based access control
- ✅ Work with CSRF protection
- ✅ Handle logout and remember-me functionality
- ✅ Distinguish between 401 and 403 errors

---

## 📖 Theory & Concepts

### Form-Based Login

Unlike HTTP Basic (Module 1), form login provides a better user experience:

```
┌─────────┐                       ┌─────────┐
│ Browser │                       │ Server  │
└────┬────┘                       └────┬────┘
     │                                 │
     │  GET /products                  │
     │ ─────────────────────────────>  │
     │                                 │
     │  302 Redirect to /login         │
     │ <─────────────────────────────  │
     │                                 │
     │  GET /login (show form)         │
     │ ─────────────────────────────>  │
     │                                 │
     │  200 OK (login.html)            │
     │ <─────────────────────────────  │
     │                                 │
     │  POST /perform_login            │
     │  username=user&password=pass    │
     │ ─────────────────────────────>  │
     │                                 │
     │  302 Redirect to /products      │
     │  Set-Cookie: JSESSIONID=XXX     │
     │ <─────────────────────────────  │
     │                                 │
     │  GET /products                  │
     │  Cookie: JSESSIONID=XXX         │
     │ ─────────────────────────────>  │
     │                                 │
     │  200 OK (products page)         │
     │ <─────────────────────────────  │
```

**Key Points:**
- User sees a proper login form, not a browser dialog
- Credentials sent via POST (more secure than GET)
- Session cookie (JSESSIONID) stores authentication state
- Subsequent requests include the session cookie

### Session-Based Authentication (Stateful)

**What is a Session?**
- Server-side storage of user state
- Identified by JSESSIONID cookie
- Contains authentication info, user details, attributes

**Session Lifecycle:**

```
Login → Create Session → Store in Server Memory/Redis
                        ↓
                    Generate JSESSIONID
                        ↓
                    Send Cookie to Browser
                        ↓
                    User Makes Requests
                        ↓
                    Cookie Sent Automatically
                        ↓
                    Server Validates Session
                        ↓
                    Logout → Invalidate Session
```

**Advantages:**
- ✅ Simple to implement
- ✅ Server controls session lifetime
- ✅ Can invalidate sessions immediately
- ✅ Works great for traditional web apps

**Disadvantages:**
- ❌ Not ideal for REST APIs
- ❌ Doesn't scale horizontally easily
- ❌ Requires sticky sessions or session replication
- ❌ Not suitable for mobile apps

### In-Memory User Storage

In this module, users are stored in memory (not database):

```java
UserDetails user = User.builder()
    .username("user")
    .password(passwordEncoder().encode("password"))
    .roles("USER")
    .build();

UserDetails admin = User.builder()
    .username("admin")
    .password(passwordEncoder().encode("admin"))
    .roles("ADMIN")
    .build();

return new InMemoryUserDetailsManager(user, admin);
```

**Pros:**
- ✅ Fast and simple
- ✅ Perfect for learning/testing
- ✅ No database required

**Cons:**
- ❌ Users reset on restart
- ❌ Can't register new users
- ❌ Not production-ready

**Production Alternative:** Database-backed `UserDetailsService` (we'll do this in Module 4)

### Role-Based Access Control (RBAC)

**Roles vs Authorities:**

| Concept | Description | Example |
|---------|-------------|---------|
| **Role** | High-level group | `ROLE_ADMIN`, `ROLE_USER` |
| **Authority** | Specific permission | `READ_PRODUCTS`, `DELETE_USERS` |

**In Spring Security:**
- Roles are just authorities with `ROLE_` prefix
- `hasRole("ADMIN")` checks for `ROLE_ADMIN` authority
- `hasAuthority("ROLE_ADMIN")` is equivalent

**Configuration in this module:**

```java
.requestMatchers("/admin/**").hasRole("ADMIN")           // Only ADMIN
.requestMatchers("/api/products/**").hasAnyRole("USER", "ADMIN")  // USER or ADMIN
.requestMatchers("/", "/login").permitAll()               // Everyone
.anyRequest().authenticated()                             // Must be logged in
```

### CSRF Protection (Cross-Site Request Forgery)

**What is CSRF?**

An attack where a malicious site tricks a user's browser into making unwanted requests to your site:

```
1. User logs into yourbank.com
2. User visits evilsite.com (in another tab)
3. evilsite.com has: <form action="https://yourbank.com/transfer" method="POST">
4. Form auto-submits, transferring money!
5. Request includes user's session cookie automatically
```

**How Spring Security Prevents This:**

1. **Generate Token:** Server creates unique CSRF token per session
2. **Embed in Forms:** Token included in every form (hidden field)
3. **Validate on Submit:** Server checks if token matches session
4. **Reject if Missing:** Requests without valid token return 403

**In Thymeleaf:**
```html
<form th:action="@{/perform_login}" method="post">
    <!-- CSRF token automatically added by Thymeleaf + Spring Security -->
    <input type="text" name="username">
    <button type="submit">Login</button>
</form>
```

**CSRF is Enabled by Default** for:
- POST, PUT, DELETE, PATCH
- Not required for GET (safe methods)

**When to Disable CSRF:**
- Pure REST APIs (use tokens like JWT instead)
- Stateless applications
- When using other CSRF protection mechanisms

### Password Encoding with BCrypt

**Never store plain-text passwords!**

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**BCrypt Features:**
- ✅ One-way hashing (can't decrypt)
- ✅ Automatic salting (prevents rainbow tables)
- ✅ Configurable work factor (slow = secure)
- ✅ Industry standard

**Example:**
```
Plain:    "password"
BCrypt:   "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
          └─┬─┘└┬┘└──────┬───────┘└────┬────┘
         Version│    Salt         Hash
             Rounds
```

### Remember-Me Functionality

Allows users to stay logged in across browser sessions:

```java
.rememberMe(remember -> remember
    .key("uniqueAndSecretKey")
    .tokenValiditySeconds(86400)  // 24 hours
    .rememberMeParameter("remember-me")
)
```

**How it Works:**
1. User checks "Remember Me" on login
2. Server creates remember-me cookie (separate from session)
3. Remember-me cookie has longer expiration (24 hours)
4. If session expires, remember-me cookie auto-authenticates
5. Security consideration: Less secure than re-authentication

**Cookie Comparison:**

| Cookie | Duration | Purpose |
|--------|----------|---------|
| **JSESSIONID** | Session (browser close) | Current authentication |
| **remember-me** | 24 hours (configurable) | Re-authentication |

### 401 vs 403 - The Crucial Difference

| Code | Name | Meaning | When | Example |
|------|------|---------|------|---------|
| **401** | Unauthorized | Not authenticated | No valid credentials | Not logged in |
| **403** | Forbidden | Not authorized | Valid credentials, insufficient permissions | USER trying to access /admin |

**In Form Login (Module 2):**
- Unauthenticated requests → **302 Redirect** to /login (not 401!)
- Wrong role → **403 Forbidden**

**In REST APIs (Module 3+):**
- No credentials → **401 Unauthorized**
- Wrong role → **403 Forbidden**

### Session Management

**Configuration Options:**

```java
.sessionManagement(session -> session
    .maximumSessions(1)              // Only 1 session per user
    .maxSessionsPreventsLogin(false) // New login kicks out old session
)
```

**Strategies:**

| Setting | Behavior |
|---------|----------|
| `maximumSessions(1)` + `maxSessionsPreventsLogin(false)` | New login invalidates old session |
| `maximumSessions(1)` + `maxSessionsPreventsLogin(true)` | Prevent login if already logged in |
| `maximumSessions(-1)` | Unlimited concurrent sessions |

---

## 🆕 What's Different Here

**Compared to Module 1 (Default Security):**
- ✅ Custom SecurityConfig (not auto-configured)
- ✅ Form login instead of HTTP Basic
- ✅ Custom login page with Thymeleaf
- ✅ In-memory users with BCrypt passwords
- ✅ Role-based access control (USER vs ADMIN)
- ✅ Remember-me functionality
- ✅ Custom access denied page
- ✅ Web pages for user interface

**What's the Same:**
- ✅ Same domain model (Product, Tag)
- ✅ Same 3-layer architecture
- ✅ Session-based (stateful) authentication
- ✅ CSRF protection enabled

**Compared to Module 3+ (REST APIs):**
- ❌ Not stateless (uses sessions)
- ❌ Not suitable for APIs consumed by mobile apps
- ❌ Redirects instead of 401 responses
- ✅ Perfect for traditional web applications

---

## 🗂️ Project Structure

```
module-2-web-security/
│
├── src/main/java/com/vbforge/security/websec/
│   ├── WebSecurityApplication.java          # Main class
│   │
│   ├── controller/
│   │   ├── WebController.java               # Web pages (Thymeleaf)
│   │   ├── ProductController.java           # REST API
│   │   └── TagController.java               # REST API
│   │
│   ├── security/
│   │   └── WebSecurityConfig.java           # ⭐ Security configuration
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
│   └── templates/                            # ⭐ Thymeleaf view templates
│       ├── home.html                         # Public landing page
│       ├── login.html                        # Custom authentication (login) page
│       ├── products.html                     # Secured products listing (authentication required)
│       ├── admin.html                        # Admin dashboard (ROLE_ADMIN only)
│       └── access-denied.html                # 403 – Access Denied error page
│
└── src/test/
    ├── java/.../
    │   ├── repository/                      # @DataJpaTest
    │   ├── service/                         # Mockito tests
    │   └── security/
    │       └── WebSecurityIntegrationTest.java  # ⭐ Security tests
    └── resources/
        └── application-test.properties
```

**Key Differences from Module 1:**
- ✅ `security/` package with custom config
- ✅ `templates/` folder with HTML pages
- ✅ `WebController` for rendering views

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
cd module-2-web-security
mvn clean install
```

### 4️⃣ Run the Application

```bash
mvn spring-boot:run
```

**Console Output:**
```
=================================================================
  Module 2 - Web Security is running!
=================================================================
  🌐 Application URL: http://localhost:8082
  🔐 Login Page:      http://localhost:8082/login
  🛍️  Products Page:   http://localhost:8082/products
  👑 Admin Page:      http://localhost:8082/admin

  Test Credentials:
  📝 Regular User:    user / password
  👑 Admin User:      admin / admin
=================================================================
```

### 5️⃣ Access the Application

Open your browser and navigate to: **http://localhost:8082**

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
   - Form login behavior
   - Role-based access control
   - CSRF protection
   - Session management
   - 401 vs 403 responses

### Key Security Test Scenarios

```java
// ✅ Test 1: Successful login
formLogin("/perform_login")
    .user("user")
    .password("password")
    → Authenticated + Redirect to /products

// ✅ Test 2: Failed login
formLogin("/perform_login")
    .user("user")
    .password("wrong")
    → Unauthenticated + Redirect to /login?error

// ✅ Test 3: Role restriction
@WithMockUser(roles = "USER")
GET /admin
    → 403 Forbidden

// ✅ Test 4: CSRF protection
@WithMockUser
POST /api/products (without .with(csrf()))
    → 403 Forbidden

// ✅ Test 5: CSRF with token
@WithMockUser
POST /api/products (with .with(csrf()))
    → 201 Created
```

---

## 🎨 User Interface Guide

### Home Page (/)

**Access:** Public (no login required)

**Features:**
- Welcome message
- Login button (if not authenticated)
- Products/Admin/Logout buttons (if authenticated)
- Feature list explaining module capabilities

**Try it:**
```bash
curl http://localhost:8082/
```

### Login Page (/login)

**Access:** Public (no login required)

**Features:**
- Username and password fields
- Remember-me checkbox
- Test credentials displayed on page
- Error message if login fails
- Success message after logout

**Try it:**
1. Navigate to http://localhost:8082/login
2. Try logging in with **user / password**
3. Try logging in with **admin / admin**
4. Try wrong credentials to see error

### Products Page (/products)

**Access:** Requires authentication (USER or ADMIN role)

**Features:**
- Displays all products from database
- Shows current user and roles
- Links to home and admin (if ADMIN)
- Logout button
- API endpoint information

**Try it:**
1. Login first
2. Navigate to http://localhost:8082/products
3. Products will be empty initially (add via API)

### Admin Page (/admin)

**Access:** Requires ADMIN role only

**Features:**
- Admin-only dashboard
- Feature cards (demo only)
- Shows implementation notes
- Cannot be accessed by USER role (403 error)

**Try it:**
1. Login as **admin / admin**
2. Navigate to http://localhost:8082/admin
3. Try with **user / password** → See 403 error

### Access Denied Page (/access-denied)

**Access:** Public (shown on 403 errors)

**Features:**
- Clear explanation of 403 vs 401
- Current user information
- Suggestion to login with correct role
- Links to home and products

**Trigger it:**
1. Login as **user / password**
2. Try accessing http://localhost:8082/admin
3. See 403 Access Denied page

---

## 📸 API Examples

The REST API is the same as Module 1, but now protected by form login.

### Using Browser (Easiest)

1. **Login first:** http://localhost:8082/login
2. **Then access API:** http://localhost:8082/api/products

Your session cookie will be sent automatically!

### Using cURL with Session

```bash
# 1. Login and save cookies
curl -c cookies.txt \
  -d "username=user&password=password" \
  -X POST http://localhost:8082/perform_login

# 2. Use cookies for API calls
curl -b cookies.txt \
  http://localhost:8082/api/products
```

### Using cURL with CSRF Token

For POST/PUT/DELETE, you need CSRF token:

```bash
# This is complex with cURL - better to use Postman or browser!

# 1. Get login page to extract CSRF token
# 2. Login with CSRF token
# 3. Use session + CSRF for POST requests

# It's easier to just use the browser or Postman!
```

### Using Postman

**Setup:**
1. Create new request to http://localhost:8082/login (GET)
2. This will set the session cookie
3. In body, use `x-www-form-urlencoded`:
   - username: user
   - password: password
4. Send POST to http://localhost:8082/perform_login
5. Now you can make API requests with the session cookie

**Create Product:**
```http
POST http://localhost:8082/api/products
Content-Type: application/json

{
  "name": "Laptop",
  "description": "Gaming laptop",
  "price": 1299.99
}
```

**Note:** Postman handles CSRF automatically if you've logged in via the form first.

### Creating Products via API

Once logged in:

```bash
# Create a product
curl -b cookies.txt \
  -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "Gaming laptop",
    "price": 1299.99
  }'

# Get all products
curl -b cookies.txt \
  http://localhost:8082/api/products

# Create a tag
curl -b cookies.txt \
  -X POST http://localhost:8082/api/tags \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics"
  }'
```

---

## ⚠️ Common Pitfalls

### 1. CSRF Token Missing on POST

**Symptom:** 403 Forbidden when trying to POST/PUT/DELETE

**Cause:** CSRF token not included in request

**Solutions:**

**In Thymeleaf Forms:**
```html
<!-- Automatically included by th:action -->
<form th:action="@{/api/products}" method="post">
    <!-- CSRF token added automatically -->
</form>
```

**In JavaScript/AJAX:**
```javascript
// Get CSRF token from meta tag
const token = document.querySelector('meta[name="_csrf"]').content;
const header = document.querySelector('meta[name="_csrf_header"]').content;

fetch('/api/products', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        [header]: token
    },
    body: JSON.stringify(product)
});
```

**In Tests:**
```java
mockMvc.perform(post("/api/products")
    .with(csrf())  // Include CSRF token
    .content(json))
```

### 2. Session Cookie Not Sent

**Symptom:** Keep getting redirected to login

**Cause:** Browser not storing/sending JSESSIONID cookie

**Solutions:**
- Check browser cookie settings
- Ensure same-origin requests
- In Postman: Enable "Automatically follow redirects"
- In cURL: Use `-c` to save cookies, `-b` to send them

### 3. Can't Access Admin Page

**Symptom:** 403 Forbidden on /admin

**Cause:** Logged in as USER, not ADMIN

**Solution:** Logout and login with **admin / admin**

### 4. Port Already in Use

**Symptom:** `Port 8082 already in use`

**Solution:** Change port in `application.properties`:
```properties
server.port=8083
```

### 5. Remember-Me Not Working

**Symptom:** Session expires even with remember-me checked

**Cause:** Different domain, cookie settings, or short validity

**Check:**
- Cookie is created (check browser dev tools)
- Cookie validity: 24 hours default
- Cookie domain matches your host

### 6. Password Encoding Mismatch

**Symptom:** Login always fails even with correct credentials

**Cause:** Passwords not BCrypt encoded or wrong encoder

**Solution:**
```java
// Make sure you're using BCrypt
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// And encoding passwords when creating users
.password(passwordEncoder().encode("password"))
```

---

## 🎯 Key Takeaways

After completing this module, you should understand:

✅ **Form-Based Authentication**
- How form login differs from HTTP Basic
- Custom login pages with Thymeleaf
- Login success/failure handling

✅ **Session Management**
- Stateful authentication
- JSESSIONID cookie lifecycle
- Session invalidation on logout

✅ **Role-Based Access Control**
- Difference between roles and authorities
- `hasRole()` vs `hasAuthority()`
- Protecting endpoints by role

✅ **CSRF Protection**
- Why CSRF attacks are dangerous
- How Spring Security prevents them
- When to enable/disable CSRF

✅ **Password Security**
- Never store plain-text passwords
- BCrypt encoding and salting
- Password verification process

✅ **Remember-Me**
- Persistent authentication
- Security trade-offs
- Cookie configuration

✅ **401 vs 403**
- Not authenticated (401) → Redirect in form login
- Not authorized (403) → Access denied page

✅ **User Experience**
- Custom login pages
- Error handling
- Access denied pages
- Role-specific navigation

---

## 🤔 Reflection Questions

1. Why do we redirect to login instead of returning 401 in web applications?
2. What's the security implication of remember-me functionality?
3. How does CSRF protection work? Could you bypass it?
4. Why use BCrypt instead of MD5 or SHA for passwords?
5. What happens if two users with the same username try to login? (Check `maximumSessions`)
6. How would you scale this application horizontally? (Hint: session replication)
7. When would you NOT want to use session-based authentication?

---

## 🐛 Troubleshooting

### MySQL Connection Failed

```bash
# Check MySQL is running
sudo systemctl status mysql

# Test connection
mysql -u dev_user -p -h localhost
```

### Tests Failing

```bash
# Ensure test profile active
# Check @ActiveProfiles("test") present
# Verify H2 database in test config
```

### Can't Login

**Check:**
1. Username and password are correct
2. User is properly encoded with BCrypt
3. Check console logs for authentication errors
4. Verify `WebSecurityConfig` is loaded

### Thymeleaf Template Not Found

**Check:**
1. Templates are in `src/main/resources/templates/`
2. File names match controller return values
3. `.html` extension present
4. Thymeleaf dependency in pom.xml

---

## 📚 Further Reading

### Official Documentation
- [Spring Security - Form Login](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html)
- [Spring Security - Session Management](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html)
- [Spring Security - CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Spring Security - Remember Me](https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html)
- [Thymeleaf + Spring Security](https://www.thymeleaf.org/doc/articles/springsecurity.html)

### Recommended Articles
- [Understanding BCrypt](https://auth0.com/blog/hashing-in-action-understanding-bcrypt/)
- [CSRF Attacks Explained](https://owasp.org/www-community/attacks/csrf)
- [Session vs Token Authentication](https://ponyfoo.com/articles/json-web-tokens-vs-session-cookies)
- [Spring Security Architecture Deep Dive](https://spring.io/guides/topicals/spring-security-architecture)

### Videos
- Spring Security Form Login Tutorial (YouTube)
- Understanding Sessions and Cookies
- CSRF Protection Explained

---

## 🎓 Practice Exercises

1. **Add a "Forgot Password" link** to the login page
2. **Implement account lockout** after 3 failed login attempts
3. **Add custom login success handler** to redirect based on role
4. **Create a user registration page** (hint: won't work with InMemoryUserDetailsManager)
5. **Add a "Settings" page** where users can change preferences
6. **Implement concurrent session control** with session registry
7. **Add logging** to track all login attempts
8. **Create a "User Profile" page** showing current user details

---

## 📊 Comparison: Module 1 vs Module 2

| Feature | Module 1 | Module 2 |
|---------|----------|----------|
| **Authentication Type** | HTTP Basic | Form Login |
| **Login UI** | Browser dialog | Custom HTML page |
| **User Storage** | Generated user | In-memory (2 users) |
| **Roles** | None | USER, ADMIN |
| **CSRF** | Enabled | Enabled |
| **Sessions** | Used | Used |
| **Remember-Me** | No | Yes |
| **Custom Pages** | No | Yes (Thymeleaf) |
| **Access Denied** | Default | Custom page |
| **Logout** | Default | Custom handling |

---

## 🎯 What's Next?

**Module 3 - REST + Basic Auth** will introduce:
- Stateless authentication
- No sessions (SessionCreationPolicy.STATELESS)
- REST-friendly error responses (401, 403)
- Proper API security
- No CSRF (not needed for stateless)

**Key Change:** From stateful (sessions) to stateless (every request authenticated)

---

## 🔒 Security Configuration

**DO NOT use the example credentials in production!**

1. Copy `application-dev.properties.example` to `application-dev.properties`
2. Update all credentials with secure values
3. Never commit `application.properties` to Git
4. Use environment variables for production deployment

---

**Happy Learning! 🚀**

*Traditional web security is still widely used and important to understand!*
