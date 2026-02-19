# Services Integration Guide

This document provides a comprehensive guide for integrating and using the TonTin platform services in the Angular frontend.

## Table of Contents

1. [Overview](#overview)
2. [Available Services](#available-services)
3. [Service Architecture](#service-architecture)
4. [Integration Examples](#integration-examples)
5. [Best Practices](#best-practices)
6. [API Endpoints](#api-endpoints)
7. [Error Handling](#error-handling)
8. [Testing](#testing)

---

## Overview

The TonTin platform provides a set of core services that handle all backend API communication. These services are built using Angular's dependency injection system and follow reactive programming patterns using RxJS.

All services are located in `src/app/core/services/` and can be imported using the barrel export:

```typescript
import { DarService, PaymentService, NotificationService, UserService } from '@core/services';
```

---

## Available Services

### 1. DarService

**Purpose:** Manages all Dâr-related operations (create, read, update, delete, join, leave).

**Key Features:**
- CRUD operations for Dârs
- Member management
- Tour/cycle management
- Transaction history
- Messaging within Dârs
- Invite code generation

**Location:** `src/app/core/services/dar.service.ts`

### 2. PaymentService

**Purpose:** Handles all payment and contribution operations.

**Key Features:**
- Payment processing
- Fee calculation
- Payment method management
- Wallet operations
- Payment history
- Receipt generation

**Location:** `src/app/core/services/payment.service.ts`

### 3. NotificationService

**Purpose:** Manages user notifications and real-time updates.

**Key Features:**
- Notification CRUD operations
- Read/unread status management
- Notification preferences
- Real-time polling
- Push notification support

**Location:** `src/app/core/services/notification.service.ts`

### 4. UserService

**Purpose:** Handles user profile, settings, and trust score management.

**Key Features:**
- User profile management
- Avatar upload
- Password change
- Two-factor authentication
- Trust score and rankings
- KYC verification
- User search and follow

**Location:** `src/app/core/services/user.service.ts`

---

## Service Architecture

### State Management

Each service uses BehaviorSubjects to maintain local state and provide reactive streams:

```typescript
// Example from DarService
private darsSubject = new BehaviorSubject<Dar[]>([]);
public dars$ = this.darsSubject.asObservable();
```

### HTTP Communication

All services use Angular's `HttpClient` and return Observables:

```typescript
getMyDars(status?: string, page: number = 0, size: number = 20): Observable<PaginatedResponse<Dar>> {
  // ...
  return this.http.get<PaginatedResponse<Dar>>(`${this.apiUrl}/my-dars`, { params });
}
```

### Error Handling

Services return HTTP errors as-is. Components should handle errors appropriately:

```typescript
this.darService.getMyDars().subscribe({
  next: (response) => { /* Handle success */ },
  error: (err) => { /* Handle error */ }
});
```

---

## Integration Examples

### Example 1: Loading and Displaying Dârs

**Component:**
```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, takeUntil, finalize } from 'rxjs';
import { DarService, Dar } from '@core/services';

@Component({
  selector: 'app-my-dars',
  templateUrl: './my-dars.component.html'
})
export class MyDarsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  dars: Dar[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(private darService: DarService) {}

  ngOnInit(): void {
    this.loadDars();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDars(): void {
    this.isLoading = true;
    this.error = null;

    this.darService.getMyDars('active', 0, 20)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (response) => {
          this.dars = response.content;
        },
        error: (err) => {
          this.error = 'Failed to load Dârs';
          console.error(err);
        }
      });
  }
}
```

### Example 2: Making a Payment

**Component:**
```typescript
import { Component } from '@angular/core';
import { PaymentService, MakePaymentRequest } from '@core/services';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pay-contribution',
  templateUrl: './pay-contribution.component.html'
})
export class PayContributionComponent {
  isSubmitting = false;
  error: string | null = null;

  constructor(
    private paymentService: PaymentService,
    private router: Router
  ) {}

  makePayment(darId: number, amount: number, paymentMethodId: number): void {
    this.isSubmitting = true;
    this.error = null;

    const request: MakePaymentRequest = {
      darId,
      amount,
      paymentMethodId,
      cycleNumber: 1
    };

    this.paymentService.makePayment(request)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (payment) => {
          console.log('Payment successful:', payment);
          this.router.navigate(['/dashboard/client/dar', darId]);
        },
        error: (err) => {
          this.error = err.error?.message || 'Payment failed';
          console.error(err);
        }
      });
  }
}
```

### Example 3: Managing Notifications

**Component:**
```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { NotificationService, Notification } from '@core/services';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html'
})
export class NotificationsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  notifications: Notification[] = [];
  unreadCount = 0;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    // Load notifications
    this.loadNotifications();
    
    // Subscribe to unread count updates
    this.notificationService.unreadCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => this.unreadCount = count);
    
    // Start polling for new notifications
    this.notificationService.startPolling();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.notificationService.stopPolling();
  }

  loadNotifications(): void {
    this.notificationService.getNotifications(0, 20)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.notifications = response.content;
        },
        error: (err) => console.error('Failed to load notifications:', err)
      });
  }

  markAsRead(notificationId: number): void {
    this.notificationService.markAsRead(notificationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const notif = this.notifications.find(n => n.id === notificationId);
          if (notif) notif.isRead = true;
        },
        error: (err) => console.error('Failed to mark as read:', err)
      });
  }
}
```

### Example 4: User Profile Management

**Component:**
```typescript
import { Component, OnInit } from '@angular/core';
import { UserService, User, UpdateProfileRequest } from '@core/services';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  profileForm: FormGroup;
  isSubmitting = false;

  constructor(
    private userService: UserService,
    private fb: FormBuilder
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phoneNumber: [''],
      bio: ['']
    });
  }

  ngOnInit(): void {
    // Subscribe to current user
    this.userService.currentUser$.subscribe(user => {
      this.user = user;
      if (user) {
        this.profileForm.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          phoneNumber: user.phoneNumber,
          bio: user.bio
        });
      }
    });

    // Load current user
    this.userService.getCurrentUser().subscribe();
  }

  saveProfile(): void {
    if (this.profileForm.invalid) return;

    this.isSubmitting = true;
    const request: UpdateProfileRequest = this.profileForm.value;

    this.userService.updateProfile(request)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (user) => {
          console.log('Profile updated:', user);
          alert('Profile saved successfully!');
        },
        error: (err) => {
          console.error('Failed to update profile:', err);
          alert('Failed to save profile. Please try again.');
        }
      });
  }

  uploadAvatar(file: File): void {
    this.userService.uploadAvatar(file)
      .subscribe({
        next: (response) => {
          console.log('Avatar uploaded:', response.avatarUrl);
        },
        error: (err) => console.error('Failed to upload avatar:', err)
      });
  }
}
```

---

## Best Practices

### 1. Always Unsubscribe

Use `takeUntil` pattern to prevent memory leaks:

```typescript
private destroy$ = new Subject<void>();

ngOnInit() {
  this.service.getData()
    .pipe(takeUntil(this.destroy$))
    .subscribe(/* ... */);
}

ngOnDestroy() {
  this.destroy$.next();
  this.destroy$.complete();
}
```

### 2. Handle Loading States

Always provide feedback to users:

```typescript
isLoading = false;

loadData() {
  this.isLoading = true;
  this.service.getData()
    .pipe(finalize(() => this.isLoading = false))
    .subscribe(/* ... */);
}
```

### 3. Handle Errors Gracefully

Provide meaningful error messages:

```typescript
error: string | null = null;

loadData() {
  this.error = null;
  this.service.getData().subscribe({
    next: (data) => { /* ... */ },
    error: (err) => {
      this.error = err.error?.message || 'An error occurred';
      console.error('Error loading data:', err);
    }
  });
}
```

### 4. Use Reactive Streams

Subscribe to service observables for automatic updates:

```typescript
ngOnInit() {
  // Direct subscription to reactive stream
  this.darService.dars$.subscribe(dars => {
    this.dars = dars;
  });

  // Trigger data load
  this.darService.getMyDars().subscribe();
}
```

### 5. Cache Clearing

Clear service caches on logout:

```typescript
onLogout() {
  this.darService.clearCache();
  this.paymentService.clearCache();
  this.notificationService.clearCache();
  this.userService.clearCache();
  // Proceed with logout...
}
```

### 6. Type Safety

Always use TypeScript interfaces:

```typescript
import { Dar, CreateDarRequest } from '@core/services';

createDar(data: CreateDarRequest): void {
  this.darService.createDar(data).subscribe(/* ... */);
}
```

---

## API Endpoints

### DarService Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/dars/my-dars` | Get user's Dârs |
| GET | `/api/v1/dars/{id}` | Get Dâr details |
| POST | `/api/v1/dars` | Create new Dâr |
| PUT | `/api/v1/dars/{id}` | Update Dâr |
| DELETE | `/api/v1/dars/{id}` | Delete Dâr |
| POST | `/api/v1/dars/join` | Join a Dâr |
| POST | `/api/v1/dars/{id}/leave` | Leave a Dâr |
| GET | `/api/v1/dars/{id}/members` | Get Dâr members |
| POST | `/api/v1/dars/{id}/invite` | Invite member |
| GET | `/api/v1/dars/{id}/tours` | Get payment schedule |
| GET | `/api/v1/dars/{id}/messages` | Get Dâr messages |
| POST | `/api/v1/dars/{id}/messages` | Send message |

### PaymentService Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/payments/summary` | Get payment summary |
| GET | `/api/v1/payments` | Get payment history |
| POST | `/api/v1/payments/contribute` | Make contribution |
| GET | `/api/v1/payments/calculate-fees` | Calculate fees |
| GET | `/api/v1/payment-methods` | Get payment methods |
| POST | `/api/v1/payment-methods` | Add payment method |
| DELETE | `/api/v1/payment-methods/{id}` | Remove payment method |
| GET | `/api/v1/wallet` | Get wallet |
| POST | `/api/v1/wallet/deposit` | Deposit to wallet |
| POST | `/api/v1/wallet/withdraw` | Withdraw from wallet |

### NotificationService Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/notifications` | Get notifications |
| GET | `/api/v1/notifications/summary` | Get summary |
| GET | `/api/v1/notifications/unread-count` | Get unread count |
| PUT | `/api/v1/notifications/{id}/read` | Mark as read |
| PUT | `/api/v1/notifications/mark-all-read` | Mark all as read |
| DELETE | `/api/v1/notifications/{id}` | Delete notification |
| PUT | `/api/v1/notifications/{id}/archive` | Archive notification |
| GET | `/api/v1/notification-preferences` | Get preferences |
| PUT | `/api/v1/notification-preferences` | Update preferences |

### UserService Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/me` | Get current user |
| GET | `/api/v1/profile/me` | Get full profile |
| PUT | `/api/v1/profile` | Update profile |
| POST | `/api/v1/profile/avatar` | Upload avatar |
| PUT | `/api/v1/profile/change-password` | Change password |
| GET | `/api/v1/profile/settings` | Get settings |
| PUT | `/api/v1/profile/settings` | Update settings |
| GET | `/api/v1/trust/me` | Get trust score |
| GET | `/api/v1/rankings` | Get trust rankings |
| GET | `/api/v1/users/search` | Search users |

---

## Error Handling

### Standard Error Format

All services expect errors in this format:

```typescript
{
  error: {
    message: string,
    code?: string,
    details?: any
  }
}
```

### Component Error Handling

```typescript
this.service.getData().subscribe({
  error: (err) => {
    // Extract error message
    const message = err.error?.message || 'An error occurred';
    
    // Log for debugging
    console.error('Service error:', err);
    
    // Display to user
    this.error = message;
    
    // Or use a toast service
    // this.toastService.error(message);
  }
});
```

### HTTP Status Codes

| Code | Meaning | Handling |
|------|---------|----------|
| 400 | Bad Request | Show validation errors |
| 401 | Unauthorized | Redirect to login |
| 403 | Forbidden | Show access denied |
| 404 | Not Found | Show not found message |
| 500 | Server Error | Show generic error |

---

## Testing

### Service Testing Example

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DarService } from './dar.service';

describe('DarService', () => {
  let service: DarService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DarService]
    });
    service = TestBed.inject(DarService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch Dârs', () => {
    const mockDars = [
      { id: 1, name: 'Test Dâr', /* ... */ }
    ];

    service.getMyDars().subscribe(response => {
      expect(response.content.length).toBe(1);
      expect(response.content[0].name).toBe('Test Dâr');
    });

    const req = httpMock.expectOne(`${service['apiUrl']}/my-dars?page=0&size=20`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: mockDars, totalElements: 1 });
  });
});
```

### Component Testing Example

```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MyDarsComponent } from './my-dars.component';
import { DarService } from '@core/services';

describe('MyDarsComponent', () => {
  let component: MyDarsComponent;
  let fixture: ComponentFixture<MyDarsComponent>;
  let darService: jasmine.SpyObj<DarService>;

  beforeEach(async () => {
    const darServiceSpy = jasmine.createSpyObj('DarService', ['getMyDars']);

    await TestBed.configureTestingModule({
      declarations: [MyDarsComponent],
      providers: [
        { provide: DarService, useValue: darServiceSpy }
      ]
    }).compileComponents();

    darService = TestBed.inject(DarService) as jasmine.SpyObj<DarService>;
  });

  it('should load Dârs on init', () => {
    const mockResponse = {
      content: [{ id: 1, name: 'Test Dâr' }],
      totalElements: 1
    };
    darService.getMyDars.and.returnValue(of(mockResponse));

    component.ngOnInit();

    expect(darService.getMyDars).toHaveBeenCalled();
    expect(component.dars.length).toBe(1);
  });

  it('should handle error when loading Dârs', () => {
    darService.getMyDars.and.returnValue(
      throwError(() => new Error('Failed'))
    );

    component.ngOnInit();

    expect(component.error).toBeTruthy();
  });
});
```

---

## Migration Guide

### From Mock Data to Services

**Before:**
```typescript
dars = [
  { id: 1, name: 'Family Savings', /* ... */ }
];
```

**After:**
```typescript
dars: Dar[] = [];

ngOnInit() {
  this.darService.getMyDars().subscribe(
    response => this.dars = response.content
  );
}
```

---

## Troubleshooting

### Issue: CORS Errors

**Solution:** Ensure backend CORS is configured:
```java
@CrossOrigin(origins = "http://localhost:4200")
```

### Issue: 401 Unauthorized

**Solution:** Check if token is properly stored and sent:
```typescript
// Token should be in localStorage
const token = localStorage.getItem('token');
```

### Issue: Data Not Updating

**Solution:** Ensure you're subscribing to the service observable:
```typescript
// Wrong - not subscribed
this.service.getData();

// Correct - subscribed
this.service.getData().subscribe();
```

---

## Next Steps

1. **Implement Real-time Updates:** Add WebSocket/SSE for real-time notifications
2. **Add Caching Layer:** Implement NgRx or similar for global state
3. **Optimize API Calls:** Add request debouncing and caching
4. **Add Offline Support:** Implement service workers for offline functionality
5. **Enhance Error Handling:** Add retry logic and exponential backoff

---

## Support

For issues or questions:
- Check the API documentation: `/docs/api`
- Review the backend logs: `docker compose logs platform-back`
- Contact the development team

**Last Updated:** 2024
**Version:** 1.0.0