# TonTin Platform - Frontend Project Structure

## 📁 Overview

This document describes the Angular frontend architecture following best practices and industry standards.

---

## 🏗️ Project Structure

```
src/app/
├── core/                           # Singleton services, guards, interceptors
│   ├── guards/                     # Route guards
│   │   ├── auth.guard.ts          # Authentication guard
│   │   ├── guest.guard.ts         # Guest-only guard
│   │   └── role.guard.ts          # Role-based access control
│   ├── interceptors/               # HTTP interceptors
│   │   └── auth.interceptor.ts    # JWT token interceptor
│   └── services/                   # Core singleton services
│       ├── dar.service.ts         # Dâr management service
│       ├── notification.service.ts # Notification service
│       ├── payment.service.ts     # Payment service
│       ├── user.service.ts        # User service
│       └── index.ts               # Barrel export
│
├── shared/                         # Shared/reusable modules
│   ├── components/                 # Shared components
│   │   ├── navigation/            # Navigation components
│   │   │   ├── auth-navbar.component.ts      # Minimal navbar for auth pages
│   │   │   ├── public-navbar.component.ts    # Full navbar for public pages
│   │   │   └── index.ts           # Barrel export
│   │   └── index.ts               # Barrel export
│   ├── layouts/                    # Application layouts
│   │   └── client-layout/         # Main dashboard layout
│   │       ├── client-layout.component.ts
│   │       ├── client-layout.component.html
│   │       └── client-layout.component.scss
│   ├── models/                     # Shared TypeScript interfaces/types
│   └── index.ts                    # Barrel export
│
├── features/                       # Feature modules (lazy-loaded)
│   ├── auth/                      # Authentication feature
│   │   ├── pages/                 # Auth pages
│   │   │   ├── login/            # Login page
│   │   │   ├── register/         # Registration page
│   │   │   └── reset-password/   # Password reset page
│   │   └── services/             # Auth-specific services
│   │       └── auth.service.ts   # Authentication service
│   │
│   ├── public/                    # Public-facing pages
│   │   ├── home/                 # Landing page
│   │   │   ├── landing.component.ts
│   │   │   ├── landing.component.html
│   │   │   └── landing.component.scss
│   │   ├── about/                # About us page
│   │   │   ├── about.component.ts
│   │   │   ├── about.component.html
│   │   │   └── about.component.scss
│   │   └── contact/              # Contact us page
│   │       ├── contact.component.ts
│   │       ├── contact.component.html
│   │       └── contact.component.scss
│   │
│   └── dashboard/                 # Dashboard feature module
│       ├── shared/               # Dashboard-specific shared components
│       │   └── (future shared dashboard components)
│       └── features/             # Dashboard sub-features
│           ├── overview/         # Main dashboard overview
│           │   ├── client.component.ts
│           │   ├── client.component.html
│           │   └── client.component.scss
│           ├── dars/            # My Dârs management
│           │   ├── my-dars.component.ts
│           │   ├── my-dars.component.html
│           │   └── my-dars.component.scss
│           ├── dar-details/     # Individual Dâr details
│           │   ├── dar-details.component.ts
│           │   ├── dar-details.component.html
│           │   └── dar-details.component.scss
│           ├── create-dar/      # Create new Dâr
│           │   ├── create-dar.component.ts
│           │   ├── create-dar.component.html
│           │   └── create-dar.component.scss
│           ├── payments/        # Payment management
│           │   ├── pay-contribution.component.ts
│           │   ├── pay-contribution.component.html
│           │   └── pay-contribution.component.scss
│           ├── reports/         # Financial reports
│           │   ├── reports.component.ts
│           │   ├── reports.component.html
│           │   └── reports.component.scss
│           ├── notifications/   # Notifications center
│           │   ├── notifications.component.ts
│           │   ├── notifications.component.html
│           │   └── notifications.component.scss
│           ├── trust-rankings/  # Trust score rankings
│           │   ├── trust-rankings.component.ts
│           │   ├── trust-rankings.component.html
│           │   └── trust-rankings.component.scss
│           ├── profile/         # User profile settings
│           │   ├── profile.component.ts
│           │   ├── profile.component.html
│           │   └── profile.component.scss
│           └── admin/           # Admin dashboard
│               ├── admin.component.ts
│               ├── admin.component.html
│               └── admin.component.scss
│
├── assets/                         # Static assets
│   ├── logo.png                   # Main logo (PNG)
│   └── logo.svg                   # Logo (SVG backup)
│
├── app.component.ts               # Root component
├── app.routes.ts                  # Application routes
└── app.config.ts                  # App configuration
```

