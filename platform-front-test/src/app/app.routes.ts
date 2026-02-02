import { Routes } from "@angular/router";
import { AuthGuard } from "./core/guards/auth.guard";
import { RoleGuard } from "./core/guards/role.guard";

/**
 * Application Routes Configuration
 *
 * This file defines the main routing configuration for the application.
 * All feature modules are lazy-loaded for optimal performance.
 *
 * Route Structure:
 * - Public routes (no authentication required)
 *   - /auth/* - Authentication pages (login, register, etc.)
 *
 * - Protected routes (authentication required)
 *   - /dashboard - Dashboard module
 *   - /users - User management module
 *   - /products - Products module
 *   - /settings - Settings module
 *
 * Guards:
 * - AuthGuard: Protects routes requiring authentication
 * - RoleGuard: Protects routes requiring specific roles/permissions
 */
export const routes: Routes = [
  // Default route - redirect to dashboard
  {
    path: "",
    redirectTo: "dashboard",
    pathMatch: "full",
  },

  // Authentication Module (Public - No guard)
  {
    path: "auth",
    loadChildren: () =>
      import("./features/auth/auth.module").then((m) => m.AuthModule),
    data: {
      title: "Authentication",
      preload: true, // Preload this module for faster navigation
    },
  },

  // Dashboard Module (Protected - Auth required)
  {
    path: "dashboard",
    loadChildren: () =>
      import("./features/dashboard/dashboard.module").then(
        (m) => m.DashboardModule,
      ),
    canActivate: [AuthGuard],
    data: {
      title: "Dashboard",
      breadcrumb: "Dashboard",
      preload: true,
    },
  },

  // User Management Module (Protected - Auth + Role required)
  {
    path: "users",
    loadChildren: () =>
      import("./features/user-management/user-management.module").then(
        (m) => m.UserManagementModule,
      ),
    canActivate: [AuthGuard, RoleGuard],
    canLoad: [AuthGuard, RoleGuard],
    data: {
      title: "User Management",
      breadcrumb: "Users",
      roles: ["ADMIN", "USER_MANAGER"],
      permissions: ["USER_READ"],
    },
  },

  // Products Module (Protected - Auth required)
  {
    path: "products",
    loadChildren: () =>
      import("./features/products/products.module").then(
        (m) => m.ProductsModule,
      ),
    canActivate: [AuthGuard],
    data: {
      title: "Products",
      breadcrumb: "Products",
    },
  },

  // Settings Module (Protected - Auth required)
  {
    path: "settings",
    loadChildren: () =>
      import("./features/settings/settings.module").then(
        (m) => m.SettingsModule,
      ),
    canActivate: [AuthGuard],
    data: {
      title: "Settings",
      breadcrumb: "Settings",
    },
  },

  // Error Pages
  {
    path: "unauthorized",
    loadComponent: () =>
      import("./features/errors/pages/unauthorized/unauthorized.component").then(
        (c) => c.UnauthorizedComponent,
      ),
    data: {
      title: "Unauthorized",
      hideNavigation: true,
    },
  },
  {
    path: "forbidden",
    loadComponent: () =>
      import("./features/errors/pages/forbidden/forbidden.component").then(
        (c) => c.ForbiddenComponent,
      ),
    data: {
      title: "Forbidden",
      hideNavigation: true,
    },
  },
  {
    path: "not-found",
    loadComponent: () =>
      import("./features/errors/pages/not-found/not-found.component").then(
        (c) => c.NotFoundComponent,
      ),
    data: {
      title: "Page Not Found",
      hideNavigation: true,
    },
  },
  {
    path: "server-error",
    loadComponent: () =>
      import("./features/errors/pages/server-error/server-error.component").then(
        (c) => c.ServerErrorComponent,
      ),
    data: {
      title: "Server Error",
      hideNavigation: true,
    },
  },

  // Wildcard route - must be last
  {
    path: "**",
    redirectTo: "not-found",
  },
];

/**
 * Route Configuration Notes:
 *
 * 1. Lazy Loading:
 *    All feature modules use lazy loading with loadChildren.
 *    This reduces initial bundle size and improves load time.
 *
 * 2. Route Guards:
 *    - AuthGuard: Checks if user is authenticated
 *    - RoleGuard: Checks if user has required roles/permissions
 *    - Both canActivate and canLoad are used for complete protection
 *
 * 3. Route Data:
 *    - title: Page title for browser tab
 *    - breadcrumb: Breadcrumb label
 *    - roles: Required roles for access (array)
 *    - permissions: Required permissions for access (array)
 *    - requireAllRoles: If true, user must have ALL roles (default: false)
 *    - requireAllPermissions: If true, user must have ALL permissions (default: false)
 *    - preload: If true, module will be preloaded
 *    - hideNavigation: If true, navigation will be hidden
 *
 * 4. Preloading Strategy:
 *    Can be configured in app.config.ts to preload specific modules
 *    based on the 'preload' data flag.
 *
 * 5. Error Handling:
 *    - 401 Unauthorized: User not authenticated
 *    - 403 Forbidden: User authenticated but lacks permissions
 *    - 404 Not Found: Route does not exist
 *    - 500 Server Error: Backend error occurred
 *
 * Example Usage in Components:
 *
 * // Access route data
 * constructor(private route: ActivatedRoute) {
 *   this.route.data.subscribe(data => {
 *     console.log(data.title); // Page title
 *     console.log(data.roles); // Required roles
 *   });
 * }
 *
 * Example Navigation:
 *
 * // Programmatic navigation
 * this.router.navigate(['/dashboard']);
 * this.router.navigate(['/users', userId]);
 * this.router.navigate(['/products'], { queryParams: { category: 'electronics' }});
 *
 * // Template navigation
 * <a routerLink="/dashboard">Dashboard</a>
 * <a [routerLink]="['/users', user.id]">View User</a>
 */
