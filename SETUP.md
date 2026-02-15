# 🚀 Setup Guide

## Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL 8+
- Docker & Docker Compose (for Module 5)

---

## 1. Clone Repository
```bash
git clone https://github.com/YOUR_USERNAME/spring-security-playground.git
cd spring-security-playground
```

---

## 2. Setup Database
```bash
mysql -u root -p

CREATE DATABASE security_playground_db;
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON security_playground_db.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

## 3. Configure Environment

### For Modules 1-4:
Copy example files and update with your values:
```bash
# Module 4 example
cd module-4-rest-jwt/src/main/resources
cp application-dev.properties.example application-dev.properties
```

Edit `application-dev.properties` and update:
- `spring.datasource.username`
- `spring.datasource.password`
- `jwt.secret` (generate your own!)

### For Module 5:
```bash
cd module-5-oauth2
cp .env.example .env
```

Edit `.env` and update:
- `KEYCLOAK_ADMIN_PASSWORD`

---

## 4. Generate JWT Secret

For Module 4, generate a secure secret:
```bash
# Generate random 256-bit secret (base64 encoded)
openssl rand -base64 32

# Use this value for jwt.secret in application.properties
```

---

## 5. Run Modules

Each module can run independently:
```bash
cd module-4-rest-jwt
mvn spring-boot:run
```

---

## ⚠️ Security Notes

**NEVER commit:**
- `application-dev.properties` with real credentials
- `.env` files
- Any file containing passwords or secrets

**ALWAYS use:**
- `.env.example` and `application-dev.properties.example` as templates
- Environment variables in production
- Strong, unique passwords

---