---

## 🎯 Design Principles

### 1. **Feature-Based Organization**
- Each feature is self-contained with its own components, services, and routes
- Features are lazy-loaded for better performance
- Clear separation between public and authenticated features

### 2. **Core Module (Singleton Services)**
```typescript
core/
├── guards/      // Route protection
├── interceptors/ // HTTP request/response handling
└── services/    // App-wide singleton services
```

**Rules:**
- Import CoreModule only ONCE in AppModule
- Services are providedIn: 'root'
- Never import CoreModule in feature modules

### 3. **Shared Module (Reusable Components)**
```typescript
shared/
├── components/  // Reusable UI components
├── layouts/     // Application layouts
└── models/      // Shared TypeScript interfaces
```

**Rules:**
- Can be imported in any feature module
- Should not have dependencies on features
- Contains only presentation components

### 4. **Features Module (Business Logic)**
```typescript
features/
├── auth/        // Authentication & authorization
├── public/      // Public-facing pages
└── dashboard/   // Protected dashboard features
```

**Rules:**
- Lazy-loaded when possible
- Self-contained (own services, components, routes)
- Can import from shared and core

---

## 📂 Folder Responsibilities

### `/core`
**Purpose:** Application-wide singleton services and utilities
**Contains:**
- Authentication guards
- HTTP interceptors
- Global services (API clients, state management)
- App-wide utilities

**Import:** Only in app.config.ts or providers

### `/shared`
**Purpose:** Reusable components and utilities used across features
**Contains:**
- UI components (buttons, cards, modals)
- Navigation components (navbars, sidebars)
- Layouts (page templates)
- Pipes and directives
- Shared interfaces/models

**Import:** Anywhere needed

### `/features`
**Purpose:** Business features and user-facing functionality
**Contains:**
- Feature-specific components
- Feature-specific services
- Feature routes
- Feature models

**Import:** Via lazy loading in routes

---

## 🔄 Import Paths

### Before Restructuring (❌ Old):
```typescript
import { AuthNavbarComponent } from '../../../../shared/components/auth-navbar.component';
import { ClientComponent } from '../../dashboard/pages/client/client.component';
```

### After Restructuring (✅ New):
```typescript
import { AuthNavbarComponent } from '@app/shared/components/navigation';
import { ClientComponent } from '@app/features/dashboard/features/overview';
```

### Path Aliases (tsconfig.json):
```json
{
  "compilerOptions": {
    "paths": {
      "@app/*": ["src/app/*"],
      "@core/*": ["src/app/core/*"],
      "@shared/*": ["src/app/shared/*"],
      "@features/*": ["src/app/features/*"],
      "@environments/*": ["src/environments/*"]
    }
  }
}
```

---

## 🗺️ Routing Structure

### Public Routes (No Authentication)
```
/                           → Home/Landing Page
/about                      → About Us
/contact                    → Contact Us
/auth/login                 → Login
/auth/register              → Register
/auth/reset-password        → Reset Password
```

### Protected Routes (Authentication Required)
```
/dashboard/client           → Client Dashboard Layout
  ├── (empty)              → Overview/Home
  ├── my-dars              → My Dârs List
  ├── dar/:id              → Dâr Details
  ├── create-dar           → Create New Dâr
  ├── pay-contribution     → Payment Page
  ├── pay-contribution/:id → Payment for Specific Dâr
  ├── reports              → Financial Reports
  ├── notifications        → Notifications Center
  ├── trust-rankings       → Trust Score Rankings
  └── profile              → User Profile Settings

/dashboard/admin            → Admin Dashboard (ROLE_ADMIN only)
```

