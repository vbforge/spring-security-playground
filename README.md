# 🔐 Spring Security Playground – Complete Learning Path

> **Master Spring Security through 5 progressive projects with identical domain models but different security approaches**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-blue)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🎯 What is This?

This repository is a **hands-on Spring Security learning journey** designed to take you from basics to enterprise-level security.

Each module implements the **same Product-Tag REST API** but with **different security configurations**, allowing you to:
- ✅ Compare security approaches side-by-side
- ✅ Understand the evolution from stateful to stateless
- ✅ Build production-ready security knowledge
- ✅ See real-world testing strategies

---

## 🗺️ Projects Navigation

| # | Module | Security Approach | Auth Type | Session | Best For |
|---|--------|------------------|-----------|---------|----------|
| **1** | [default-security](module-1-default-security/README.md) | Auto-configuration | HTTP Basic | Stateful | Understanding Spring Security defaults |
| **2** | [web-security](module-2-web-security/README.md) | Form Login | Session-based | Stateful | Traditional MVC applications |
| **3** | [rest-basic](module-3-rest-basic/README.md) | HTTP Basic | Per-request | Stateless | Simple REST APIs |
| **4** | [rest-jwt](module-4-rest-jwt/README.md) | JWT Tokens | Token-based | Stateless | Modern REST APIs (Industry Standard) |
| **5** | [oauth2-resource-server](module-5-oauth2/README.md) | OAuth2 + Keycloak | External IdP | Stateless | Enterprise microservices |

---

## 🎓 Learning Path

**Choose your starting point:**

- 🟢 **New to Spring Security?** → Start with Module 1
- 🟡 **Know the basics?** → Jump to Module 3
- 🔵 **Want production skills?** → Focus on Modules 4-5
- 🟣 **Complete mastery?** → Go through all 5 in order

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.5.10 |
| **Security** | Spring Security 6.x |
| **Build Tool** | Maven 3.8+ |
| **Database (Dev)** | MySQL 8.x |
| **Database (Test)** | H2 (in-memory) |
| **Testing** | JUnit 5 + Mockito + MockMvc |
| **Utilities** | Lombok, MapStruct |
| **OAuth2 (Module 5)** | Keycloak (Docker) |

---

## 📋 Prerequisites

Before starting, ensure you have:

- ✅ **JDK 17+** installed ([Download](https://adoptium.net/))
- ✅ **Maven 3.8+** installed
- ✅ **MySQL 8+** running locally
  - Host: `localhost:3306`
  - Database: `security_playground_db`
  - User: `dev_user` / Password: `dev_password`
- ✅ **Postman** or **curl** for API testing
- ✅ **Docker** (for Module 5 - Keycloak)
- ✅ IDE (IntelliJ IDEA, VS Code, Eclipse)

### 🐬 MySQL Quick Setup

```bash
# Create database
mysql -u root -p
CREATE DATABASE security_playground_db;
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON security_playground_db.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
exit;
```

---

## 🏗️ Project Structure

```
spring-security-playground/
│
├── README.md                          # ← You are here
├── SETUP.md                           # Project setup instructions
├── .env                               # NOT tracked by Git
├── .gitignore                         # Defines files excluded from version control
├── pom.xml                            # Parent POM
│
├── module-1-default-security/         # Project 1: Default Security
│   ├── README.md                      # Theory + How-to
│   ├── pom.xml
│   └── src/
│       ├── main/
│       └── test/
│
├── module-2-web-security/             # Project 2: Session-based
│   ├── README.md
│   └── ...
│
├── module-3-rest-basic/               # Project 3: Stateless Basic Auth
│   ├── README.md
│   └── ...
│
├── module-4-rest-jwt/                 # Project 4: JWT Authentication
│   ├── README.md
│   └── ...
│
└── module-5-oauth2/                   # Project 5: OAuth2 Resource Server
    ├── README.md
    ├── docker-compose.yml             # Keycloak setup
    └── ...
```

---

## 🔄 Comparison Matrix

| Feature | Module 1 | Module 2 | Module 3 | Module 4 | Module 5 |
|---------|----------|----------|----------|----------|----------|
| **Stateless** | ❌ | ❌ | ✅ | ✅ | ✅ |
| **CSRF Protection** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Session Used** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Login Endpoint** | Auto | `/login` | Headers | `/auth/login` | External (Keycloak) |
| **Password Storage** | In-memory | In-memory | Database | Database | External IdP |
| **Token Type** | JSESSIONID | JSESSIONID | None | JWT | JWT (Keycloak) |
| **Best For** | Learning | MVC Apps | Simple APIs | Modern APIs | Enterprise |

---

## 📦 Common Domain Model (All Modules)

We use a **simple but powerful** domain to focus on security, not business logic:

### 🛍️ Product
```java
- id: Long
- name: String
- description: String
- price: BigDecimal
- createdAt: LocalDateTime
```

### 🏷️ Tag
```java
- id: Long
- name: String
```

### 🔗 Relationship
```
Product ←→ Tag
   (Many-to-Many)
```

This allows exploration of:
- Role-based access (ADMIN vs USER)
- CRUD operation restrictions
- Method-level security
- Ownership rules (in JWT module)

---

## 🏛️ Common Architecture (All Modules)

Every module follows **clean 3-layer architecture**:

```
com.vbforge.security.<module>
│
├── controller/           # REST endpoints
├── service/              # Business logic
│   └── impl/
├── repository/           # Data access
├── dto/                  # Data Transfer Objects
├── mapper/               # DTO ↔ Entity mapping
├── entity/               # JPA entities
├── exception/            # Custom exceptions
│   └── GlobalExceptionHandler
├── config/               # Spring configuration
└── security/             # Security configuration (varies by module)
```

**Why this matters:**
- Real-world project structure
- Separation of concerns
- Easy to test each layer
- Production-ready patterns

---

## 🗄️ Database Strategy

| Environment | Database | Configuration File |
|-------------|----------|-------------------|
| **Development** | MySQL 8.x | `application.properties` |
| **Testing** | H2 (in-memory) | `application-test.properties` |

**In tests:**
```java
@SpringBootTest
@ActiveProfiles("test")  // ← Switches to H2
class ProductServiceTest { ... }
```

This mimics real production environments where dev and test databases differ.

---

## 🧪 Testing Strategy (All Modules)

Each module includes comprehensive tests:

### ✅ Repository Layer
```java
@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest { ... }
```

### ✅ Service Layer
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest { ... }
```

### ✅ Controller Layer
```java
@WebMvcTest(ProductController.class)
class ProductControllerTest { ... }
```

### ✅ Security Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    // Test role restrictions, 401 vs 403, token validation, etc.
}
```

**Coverage includes:**
- ✅ Happy paths
- ✅ Negative scenarios (wrong credentials, expired tokens)
- ✅ Role-based access restrictions
- ✅ 401 vs 403 behavior
- ✅ CSRF testing (web module)
- ✅ Token validation (JWT module)

---

## 🚀 Quick Start

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/yourusername/spring-security-playground.git
cd spring-security-playground
```

### 2️⃣ Build All Modules
```bash
mvn clean install
```

### 3️⃣ Run a Specific Module
```bash
cd module-1-default-security
mvn spring-boot:run
```

### 4️⃣ Test Endpoints
```bash
# Example: Get all products (authentication required)
curl -u user:password http://localhost:8080/api/products
```

---

## 📚 What You Will Master

After completing all 5 modules, you will deeply understand:

| Concept | Description |
|---------|-------------|
| **Spring Security Filter Chain** | How requests flow through security filters |
| **Authentication vs Authorization** | The crucial difference and when to use each |
| **Stateful vs Stateless** | Sessions vs tokens, pros/cons |
| **CSRF Protection** | Why it matters, when to enable/disable |
| **Password Encoding** | BCrypt, security best practices |
| **JWT Internals** | Token structure, claims, validation |
| **OAuth2 Resource Server** | Enterprise authentication patterns |
| **Security Testing** | How to verify security configurations |
| **Role-Based Access Control** | Method and endpoint security |
| **Custom Security Filters** | Extending Spring Security |

**This knowledge puts you beyond junior level.**

---

## 📖 Module-Specific Documentation

Each module contains its own README with:
- 🎓 **Theory**: Concepts explained clearly
- 🔑 **Key Features**: What makes this approach unique
- 🏗️ **Architecture**: Security configuration details
- 🚀 **How to Run**: Step-by-step instructions
- 🧪 **Testing Guide**: How to verify behavior
- 📸 **API Examples**: Postman/cURL commands
- 🤔 **Common Pitfalls**: What learners often get wrong
- 📚 **Further Reading**: Links to official documentation

---

## 🐛 Common Troubleshooting

### Problem: "Cannot connect to MySQL"
```bash
# Check if MySQL is running
sudo systemctl status mysql

# Verify database exists
mysql -u dev_user -p
SHOW DATABASES;
```

### Problem: "Port 8080 already in use"
```bash
# Change port in application.properties
server.port=8081
```

### Problem: "Tests fail with H2 errors"
```bash
# Ensure @ActiveProfiles("test") is present
# Check application-test.properties exists
```

### Problem: "401 Unauthorized" in tests
```bash
# For security tests, use @WithMockUser or provide credentials
@Test
@WithMockUser(roles = "ADMIN")
void testAdminEndpoint() { ... }
```

---

## ⚠️ Security Notice

**This is a learning project.** Before deploying to production:

1.  **Never use default credentials** (user/password, admin/admin)
2.  **Generate strong secrets** for JWT
3.  **Use environment variables** for all secrets
4.  **Enable HTTPS** in production
5.  **Update Keycloak admin password** immediately
6.  **Use proper database** (not H2) in production
7.  **Review all security configurations**

---