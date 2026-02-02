import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
  UrlTree
} from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth/auth.service';
import { LoggerService } from '../services/logger/logger.service';
import { NotificationService } from '../services/notification/notification.service';

/**
 * Role Guard
 *
 * Protects routes based on user roles and permissions.
 * Provides fine-grained access control for different user types.
 *
 * Key Features:
 * - Role-based route protection
 * - Permission-based route protection
 * - Supports multiple roles/permissions (ANY or ALL logic)
 * - Redirects unauthorized users
 * - Logs access attempts
 * - Shows user-friendly error messages
 *
 * Usage in routes:
 * {
 *   path: 'admin',
 *   component: AdminComponent,
 *   canActivate: [RoleGuard],
 *   data: {
 *     roles: ['ADMIN', 'SUPER_ADMIN'],
 *     requireAllRoles: false, // default: false (ANY)
 *     permissions: ['USER_MANAGE', 'SYSTEM_SETTINGS'],
 *     requireAllPermissions: false // default: false (ANY)
 *   }
 * }
 */
@Injectable({
  providedIn: 'root'
})
export class RoleGuard {
  constructor(
    private authService: AuthService,
    private router: Router,
    private logger: LoggerService,
    private notificationService: NotificationService
  ) {}

  /**
   * Determines if a route can be activated based on user roles/permissions
   */
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.checkAccess(route, state.url);
  }

  /**
   * Determines if child routes can be activated
   */
  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.checkAccess(childRoute, state.url);
  }

  /**
   * Check if user has required access
   */
  private checkAccess(
    route: ActivatedRouteSnapshot,
    url: string
  ): Observable<boolean | UrlTree> {
    return this.authService.isAuthenticated$.pipe(
      take(1),
      map(isAuthenticated => {
        // First check if user is authenticated
        if (!isAuthenticated) {
          this.logger.warn('Access denied - not authenticated', { url });
          return this.router.createUrlTree(['/auth/login'], {
            queryParams: { returnUrl: url }
          });
        }

        // Get route data
        const requiredRoles = route.data['roles'] as string[] | undefined;
        const requiredPermissions = route.data['permissions'] as string[] | undefined;
        const requireAllRoles = route.data['requireAllRoles'] as boolean ?? false;
        const requireAllPermissions = route.data['requireAllPermissions'] as boolean ?? false;

        // Check roles if specified
        if (requiredRoles && requiredRoles.length > 0) {
          const hasAccess = requireAllRoles
            ? this.authService.hasAllRoles(requiredRoles)
            : this.authService.hasAnyRole(requiredRoles);

          if (!hasAccess) {
            this.handleAccessDenied('roles', requiredRoles, url);
            return this.router.createUrlTree(['/unauthorized']);
          }
        }

        // Check permissions if specified
        if (requiredPermissions && requiredPermissions.length > 0) {
          const hasAccess = requireAllPermissions
            ? this.authService.hasAllPermissions(requiredPermissions)
            : this.authService.hasAnyPermission(requiredPermissions);

          if (!hasAccess) {
            this.handleAccessDenied('permissions', requiredPermissions, url);
            return this.router.createUrlTree(['/unauthorized']);
          }
        }

        // Access granted
        this.logger.debug('Role/Permission check passed', {
          url,
          user: this.authService.currentUserValue?.email,
          roles: this.authService.currentUserValue?.roles,
          permissions: this.authService.currentUserValue?.permissions
        });

        return true;
      })
    );
  }

  /**
   * Handle access denied scenario
   */
  private handleAccessDenied(
    type: 'roles' | 'permissions',
    required: string[],
    url: string
  ): void {
    const user = this.authService.currentUserValue;
    const currentRoles = user?.roles || [];
    const currentPermissions = user?.permissions || [];

    this.logger.warn(`Access denied - insufficient ${type}`, {
      url,
      user: user?.email,
      required,
      current: type === 'roles' ? currentRoles : currentPermissions
    });

    this.notificationService.error(
      `You don't have the required ${type} to access this page.`,
      'Access Denied'
    );
  }

  /**
   * Check if user can load lazy-loaded module
   */
  canLoad(route: any): Observable<boolean> | Promise<boolean> | boolean {
    const requiredRoles = route.data?.['roles'] as string[] | undefined;
    const requiredPermissions = route.data?.['permissions'] as string[] | undefined;

    // Check authentication
    if (!this.authService.isAuthenticated) {
      this.logger.warn('Cannot load module - not authenticated');
      this.router.navigate(['/auth/login']);
      return false;
    }

    // Check roles
    if (requiredRoles && requiredRoles.length > 0) {
      if (!this.authService.hasAnyRole(requiredRoles)) {
        this.logger.warn('Cannot load module - insufficient roles', {
          required: requiredRoles
        });
        this.router.navigate(['/unauthorized']);
        return false;
      }
    }

    // Check permissions
    if (requiredPermissions && requiredPermissions.length > 0) {
      if (!this.authService.hasAnyPermission(requiredPermissions)) {
        this.logger.warn('Cannot load module - insufficient permissions', {
          required: requiredPermissions
        });
        this.router.navigate(['/unauthorized']);
        return false;
      }
    }

    return true;
  }
}
