# Feature-Based Organization Guide

## 📋 Overview

This document explains the feature-based architecture reorganization of the TonTin Platform frontend. All features now follow a consistent, modular structure inspired by modern Angular best practices.

**Date**: February 2025  
**Status**: ✅ Complete

---

## 🏗️ Architecture Pattern

Every feature module follows this standardized structure:

```
features/{feature-name}/
├── pages/                    # All component files
│   ├── {component}.component.ts
│   ├── {component}.component.html
│   ├── {component}.component.scss
│   └── {component}.component.spec.ts
├── services/                 # Feature-specific services
│   └── {feature}.service.ts
├── guards/                   # Feature-specific guards (optional)
│   └── {feature}.guard.ts
├── {feature}.routes.ts       # Feature routing configuration
└── README.md                 # Feature documentation
```

---

## 🎯 Benefits

### 1. **Modularity**
- Each feature is self-contained
- Easy to understand and maintain
- Clear boundaries between features

### 2. **Scalability**
- Add new features without affecting existing ones
- Easy to split into micro-frontends if needed
- Simple to test in isolation

### 3. **Discoverability**
- All feature code in one place
- Consistent structure across all features
- README documentation for each feature

### 4. **Maintainability**
- Clear separation of concerns
- Easy to locate and modify code
- Reduced coupling between features

---

## 📁 Current Feature Structure

### Authentication Feature
```
features/auth/
├── pages/
│   ├── login/
│   │   ├── login.component.ts
│   │   ├── login.component.html
│   │   └── login.component.scss
│   ├── register/
│   │   ├── register.component.ts
│   │   ├── register.component.html
│   │   └── register.component.scss
│   └── reset-password/
│       ├── reset-password.component.ts
│       ├── reset-password.component.html
│       └── reset-password.component.scss
├── services/
│   └── auth.service.ts
├── guards/
│   ├── auth.guard.ts          # Protects authenticated routes
│   └── guest.guard.ts         # Protects guest-only routes
├── auth.routes.ts
└── README.md
```

### Dârs Feature
```
features/dashboard/features/dars/
├── pages/
│   ├── my-dars.component.ts          # List user's Dârs
│   ├── my-dars.component.html
│   ├── my-dars.component.scss
│   ├── create-dar.component.ts       # Create new Dâr
│   ├── create-dar.component.html
│   ├── create-dar.component.scss
│   ├── dar-details.component.ts      # View Dâr details
│   ├── dar-details.component.html
│   └── dar-details.component.scss
├── services/
│   └── dar.service.ts                # Dâr CRUD operations
├── dars.routes.ts
└── README.md
```

### Notifications Feature
```
features/dashboard/features/notifications/
├── pages/
│   ├── notifications.component.ts
│   ├── notifications.component.html
│   └── notifications.component.scss
├── services/
│   └── notification.service.ts       # Notification management
├── notifications.routes.ts
└── README.md
```

### Payments Feature
```
features/dashboard/features/payments/
├── pages/
│   ├── pay-contribution.component.ts
│   ├── pay-contribution.component.html
│   └── pay-contribution.component.scss
├── services/
│   └── payment.service.ts            # Payment processing
├── payments.routes.ts
└── README.md
```

### Profile Feature
```
features/dashboard/features/profile/
├── pages/
│   ├── profile.component.ts
│   ├── profile.component.html
│   └── profile.component.scss
├── services/
│   └── user.service.ts               # User profile management
├── profile.routes.ts
└── README.md
```

### Overview Feature (Dashboard)
```
features/dashboard/features/overview/
├── pages/
│   ├── client.component.ts
│   ├── client.component.html
│   ├── client.component.scss
│   └── client.component.spec.ts
├── overview.routes.ts
└── README.md
```

### Reports Feature
```
features/dashboard/features/reports/
├── pages/
│   ├── reports.component.ts
│   ├── reports.component.html
│   └── reports.component.scss
├── reports.routes.ts
└── README.md
```

### Trust Rankings Feature
```
features/dashboard/features/trust-rankings/
├── pages/
│   ├── trust-rankings.component.ts
│   ├── trust-rankings.component.html
│   └── trust-rankings.component.scss
├── trust-rankings.routes.ts
└── README.md
```