---

## 🎨 Component Naming Conventions

### Files
```
feature-name.component.ts       // Component logic
feature-name.component.html     // Template
feature-name.component.scss     // Styles
feature-name.component.spec.ts  // Unit tests
```

### Classes
```typescript
// PascalCase + Component suffix
export class FeatureNameComponent { }
```

### Selectors
```typescript
// kebab-case with app prefix
selector: 'app-feature-name'
```

---

## 🧩 Component Types

### 1. **Smart Components** (Container Components)
- Located in feature folders
- Handle business logic and data fetching
- Connect to services
- Pass data to presentation components

**Example:** `my-dars.component.ts`

### 2. **Presentation Components** (Dumb Components)
- Located in shared/components
- Receive data via @Input()
- Emit events via @Output()
- No business logic
- Reusable across features

**Example:** `auth-navbar.component.ts`

### 3. **Layout Components**
- Located in shared/layouts
- Define page structure
- Contain router-outlet
- Handle navigation structure

**Example:** `client-layout.component.ts`

---

## 📦 Module Organization

### Feature Module Example
```typescript
features/dashboard/features/dars/
├── my-dars.component.ts           // Smart component
├── my-dars.component.html         // Template
├── my-dars.component.scss         // Styles
└── models/                        // Feature-specific models
    └── dar.model.ts
```

### Shared Component Example
```typescript
shared/components/navigation/
├── auth-navbar.component.ts       // Presentation component
├── public-navbar.component.ts     // Presentation component
└── index.ts                       // Barrel export
```

---

## 🔐 Security & Guards

### Guard Usage
```typescript
// Public routes (unauthenticated only)
canActivate: [guestGuard]

// Protected routes (authenticated only)
canActivate: [authGuard]

// Role-based routes
canActivate: [authGuard, roleGuard]
data: { roles: ['ROLE_ADMIN', 'ROLE_CLIENT'] }
```

---

## 🎯 Best Practices Followed

✅ **Separation of Concerns**
- Business logic in components
- Data access in services
- Routing in route files

