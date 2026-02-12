# Services Architecture Guide

## 📋 Overview

This document explains the difference between **Core Services** and **Feature Services** in the TonTin Platform frontend, when to use each, and how they work together.

**Key Principle**: Services should be placed based on their **scope** and **responsibility**, not just convenience.

---

## 🎯 Core Services vs Feature Services

### Core Services (`src/app/core/services/`)

**Purpose**: Handle cross-cutting concerns and app-wide functionality

**Characteristics**:
- ✅ Used by **multiple features**
- ✅ App-wide state management
- ✅ Global configuration
- ✅ Cross-cutting concerns
- ✅ Framework-level operations

**Examples**:
- **Authentication/Authorization** (if used everywhere)
- **HTTP Interceptors** (request/response handling)
- **Error Handling** (global error service)
- **Logging Service** (app-wide logging)
- **Configuration Service** (app settings)
- **Theme Service** (app-wide theming)
- **Cache Service** (app-wide caching)
- **Translation Service** (i18n)

### Feature Services (`src/app/features/{feature}/services/`)

**Purpose**: Handle feature-specific business logic and data

**Characteristics**:
- ✅ Used **primarily by one feature**
- ✅ Domain-specific operations
- ✅ Encapsulated with the feature
- ✅ Can be lazy-loaded with feature
- ✅ Feature-specific state

**Examples**:
- **dar.service.ts** (Dâr CRUD operations)
- **notification.service.ts** (Notification management)
- **payment.service.ts** (Payment processing)
- **user.service.ts** (User profile management)
- **auth.service.ts** (Auth feature operations)

---

## 📊 Current TonTin Architecture

### What We Did (Feature-Based)

We moved ALL services to features for maximum **feature encapsulation**:

```
✅ Current Structure (Feature-Based):
features/
├── auth/services/
│   └── auth.service.ts          (Auth operations)
├── dars/services/
│   └── dar.service.ts           (Dâr CRUD)
├── notifications/services/
│   └── notification.service.ts  (Notifications)
├── payments/services/
│   └── payment.service.ts       (Payments)
└── profile/services/
    └── user.service.ts          (User profile)

core/
├── guards/
│   └── role.guard.ts           (Cross-cutting guard)
└── interceptors/
    └── auth.interceptor.ts     (HTTP interceptor)
```

### Why This Approach?

**Advantages**:
1. ✅ **Maximum modularity** - Each feature is self-contained
2. ✅ **Clear ownership** - Easy to know which team/developer owns what
3. ✅ **Lazy loading** - Services load only when feature loads
4. ✅ **Easy to remove** - Delete feature folder = delete everything
5. ✅ **Micro-frontend ready** - Easy to split into separate apps

**Trade-offs**:
- ⚠️ If multiple features need the same service, they import from the owning feature
- ⚠️ Need to be careful about circular dependencies

---

## 🤔 When to Use Core vs Feature Services

### Use CORE Services When:

1. **Multiple Unrelated Features Need It**
   ```typescript
   // Example: Logger used by auth, dars, payments, etc.
   @Injectable({ providedIn: 'root' })
   export class LoggerService {
     log(message: string): void {
       console.log(`[${new Date().toISOString()}] ${message}`);
     }
   }
   // Location: src/app/core/services/logger.service.ts
   ```

2. **App-Wide State Management**
   ```typescript
   // Example: Theme service affects entire app
   @Injectable({ providedIn: 'root' })
   export class ThemeService {
     private theme$ = new BehaviorSubject<'light' | 'dark'>('light');
     // ...
   }
   // Location: src/app/core/services/theme.service.ts
   ```

3. **Framework-Level Operations**
   ```typescript
   // Example: HTTP error handling for all requests
   @Injectable({ providedIn: 'root' })
   export class ErrorHandlerService {
     handleError(error: HttpErrorResponse): void {
       // Global error handling
     }
   }
   // Location: src/app/core/services/error-handler.service.ts
   ```

### Use FEATURE Services When:

1. **Feature-Specific Operations**
   ```typescript
   // Example: Dâr operations only used by dars feature
   @Injectable({ providedIn: 'root' })
   export class DarService {
     getDars(): Observable<Dar[]> { }
     createDar(dar: CreateDarRequest): Observable<Dar> { }
   }
   // Location: features/dashboard/features/dars/services/dar.service.ts
   ```

