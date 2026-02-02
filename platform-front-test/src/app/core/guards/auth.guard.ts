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

/**
 * Auth Guard
 *
 * Protects routes that require authentication.
 * Redirects unauthenticated users to the login page.
 *
 * Key Features:
 * - Checks authentication status before allowing route access
 * - Redirects to login page with return URL
 * - Preserves query parameters
 * - Logs access attempts
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard {
  constructor(
    private authService: AuthService,
    private router: Router,
    private logger: LoggerService
  ) {}

  /**
   * Determines if a route can be activated
   */
  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.checkAuth(state.url, route);
  }

  /**
   * Determines if child routes can be activated
   */
  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.checkAuth(state.url, childRoute);
  }

  /**
   * Determines if a route can be loaded (for lazy-loaded modules)
   */
  canLoad(): Observable<boolean> | Promise<boolean> | boolean {
    if (this.authService.isAuthenticated) {
      return true;
    }

    this.logger.warn('Attempt to load module without authentication');
    this.router.navigate(['/auth/login']);
    return false;
  }

  /**
   * Check authentication status
   */
  private checkAuth(url: string, route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> {
    return this.authService.isAuthenticated$.pipe(
      take(1),
      map(isAuthenticated => {
        if (isAuthenticated) {
          // Check if route has additional requirements
          const requiredRoles = route.data['roles'] as string[];
          const requiredPermissions = route.data['permissions'] as string[];

          if (requiredRoles && requiredRoles.length > 0) {
            if (!this.authService.hasAnyRole(requiredRoles)) {
              this.logger.warn('Access denied - insufficient roles', {
                required: requiredRoles,
                user: this.authService.currentUserValue?.email
              });
              return this.router.createUrlTree(['/unauthorized']);
            }
          }

          if (requiredPermissions && requiredPermissions.length > 0) {
            if (!this.authService.hasAnyPermission(requiredPermissions)) {
              this.logger.warn('Access denied - insufficient permissions', {
                required: requiredPermissions,
                user: this.authService.currentUserValue?.email
              });
              return this.router.createUrlTree(['/unauthorized']);
            }
          }

          this.logger.debug('Route access granted', { url, user: this.authService.currentUserValue?.email });
          return true;
        }

        // Not authenticated - redirect to login with return URL
        this.logger.info('Redirecting to login - not authenticated', { attemptedUrl: url });
        return this.router.createUrlTree(['/auth/login'], {
          queryParams: { returnUrl: url }
        });
      })
    );
  }
}
