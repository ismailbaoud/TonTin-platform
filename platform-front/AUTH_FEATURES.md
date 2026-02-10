# Authentication Features Documentation

## Overview

This document describes the authentication features implemented in the TonTin platform frontend.

## Features Implemented

### 1. Register Page (`/auth/register`)

**Location:** `src/app/features/auth/pages/register/`

**Features:**
- Full registration form with validation
- Email/Username input field
- Password field with visibility toggle
- Confirm password field with visibility toggle
- Password match validation
- Real-time form validation with error messages
- Responsive design with dark mode support
- Loading states during submission
- Navigation link to login page

**Form Validations:**
- Email/Username: Required, minimum 3 characters
- Password: Required, minimum 8 characters
- Confirm Password: Required, must match password

**User Experience:**
- Visual feedback on form errors
- Password visibility toggle buttons
- Disabled submit button while processing
- Error messages displayed inline below fields
- Success message on registration
- Auto-redirect to login page after successful registration

### 2. Login Page (`/auth/login`)

**Location:** `src/app/features/auth/pages/login/`

**Features:**
- Full login form with validation
- Email/Username input field
- Password field with visibility toggle
- Forgot password link (placeholder)
- Real-time form validation
- Responsive design with dark mode support
- Loading states during submission
- Navigation link to register page

**Form Validations:**
- Email/Username: Required
- Password: Required

**User Experience:**
- Visual feedback on form errors
- Password visibility toggle button
- Disabled submit button while processing
- Error messages displayed inline below fields
- "Forgot Password?" link for password recovery (TODO)
- Success message on login
- Auto-redirect to dashboard after successful login

## Routing Configuration

The application uses Angular's standalone component routing with lazy loading:

```typescript
// Direct component loading (simplified routing)
{
  path: "auth/register",
  loadComponent: () => import("./features/auth/pages/register/register.component")
    .then(m => m.RegisterComponent)
}

{
  path: "auth/login",
  loadComponent: () => import("./features/auth/pages/login/login.component")
    .then(m => m.LoginComponent)
}
```

**Available Routes:**
- `/` → Redirects to `/auth/register`
- `/auth/register` → Registration page
- `/auth/login` → Login page
- Any invalid route → Redirects to `/auth/register`

## Authentication Service

**Location:** `src/app/features/auth/services/auth.service.ts`

The `AuthService` provides all authentication-related functionality.

### Current Implementation (Hardcoded/Mock)

The service currently uses **hardcoded data** for development purposes:

**Mock User Database:**
- Username: `admin`
- Password: `password123`
- User data stored in-memory

**Available Methods:**

1. **`register(data: RegisterRequest): Observable<RegisterResponse>`**
   - Registers a new user
   - Validates user doesn't already exist
   - Validates password length (min 8 characters)
   - Simulates 1-second network delay
   - Returns success response with user ID

2. **`login(data: LoginRequest): Observable<LoginResponse>`**
   - Authenticates user with email/username and password
   - Validates credentials against mock database
   - Generates mock JWT token
   - Simulates 1-second network delay
   - Returns token and user data on success

3. **`logout(): Observable<void>`**
   - Logs out the current user
   - Clears authentication state

4. **`isAuthenticated(): Observable<boolean>`**
   - Checks if user is currently authenticated
   - Currently returns `false` (not implemented)

5. **`getCurrentUser(): Observable<User | null>`**
   - Gets the current authenticated user
   - Currently returns `null` (not implemented)

6. **`refreshToken(refreshToken: string): Observable<LoginResponse>`**
   - Refreshes the authentication token
   - Currently throws "Not implemented" error

### Switching to Real API

The service is structured to easily switch to real API calls. Comments in the code show exactly how to convert each method.

**Steps to Switch to API:**

1. **Inject HttpClient:**
   ```typescript
   constructor(private http: HttpClient) {}
   ```

2. **Update environment configuration:**
   ```typescript
   // src/environments/environment.ts
   export const environment = {
     production: false,
     apiUrl: 'http://localhost:8080/api'
   };
   ```

3. **Replace mock implementations with HTTP calls:**
   ```typescript
   // Example for login:
   login(data: LoginRequest): Observable<LoginResponse> {
     return this.http.post<LoginResponse>(
       `${environment.apiUrl}/auth/login`,
       data
     );
   }
   ```

## Models & Interfaces

**RegisterRequest:**
```typescript
{
  emailOrUsername: string;
  password: string;
}
```

**RegisterResponse:**
```typescript
{
  success: boolean;
  message: string;
  userId?: number;
  email?: string;
}
```

**LoginRequest:**
```typescript
{
  emailOrUsername: string;
  password: string;
  rememberMe?: boolean;
}
```

