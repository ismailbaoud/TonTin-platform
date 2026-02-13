# API Integration Documentation

## Overview

The TonTin Platform frontend is now fully integrated with the backend API. This document describes the authentication system, API endpoints, and how to use them.

---

## 🔄 What Changed

### Before (Mock Implementation)
- Hardcoded user database in memory
- Simulated network delays
- No real authentication
- Data lost on page refresh

### After (Real API Implementation)
- Full backend API integration
- JWT token-based authentication
- Persistent user sessions
- Real user registration and login
- Token refresh mechanism
- Secure HTTP interceptor

---

## 🏗️ Architecture

### Authentication Flow

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│   Angular   │  HTTP   │ Auth         │  JWT    │   Backend    │
│  Frontend   │────────>│ Interceptor  │────────>│     API      │
└─────────────┘         └──────────────┘         └──────────────┘
       │                        │                         │
       │                        │                         │
       │  1. Login/Register     │                         │
       │───────────────────────>│                         │
       │                        │  2. Add Headers         │
       │                        │────────────────────────>│
       │                        │                         │
       │                        │  3. Return Tokens       │
       │                        │<────────────────────────│
       │  4. Store Tokens       │                         │
       │<───────────────────────│                         │
       │                        │                         │
       │  5. Subsequent Requests│                         │
       │───────────────────────>│  6. Add JWT Token       │
       │                        │────────────────────────>│
       │                        │  7. Authenticated       │
       │                        │     Response            │
       │  8. Return Data        │<────────────────────────│
       │<───────────────────────│                         │
```

---

## 📡 API Endpoints

### Base URL
- **Development:** `http://localhost:9090/api`
- **Production:** `https://api.tontin.example.com/api`

### Authentication Endpoints

#### 1. Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "userName": "john_doe",
  "email": "john.doe@example.com",
  "password": "Password123@"
}
```

**Response (201 Created):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userName": "john_doe",
  "email": "john.doe@example.com",
  "creationDate": "2024-02-06",
  "emailConfirmed": false,
  "accountAccessFileCount": 0,
  "resetPasswordDate": null,
  "role": "ROLE_CLIENT",
  "picture": null,
  "status": "PENDING",
  "createdAt": "2024-02-06T12:00:00",
  "updatedAt": "2024-02-06T12:00:00"
}
```

**Password Requirements:**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@$!%*?&)

---

#### 2. Login User
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "Password123@"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "userName": "john_doe",
    "email": "john.doe@example.com",
    "role": "ROLE_CLIENT",
    "status": "ACTIVE",
    "emailConfirmed": true,
    "createdAt": "2024-02-06T12:00:00",
    "updatedAt": "2024-02-06T12:00:00"
  }
}
```

---

#### 3. Logout User
```http
POST /api/v1/auth/logout
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "message": "Logout successful. Please discard your tokens."
}
```

---

#### 4. Get Current User
```http
GET /api/v1/auth/me
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "userName": "john_doe",
  "email": "john.doe@example.com",
  "role": "ROLE_CLIENT",
  "status": "ACTIVE",
  "emailConfirmed": true,
  "createdAt": "2024-02-06T12:00:00",
  "updatedAt": "2024-02-06T12:00:00"
}
```

---

#### 5. Refresh Token
```http
POST /api/v1/auth/refresh-token
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

#### 6. Verify Email
```http
GET /api/v1/auth/verify?code={verificationCode}
```

**Response (200 OK):**
```json
{
  "message": "Email verified successfully"
}
```

---

## 🔐 Authentication Service

### Location
`src/app/features/auth/services/auth.service.ts`

### Key Features

#### Token Management
```typescript
// Check if user is authenticated
const isAuth = authService.isAuthenticated(); // Returns boolean

// Get stored token
const token = authService.getToken(); // Returns JWT token or null

// Get stored user
const user = authService.getStoredUser(); // Returns UserResponse or null
```

#### Login
```typescript
authService.login({ emailOrUsername: 'user@example.com', password: 'Password123@' })
  .subscribe({
    next: (response) => {
      console.log('Logged in:', response.user);
      // Token automatically stored in localStorage
      // User data stored in localStorage
      // Navigate to dashboard
    },
    error: (error) => {
      console.error('Login failed:', error.message);
    }
  });
```

#### Register
```typescript
authService.register({ emailOrUsername: 'john_doe', password: 'Password123@' })
  .subscribe({
    next: (response) => {
      console.log('Registered:', response.userName);
      // Navigate to login
    },
    error: (error) => {
      console.error('Registration failed:', error.message);
    }
  });
```

