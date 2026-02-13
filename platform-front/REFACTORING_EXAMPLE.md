# Refactoring Example: Separating Types, Enums, and Constants

## 📋 Overview

This document provides a **step-by-step example** of how to refactor the Dars feature to follow all best practices by separating types, enums, and constants.

---

## 🎯 Current State (Before Refactoring)

### Current Structure
```
features/dars/
├── pages/
│   ├── my-dars.component.ts
│   ├── create-dar.component.ts
│   └── dar-details.component.ts
├── services/
│   └── dar.service.ts (contains 120+ lines of interfaces!)
├── dars.routes.ts
└── README.md
```

### Current Service (Excerpt)
```typescript
// dar.service.ts - BEFORE (not ideal)
import { Injectable } from '@angular/core';

export interface Member {
  id: number;
  userId: number;
  userName: string;
  email: string;
  role: "organizer" | "member";  // ❌ String union
  paymentStatus: "paid" | "pending" | "overdue";  // ❌ String union
}

export interface Dar {
  id: number;
  name: string;
  status: "pending" | "active" | "completed" | "cancelled";  // ❌ String union
  frequency: "weekly" | "monthly" | "bi-weekly";  // ❌ String union
}

@Injectable({ providedIn: 'root' })
export class DarService {
  private apiUrl = `${environment.apiUrl}/dars`;
  private defaultPageSize = 20;  // ❌ Magic number
  
  getDars(page = 0, size = 20): Observable<Dar[]> {  // ❌ Magic number
    // ...
  }
}
```

**Problems**:
- ❌ 10+ interfaces in one file
- ❌ String unions instead of enums
- ❌ Magic numbers everywhere
- ❌ Service file is 500+ lines
- ❌ Hard to find types
- ❌ Types can't be reused easily

---

## ✅ Target State (After Refactoring)

### Target Structure
```
features/dars/
├── pages/
│   ├── my-dars.component.ts
│   ├── create-dar.component.ts
│   └── dar-details.component.ts
├── services/
│   └── dar.service.ts (service logic only - clean!)
├── models/
│   ├── dar.model.ts
│   ├── member.model.ts
│   ├── tour.model.ts
│   ├── transaction.model.ts
│   ├── message.model.ts
│   ├── dar-request.model.ts
│   └── index.ts
├── enums/
│   ├── dar-status.enum.ts
│   ├── payment-status.enum.ts
│   ├── dar-frequency.enum.ts
│   ├── member-role.enum.ts
│   └── index.ts
├── constants/
│   ├── dar.constants.ts
│   └── index.ts
├── dars.routes.ts
└── README.md
```

---

## 🔨 Step-by-Step Refactoring

### Step 1: Create Enums

#### Create `enums/dar-status.enum.ts`
```typescript
/**
 * Dâr lifecycle status
 */
export enum DarStatus {
  /** Dâr is being set up, not yet active */
  PENDING = 'pending',
  
  /** Dâr is active and accepting contributions */
  ACTIVE = 'active',
  
  /** Dâr has completed all cycles */
  COMPLETED = 'completed',
  
  /** Dâr was cancelled before completion */
  CANCELLED = 'cancelled'
}
```

#### Create `enums/payment-status.enum.ts`
```typescript
/**
 * Payment status for contributions
 */
export enum PaymentStatus {
  /** Payment has been made */
  PAID = 'paid',
  
  /** Payment is due but not yet made */
  PENDING = 'pending',
  
  /** Payment is past due date */
  OVERDUE = 'overdue',
  
  /** Payment is scheduled for future */
  FUTURE = 'future'
}
```

#### Create `enums/dar-frequency.enum.ts`
```typescript
/**
 * Contribution payment frequency
 */
export enum DarFrequency {
  WEEKLY = 'weekly',
  MONTHLY = 'monthly',
  BI_WEEKLY = 'bi-weekly'
}
```