**LoginResponse:**
```typescript
{
  success: boolean;
  token: string;
  refreshToken?: string;
  user: User;
}
```

**User:**
```typescript
{
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  createdAt: string;
}
```

## Design System

### Colors
- **Primary:** `#13ec5b` (TonTin green)
- **Primary Hover:** `#10c94d`
- **Background Light:** `#f6f8f6`
- **Background Dark:** `#102216`
- **Card Background (Light):** `#ffffff`
- **Card Background (Dark):** `#1a2c20`

### Typography
- **Font Family:** Inter (Google Fonts)
- **Heading Sizes:** 26px (page title), 24px (logo)
- **Body Text:** 16px (forms), 14px (labels), 12px (errors)

### Components
- **Input Fields:** 
  - Height: 48px (h-12)
  - Border radius: 8px (rounded-lg)
  - Border color: `#cfe7d7` (light) / `#2A3C30` (dark)
  - Focus: Primary color ring

- **Buttons:**
  - Height: 48px (h-12)
  - Border radius: 8px (rounded-lg)
  - Primary: Green background with dark text
  - Hover: Slightly darker green

- **Error Messages:**
  - Color: Red (#ef4444)
  - Size: 12px (text-xs)
  - Icon: Material Symbols "error"

## Dark Mode Support

Both pages fully support dark mode:
- Automatically respects system preference
- Manual toggle can be added
- All colors, borders, and backgrounds adapt
- Smooth transitions between themes

## Material Symbols

The app uses Google Material Symbols for icons:
- `visibility` / `visibility_off` - Password toggle
- `error` - Error messages
- `arrow_forward` - Submit buttons
- Custom TonTin logo SVG

## Testing the Features

### Test Register Flow:
1. Navigate to `http://localhost:4200/auth/register`
2. Enter email/username (min 3 chars)
3. Enter password (min 8 chars)
4. Enter matching confirm password
5. Click "Create Account"
6. Should see success message and redirect to login

### Test Login Flow:
1. Navigate to `http://localhost:4200/auth/login`
2. Enter: `admin` / `password123` (or any registered user)
3. Click "Log In"
4. Should see success in console and attempt redirect to dashboard

### Test Validation:
1. Try submitting empty forms
2. Try passwords that don't match (register)
3. Try passwords shorter than 8 characters
4. Try logging in with wrong credentials

## Next Steps / TODO

### Authentication State Management
- [ ] Add NgRx or standalone signals for auth state
- [ ] Persist authentication token (localStorage/sessionStorage)
- [ ] Add HTTP interceptor for adding auth token to requests
- [ ] Implement token refresh logic

### Route Guards
- [ ] Create auth guard for protected routes
- [ ] Redirect logged-in users away from auth pages
- [ ] Add role-based access control

### Password Recovery
- [ ] Implement forgot password flow
- [ ] Add reset password page
- [ ] Email verification system

### User Profile
- [ ] Add profile completion after registration
- [ ] Remember me functionality
- [ ] Social login options (Google, Facebook, etc.)

### Security Enhancements
- [ ] Add CSRF protection
- [ ] Implement rate limiting
- [ ] Add reCAPTCHA
- [ ] Password strength indicator
- [ ] Two-factor authentication (2FA)

### API Integration
- [ ] Connect to backend endpoints
- [ ] Add proper error handling
- [ ] Implement retry logic
- [ ] Add loading spinners/skeletons

## File Structure

```
src/app/features/auth/
├── pages/
│   ├── register/
│   │   ├── register.component.ts
│   │   ├── register.component.html
│   │   └── register.component.scss
│   └── login/
│       ├── login.component.ts
│       ├── login.component.html
│       └── login.component.scss
├── services/
│   └── auth.service.ts
└── auth.routes.ts (optional - not currently used)
```

## Dependencies

- **@angular/forms:** Reactive forms with validation
- **@angular/router:** Routing and navigation
- **@angular/common/http:** HTTP client (ready for API)
- **RxJS:** Reactive programming with Observables
- **Tailwind CSS:** Utility-first styling
- **Google Fonts:** Inter font family
- **Material Symbols:** Icon library

## Browser Support

- Chrome/Edge: Latest 2 versions
- Firefox: Latest 2 versions
- Safari: Latest 2 versions
- Mobile browsers: iOS Safari, Chrome Mobile

## Accessibility

- Semantic HTML structure
- Proper form labels
- ARIA attributes where needed
- Keyboard navigation support
- Focus states on interactive elements
- Error announcements for screen readers

---

**Last Updated:** December 2024
**Version:** 1.0.0
**Maintained by:** TonTin Development Team