✅ **DRY (Don't Repeat Yourself)**
- Shared components for reusable UI
- Barrel exports for cleaner imports
- Service layer for data operations

✅ **Scalability**
- Feature-based organization
- Lazy loading for performance
- Clear module boundaries

✅ **Maintainability**
- Consistent naming conventions
- Clear folder hierarchy
- Documentation and comments

✅ **Performance**
- Lazy-loaded routes
- OnPush change detection (where applicable)
- Optimized bundle sizes

✅ **Type Safety**
- TypeScript interfaces in models/
- Strict type checking
- No 'any' types

---

## 🚀 Development Workflow

### Adding a New Feature
1. Create folder in `features/`
2. Add components, services, models
3. Create route in `app.routes.ts`
4. Add navigation link in layout/navbar

### Adding a Shared Component
1. Create in `shared/components/`
2. Export in `index.ts` (barrel export)
3. Import where needed

### Adding a Service
1. Global service → `core/services/`
2. Feature service → `features/[feature]/services/`
3. Use `providedIn: 'root'` for singletons

---

## 📋 File Naming Standards

| Type | Pattern | Example |
|------|---------|---------|
| Component | `feature-name.component.ts` | `my-dars.component.ts` |
| Service | `feature-name.service.ts` | `dar.service.ts` |
| Guard | `guard-name.guard.ts` | `auth.guard.ts` |
| Interceptor | `interceptor-name.interceptor.ts` | `auth.interceptor.ts` |
| Interface | `interface-name.interface.ts` or `.model.ts` | `dar.model.ts` |
| Pipe | `pipe-name.pipe.ts` | `date-format.pipe.ts` |

---

## 🎨 Component Architecture

### Smart Component (Container)
```typescript
@Component({
  selector: 'app-my-dars',
  standalone: true,
  imports: [CommonModule, RouterModule, /* ... */],
  templateUrl: './my-dars.component.html'
})
export class MyDarsComponent implements OnInit {
  // Business logic
  // Service calls
  // State management
}
```

### Presentation Component
```typescript
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `<!-- Inline template -->`
})
export class NavbarComponent {
  @Input() items!: MenuItem[];
  @Output() itemClick = new EventEmitter<MenuItem>();
}
```

---

## 🔗 Navigation Hierarchy

```
App Shell
├── Public Layout (auth-navbar)
│   ├── Home
│   ├── About
│   └── Contact
│
├── Auth Layout (auth-navbar)
│   ├── Login
│   ├── Register
│   └── Reset Password
│
└── Dashboard Layout (client-layout with sidebar)
    ├── Overview
    ├── My Dârs
    ├── Dâr Details
    ├── Create Dâr
    ├── Payments
    ├── Reports
    ├── Notifications
    ├── Trust Rankings
    └── Profile
```

---

## 📊 Service Layer Architecture

### Service Types

**1. Core Services (Singleton)**
- AuthService
- DarService
- PaymentService
- NotificationService
- UserService

**2. Feature Services**
- Scoped to specific features
- Can inject core services

**3. Utility Services**
- Helper functions
- Data transformation
- Validation

---

## 🎯 Key Benefits of This Structure

### ✅ Scalability
- Easy to add new features
- Clear module boundaries
- Independent feature development

### ✅ Maintainability
- Predictable file locations
- Consistent patterns
- Easy to onboard new developers

### ✅ Performance
- Lazy loading reduces initial bundle
- Tree-shaking removes unused code
- Optimized change detection

### ✅ Testing
- Isolated components
- Mockable services
- Testable guards

### ✅ Collaboration
- Feature-based teams
- Minimal merge conflicts
- Clear ownership

---

## 📝 Migration Notes

### What Changed

**Navigation Components:**
```
OLD: shared/components/auth-navbar.component.ts
NEW: shared/components/navigation/auth-navbar.component.ts
```

**Layouts:**
```
OLD: features/dashboard/layouts/client-layout.component.ts
NEW: shared/layouts/client-layout/client-layout.component.ts
```

**Public Pages:**
```
OLD: features/landing/
OLD: features/about/
OLD: features/contact/
NEW: features/public/home/
NEW: features/public/about/
NEW: features/public/contact/
```

**Dashboard Pages:**
```
OLD: features/dashboard/pages/client/
OLD: features/dashboard/pages/my-dars/
OLD: features/dashboard/pages/[feature]/
NEW: features/dashboard/features/overview/
NEW: features/dashboard/features/dars/
NEW: features/dashboard/features/[feature]/
```

### Import Path Updates

All import paths have been updated to reflect the new structure:
- ✅ Route imports in `app.routes.ts`
- ✅ Component imports in all pages
- ✅ Service imports in layouts
- ✅ Navbar imports in all components

---

## 🔮 Future Enhancements

### Recommended Additions

**1. State Management**
```
core/
└── state/
    ├── app.state.ts
    ├── dar.state.ts
    └── user.state.ts
```

**2. Shared UI Components**
```
shared/
└── components/
    └── ui/
        ├── button/
        ├── card/
        ├── modal/
        └── table/
```

**3. Models/Interfaces**
```
shared/
└── models/
    ├── dar.model.ts
    ├── user.model.ts
    ├── payment.model.ts
    └── notification.model.ts
```

**4. Utilities**
```
shared/
└── utils/
    ├── validators/
    ├── helpers/
    └── constants/
```

---

## 📚 Additional Resources

- [Angular Style Guide](https://angular.io/guide/styleguide)
- [Angular Architecture Best Practices](https://angular.io/guide/architecture)
- [Standalone Components](https://angular.io/guide/standalone-components)

---

## 🛠️ Build & Serve

### Development
```bash
cd platform-front
ng serve
```

### Production Build
```bash
ng build --configuration production
```

### Run Tests
```bash
ng test
```

---

**Last Updated:** February 2024  
**Version:** 1.0.0  
**Maintainer:** TonTin Development Team