#### Create `enums/member-role.enum.ts`
```typescript
/**
 * Member roles in a Dâr
 */
export enum MemberRole {
  /** Dâr organizer with admin privileges */
  ORGANIZER = 'organizer',
  
  /** Regular member with standard privileges */
  MEMBER = 'member'
}
```

#### Create `enums/index.ts`
```typescript
export * from './dar-status.enum';
export * from './payment-status.enum';
export * from './dar-frequency.enum';
export * from './member-role.enum';
```

---

### Step 2: Create Constants

#### Create `constants/dar.constants.ts`
```typescript
/**
 * Dâr feature configuration constants
 */
export const DAR_CONFIG = {
  /** Default page size for pagination */
  DEFAULT_PAGE_SIZE: 20,
  
  /** Maximum page size allowed */
  MAX_PAGE_SIZE: 100,
  
  /** Minimum number of members required */
  MIN_MEMBERS: 2,
  
  /** Maximum number of members allowed */
  MAX_MEMBERS: 100,
  
  /** Minimum contribution amount */
  MIN_CONTRIBUTION: 1,
  
  /** Maximum contribution amount */
  MAX_CONTRIBUTION: 1000000,
  
  /** Polling interval for real-time updates (milliseconds) */
  POLLING_INTERVAL_MS: 30000,
  
  /** Default currency */
  DEFAULT_CURRENCY: 'USD',
  
  /** Date format for display */
  DATE_FORMAT: 'MMM dd, yyyy',
  
  /** Maximum name length */
  MAX_NAME_LENGTH: 100,
  
  /** Maximum description length */
  MAX_DESCRIPTION_LENGTH: 500
} as const;

/**
 * Dâr API endpoints (relative to base API URL)
 */
export const DAR_ENDPOINTS = {
  BASE: '/dars',
  MY_DARS: '/dars/my-dars',
  JOIN: '/dars/join',
  PUBLIC: '/dars/public',
  DETAILS: (id: number) => `/dars/${id}`,
  MEMBERS: (id: number) => `/dars/${id}/members`,
  TOURS: (id: number) => `/dars/${id}/tours`,
  MESSAGES: (id: number) => `/dars/${id}/messages`
} as const;

/**
 * Dâr error messages
 */
export const DAR_ERROR_MESSAGES = {
  LOAD_FAILED: 'Failed to load Dâr details. Please try again.',
  CREATE_FAILED: 'Failed to create Dâr. Please try again.',
  UPDATE_FAILED: 'Failed to update Dâr. Please try again.',
  DELETE_FAILED: 'Failed to delete Dâr. Please try again.',
  JOIN_FAILED: 'Failed to join Dâr. Please check your invite code.',
  LEAVE_FAILED: 'Failed to leave Dâr. Please try again.',
  INVALID_NAME: 'Dâr name must be between 3 and 100 characters.',
  INVALID_CONTRIBUTION: 'Contribution amount must be at least $1.',
  INSUFFICIENT_MEMBERS: 'A Dâr must have at least 2 members.'
} as const;

/**
 * Type helper for DAR_CONFIG
 */
export type DarConfig = typeof DAR_CONFIG;
```

#### Create `constants/index.ts`
```typescript
export * from './dar.constants';
```

---

### Step 3: Create Models

