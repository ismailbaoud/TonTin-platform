# Swagger Quick Start Guide

## 🚀 Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui/index.html
```

## 📚 Alternative URLs

- **Swagger UI (redirect):** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON spec:** http://localhost:8080/v3/api-docs
- **OpenAPI YAML spec:** http://localhost:8080/v3/api-docs.yaml

## 🔐 How to Test Authenticated Endpoints

### Step 1: Register a User
1. In Swagger UI, find **"Authentication"** section
2. Click on `POST /api/v1/auth/register`
3. Click **"Try it out"**
4. Fill in the request body:
   ```json
   {
     "firstname": "John",
     "lastname": "Doe",
     "email": "john.doe@example.com",
     "password": "StrongPass123!"
   }
   ```
5. Click **"Execute"**

### Step 2: Verify Email (if required)
1. Check your email or application logs for verification code
2. Use `POST /api/v1/auth/verify` with the code

### Step 3: Login
1. Click on `POST /api/v1/auth/login`
2. Click **"Try it out"**
3. Fill in credentials:
   ```json
   {
     "email": "john.doe@example.com",
     "password": "StrongPass123!"
   }
   ```
4. Click **"Execute"**
5. **Copy** the `accessToken` from the response

### Step 4: Authorize in Swagger
1. Click the **"Authorize"** button (🔒 lock icon) at the top right
2. Paste your JWT token in the **"Value"** field
3. Click **"Authorize"**
4. Click **"Close"**

### Step 5: Test Protected Endpoints
Now you can test any endpoint marked with 🔒:
- All `/api/v1/dart/**` endpoints
- All `/api/v1/member/**` endpoints
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`

## 📋 Available Endpoints

### Public Endpoints (No Auth Required)
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login and get JWT token
- `POST /api/v1/auth/verify` - Verify email
- `POST /api/v1/auth/refresh-token` - Refresh expired token

### Protected Endpoints (Auth Required)
#### Authentication
- `GET /api/v1/auth/me` - Get current user profile
- `POST /api/v1/auth/logout` - Logout user

#### Dart Management
- `POST /api/v1/dart` - Create new dart
- `GET /api/v1/dart/{id}` - Get dart details
- `PUT /api/v1/dart/{id}` - Update dart
- `DELETE /api/v1/dart/{id}` - Delete dart

#### Member Management
- `GET /api/v1/member/dart/{dartId}` - List all members in a dart
- `POST /api/v1/member/dart/{dartId}/user/{userId}` - Add member to dart
- `PUT /api/v1/member/{memberId}` - Update member details
- `DELETE /api/v1/member/{memberId}/dart/{dartId}` - Remove member

## 🛠️ Quick Commands

### Check if Swagger is running:
```bash
curl -s http://localhost:8080/v3/api-docs | jq '.info.title'
```

### Get all endpoint paths:
```bash
curl -s http://localhost:8080/v3/api-docs | jq '.paths | keys'
```

### Check Swagger UI status:
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui/index.html
```

### Start the application:
```bash
mvn spring-boot:run
```

### Start in background:
```bash
nohup mvn spring-boot:run > /tmp/tontin.log 2>&1 &
```

### Check application logs:
```bash
tail -f /tmp/tontin.log
```

### Stop background process:
```bash
# Find process ID
lsof -ti :8080

# Kill the process
kill $(lsof -ti :8080)
```

## 💡 Tips

1. **Token Expiration:** If you get 401 errors, your token may have expired. Login again to get a new token.

2. **Clear Authorization:** Click the "Authorize" button and then "Logout" to clear your token.

3. **Request Examples:** Each endpoint shows example request/response bodies.

4. **Response Codes:** Check the "Responses" section for each endpoint to see all possible HTTP status codes.

5. **Validation Errors:** If you get 400 Bad Request, check the error message for validation details.

6. **Security:** Endpoints marked with 🔒 require authentication.

## 🔍 Swagger UI Features

- **Try it out:** Test endpoints directly from the browser
- **Schema Documentation:** View all request/response models
- **Authentication:** JWT Bearer token support
- **Response Preview:** See actual API responses
- **Export:** Download OpenAPI specification in JSON/YAML
- **Filtering:** Search endpoints by path or tag
- **Sorting:** Endpoints sorted by HTTP method

## 📞 Support

For issues or questions:
- Check the main documentation in `SWAGGER_FIX_SUMMARY.md`
- Review application logs for errors
- Contact: support@tontin.com

---

**Last Updated:** 2026-02-02
**API Version:** 1.0.0
**Spring Boot:** 4.0.0
**SpringDoc OpenAPI:** 2.7.0