2. **Domain-Specific Logic**
   ```typescript
   // Example: Payment processing logic
   @Injectable({ providedIn: 'root' })
   export class PaymentService {
     processPayment(payment: Payment): Observable<PaymentResult> { }
   }
   // Location: features/dashboard/features/payments/services/payment.service.ts
   ```

3. **Feature State Management**
   ```typescript
   // Example: Notification state for notifications feature
   @Injectable({ providedIn: 'root' })
   export class NotificationService {
     private notifications$ = new BehaviorSubject<Notification[]>([]);
     // Only used by notifications feature
   }
   // Location: features/dashboard/features/notifications/services/notification.service.ts
   ```

---

## 🔄 Sharing Services Between Features

### Scenario: Payment Feature Needs Dâr Data

**Option 1: Import from Owning Feature (Recommended)**
```typescript
// payments/pages/pay-contribution.component.ts
import { DarService } from '../../dars/services/dar.service';

export class PayContributionComponent {
  constructor(private darService: DarService) {}
  
  loadDar(id: number): void {
    this.darService.getDarById(id).subscribe(/* ... */);
  }
}
```

**Option 2: Create Shared Core Service (If Many Features Need It)**
```typescript
// If 5+ features need Dâr data, consider moving to core
// core/services/dar.service.ts
@Injectable({ providedIn: 'root' })
export class DarService {
  // Shared across all features
}
```

**Option 3: Facade Pattern (Advanced)**
```typescript
// features/payments/services/payment-dar.facade.ts
@Injectable({ providedIn: 'root' })
export class PaymentDarFacade {
  constructor(
    private darService: DarService,
    private paymentService: PaymentService
  ) {}
  
  // Combines both services for payment feature
}
```

---

## ⚠️ Important Considerations

### 1. Circular Dependencies

**Problem**:
```typescript
// ❌ Don't do this!
// dars/services/dar.service.ts
import { PaymentService } from '../../payments/services/payment.service';

// payments/services/payment.service.ts
import { DarService } from '../../dars/services/dar.service';
```

**Solution**: Use a shared service or events
```typescript
// ✅ Better: Use shared service or events
// core/services/dar-payment.service.ts (if needed by both)
```

### 2. providedIn: 'root'

All services (core or feature) typically use `providedIn: 'root'`:
```typescript
@Injectable({
  providedIn: 'root'  // Makes it a singleton, tree-shakeable
})
```

**Why?**
- Single instance across app
- Tree-shakeable (removed if not used)
- No need to add to providers array

### 3. Lazy Loading Consideration

Feature services are still **eagerly loaded** if:
- They use `providedIn: 'root'`
- They're imported by a non-lazy module

To make truly lazy:
```typescript
@Injectable()  // No providedIn
export class FeatureService { }

// In feature routes
{
  path: 'feature',
  loadChildren: () => import('./feature/feature.module'),
  providers: [FeatureService]  // Lazy loaded with feature
}
```

---

## 📋 Decision Matrix

| Criteria | Core Service | Feature Service |
|----------|--------------|-----------------|
| Used by 3+ features | ✅ | ❌ |
| Feature-specific logic | ❌ | ✅ |
| App-wide state | ✅ | ❌ |
| Domain operations | ❌ | ✅ |
| Cross-cutting concern | ✅ | ❌ |
| Can be lazy-loaded | ❌ | ✅ |
| Tightly coupled to feature | ❌ | ✅ |

---

## 🎯 TonTin Platform Strategy

### Current Approach (Feature-First)

We chose **feature-first** for maximum modularity:

**Pros**:
- ✅ Clear feature boundaries
- ✅ Easy to understand ownership
- ✅ Ready for micro-frontends
- ✅ Simple to add/remove features

**When to Refactor to Core**:
- When 3+ features import the same service
- When service becomes truly cross-cutting
- When it causes circular dependencies
- When it handles app-wide state

### Example Refactoring Trigger

```typescript
// If you see this in multiple features:
import { DarService } from '../../../dars/services/dar.service';
import { DarService } from '../../../../dars/services/dar.service';
import { DarService } from '../../dars/services/dar.service';

// Consider moving to:
// core/services/dar.service.ts
```

---

## 🛠️ Practical Guidelines

### 1. Start with Feature Services

