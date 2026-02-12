# 🛠️ Development Guide

## Quick Start for Developers

This guide will help you get started with development in this Angular application.

---

## 📋 Table of Contents

1. [Setup](#setup)
2. [Project Structure](#project-structure)
3. [Creating New Features](#creating-new-features)
4. [Component Development](#component-development)
5. [Service Development](#service-development)
6. [Routing](#routing)
7. [State Management](#state-management)
8. [Common Tasks](#common-tasks)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)

---

## 🚀 Setup

### 1. Initial Setup

```bash
# Clone the repository
git clone <repository-url>
cd platform-front-test

# Install dependencies
npm install

# Start development server
npm start
# or
ng serve
```

### 2. Verify Installation

Open `http://localhost:4200` in your browser. You should see the application running.

### 3. Install Recommended VS Code Extensions

- Angular Language Service
- Prettier - Code formatter
- ESLint
- Angular Snippets
- GitLens

---

## 📁 Project Structure

### Module Organization

```
src/app/
├── core/           → Import ONCE in app.config.ts
├── shared/         → Import in feature modules
└── features/       → Lazy-loaded business features
```

### Feature Module Structure

```
feature-name/
├── pages/          → Smart/Container components (have logic)
├── components/     → Dumb/Presentational components (pure UI)
├── services/       → Feature-specific services
├── models/         → Interfaces and types
├── feature-routing.module.ts
└── feature.module.ts
```

---

## 🆕 Creating New Features

### Step 1: Generate Feature Module

```bash
ng generate module features/my-feature --routing
```

### Step 2: Create Module Structure

```bash
cd src/app/features/my-feature
mkdir pages components services models
```

### Step 3: Generate Components

```bash
# Smart component (goes in pages/)
ng g c features/my-feature/pages/my-feature-list

# Dumb component (goes in components/)
ng g c features/my-feature/components/my-feature-card
```

### Step 4: Generate Service

```bash
ng g s features/my-feature/services/my-feature
```

### Step 5: Configure Module

```typescript
// my-feature.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MyFeatureRoutingModule } from './my-feature-routing.module';
import { SharedModule } from '../../shared/shared.module';

// Import your components
import { MyFeatureListComponent } from './pages/my-feature-list/my-feature-list.component';
import { MyFeatureCardComponent } from './components/my-feature-card/my-feature-card.component';

@NgModule({
  declarations: [
    MyFeatureListComponent,
    MyFeatureCardComponent
  ],
  imports: [
    CommonModule,
    MyFeatureRoutingModule,
    SharedModule  // Provides shared components, directives, pipes
  ]
})
export class MyFeatureModule { }
```

### Step 6: Configure Routing

```typescript
// my-feature-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { MyFeatureListComponent } from './pages/my-feature-list/my-feature-list.component';

const routes: Routes = [
  {
    path: '',
    component: MyFeatureListComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MyFeatureRoutingModule { }
```

### Step 7: Add to Main Routes

```typescript
// app.routes.ts
{
  path: 'my-feature',
  loadChildren: () => import('./features/my-feature/my-feature.module')
    .then(m => m.MyFeatureModule),
  canActivate: [AuthGuard]
}
```

---

## 🎨 Component Development

### Smart Component (Container)

**Location**: `pages/`  
**Purpose**: Handle business logic, data fetching, state management

```typescript
// user-list-page.component.ts
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-list-page',
  template: `
    <app-user-list
      [users]="users$ | async"
      [loading]="loading$ | async"
      (userSelected)="onUserSelected($event)"
      (deleteUser)="onDeleteUser($event)">
    </app-user-list>
  `
})
export class UserListPageComponent implements OnInit {
  users$: Observable<User[]>;
  loading$: Observable<boolean>;

  constructor(private userService: UserService) {
    this.users$ = this.userService.users$;
    this.loading$ = this.userService.loading$;
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.loadUsers().subscribe();
  }

  onUserSelected(user: User): void {
    // Navigate to detail page
    this.router.navigate(['/users', user.id]);
  }

  onDeleteUser(userId: string): void {
    this.userService.deleteUser(userId).subscribe({
      next: () => this.loadUsers(),
      error: (error) => console.error('Delete failed', error)
    });
  }
}
```

### Dumb Component (Presentational)

**Location**: `components/`  
**Purpose**: Display UI, emit events, no business logic

```typescript
// user-list.component.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent {
  @Input() users: User[] = [];
  @Input() loading: boolean = false;
  @Output() userSelected = new EventEmitter<User>();
  @Output() deleteUser = new EventEmitter<string>();

  onSelectUser(user: User): void {
    this.userSelected.emit(user);
  }

  onDeleteUser(userId: string): void {
    if (confirm('Are you sure?')) {
      this.deleteUser.emit(userId);
    }
  }

  trackByUserId(index: number, user: User): string {
    return user.id;
  }
}
```

```html
<!-- user-list.component.html -->
<div class="user-list">
  <div *ngIf="loading" class="loading">Loading...</div>
  
  <div *ngIf="!loading && users.length === 0" class="empty">
    No users found
  </div>

  <div class="user-grid">
    <app-user-card
      *ngFor="let user of users; trackBy: trackByUserId"
      [user]="user"
      (click)="onSelectUser(user)"
      (delete)="onDeleteUser(user.id)">
    </app-user-card>
  </div>
</div>
```

---

## 🔧 Service Development

### API Service (Data Access)

```typescript
// user-api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getUser(id: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  createUser(user: Partial<User>): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user);
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

### Business Service (Business Logic)

```typescript
// user.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { UserApiService } from './user-api.service';
import { UserStateService } from './user-state.service';
import { User } from '../models/user.model';
import { NotificationService } from '../../../core/services/notification/notification.service';

@Injectable()
export class UserService {
  users$ = this.userState.users$;
  loading$ = this.userState.loading$;

  constructor(
    private userApi: UserApiService,
    private userState: UserStateService,
    private notificationService: NotificationService
  ) {}

  loadUsers(): Observable<User[]> {
    this.userState.setLoading(true);
    
    return this.userApi.getUsers().pipe(
      map(users => this.filterActiveUsers(users)),
      tap(users => {
        this.userState.setUsers(users);
        this.userState.setLoading(false);
      })
    );
  }

  deleteUser(id: string): Observable<void> {
    return this.userApi.deleteUser(id).pipe(
      tap(() => {
        this.notificationService.success('User deleted successfully');
        this.userState.removeUser(id);
      })
    );
  }

  private filterActiveUsers(users: User[]): User[] {
    return users.filter(u => u.isActive);
  }
}
```

### State Service

```typescript
// user-state.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable()
export class UserStateService {
  private usersSubject = new BehaviorSubject<User[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public users$: Observable<User[]> = this.usersSubject.asObservable();
  public loading$: Observable<boolean> = this.loadingSubject.asObservable();

  setUsers(users: User[]): void {
    this.usersSubject.next(users);
  }

  addUser(user: User): void {
    const current = this.usersSubject.value;
    this.usersSubject.next([...current, user]);
  }

  updateUser(id: string, updates: Partial<User>): void {
    const current = this.usersSubject.value;
    const updated = current.map(u => 
      u.id === id ? { ...u, ...updates } : u
    );
    this.usersSubject.next(updated);
  }

  removeUser(id: string): void {
    const current = this.usersSubject.value;
    const filtered = current.filter(u => u.id !== id);
    this.usersSubject.next(filtered);
  }

  setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  clear(): void {
    this.usersSubject.next([]);
  }
}
```

---

## 🛣️ Routing

### Basic Route

```typescript
{
  path: 'users',
  component: UserListComponent
}
```

### Route with Parameters

```typescript
{
  path: 'users/:id',
  component: UserDetailComponent
}
```

### Protected Route (Auth Required)

```typescript
{
  path: 'admin',
  component: AdminComponent,
  canActivate: [AuthGuard]
}
```

### Protected Route (Role Required)

```typescript
{
  path: 'admin',
  component: AdminComponent,
  canActivate: [AuthGuard, RoleGuard],
  data: {
    roles: ['ADMIN', 'SUPER_ADMIN'],
    permissions: ['USER_MANAGE']
  }
}
```

### Lazy Loaded Module

```typescript
{
  path: 'users',
  loadChildren: () => import('./features/users/users.module')
    .then(m => m.UsersModule)
}
```

---

## 📊 State Management

### Using BehaviorSubject Pattern

```typescript
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

interface AppState {
  user: User | null;
  theme: 'light' | 'dark';
  sidebarOpen: boolean;
}

@Injectable({ providedIn: 'root' })
export class StateService {
  private stateSubject = new BehaviorSubject<AppState>({
    user: null,
    theme: 'light',
    sidebarOpen: true
  });

  public state$ = this.stateSubject.asObservable();

  get currentState(): AppState {
    return this.stateSubject.value;
  }

  updateState(partial: Partial<AppState>): void {
    this.stateSubject.next({
      ...this.currentState,
      ...partial
    });
  }

  setUser(user: User | null): void {
    this.updateState({ user });
  }

  toggleSidebar(): void {
    this.updateState({ 
      sidebarOpen: !this.currentState.sidebarOpen 
    });
  }
}
```

---

## 🎯 Common Tasks

### 1. Add a New API Endpoint

```typescript
// In your API service
createProduct(product: Product): Observable<Product> {
  return this.http.post<Product>(`${this.apiUrl}/products`, product);
}
```

### 2. Add Form Validation

```typescript
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

export class UserFormComponent implements OnInit {
  userForm: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.userForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      const userData = this.userForm.value;
      // Submit data
    }
  }
}
```

### 3. Handle HTTP Errors

```typescript
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

this.userService.getUsers().pipe(
  catchError(error => {
    this.notificationService.error('Failed to load users');
    console.error('Error loading users:', error);
    return throwError(() => error);
  })
).subscribe();
```

### 4. Add Loading Indicator

```typescript
// Component
isLoading = false;

loadData(): void {
  this.isLoading = true;
  this.dataService.getData().subscribe({
    next: (data) => {
      this.data = data;
      this.isLoading = false;
    },
    error: () => {
      this.isLoading = false;
    }
  });
}
```

### 5. Navigate Programmatically

```typescript
import { Router } from '@angular/router';

constructor(private router: Router) {}

navigateToUser(userId: string): void {
  this.router.navigate(['/users', userId]);
}

navigateWithQuery(): void {
  this.router.navigate(['/products'], {
    queryParams: { category: 'electronics', page: 1 }
  });
}
```

---

## ✅ Best Practices

### 1. Always Unsubscribe

```typescript
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export class MyComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.dataService.data$
      .pipe(takeUntil(this.destroy$))
      .subscribe(data => this.data = data);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

### 2. Use Async Pipe (No Manual Subscription)

```typescript
// Component
users$ = this.userService.users$;

// Template
<div *ngFor="let user of users$ | async">
  {{ user.name }}
</div>
```

### 3. Use TrackBy in ngFor

```typescript
// Component
trackByUserId(index: number, user: User): string {
  return user.id;
}

// Template
<div *ngFor="let user of users; trackBy: trackByUserId">
  {{ user.name }}
</div>
```

### 4. Use OnPush Change Detection

```typescript
import { ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserCardComponent {
  @Input() user: User;
}
```

### 5. Create Reusable Interfaces

```typescript
// models/user.model.ts
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateUserDto {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface UpdateUserDto {
  firstName?: string;
  lastName?: string;
  isActive?: boolean;
}
```

---

## 🐛 Troubleshooting

### Module not found

```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Compilation errors

```bash
# Clear Angular cache
rm -rf .angular
ng serve
```

### Port already in use

```bash
# Use different port
ng serve --port 4201
```

### Can't resolve module

Check that:
1. Module is properly exported
2. Module is imported in the consuming module
3. Path is correct

### HTTP requests failing

Check:
1. API URL in environment file
2. CORS configuration on backend
3. Authentication token is being sent
4. Network tab in browser DevTools

---

## 📚 Additional Resources

- [Angular Documentation](https://angular.io/docs)
- [RxJS Documentation](https://rxjs.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Angular Style Guide](https://angular.io/guide/styleguide)

---

## 🆘 Getting Help

1. Check the [ARCHITECTURE.md](./ARCHITECTURE.md) for detailed architecture info
2. Check existing code for examples
3. Search the codebase for similar implementations
4. Ask the team on Slack/Teams
5. Create a GitHub issue

---

**Happy Coding! 🚀**