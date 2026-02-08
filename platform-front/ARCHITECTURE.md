# Advanced Angular Application Architecture

## 📋 Table of Contents

1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Core Concepts](#core-concepts)
4. [Module Organization](#module-organization)
5. [Separation of Concerns](#separation-of-concerns)
6. [State Management](#state-management)
7. [Routing Strategy](#routing-strategy)
8. [Best Practices](#best-practices)
9. [Development Guidelines](#development-guidelines)
10. [Testing Strategy](#testing-strategy)

---

## 🎯 Overview

This Angular application follows an **advanced, scalable architecture** designed for large enterprise projects. It implements industry best practices including:

- **High separation of concerns** with distinct layers
- **Modular architecture** with lazy loading
- **Clean code principles** (SOLID, DRY, KISS)
- **Observable-based state management**
- **Comprehensive error handling**
- **Type-safe development**
- **Smart vs Presentational component pattern**

### Key Architectural Patterns

- **Core-Shared-Features Pattern**: Separates singleton services, reusable components, and feature modules
- **Service Layer Pattern**: Separates API calls, business logic, and state management
- **Guard-Interceptor Pattern**: Centralized authentication and HTTP handling
- **Smart/Dumb Components**: Container components handle logic, presentational components handle UI

---

## 📁 Project Structure

```
src/
├── app/
│   ├── core/                          # Singleton services, guards, interceptors (imported once)
│   │   ├── guards/                    # Route guards
│   │   │   ├── auth.guard.ts         # Authentication guard
│   │   │   └── role.guard.ts         # Role-based access control guard
│   │   ├── interceptors/             # HTTP interceptors
│   │   │   ├── auth.interceptor.ts   # Adds auth token to requests
│   │   │   ├── error.interceptor.ts  # Centralized error handling
│   │   │   └── loading.interceptor.ts # Loading state management
│   │   ├── services/                 # Core singleton services
│   │   │   ├── auth/
│   │   │   │   └── auth.service.ts   # Authentication & authorization
│   │   │   ├── storage/
│   │   │   │   └── storage.service.ts # Local/session storage wrapper
│   │   │   ├── logger/
│   │   │   │   └── logger.service.ts # Application logging
│   │   │   ├── notification/
│   │   │   │   └── notification.service.ts # Toast notifications
│   │   │   └── loading/
│   │   │       └── loading.service.ts # Loading state
│   │   ├── models/                   # Core interfaces and types
│   │   ├── enums/                    # Application-wide enums
│   │   ├── constants/                # Application constants
│   │   ├── utils/                    # Utility functions
│   │   └── core.module.ts           # Core module (import once in AppModule)
│   │
│   ├── shared/                       # Reusable components, directives, pipes
│   │   ├── components/              # Shared UI components
│   │   │   ├── loader/
│   │   │   ├── modal/
│   │   │   ├── confirm-dialog/
│   │   │   ├── toast/
│   │   │   ├── pagination/
│   │   │   ├── data-table/
│   │   │   ├── search-bar/
│   │   │   ├── breadcrumb/
│   │   │   ├── card/
│   │   │   ├── button/
│   │   │   ├── input/
│   │   │   ├── select/
│   │   │   └── date-picker/
│   │   ├── directives/              # Custom directives
│   │   │   ├── highlight.directive.ts
│   │   │   ├── click-outside.directive.ts
│   │   │   ├── lazy-load-image.directive.ts
│   │   │   └── permission.directive.ts
│   │   ├── pipes/                   # Custom pipes
│   │   │   ├── safe-html.pipe.ts
│   │   │   ├── truncate.pipe.ts
│   │   │   ├── time-ago.pipe.ts
│   │   │   ├── filter.pipe.ts
│   │   │   └── sort.pipe.ts
│   │   ├── models/                  # Shared interfaces
│   │   ├── validators/              # Custom validators
│   │   └── shared.module.ts        # Shared module (import in feature modules)
│   │
│   ├── features/                    # Feature modules (lazy-loaded)
│   │   ├── auth/                   # Authentication feature
│   │   │   ├── pages/
│   │   │   │   ├── login/
│   │   │   │   ├── register/
│   │   │   │   ├── forgot-password/
│   │   │   │   └── reset-password/
│   │   │   ├── services/
│   │   │   ├── models/
│   │   │   ├── auth-routing.module.ts
│   │   │   └── auth.module.ts
│   │   │
│   │   ├── dashboard/              # Dashboard feature
│   │   │   ├── pages/
│   │   │   │   └── dashboard-home/
│   │   │   ├── components/
│   │   │   ├── services/
│   │   │   ├── models/
│   │   │   ├── dashboard-routing.module.ts
│   │   │   └── dashboard.module.ts
│   │   │
│   │   ├── user-management/        # User management feature
│   │   │   ├── pages/
│   │   │   │   ├── user-list/
│   │   │   │   ├── user-detail/
│   │   │   │   └── user-create/
│   │   │   ├── components/
│   │   │   ├── services/
│   │   │   ├── models/
│   │   │   ├── user-management-routing.module.ts
│   │   │   └── user-management.module.ts
│   │   │
│   │   ├── products/               # Products feature
│   │   └── settings/               # Settings feature
│   │
│   ├── layout/                     # Layout components
│   │   ├── header/
│   │   ├── footer/
│   │   ├── sidebar/
│   │   └── navigation/
│   │
│   ├── app.component.ts           # Root component
│   ├── app.config.ts              # Application configuration
│   └── app.routes.ts              # Root routing configuration
│
├── environments/                   # Environment configurations
│   ├── environment.ts             # Development environment
│   └── environment.prod.ts        # Production environment
│
└── assets/                        # Static assets
    ├── images/
    ├── icons/
    ├── fonts/
    └── i18n/                      # Internationalization files
```

---

## 🏗️ Core Concepts

### 1. Core Module

The **Core Module** contains singleton services that should be instantiated only once in the application lifecycle.

**Rules:**
- Import **ONLY** in `AppModule`
- Contains guards, interceptors, and singleton services
- Uses constructor check to prevent re-import
- No components declared here

**Example:**
```typescript
@NgModule({
  providers: [
    AuthService,
    StorageService,
    LoggerService,
    // ... other singleton services
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded!');
    }
  }
}
```

### 2. Shared Module

The **Shared Module** contains reusable components, directives, and pipes used across multiple features.

**Rules:**
- Can be imported by any feature module
- Exports common Angular modules (CommonModule, FormsModule, etc.)
- Contains NO services (use `providedIn: 'root'` instead)
- All declarations should be exported

**Example:**
```typescript
@NgModule({
  declarations: [
    LoaderComponent,
    ModalComponent,
    // ... other shared components
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
  ],
  exports: [
    CommonModule,
    ReactiveFormsModule,
    LoaderComponent,
    ModalComponent,
    // ... export everything that other modules need
  ]
})
export class SharedModule { }
```

### 3. Feature Modules

**Feature Modules** are self-contained modules representing a specific application feature.

**Rules:**
- Lazy-loaded for better performance
- Import `SharedModule` for common functionality
- Should NOT import `CoreModule`
- Organized by feature domain (not by file type)

**Structure:**
```
feature-name/
├── pages/                 # Smart/Container components
├── components/            # Dumb/Presentational components
├── services/             # Feature-specific services
├── models/               # Feature-specific interfaces
├── guards/               # Feature-specific guards (if any)
├── feature-routing.module.ts
└── feature.module.ts
```

---

## 🔄 Separation of Concerns

### Service Layer Architecture

Services are organized into distinct layers:

#### 1. **API Services** (Data Access Layer)
- Handle HTTP requests
- No business logic
- Return observables
- Located in `core/services/api/`

```typescript
@Injectable({ providedIn: 'root' })
export class UserApiService {
  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }
}
```

#### 2. **Business Services** (Business Logic Layer)
- Implement business rules
- Transform data
- Coordinate between API and state
- Located in feature modules

```typescript
@Injectable()
export class UserBusinessService {
  constructor(
    private userApi: UserApiService,
    private userState: UserStateService
  ) {}

  loadUsers(): Observable<User[]> {
    return this.userApi.getUsers().pipe(
      tap(users => this.userState.setUsers(users)),
      map(users => this.filterActiveUsers(users))
    );
  }

  private filterActiveUsers(users: User[]): User[] {
    return users.filter(u => u.isActive);
  }
}
```

#### 3. **State Services** (State Management Layer)
- Manage component/feature state
- Use BehaviorSubject for reactive state
- Provide observables for components

```typescript
@Injectable()
export class UserStateService {
  private usersSubject = new BehaviorSubject<User[]>([]);
  public users$ = this.usersSubject.asObservable();

  setUsers(users: User[]): void {
    this.usersSubject.next(users);
  }

  getUsers(): User[] {
    return this.usersSubject.value;
  }
}
```

### Component Architecture

#### Smart Components (Containers)
- Handle business logic
- Subscribe to services
- Manage state
- Located in `pages/` directory

```typescript
@Component({
  selector: 'app-user-list-page',
  template: `
    <app-user-list
      [users]="users$ | async"
      (userSelected)="onUserSelected($event)"
      (deleteUser)="onDeleteUser($event)">
    </app-user-list>
  `
})
export class UserListPageComponent {
  users$ = this.userService.users$;

  constructor(private userService: UserBusinessService) {}

  onUserSelected(user: User): void {
    // Handle selection logic
  }

  onDeleteUser(userId: string): void {
    // Handle deletion logic
  }
}
```

#### Dumb Components (Presentational)
- Pure UI components
- Receive data via `@Input()`
- Emit events via `@Output()`
- No service dependencies
- Located in `components/` directory

```typescript
@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html'
})
export class UserListComponent {
  @Input() users: User[] = [];
  @Output() userSelected = new EventEmitter<User>();
  @Output() deleteUser = new EventEmitter<string>();

  onSelect(user: User): void {
    this.userSelected.emit(user);
  }

  onDelete(userId: string): void {
    this.deleteUser.emit(userId);
  }
}
```

---

## 🛡️ State Management

### Observable-Based State Pattern

This architecture uses **RxJS BehaviorSubjects** for state management:

**Advantages:**
- No external dependencies
- Simple and lightweight
- Full TypeScript support
- Easy to debug

**Example:**
```typescript
@Injectable({ providedIn: 'root' })
export class GlobalStateService {
  private stateSubject = new BehaviorSubject<AppState>(initialState);
  public state$ = this.stateSubject.asObservable();

  get currentState(): AppState {
    return this.stateSubject.value;
  }

  updateState(partial: Partial<AppState>): void {
    const newState = {
      ...this.currentState,
      ...partial
    };
    this.stateSubject.next(newState);
  }
}
```

### Alternative: NgRx (for large applications)

For very large applications, consider **NgRx** for more structured state management:
- Centralized store
- Actions and reducers
- DevTools support
- Time-travel debugging

---

## 🛣️ Routing Strategy

### Lazy Loading

All feature modules are lazy-loaded:

```typescript
// app.routes.ts
export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module')
      .then(m => m.AuthModule)
  },
  {
    path: 'dashboard',
    loadChildren: () => import('./features/dashboard/dashboard.module')
      .then(m => m.DashboardModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'users',
    loadChildren: () => import('./features/user-management/user-management.module')
      .then(m => m.UserManagementModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMIN'] }
  }
];
```

### Route Guards

#### AuthGuard
Protects routes requiring authentication:
```typescript
{
  path: 'dashboard',
  component: DashboardComponent,
  canActivate: [AuthGuard]
}
```

#### RoleGuard
Protects routes based on user roles/permissions:
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

---

## ✅ Best Practices

### 1. **Dependency Injection**
- Use constructor injection
- Inject interfaces when possible
- Keep constructors simple

```typescript
constructor(
  private authService: AuthService,
  private logger: LoggerService
) {}
```

### 2. **Observables & Subscriptions**
- Use async pipe in templates (auto-unsubscribe)
- Use `takeUntil` for manual subscriptions
- Avoid nested subscriptions

```typescript
// Good
export class MyComponent implements OnDestroy {
  private destroy$ = new Subject<void>();
  
  ngOnInit(): void {
    this.dataService.getData()
      .pipe(takeUntil(this.destroy$))
      .subscribe(data => this.data = data);
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

### 3. **Error Handling**
- Use centralized error interceptor
- Provide user-friendly error messages
- Log errors appropriately

```typescript
this.userService.getUser(id).pipe(
  catchError(error => {
    this.notificationService.error('Failed to load user');
    return of(null);
  })
);
```

### 4. **Type Safety**
- Always use TypeScript interfaces
- Enable strict mode in tsconfig.json
- Avoid `any` type

```typescript
interface User {
  id: string;
  name: string;
  email: string;
  roles: UserRole[];
}
```

### 5. **Naming Conventions**
- Components: `user-list.component.ts`
- Services: `user.service.ts`
- Interfaces: `user.interface.ts` or `user.model.ts`
- Enums: `user-role.enum.ts`
- Constants: `app.constants.ts`

### 6. **Performance Optimization**
- Use `OnPush` change detection
- Lazy load feature modules
- Use trackBy in *ngFor
- Implement virtual scrolling for large lists

```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserListComponent {
  trackByUserId(index: number, user: User): string {
    return user.id;
  }
}
```

---

## 👨‍💻 Development Guidelines

### Code Organization

1. **One component per file**
2. **Keep files small** (< 400 lines)
3. **Extract complex logic** to services
4. **Use barrel exports** for clean imports

```typescript
// index.ts (barrel export)
export * from './user.service';
export * from './user.model';
export * from './user.component';
```

### Component Guidelines

- **Lifecycle hooks order:**
  1. Constructor
  2. Lifecycle hooks (in order of execution)
  3. Public methods
  4. Private methods

- **Keep templates simple:**
  - Move complex logic to component class
  - Use pipes for transformations
  - Avoid logic in templates

### Service Guidelines

- **Single Responsibility:** One service, one purpose
- **Stateless when possible**
- **Return Observables** for async operations
- **Use providedIn: 'root'** for most services

---

## 🧪 Testing Strategy

### Unit Tests

#### Services
```typescript
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should login user', () => {
    const mockUser = { email: 'test@test.com', token: 'abc' };
    
    service.login('test@test.com', 'password').subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne('/api/auth/login');
    req.flush(mockUser);
  });
});
```

#### Components
```typescript
describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserListComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
  });

  it('should display users', () => {
    component.users = mockUsers;
    fixture.detectChanges();
    
    const userElements = fixture.nativeElement.querySelectorAll('.user-item');
    expect(userElements.length).toBe(mockUsers.length);
  });
});
```

### Integration Tests

Test feature modules as a whole:
- Test routing
- Test guards
- Test interceptors
- Test component interactions

### E2E Tests

Use Cypress or Protractor for end-to-end testing:
- Test critical user flows
- Test authentication flow
- Test CRUD operations

---

## 📚 Additional Resources

### Recommended Reading
- [Angular Style Guide](https://angular.io/guide/styleguide)
- [RxJS Documentation](https://rxjs.dev/)
- [Clean Code in TypeScript](https://github.com/labs42io/clean-code-typescript)

### Useful Tools
- **Angular CLI:** Project scaffolding and building
- **TSLint/ESLint:** Code linting
- **Prettier:** Code formatting
- **Husky:** Git hooks
- **Compodoc:** Documentation generation

### VS Code Extensions
- Angular Language Service
- Prettier
- ESLint
- Angular Snippets
- GitLens

---

## 🚀 Getting Started

### Installation
```bash
npm install
```

### Development Server
```bash
ng serve
```

### Build
```bash
ng build --configuration=production
```

### Run Tests
```bash
ng test
ng e2e
```

### Generate Components
```bash
# Generate feature module
ng generate module features/feature-name --routing

# Generate smart component
ng generate component features/feature-name/pages/page-name

# Generate dumb component
ng generate component features/feature-name/components/component-name

# Generate service
ng generate service features/feature-name/services/service-name
```

---

## 📝 Version History

- **v1.0.0** - Initial architecture setup with core, shared, and feature modules
- Advanced interceptors and guards
- Comprehensive logging and error handling
- Observable-based state management

---

## 👥 Contributing

1. Follow the established architecture patterns
2. Write unit tests for all new code
3. Update documentation when adding features
4. Follow Angular style guide
5. Use meaningful commit messages

---

## 📄 License

MIT License - feel free to use this architecture for your projects!