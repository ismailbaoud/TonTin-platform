# Auth Feature Module

## 📋 Overview

This is the Authentication feature module for TonTin Platform. It handles user registration, login, and authentication flows.

**Current Status**: ✅ Hardcoded (Development Mode)  
**API Ready**: ✅ Yes - Easy to switch to backend integration

---

## 🏗️ Architecture

```
features/auth/
├── pages/
│   ├── register/              # Registration page
│   │   ├── register.component.ts
│   │   ├── register.component.html
│   │   └── register.component.scss
│   └── login/                 # Login page (placeholder)
│       └── login.component.ts
├── services/
│   └── auth.service.ts        # Authentication service (HARDCODED)
├── auth.routes.ts             # Feature routing
└── README.md                  # This file
```

---

## 🎯 Features

### Current Implementation (Hardcoded)

- ✅ **User Registration**
  - Form validation (email/username, password, confirm password)
  - Password visibility toggle
  - Real-time validation feedback
  - Simulated network delay (1 second)
  - Success/error handling

- ✅ **Mock Authentication Service**
  - In-memory user storage
  - Password validation
  - Duplicate user detection
  - Mock JWT token generation

- ✅ **UI/UX**
  - Responsive design with Tailwind CSS
  - Dark mode support
  - Material Icons integration
  - Loading states
  - Error messages
  - Form accessibility

---

## 🔄 Switching from Hardcoded to API

The authentication service is **structured to make API integration simple**. Here's how:

### Current (Hardcoded) Code

```typescript
register(data: RegisterRequest): Observable<RegisterResponse> {
  // Hardcoded implementation
  return new Observable<RegisterResponse>(observer => {
    setTimeout(() => {
      // Mock logic here
      observer.next(mockResponse);
    }, 1000);
  });
}
```

### Switch to API (3 Steps)

#### Step 1: Inject HttpClient

```typescript
// In auth.service.ts
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

constructor(private http: HttpClient) {}
```

#### Step 2: Replace Method Body

```typescript
register(data: RegisterRequest): Observable<RegisterResponse> {
  // Simply replace with this one line:
  return this.http.post<RegisterResponse>(
    `${environment.apiUrl}/auth/register`,
    data
  );
}
```

#### Step 3: Update Environment Config

```typescript
// environment.development.ts
export const environment = {
  apiUrl: 'http://localhost:9090/api',  // Your Spring Boot backend
  // ... other config
};
```

**That's it!** The component doesn't need to change at all.

---

## 📝 API Endpoints (When Ready)

### Register
```
POST /api/auth/register
Content-Type: application/json

Request Body:
{
  "emailOrUsername": "string",
  "password": "string"
}

Response (Success - 200):
{
  "success": true,
  "message": "Account created successfully",
  "userId": 123,
  "email": "user@example.com"
}

Response (Error - 400):
{
  "success": false,
  "message": "User already exists"
}
```

### Login
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "emailOrUsername": "string",
  "password": "string",
  "rememberMe": boolean
}

Response (Success - 200):
{
  "success": true,
  "token": "jwt-token-here",
  "refreshToken": "refresh-token-here",
  "user": {
    "id": 123,
    "username": "user",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

---

## 🧪 Testing the Current Implementation

### Test Registration

1. Navigate to: `http://localhost:4200/auth/register`
2. Enter any email/username (min 3 characters)
3. Enter password (min 8 characters)
4. Confirm password
5. Click "Create Account"

**What happens:**
- Form validation runs
- Shows loading state for 1 second
- Simulates successful registration
- Navigates to login page
- User is stored in memory (lost on refresh)

### Test Existing Users

Try registering with:
- Username: `admin`
- Password: `password123`

**What happens:**
- Shows error: "User already exists"

---

## 📦 Models/Interfaces

All TypeScript interfaces are defined in `auth.service.ts`:

```typescript
interface RegisterRequest {
  emailOrUsername: string;
  password: string;
}

interface RegisterResponse {
  success: boolean;
  message: string;
  userId?: number;
  email?: string;
}

interface LoginRequest {
  emailOrUsername: string;
  password: string;
  rememberMe?: boolean;
}

interface LoginResponse {
  success: boolean;
  token: string;
  refreshToken?: string;
  user: User;
}

interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  createdAt: string;
}
```

**These match your Spring Boot DTOs** - Just update them if your backend uses different field names.

---

## 🎨 Styling

Uses **Tailwind CSS** with custom theme:

```javascript
colors: {
  primary: "#13ec5b",           // TonTin green
  "background-light": "#f6f8f6",
  "background-dark": "#102216",
}
```

**Features:**
- Responsive design (mobile-first)
- Dark mode support
- Smooth transitions
- Material Icons
- Custom form styling

---

## 🔐 Security Considerations

### Current (Hardcoded)
- No actual security (development only)
- Passwords stored in plain text (in-memory)
- Mock JWT tokens

### When Connecting to API
- ✅ Passwords will be hashed by backend
- ✅ Real JWT tokens from backend
- ✅ HTTPS in production
- ✅ Token storage in localStorage
- ✅ HTTP interceptors for auth headers
- ✅ CSRF protection
- ✅ XSS protection

---

## 🚀 Next Steps

### To Complete Auth Feature

1. **Create Login Page**
   - Copy register structure
   - Update form fields
   - Connect to auth service

2. **Add Auth Guard**
   - Protect routes requiring authentication
   - Redirect to login if not authenticated

3. **Add HTTP Interceptor**
   - Automatically add JWT token to requests
   - Handle 401/403 errors

4. **Add Token Storage**
   - Store JWT in localStorage
   - Implement token refresh
   - Handle token expiration

5. **Connect to Backend API**
   - Follow "Switching to API" guide above
   - Test with real Spring Boot backend
   - Handle API errors properly

---

## 📚 Related Files

- **Environment Config**: `src/environments/environment.development.ts`
- **App Routes**: `src/app/app.routes.ts`
- **Tailwind Config**: `src/index.html` (inline config)
- **Global Styles**: `src/styles.scss`

---

## 💡 Tips

### Development
```bash
# Start dev server
npm start

# Access register page
http://localhost:4200/auth/register

# Access login page
http://localhost:4200/auth/login
```

### Debugging
```typescript
// Enable debug logs in auth.service.ts
console.log('📝 Register attempt:', data);
console.log('✅ Registration successful:', response);
```

### Form Validation
- Email/Username: min 3 characters
- Password: min 8 characters
- Confirm Password: must match password

---

## 🤝 Contributing

When adding new auth features:

1. Keep the **hardcoded/API switch pattern**
2. Add **loading states** for better UX
3. Include **error handling**
4. Add **form validation**
5. Update this **README**

---

## ✅ Checklist for API Integration

- [ ] Backend API is running (`http://localhost:9090`)
- [ ] API endpoints match the ones documented above
- [ ] Environment file has correct `apiUrl`
- [ ] HttpClient is injected in auth.service.ts
- [ ] Register method uses `http.post()`
- [ ] Login method uses `http.post()`
- [ ] Error responses match expected format
- [ ] CORS is configured on backend
- [ ] Test registration with real API
- [ ] Test login with real API
- [ ] Handle API errors properly

---

**Status**: Ready for development ✅  
**Last Updated**: February 2025  
**Version**: 1.0.0