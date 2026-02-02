# 🚀 Advanced Angular Application

> A production-ready Angular application with enterprise-grade architecture, designed for large-scale projects with high separation of concerns.

[![Angular](https://img.shields.io/badge/Angular-18+-red.svg)](https://angular.io/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0+-blue.svg)](https://www.typescriptlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Development](#development)
- [Building & Deployment](#building--deployment)
- [Testing](#testing)
- [Code Quality](#code-quality)
- [Documentation](#documentation)
- [Contributing](#contributing)

---

## 🎯 Overview

This is an **advanced Angular application** built with best practices for enterprise-level projects. It features a modular architecture with clear separation of concerns, making it scalable, maintainable, and testable.

### What Makes This Architecture Special?

- ✅ **Modular Design**: Core, Shared, and Feature modules with clear boundaries
- ✅ **Lazy Loading**: All feature modules are lazy-loaded for optimal performance
- ✅ **Smart/Dumb Components**: Clear separation between container and presentational components
- ✅ **Service Layer Pattern**: Separated API, business logic, and state management
- ✅ **Centralized Error Handling**: HTTP interceptors for consistent error management
- ✅ **Authentication & Authorization**: Complete auth flow with JWT tokens and role-based access
- ✅ **Type Safety**: Full TypeScript with strict mode enabled
- ✅ **Reactive Programming**: RxJS observables throughout the application
- ✅ **Scalable State Management**: Observable-based state pattern (easily upgradeable to NgRx)

---

## ⭐ Key Features

### Core Infrastructure

- **🔐 Authentication System**
  - JWT-based authentication
  - Token refresh mechanism
  - Auto-login on page reload
  - Secure token storage

- **🛡️ Authorization & Guards**
  - Role-based access control (RBAC)
  - Permission-based access control
  - Route guards (Auth, Role)
  - Protected routes

- **🔄 HTTP Interceptors**
  - Authentication token injection
  - Centralized error handling
  - Loading state management
  - Request/response logging

- **📝 Logging System**
  - Multiple log levels (Debug, Info, Warn, Error)
  - Console output with formatting
  - Log history storage
  - Performance measurement tools

- **💾 Storage Service**
  - Type-safe localStorage/sessionStorage wrapper
  - Automatic JSON serialization
  - Expiration support
  - Quota management

- **🔔 Notification System**
  - Toast notifications
  - Multiple notification types
  - Auto-dismiss functionality
  - Notification queue management

### Shared Components

- Reusable UI components (Loader, Modal, Toast, etc.)
- Custom directives (Highlight, ClickOutside, LazyLoad, etc.)
- Custom pipes (Truncate, TimeAgo, SafeHtml, etc.)
- Form components (Input, Select, DatePicker)
- Data display (DataTable, Pagination)

### Feature Modules

- **Authentication Module**: Login, Register, Password Reset
- **Dashboard Module**: Overview, Analytics, Reports
- **User Management Module**: User CRUD operations
- **Products Module**: Product management
- **Settings Module**: Application settings

---

## 🏗️ Architecture

This application follows a **three-tier architecture**:

### 1. Core Module (Singleton Layer)
- Imported **once** in AppModule
- Contains singleton services, guards, interceptors
- Prevents re-import with constructor check

### 2. Shared Module (Utility Layer)
- Imported in feature modules
- Contains reusable components, directives, pipes
- Exports common Angular modules

### 3. Feature Modules (Business Layer)
- Lazy-loaded modules
- Self-contained business features
- Organized by domain, not file type

```
┌─────────────────────────────────────────┐
│           AppModule (Root)              │
│  ┌─────────────────────────────────┐   │
│  │     CoreModule (Once)           │   │
│  │  - Guards, Interceptors         │   │
│  │  - Singleton Services           │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
              │
              ├─── Feature Module 1 (Lazy)
              │    └── SharedModule
              │
              ├─── Feature Module 2 (Lazy)
              │    └── SharedModule
              │
              └─── Feature Module N (Lazy)
                   └── SharedModule
```

For detailed architecture documentation, see [ARCHITECTURE.md](./ARCHITECTURE.md).

---

## 📁 Project Structure

```
src/app/
├── core/                      # Singleton services (import once)
│   ├── guards/               # Route guards
│   ├── interceptors/         # HTTP interceptors
│   ├── services/             # Core services
│   │   ├── auth/            # Authentication service
│   │   ├── storage/         # Storage service
│   │   ├── logger/          # Logger service
│   │   ├── notification/    # Notification service
│   │   └── loading/         # Loading state service
│   └── core.module.ts
│
├── shared/                   # Reusable components
│   ├── components/          # UI components
│   ├── directives/          # Custom directives
│   ├── pipes/               # Custom pipes
│   └── shared.module.ts
│
├── features/                # Feature modules (lazy-loaded)
│   ├── auth/               # Authentication feature
│   ├── dashboard/          # Dashboard feature
│   ├── user-management/    # User management
│   ├── products/           # Products feature
│   └── settings/           # Settings feature
│
└── layout/                 # Layout components
    ├── header/
    ├── footer/
    ├── sidebar/
    └── navigation/
```

---

## 🚀 Getting Started

### Prerequisites

- **Node.js**: v18.x or higher
- **npm**: v9.x or higher
- **Angular CLI**: v18.x or higher

```bash
node --version   # Should be v18+
npm --version    # Should be v9+
ng version       # Should be v18+
```

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd platform-front-test
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment**
   - Update `src/environments/environment.ts` for development
   - Update `src/environments/environment.prod.ts` for production

4. **Start development server**
   ```bash
   ng serve
   ```

5. **Open in browser**
   ```
   http://localhost:4200
   ```

---

## 💻 Development

### Development Server

```bash
ng serve
# or with specific configuration
ng serve --configuration=development
```

Navigate to `http://localhost:4200/`. The app will automatically reload if you change any source files.

### Generate Components

```bash
# Generate a new feature module
ng generate module features/feature-name --routing

# Generate a smart component (container)
ng generate component features/feature-name/pages/page-name

# Generate a dumb component (presentational)
ng generate component features/feature-name/components/component-name

# Generate a service
ng generate service features/feature-name/services/service-name

# Generate a guard
ng generate guard core/guards/guard-name

# Generate a pipe
ng generate pipe shared/pipes/pipe-name

# Generate a directive
ng generate directive shared/directives/directive-name
```

### Code Scaffolding

Use Angular CLI to generate new components:

```bash
# Component
ng g c features/my-feature/pages/my-page

# Service
ng g s features/my-feature/services/my-service

# Interface
ng g interface core/models/my-model

# Enum
ng g enum core/enums/my-enum

# Guard
ng g guard core/guards/my-guard

# Pipe
ng g pipe shared/pipes/my-pipe

# Directive
ng g directive shared/directives/my-directive
```

---

## 🏗️ Building & Deployment

### Build for Production

```bash
npm run build
# or
ng build --configuration=production
```

The build artifacts will be stored in the `dist/` directory.

### Build Configurations

- **Development**: `ng build`
- **Production**: `ng build --configuration=production`
- **Staging**: `ng build --configuration=staging` (configure in angular.json)

### Deployment

#### Deploy to Firebase

```bash
npm install -g firebase-tools
firebase login
firebase init
firebase deploy
```

#### Deploy to Netlify

```bash
# Install Netlify CLI
npm install -g netlify-cli

# Deploy
netlify deploy --prod --dir=dist/advanced-app
```

#### Deploy to AWS S3

```bash
aws s3 sync dist/advanced-app s3://your-bucket-name --delete
```

---

## 🧪 Testing

### Unit Tests

```bash
npm run test
# or
ng test
```

Run unit tests via [Karma](https://karma-runner.github.io).

### Run Tests with Coverage

```bash
ng test --code-coverage
```

Coverage reports will be generated in the `coverage/` directory.

### End-to-End Tests

```bash
npm run e2e
# or
ng e2e
```

### Test Structure

```
feature-name/
├── pages/
│   └── page-name/
│       ├── page-name.component.ts
│       ├── page-name.component.html
│       ├── page-name.component.scss
│       └── page-name.component.spec.ts  ← Unit test
└── services/
    ├── service-name.service.ts
    └── service-name.service.spec.ts      ← Unit test
```

---

## 📊 Code Quality

### Linting

```bash
npm run lint
# or
ng lint
```

Fix linting errors automatically:

```bash
ng lint --fix
```

### Code Formatting

This project uses **Prettier** for code formatting:

```bash
npm run format
```

### Pre-commit Hooks

The project uses **Husky** for Git hooks:

- Pre-commit: Runs linting and formatting
- Pre-push: Runs tests

---

## 📚 Documentation

### Architecture Documentation

See [ARCHITECTURE.md](./ARCHITECTURE.md) for detailed information about:
- Project structure
- Module organization
- Service layer architecture
- Component patterns
- State management
- Routing strategy
- Best practices
- Testing strategy

### Generate Code Documentation

Use **Compodoc** to generate documentation:

```bash
npm install -g @compodoc/compodoc
compodoc -p tsconfig.json -s
```

Open `http://localhost:8080` to view the documentation.

---

## 🔧 Configuration

### Environment Variables

Configure environment-specific settings in:

- `src/environments/environment.ts` (Development)
- `src/environments/environment.prod.ts` (Production)

### TypeScript Configuration

- `tsconfig.json`: Base TypeScript configuration
- `tsconfig.app.json`: Application-specific configuration
- `tsconfig.spec.json`: Test-specific configuration

### Angular Configuration

- `angular.json`: Angular workspace configuration
- Build options, assets, styles, scripts
- Environment file replacements

---

## 🎨 Styling

### SCSS Structure

```
src/styles/
├── _variables.scss    # Colors, fonts, spacing
├── _mixins.scss       # Reusable mixins
├── _functions.scss    # SCSS functions
├── _typography.scss   # Font styles
├── _utilities.scss    # Utility classes
└── styles.scss        # Main style file
```

### Theme Customization

Customize the application theme by modifying:

```scss
// src/styles/_variables.scss
$primary-color: #3f51b5;
$accent-color: #ff4081;
$warn-color: #f44336;
```

---

## 🔐 Security Best Practices

- ✅ JWT tokens stored in HttpOnly cookies (recommended) or localStorage
- ✅ CSRF protection enabled
- ✅ XSS protection via Angular's built-in sanitization
- ✅ Content Security Policy (CSP) headers
- ✅ HTTPS only in production
- ✅ Environment-based API URLs
- ✅ No sensitive data in localStorage
- ✅ Token refresh mechanism
- ✅ Secure password handling

---

## 🚦 Performance Optimization

- ✅ Lazy loading of feature modules
- ✅ OnPush change detection strategy
- ✅ TrackBy functions in ngFor loops
- ✅ Async pipe for automatic subscription management
- ✅ Code splitting and bundling
- ✅ AOT compilation in production
- ✅ Tree shaking
- ✅ Image optimization
- ✅ Service worker (PWA support ready)

---

## 🌍 Internationalization (i18n)

To add internationalization support:

```bash
ng add @angular/localize
```

Configure languages in `angular.json` and create translation files.

---

## 📱 Progressive Web App (PWA)

To add PWA support:

```bash
ng add @angular/pwa
```

This adds:
- Service worker configuration
- Web app manifest
- App icons
- Offline support

---

## 🤝 Contributing

### Contribution Guidelines

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Code Style

- Follow the [Angular Style Guide](https://angular.io/guide/styleguide)
- Use meaningful variable and function names
- Write self-documenting code
- Add comments for complex logic
- Write unit tests for new features
- Update documentation

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
feat: add user profile page
fix: resolve login redirect issue
docs: update architecture documentation
style: format code with prettier
refactor: simplify auth service logic
test: add unit tests for dashboard
chore: update dependencies
```

---

## 📖 Learning Resources

### Angular Resources
- [Official Angular Documentation](https://angular.io/docs)
- [Angular Style Guide](https://angular.io/guide/styleguide)
- [RxJS Documentation](https://rxjs.dev/)

### TypeScript Resources
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Clean Code TypeScript](https://github.com/labs42io/clean-code-typescript)

### Architecture Resources
- [Angular Architecture Patterns](https://angular.io/guide/architecture)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)

---

## 🛠️ Tech Stack

- **Framework**: Angular 18+
- **Language**: TypeScript 5.0+
- **Styling**: SCSS
- **State Management**: RxJS BehaviorSubjects (upgradeable to NgRx)
- **HTTP Client**: Angular HttpClient
- **Routing**: Angular Router
- **Forms**: Reactive Forms
- **Testing**: Jasmine, Karma
- **E2E Testing**: Cypress (recommended)
- **Build Tool**: Angular CLI
- **Package Manager**: npm

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Authors

- **Your Name** - *Initial work*

---

## 🙏 Acknowledgments

- Angular team for the amazing framework
- Community contributors
- Open source projects that inspired this architecture

---

## 📞 Support

For questions and support:
- 📧 Email: support@example.com
- 💬 Slack: [Join our Slack](https://slack.example.com)
- 🐛 Issues: [GitHub Issues](https://github.com/yourrepo/issues)

---

## 🗺️ Roadmap

### Version 1.1
- [ ] Add NgRx state management
- [ ] Implement WebSocket support
- [ ] Add internationalization (i18n)
- [ ] PWA support

### Version 1.2
- [ ] Add dark mode theme
- [ ] Implement real-time notifications
- [ ] Add advanced analytics dashboard
- [ ] File upload with progress

### Version 2.0
- [ ] Micro-frontend architecture
- [ ] GraphQL integration
- [ ] Advanced caching strategies
- [ ] AI-powered features

---

**Built with ❤️ using Angular**