# 🔐 Keycloak Setup Guide

This guide walks you through setting up Keycloak as an OAuth2 Authorization Server for Module 5.

---

## 📋 Prerequisites

- Docker installed and running
- Docker Compose installed
- Module 5 application ready

---

## 🚀 Step 1: Start Keycloak

```bash
# Navigate to module 5 directory
cd module-5-oauth2

# Start Keycloak with Docker Compose
docker-compose up -d

# Check if Keycloak is running
docker-compose ps

# Wait for Keycloak to start (takes 30-60 seconds)
# Check logs
docker-compose logs -f keycloak
```

**Keycloak will be available at:** http://localhost:8080

---

## 🔧 Step 2: Access Keycloak Admin Console

1. **Open browser:** http://localhost:8080
2. **Click:** "Administration Console"
3. **Login:**
   - Username: `admin`
   - Password: `admin`

---

## 🏰 Step 3: Create Realm

1. **Click** the dropdown in top-left (currently says "master")
2. **Click** "Create Realm"
3. **Enter:**
   - Realm name: `spring-security-playground`
4. **Click** "Create"

✅ You should now be in the `spring-security-playground` realm.

---

## 👥 Step 4: Create Roles

1. **Navigate to:** Realm roles (left sidebar)
2. **Click** "Create role"
3. **Create USER role:**
   - Role name: `user`
   - Description: `Regular user role`
   - Click "Save"
4. **Create ADMIN role:**
   - Click "Create role" again
   - Role name: `admin`
   - Description: `Administrator role`
   - Click "Save"

✅ You should now have 2 realm roles: `user` and `admin`

---

## 🔑 Step 5: Create Client

1. **Navigate to:** Clients (left sidebar)
2. **Click** "Create client"
3. **General Settings:**
   - Client type: `OpenID Connect`
   - Client ID: `spring-security-playground-client`
   - Name: `Spring Security Playground`
   - Click "Next"
4. **Capability config:**
   - Client authentication: `ON`
   - Authorization: `OFF`
   - Authentication flow:
     - ✅ Standard flow
     - ✅ Direct access grants
     - ✅ Service accounts roles
   - Click "Next"
5. **Login settings:**
   - Valid redirect URIs: `http://localhost:8085/*`
   - Valid post logout redirect URIs: `http://localhost:8085/*`
   - Web origins: `http://localhost:8085`
   - Click "Save"

✅ Client created successfully!

---

## 🔐 Step 6: Get Client Secret

1. **Navigate to:** Clients → `spring-security-playground-client`
2. **Click** "Credentials" tab
3. **Copy** the "Client secret" value

**Save this secret!** You'll need it for testing.

Example: `xYz123aBc456DeF789...`

---

## 👤 Step 7: Create Users

### Create Regular User

1. **Navigate to:** Users (left sidebar)
2. **Click** "Add user"
3. **Enter:**
   - Username: `user`
   - Email: `user@example.com`
   - First name: `Regular`
   - Last name: `User`
   - Email verified: `ON`
   - Click "Create"
4. **Set password:**
   - Click "Credentials" tab
   - Click "Set password"
   - Password: `password`
   - Password confirmation: `password`
   - Temporary: `OFF`
   - Click "Save"
   - Confirm "Yes"
5. **Assign role:**
   - Click "Role mapping" tab
   - Click "Assign role"
   - Filter by realm roles
   - Select `user`
   - Click "Assign"

✅ Regular user created!

### Create Admin User

1. **Navigate to:** Users (left sidebar)
2. **Click** "Add user"
3. **Enter:**
   - Username: `admin`
   - Email: `admin@example.com`
   - First name: `Admin`
   - Last name: `User`
   - Email verified: `ON`
   - Click "Create"
4. **Set password:**
   - Click "Credentials" tab
   - Click "Set password"
   - Password: `admin`
   - Password confirmation: `admin`
   - Temporary: `OFF`
   - Click "Save"
   - Confirm "Yes"
5. **Assign roles:**
   - Click "Role mapping" tab
   - Click "Assign role"
   - Filter by realm roles
   - Select `user` AND `admin` (both!)
   - Click "Assign"

✅ Admin user created!

---

## ✅ Step 8: Verify Configuration

**Check Realm Settings:**
- Realm: `spring-security-playground` ✅
- Roles: `user`, `admin` ✅
- Client: `spring-security-playground-client` ✅
- Users: `user` (role: user), `admin` (roles: user, admin) ✅

**Issuer URI (for application.properties):**
```
http://localhost:8080/realms/spring-security-playground
```

---

## 🧪 Step 9: Test Keycloak

### Get Token with cURL

**For regular user:**
```bash
curl -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-security-playground-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=user" \
  -d "password=password"
```

**For admin user:**
```bash
curl -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-security-playground-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=admin"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer"
}
```

**Save the `access_token`!** This is the JWT token you'll use with the Spring application.

---

## 🎯 Step 10: Start Spring Application

```bash
# Make sure Keycloak is running
docker-compose ps

# Start the Spring application
mvn spring-boot:run
```

**Application will start on:** http://localhost:8085

---

## 📸 Step 11: Test Full Flow

### 1. Get Token from Keycloak

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=spring-security-playground-client" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=user" \
  -d "password=password" | jq -r '.access_token')

echo $TOKEN
```

### 2. Use Token with Spring Application

```bash
# Access products endpoint
curl http://localhost:8085/api/products \
  -H "Authorization: Bearer $TOKEN"
```

**Success!** ✅

---

## 🛑 Common Issues

### Issue 1: Keycloak not starting

```bash
# Check logs
docker-compose logs keycloak

# Restart
docker-compose down
docker-compose up -d
```

### Issue 2: "Invalid token" error

**Causes:**
- Token expired (expires in 5 minutes by default)
- Wrong issuer URI in application.properties
- Client secret incorrect

**Solution:** Get a fresh token

### Issue 3: "Access denied" (403)

**Cause:** User doesn't have required role

**Solution:** Check user's role mapping in Keycloak

### Issue 4: Can't connect to Keycloak from Spring app

**Cause:** Issuer URI incorrect

**Check:**
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/spring-security-playground
```

---

## 🔧 Useful Keycloak Endpoints

**Token endpoint:**
```
http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/token
```

**Public keys (for JWT validation):**
```
http://localhost:8080/realms/spring-security-playground/protocol/openid-connect/certs
```

**OpenID configuration:**
```
http://localhost:8080/realms/spring-security-playground/.well-known/openid-configuration
```

**Realm info:**
```
http://localhost:8080/realms/spring-security-playground
```

---

## 🧹 Cleanup

### Stop Keycloak
```bash
docker-compose down
```

### Remove volumes (delete all data)
```bash
docker-compose down -v
```

### Remove images
```bash
docker rmi quay.io/keycloak/keycloak:23.0
```

---

## 📚 Next Steps

1. ✅ Keycloak running
2. ✅ Realm, client, users created
3. ✅ Can get tokens
4. ✅ Spring app validates tokens

**Now you can:**
- Test the REST API with OAuth2 tokens
- Try different users (user vs admin)
- Explore Swagger UI with OAuth2
- Understand enterprise authentication patterns

---

**Congratulations!** 🎉

You've successfully set up Keycloak as an OAuth2 Authorization Server!