#### Create `models/dar.model.ts`
```typescript
import { DarStatus, DarFrequency } from '../enums';

/**
 * Main Dâr entity
 */
export interface Dar {
  /** Unique identifier */
  id: number;
  
  /** Dâr name */
  name: string;
  
  /** Optional description */
  description?: string;
  
  /** Optional image URL */
  image?: string;
  
  /** Current status */
  status: DarStatus;
  
  /** Organizer user ID */
  organizerId: number;
  
  /** Organizer display name */
  organizerName: string;
  
  /** Organizer avatar URL */
  organizerAvatar?: string;
  
  /** Creation timestamp */
  createdDate: string;
  
  /** Start date */
  startDate?: string;
  
  /** End date */
  endDate?: string;
  
  /** Current cycle number */
  currentCycle: number;
  
  /** Total number of cycles */
  totalCycles: number;
  
  /** Current number of members */
  totalMembers: number;
  
  /** Maximum allowed members */
  maxMembers: number;
  
  /** Contribution amount per cycle */
  contributionAmount: number;
  
  /** Total pot size */
  potSize: number;
  
  /** Payment frequency */
  frequency: DarFrequency;
  
  /** Next payout date */
  nextPayoutDate?: string;
  
  /** Next payout recipient name */
  nextPayoutRecipient?: string;
  
  /** Is current user the organizer */
  isOrganizer: boolean;
  
  /** Is current user a member */
  isMember: boolean;
  
  /** Invite code for joining */
  inviteCode?: string;
  
  /** Dâr rules/terms */
  rules?: string;
  
  /** Visibility setting */
  visibility: 'public' | 'private' | 'invite-only';
}

/**
 * Detailed Dâr with related entities
 */
export interface DarDetails extends Dar {
  /** List of members */
  members: Member[];
  
  /** Payment cycles/tours */
  tours: Tour[];
  
  /** Transaction history */
  transactions: Transaction[];
  
  /** Chat messages */
  messages: Message[];
}
```

#### Create `models/member.model.ts`
```typescript
import { MemberRole, PaymentStatus } from '../enums';

/**
 * Dâr member
 */
export interface Member {
  /** Unique member ID */
  id: number;
  
  /** User ID reference */
  userId: number;
  
  /** User display name */
  userName: string;
  
  /** User email */
  email: string;
  
  /** Avatar URL */
  avatar?: string;
  
  /** Member role */
  role: MemberRole;
  
  /** Date joined */
  joinedDate: string;
  
  /** Turn order number */
  turnOrder: number;
  
  /** Turn date */
  turnDate?: string;
  
  /** Current payment status */
  paymentStatus: PaymentStatus;
  
  /** Contribution amount */
  contributionAmount?: number;
  
  /** Trust score (0-100) */
  trustScore?: number;
}
```

#### Create `models/dar-request.model.ts`
```typescript
import { DarFrequency } from '../enums';

/**
 * Request body for creating a new Dâr
 */
export interface CreateDarRequest {
  /** Dâr name (required) */
  name: string;
  
  /** Optional description */
  description?: string;
  
  /** Optional image URL */
  image?: string;
  
  /** Contribution amount per cycle */
  contributionAmount: number;
  
  /** Maximum number of members */
  maxMembers: number;
  
  /** Payment frequency */
  frequency: DarFrequency;
  
  /** Start date (ISO format) */
  startDate: string;
  
  /** Visibility setting */
  visibility: 'public' | 'private' | 'invite-only';
  
  /** Optional rules/terms */
  rules?: string;
}

/**
 * Request body for updating a Dâr
 */
export interface UpdateDarRequest {
  /** Updated name */
  name?: string;
  
  /** Updated description */
  description?: string;
  
  /** Updated image URL */
  image?: string;
  
  /** Updated rules */
  rules?: string;
  
  /** Updated visibility */
  visibility?: 'public' | 'private' | 'invite-only';
}

/**
 * Request body for joining a Dâr
 */
export interface JoinDarRequest {
  /** Dâr ID (if known) */
  darId?: number;
  
  /** Invite code (alternative to ID) */
  inviteCode?: string;
}

/**
 * Request body for inviting a member
 */
export interface InviteMemberRequest {
  /** Dâr ID */
  darId: number;
  
  /** Invitee email */
  email?: string;
  
  /** Invitee user ID (if existing user) */
  userId?: number;
}
```

#### Create `models/index.ts`
```typescript
export * from './dar.model';
export * from './member.model';
export * from './tour.model';
export * from './transaction.model';
export * from './message.model';
export * from './dar-request.model';
export * from './paginated-response.model';
```

---