### Admin Feature
```
features/dashboard/features/admin/
├── pages/
│   ├── admin.component.ts
│   ├── admin.component.html
│   ├── admin.component.scss
│   └── admin.component.spec.ts
├── admin.routes.ts
└── README.md
```

---

## 🔄 Service Organization

### Before Reorganization
Services were centralized in `core/services/`:
```
core/services/
├── auth.service.ts
├── dar.service.ts
├── notification.service.ts
├── payment.service.ts
├── user.service.ts
└── index.ts
```

### After Reorganization
Services are now co-located with their features:

- **auth.service.ts** → `features/auth/services/`
- **dar.service.ts** → `features/dashboard/features/dars/services/`
- **notification.service.ts** → `features/dashboard/features/notifications/services/`
- **payment.service.ts** → `features/dashboard/features/payments/services/`
- **user.service.ts** → `features/dashboard/features/profile/services/`

### Guards Organization

Guards are now feature-specific:

- **auth.guard.ts** → `features/auth/guards/`
- **guest.guard.ts** → `features/auth/guards/`
- **role.guard.ts** → `core/guards/` (remains in core as it's cross-cutting)

---

## 📝 Import Path Updates

### Old Import Paths
```typescript
// ❌ Old way - importing from core
import { DarService } from '../../../../core/services/dar.service';
import { AuthService } from '../../../core/services/auth.service';
import { authGuard } from './core/guards/auth.guard';
```

### New Import Paths
```typescript
// ✅ New way - importing from feature
import { DarService } from '../services/dar.service';
import { AuthService } from '../../auth/services/auth.service';
import { authGuard } from './features/auth/guards/auth.guard';
```

---

## 🚀 Creating a New Feature

Follow these steps to create a new feature:

### 1. Create Feature Structure
```bash
cd src/app/features/
mkdir -p my-feature/{pages,services}
```

### 2. Create Component
```bash
cd my-feature/pages
ng generate component my-component --standalone
```

### 3. Create Service
```typescript
// my-feature/services/my-feature.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MyFeatureService {
  constructor(private http: HttpClient) {}

  // Add service methods here
}
```

### 4. Create Routes
```typescript
// my-feature/my-feature.routes.ts
import { Routes } from '@angular/router';

export const MY_FEATURE_ROUTES: Routes = [
  {
    path: 'my-feature',
    loadComponent: () =>
      import('./pages/my-component.component').then((m) => m.MyComponent),
    data: {
      title: 'My Feature - TonTin',
    },
  },
];
```

### 5. Create README
```markdown
# My Feature Module

## Overview
Brief description of the feature...

## Architecture
Feature structure diagram...

## API Integration
How to switch from mock to real API...
```

### 6. Add to Main Routes
```typescript
// app.routes.ts
import { MY_FEATURE_ROUTES } from './features/my-feature/my-feature.routes';

export const routes: Routes = [
  // ... other routes
  ...MY_FEATURE_ROUTES,
];
```

---

## 🔧 Shared vs Feature-Specific

### Keep in Core (`src/app/core/`)
- **Interceptors** (HTTP, error handling)
- **Cross-cutting guards** (role.guard.ts)
- **Global services** (if any)
- **App-wide configuration**

### Keep in Shared (`src/app/shared/`)
- **Reusable components** (buttons, modals, etc.)
- **Layouts** (client-layout, admin-layout)
- **Pipes** (custom formatting)
- **Directives** (custom behavior)
- **Utilities** (helper functions)

### Keep in Features (`src/app/features/`)
- **Feature components**
- **Feature services**
- **Feature guards** (feature-specific)
- **Feature routes**
- **Feature models/interfaces**

---

## 📚 Documentation

Each feature now includes a comprehensive README with:

1. **Overview** - What the feature does
2. **Architecture** - File structure
3. **Current Implementation** - Hardcoded/mock status
4. **API Integration Guide** - How to switch to real API
5. **API Endpoints** - Expected backend endpoints
6. **Models/Interfaces** - TypeScript types
7. **Security & Permissions** - Auth requirements
8. **Next Steps** - Future enhancements
9. **Related Files** - Dependencies
10. **Tips** - Development & debugging

### Example Feature READMEs
- `features/auth/README.md` - Complete example
- `features/dashboard/features/dars/README.md` - Comprehensive guide
- `features/dashboard/features/notifications/README.md` - Detailed documentation
- `features/dashboard/features/payments/README.md` - Full reference

---

## ✅ Migration Checklist

If migrating existing code to this structure:

- [ ] Create feature folder structure
- [ ] Move components to `pages/` folder
- [ ] Move/create services in `services/` folder
- [ ] Move feature-specific guards to `guards/` folder
- [ ] Create `{feature}.routes.ts` file
- [ ] Update all import paths in components
- [ ] Update all import paths in services
- [ ] Move HTML/SCSS files to `pages/` folder
- [ ] Update app.routes.ts imports
- [ ] Create comprehensive README.md
- [ ] Test the feature in isolation
- [ ] Update related documentation

---

## 🎓 Best Practices

### 1. Service Scope
- **Feature Service**: Used only within the feature → Place in `features/{feature}/services/`
- **Shared Service**: Used across multiple features → Consider if it belongs in `core/`

### 2. Component Organization
- All components go in `pages/` folder
- Keep component files together (`.ts`, `.html`, `.scss`, `.spec.ts`)
- Use descriptive names (e.g., `my-dars.component.ts`, not just `dars.ts`)

### 3. Route Configuration
- Each feature has its own routes file
- Use lazy loading for better performance
- Include metadata (title, roles, etc.)

### 4. Documentation
- Every feature must have a README
- Include hardcoded/API switch instructions
- Document all API endpoints
- Add troubleshooting tips

### 5. Testing
- Test features in isolation
- Mock external dependencies
- Use feature-specific test data

---

## 🔍 Finding Code

### By Feature
```bash
# Find all Dâr-related code
cd src/app/features/dashboard/features/dars/

# Find all auth-related code
cd src/app/features/auth/
```

### By Type
```bash
# Find all services
find src/app/features -name "*.service.ts"

# Find all components
find src/app/features -name "*.component.ts"

# Find all routes
find src/app/features -name "*.routes.ts"
```

### By Feature Name
```bash
# Search for specific feature
find src/app/features -type d -name "*notification*"
```

---

## 📊 Feature Dependencies

```
┌─────────────────────────────────────────────┐
│                  App Routes                 │
└─────────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
   ┌────▼────┐   ┌───▼────┐   ┌───▼─────┐
   │  Auth   │   │ Public │   │Dashboard│
   │ Feature │   │Feature │   │ Feature │
   └─────────┘   └────────┘   └────┬────┘
                                    │
        ┌───────────────────────────┼──────────┐
        │           │           │   │      │   │
    ┌───▼──┐   ┌───▼───┐   ┌──▼───▼──┐  ┌▼──────┐
    │ Dârs │   │Notifs │   │Payments │  │Profile│
    └──────┘   └───────┘   └─────────┘  └───────┘
        │                       │
        │                       │
        └───────────┬───────────┘
                    │
              ┌─────▼─────┐
              │Dar Service│
              └───────────┘
```

---

## 🚨 Common Issues & Solutions

### Issue: Import path errors after moving files
**Solution**: Update all import statements to reflect new paths. Use relative imports within the same feature.

### Issue: HTML/SCSS files not found
**Solution**: Ensure HTML/SCSS files are moved to the same `pages/` folder as the component `.ts` file.

### Issue: Service not found
**Solution**: Check that the service is in the correct `services/` folder and imports are updated.

### Issue: Routes not working
**Solution**: Verify that feature routes are imported in `app.routes.ts` and paths are correct.

### Issue: Guards not working
**Solution**: Update guard imports in `app.routes.ts` to point to new locations.

---

## 🎯 Summary

The new feature-based organization:

✅ **Makes code easier to find** - Everything related to a feature is in one place  
✅ **Improves maintainability** - Clear boundaries and responsibilities  
✅ **Enables scalability** - Easy to add new features or split into micro-frontends  
✅ **Enhances documentation** - Each feature has its own README  
✅ **Simplifies onboarding** - Consistent structure across all features  
✅ **Supports testing** - Features can be tested in isolation  

---

**For questions or suggestions, please update this document or contact the development team.**

**Last Updated**: February 2025  
**Version**: 2.0.0