#### Logout
```typescript
authService.logout().subscribe({
  next: () => {
    console.log('Logged out');
    // Tokens cleared from localStorage
    // Navigate to login
  }
});
```

---

## 🔧 HTTP Interceptor

### Location
`src/app/core/interceptors/auth.interceptor.ts`

### Features

1. **Automatic Token Injection**
   - Adds JWT token to all API requests (except auth endpoints)
   - Adds `Authorization: Bearer {token}` header

2. **Content-Type Header**
   - Automatically adds `Content-Type: application/json` to all requests

3. **Error Handling**
   - Catches 401 errors (unauthorized)
   - Clears tokens on authentication failure

4. **Excluded Endpoints**
   - `/auth/login` - No token needed
   - `/auth/register` - No token needed
   - `/auth/refresh-token` - Uses refresh token instead
   - `/auth/verify` - Uses verification code

### Configuration
Already configured in `app.config.ts`:
```typescript
provideHttpClient(withInterceptors([authInterceptor]))
```

---

## 💾 Local Storage

### Stored Data

| Key | Description | Example Value |
|-----|-------------|---------------|
| `tontin_token` | JWT access token | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |
| `tontin_refresh_token` | JWT refresh token | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |
| `tontin_token_expiry` | Token expiration date | `2024-02-06T13:00:00.000Z` |
| `tontin_user` | User data (JSON) | `{"id":"123","userName":"john_doe",...}` |

### Token Lifecycle

```
1. User logs in
   └─> Store token, refreshToken, expiry, user data

2. Every API request (except auth)
   └─> Interceptor adds token to Authorization header

3. Token expires (401 error)
   └─> Clear tokens
   └─> Redirect to login

4. User logs out
   └─> Clear all stored data
   └─> Redirect to login
```

---

## 🚨 Error Handling

### Error Response Format

All errors follow this structure:
```typescript
{
  message: string // User-friendly error message
}
```

### HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | Success | Login successful |
| 201 | Created | User registered |
| 400 | Bad Request | Invalid input, validation error |
| 401 | Unauthorized | Invalid credentials |
| 403 | Forbidden | Account not active |
| 404 | Not Found | User not found |
| 409 | Conflict | Email already exists |
| 500 | Server Error | Internal server error |
| 0 | Network Error | Cannot connect to backend |

### Error Messages

The `AuthService` provides user-friendly error messages:

```typescript
// Example error handling
authService.login(credentials).subscribe({
  error: (error) => {
    // error.message contains user-friendly message
    switch(error.message) {
      case 'Invalid credentials. Please try again.':
        // Show error toast
        break;
      case 'Cannot connect to server. Please check if the backend is running.':
        // Show connection error
        break;
      // etc.
    }
  }
});
```

---

## 🧪 Testing

### Prerequisites
1. Backend must be running on `http://localhost:9090`
2. Database must be accessible
3. Frontend running on `http://localhost:4200`

### Test Registration

1. Navigate to `http://localhost:4200/auth/register`
2. Enter:
   - Username: `testuser`
   - Password: `Test123@`
3. Click "Create Account"
4. Check browser console for API logs
5. Check browser DevTools → Application → Local Storage
6. Verify tokens are stored

### Test Login

1. Navigate to `http://localhost:4200/auth/login`
2. Enter:
   - Email: `testuser@tontin.com` (or registered email)
   - Password: `Test123@`
3. Click "Log In"
4. Check console for success logs
5. Verify tokens in localStorage
6. Should redirect to dashboard (when implemented)

### Test API Calls

Open browser DevTools → Network tab:
- Filter by "XHR" or "Fetch"
- Watch for requests to `localhost:9090/api/v1/auth/...`
- Check request headers for `Authorization: Bearer {token}`
- Verify response codes and data

---

## 🔍 Debugging

### Enable Debug Logging

The service automatically logs all operations:
```
📝 Registering user: john_doe
✅ Registration successful: john_doe

🔐 Logging in user: john.doe@example.com
✅ Login successful: john_doe

👋 Logging out user
✅ Logout successful

👤 Fetching current user
✅ Current user retrieved: john_doe
```

### Check Token in Browser

```javascript
// Open browser console
localStorage.getItem('tontin_token');
localStorage.getItem('tontin_user');
```

### Decode JWT Token

Use https://jwt.io to decode the token and see:
- User ID
- Email
- Expiration time
- Issued at time

### Check HTTP Interceptor

In Network tab:
- Click on any API request
- Go to "Headers" tab
- Verify `Authorization: Bearer {token}` is present
- Verify `Content-Type: application/json` is present

---

## 🐛 Common Issues

### Issue: Cannot connect to server (Status 0)

**Cause:** Backend is not running or CORS is blocking requests