### Step 4: Refactor Service

#### Refactored `services/dar.service.ts`
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../../../../environments/environment';

// Import models
import {
  Dar,
  DarDetails,
  Member,
  CreateDarRequest,
  UpdateDarRequest,
  JoinDarRequest,
  InviteMemberRequest,
  PaginatedResponse
} from '../models';

// Import enums
import { DarStatus } from '../enums';

// Import constants
import { DAR_CONFIG, DAR_ENDPOINTS } from '../constants';

/**
 * Service for managing Dârs (rotating savings groups)
 * 
 * Provides CRUD operations and state management for Dârs.
 * All data operations are observable-based for reactive UX.
 */
@Injectable({
  providedIn: 'root'
})
export class DarService {
  private apiUrl = `${environment.apiUrl}${DAR_ENDPOINTS.BASE}`;
  
  // State management
  private darsSubject = new BehaviorSubject<Dar[]>([]);
  public dars$ = this.darsSubject.asObservable();
  
  constructor(private http: HttpClient) {}
  
  /**
   * Get user's Dârs with pagination and filtering
   */
  getMyDars(
    status?: DarStatus,
    page: number = 0,
    size: number = DAR_CONFIG.DEFAULT_PAGE_SIZE
  ): Observable<PaginatedResponse<Dar>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (status) {
      params = params.set('status', status);
    }
    
    return this.http.get<PaginatedResponse<Dar>>(
      `${this.apiUrl}${DAR_ENDPOINTS.MY_DARS}`,
      { params }
    ).pipe(
      tap(response => {
        if (page === 0) {
          this.darsSubject.next(response.content);
        }
      })
    );
  }
  
  /**
   * Get detailed Dâr information by ID
   */
  getDarDetails(darId: number): Observable<DarDetails> {
    return this.http.get<DarDetails>(
      `${this.apiUrl}${DAR_ENDPOINTS.DETAILS(darId)}`
    );
  }
  
  /**
   * Create a new Dâr
   */
  createDar(request: CreateDarRequest): Observable<Dar> {
    return this.http.post<Dar>(this.apiUrl, request).pipe(
      tap(dar => {
        const currentDars = this.darsSubject.value;
        this.darsSubject.next([dar, ...currentDars]);
      })
    );
  }
  
  /**
   * Update an existing Dâr
   */
  updateDar(darId: number, request: UpdateDarRequest): Observable<Dar> {
    return this.http.put<Dar>(
      `${this.apiUrl}${DAR_ENDPOINTS.DETAILS(darId)}`,
      request
    );
  }
  
  /**
   * Delete a Dâr
   */
  deleteDar(darId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}${DAR_ENDPOINTS.DETAILS(darId)}`
    ).pipe(
      tap(() => {
        const currentDars = this.darsSubject.value;
        this.darsSubject.next(currentDars.filter(d => d.id !== darId));
      })
    );
  }
  
  /**
   * Join a Dâr using ID or invite code
   */
  joinDar(request: JoinDarRequest): Observable<Dar> {
    return this.http.post<Dar>(
      `${this.apiUrl}${DAR_ENDPOINTS.JOIN}`,
      request
    ).pipe(
      tap(dar => {
        const currentDars = this.darsSubject.value;
        this.darsSubject.next([dar, ...currentDars]);
      })
    );
  }
  
  // ... other methods
}
```

**Benefits**:
- ✅ Service is now ~200 lines (was 500+)
- ✅ No interface definitions
- ✅ Uses enums instead of strings
- ✅ Uses constants instead of magic numbers
- ✅ Clear imports from models/
- ✅ Easy to read and maintain

---

### Step 5: Update Components