**Rule of Thumb**: When creating a new service, start in the feature:
```bash
# Default location for new services
ng generate service features/my-feature/services/my-feature
```

### 2. Move to Core When Needed

**Indicators**:
- Service imported by 3+ features
- Service handles app-wide concerns
- Service causes import complexity

**How to Move**:
```bash
# 1. Move the file
mv src/app/features/feature/services/service.ts src/app/core/services/

# 2. Update imports in all features
# Change: import { Service } from '../services/service';
# To:     import { Service } from '../../../core/services/service';

# 3. Test thoroughly
npm test
```

### 3. Document Service Location

Add JSDoc to clarify:
```typescript
/**
 * Dâr Service
 * 
 * @location Feature Service (features/dars/services/)
 * @scope Primary: Dars feature, Secondary: Payments feature
 * @reason Domain-specific Dâr operations
 */
@Injectable({ providedIn: 'root' })
export class DarService { }
```

---

## 📚 Examples from TonTin

### Current Feature Services (Good as-is)

1. **auth.service.ts** → `features/auth/services/`
   - ✅ Used primarily by auth feature
   - ✅ Authentication is feature-specific
   - ✅ Even though used for guards, the logic is auth-domain

2. **dar.service.ts** → `features/dars/services/`
   - ✅ Primary: Dars feature
   - ⚠️ Secondary: Payments feature (imports from dars)
   - ✅ Keep in dars (owns the domain)

3. **payment.service.ts** → `features/payments/services/`
   - ✅ Used only by payments feature
   - ✅ Payment-specific operations

4. **notification.service.ts** → `features/notifications/services/`
   - ✅ Used only by notifications feature
   - ✅ Notification-specific state

### Potential Core Services (If Added)

1. **logger.service.ts** → `core/services/`
   - Used by all features for logging
   - App-wide concern

2. **config.service.ts** → `core/services/`
   - App-wide configuration
   - Used by multiple features

3. **cache.service.ts** → `core/services/`
   - Generic caching for any feature
   - Cross-cutting concern

---

## 🔍 Real-World Scenarios

### Scenario 1: Should AuthService be in Core?

**Question**: AuthService is used by guards, interceptors, and multiple features. Should it be in core?

**Answer**: **No, keep in auth feature**

**Reasoning**:
- Guards/interceptors import from feature (fine)
- Auth is a domain, not a cross-cutting concern
- Auth feature owns authentication logic
- Other features importing it is expected

### Scenario 2: Should DarService be in Core?

**Question**: DarService is used by dars and payments features. Move to core?

**Answer**: **No, keep in dars feature**

**Reasoning**:
- Dars feature owns the Dâr domain
- Payments importing it is fine (domain dependency)
- Only 2 features use it (threshold is 3+)
- Clear ownership: Dars team owns it

### Scenario 3: Should We Create a Shared Service?

**Question**: 4 features need user profile data. Create shared service?

**Answer**: **Yes, consider core or keep in profile feature**

**Options**:
1. Keep in profile feature, others import from there
2. Move to core if it's truly shared state
3. Create facade pattern if complex

---

## 📖 Summary

### Key Takeaways

1. **Feature Services (Default)**
   - Most services belong here
   - Domain-specific logic
   - Feature encapsulation

2. **Core Services (Exception)**
   - Cross-cutting concerns only
   - Used by 3+ features
   - App-wide state

3. **Current TonTin Strategy**
   - Feature-first approach
   - Refactor to core when needed
   - Clear ownership model

4. **When in Doubt**
   - Start with feature service
   - Move to core only when clear benefit
   - Document the decision

### Quick Decision Tree

```
New Service Needed?
├── Used by 1 feature only?
│   └── ✅ Feature Service
├── Used by 2 features?
│   └── ✅ Feature Service (in owning feature)
├── Used by 3+ features?
│   └── ⚠️ Consider Core Service
└── Cross-cutting concern?
    └── ✅ Core Service
```

---

## 🎓 Further Reading

- **FEATURE_ORGANIZATION.md** - Overall architecture guide
- **REORGANIZATION_COMPLETE.md** - Migration details
- Individual feature READMEs - Feature-specific documentation

---

**Remember**: The goal is **clarity and maintainability**. Choose the location that makes the code easier to understand and maintain for your team.

**Last Updated**: February 2025  
**Version**: 1.0.0