**Solutions:**
1. Check backend is running: `curl http://localhost:9090/actuator/health`
2. Start backend if needed: `cd platform-back && ./mvnw spring-boot:run`
3. Verify CORS configuration in backend allows `http://localhost:4200`

---

### Issue: 401 Unauthorized on login

**Cause:** Invalid credentials or user doesn't exist

**Solutions:**
1. Register a new user first
2. Verify email format: must be valid email
3. Verify password meets requirements (8+ chars, uppercase, lowercase, digit, special char)
4. Check backend logs for detailed error

---

### Issue: 409 Conflict on registration

**Cause:** Email or username already exists

**Solutions:**
1. Use a different email/username
2. Or login with existing credentials

---

### Issue: 403 Forbidden after login

**Cause:** Account is not active (pending email verification)

**Solutions:**
1. Check email for verification link
2. Or manually verify in database
3. Or update account status to ACTIVE

---

### Issue: Token not being sent with requests

**Cause:** Interceptor not configured or token not stored

**Solutions:**
1. Verify interceptor is registered in `app.config.ts`
2. Check localStorage for `tontin_token`
3. Check Network tab for `Authorization` header
4. Make sure you're logged in

---

## 📝 Frontend-Backend Data Mapping

### Registration

**Frontend Input:**
```typescript
{
  emailOrUsername: string  // Can be email or username
  password: string
}
```

**Backend Format:**
```typescript
{
  userName: string        // Extracted from emailOrUsername
  email: string          // If '@' present, use as is; else append @tontin.com
  password: string
}
```

**Transformation:**
```typescript
// If input is "john_doe"
userName: "john_doe"
email: "john_doe@tontin.com"

// If input is "john@example.com"
userName: "john@example.com"
email: "john@example.com"
```

---

### Login

**Frontend Input:**
```typescript
{
  emailOrUsername: string
  password: string
}
```

**Backend Format:**
```typescript
{
  email: string          // Must be valid email
  password: string
}
```

**Transformation:**
```typescript
// If input is "john_doe"
email: "john_doe@tontin.com"

// If input is "john@example.com"
email: "john@example.com"
```

---

## 🔐 Security Best Practices

### ✅ Implemented

- JWT token-based authentication
- Tokens stored in localStorage (consider httpOnly cookies for production)
- HTTPS for production (configured in environment)
- Password validation on backend
- Token expiration (15 minutes for access token)
- Refresh token mechanism (7 days)
- Authorization header on all authenticated requests
- CORS configuration on backend
- Input validation and sanitization

### 🎯 TODO for Production

- [ ] Implement token refresh logic in interceptor
- [ ] Move tokens to httpOnly cookies (more secure than localStorage)
- [ ] Add CSRF protection
- [ ] Implement rate limiting on auth endpoints
- [ ] Add brute force protection
- [ ] Implement 2FA (Two-Factor Authentication)
- [ ] Add password strength indicator
- [ ] Implement "Remember Me" functionality
- [ ] Add session timeout warning
- [ ] Implement secure password reset flow
- [ ] Add email verification requirement
- [ ] Log security events

---

## 📚 Related Files

### Core Files
- `src/app/features/auth/services/auth.service.ts` - Authentication service
- `src/app/core/interceptors/auth.interceptor.ts` - HTTP interceptor
- `src/app/app.config.ts` - App configuration with interceptor
- `src/environments/environment.development.ts` - Dev environment config
- `src/environments/environment.ts` - Prod environment config

### Component Files
- `src/app/features/auth/pages/login/` - Login page
- `src/app/features/auth/pages/register/` - Register page

### Documentation
- `AUTH_FEATURES.md` - Frontend auth features
- `API_INTEGRATION.md` - This file
- `DOCKER_FIXED.md` - Docker setup

---

## 🚀 Next Steps

1. **Implement Guards**
   - Create `AuthGuard` to protect routes
   - Create `GuestGuard` to redirect authenticated users away from login/register

2. **Add State Management**
   - Implement NgRx or signals for auth state
   - Store user data in global state

3. **Implement Token Refresh**
   - Update interceptor to automatically refresh expired tokens
   - Handle refresh token expiration

4. **Add User Profile**
   - Create profile page
   - Implement profile update
   - Add password change functionality

5. **Implement Email Verification**
   - Add email verification flow
   - Create verification page
   - Handle verification link clicks

6. **Add Password Reset**
   - Implement forgot password flow
   - Create reset password page
   - Add email notification

---

**Last Updated:** February 6, 2024  
**Status:** ✅ Fully Integrated with Backend API  
**Backend API:** http://localhost:9090/api  
**Frontend:** http://localhost:4200