#### Updated Component
```typescript
// pages/my-dars.component.ts - AFTER
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// Import from models
import { Dar } from '../models';

// Import from enums
import { DarStatus } from '../enums';

// Import from constants
import { DAR_CONFIG, DAR_ERROR_MESSAGES } from '../constants';

// Import service
import { DarService } from '../services/dar.service';

@Component({
  selector: 'app-my-dars',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './my-dars.component.html'
})
export class MyDarsComponent implements OnInit {
  dars: Dar[] = [];
  selectedStatus: DarStatus | null = null;
  pageSize = DAR_CONFIG.DEFAULT_PAGE_SIZE;
  
  // Expose enums to template
  DarStatus = DarStatus;
  
  constructor(private darService: DarService) {}
  
  ngOnInit(): void {
    this.loadDars();
  }
  
  loadDars(): void {
    this.darService.getMyDars(
      this.selectedStatus || undefined,
      0,
      this.pageSize
    ).subscribe({
      next: (response) => {
        this.dars = response.content;
      },
      error: () => {
        alert(DAR_ERROR_MESSAGES.LOAD_FAILED);
      }
    });
  }
  
  filterByStatus(status: DarStatus): void {
    this.selectedStatus = status;
    this.loadDars();
  }
}
```

**Benefits**:
- ✅ Clear imports with categorization
- ✅ Type safety with enums
- ✅ Reusable constants
- ✅ No magic strings/numbers

---

## 📊 Before vs After Comparison

### Before (Not Ideal)
```typescript
// dar.service.ts (500+ lines!)
export interface Member { ... }
export interface Dar { ... }
export interface Tour { ... }
// ... 10+ more interfaces

@Injectable()
export class DarService {
  private defaultPageSize = 20;  // ❌ Magic number
  
  getDars(status?: "pending" | "active"): Observable<Dar[]> {  // ❌ String union
    // ...
  }
}
```

**Problems**:
- 500+ line service file
- 10+ interfaces mixed with logic
- Magic numbers scattered
- String unions instead of enums
- Hard to find types
- Difficult to reuse types

### After (Best Practice)
```typescript
// models/dar.model.ts (30 lines)
export interface Dar { ... }

// enums/dar-status.enum.ts (10 lines)
export enum DarStatus { ... }

// constants/dar.constants.ts (50 lines)
export const DAR_CONFIG = { ... }

// services/dar.service.ts (200 lines)
import { Dar } from '../models';
import { DarStatus } from '../enums';
import { DAR_CONFIG } from '../constants';

@Injectable()
export class DarService {
  getDars(status?: DarStatus): Observable<Dar[]> {  // ✅ Enum
    const pageSize = DAR_CONFIG.DEFAULT_PAGE_SIZE;  // ✅ Constant
    // ...
  }
}
```

**Benefits**:
- Clean, focused files
- Easy to find types
- Type safety with enums
- Reusable constants
- Better maintainability
- Professional structure

---

## 🎯 Migration Checklist

For each feature, complete these steps:

- [ ] Create `models/` folder
- [ ] Create `enums/` folder
- [ ] Create `constants/` folder
- [ ] Extract interfaces to model files
- [ ] Replace string unions with enums
- [ ] Extract magic numbers to constants
- [ ] Create barrel exports (index.ts)
- [ ] Update service imports
- [ ] Update component imports
- [ ] Update tests
- [ ] Verify build
- [ ] Update documentation

---

## 📚 Summary

### What We Achieved
1. ✅ **Separated types** - Models in own files
2. ✅ **Added enums** - Type-safe status values
3. ✅ **Extracted constants** - No magic numbers
4. ✅ **Cleaner services** - Focus on logic only
5. ✅ **Better reusability** - Import from models/
6. ✅ **Improved maintainability** - Clear file structure

### File Count Comparison
- **Before**: 4 files (pages + service)
- **After**: 16 files (pages + service + models + enums + constants)

**More files, but each file has a clear purpose and is easier to work with!**

---

**Next Feature to Refactor**: Notifications or Payments  
**Estimated Time**: 2-4 hours per feature  
**Long-term Benefit**: Much easier to maintain and scale

---

**Version**: 1.0.0  
**Last Updated**: February 2025