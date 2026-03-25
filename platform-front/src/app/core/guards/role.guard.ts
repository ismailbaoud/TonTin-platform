import { inject } from "@angular/core";
import { Router, CanActivateFn, ActivatedRouteSnapshot } from "@angular/router";
import { AuthService } from "../../features/auth/services/auth.service";
import { Observable, of } from "rxjs";
import { catchError, map } from "rxjs/operators";

/**
 * Role Guard
 *
 * Protects routes based on user roles.
 * Redirects users without the required role to their appropriate dashboard.
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Get required roles from route data
  const requiredRoles = route.data["roles"] as string[];

  if (!requiredRoles || requiredRoles.length === 0) {
    console.log("✅ Role Guard: No roles required");
    return true;
  }

  const hasRequiredRole = (role: string): boolean => {
    const userRole = role.toUpperCase();
    return requiredRoles.some((requiredRole) => {
      const normalizedRequiredRole = requiredRole.toUpperCase();
      return (
        userRole === normalizedRequiredRole ||
        userRole === `ROLE_${normalizedRequiredRole}` ||
        `ROLE_${userRole}` === normalizedRequiredRole
      );
    });
  };

  const redirectByRole = (role: string): boolean => {
    const userRole = role.toUpperCase();
    if (userRole === "ROLE_ADMIN" || userRole === "ADMIN") {
      router.navigate(["/dashboard/admin"]);
      return false;
    }

    router.navigate(["/dashboard/client"]);
    return false;
  };

  const allowByRole = (role: string): boolean => {
    if (hasRequiredRole(role)) {
      console.log(`✅ Role Guard: User has required role: ${role.toUpperCase()}`);
      return true;
    }

    console.log(
      `❌ Role Guard: User role ${role.toUpperCase()} not authorized, redirecting`,
    );
    return redirectByRole(role);
  };

  const storedUser = authService.getStoredUser();
  if (storedUser?.role) {
    return allowByRole(storedUser.role);
  }

  if (!authService.getToken()) {
    console.log("❌ Role Guard: No token found, redirecting to login");
    router.navigate(["/auth/login"]);
    return false;
  }

  // Fallback: resolve user profile from API when storage is empty/stale.
  return authService.getCurrentUser().pipe(
    map((user) => allowByRole(user.role)),
    catchError((): Observable<boolean> => {
      console.log("❌ Role Guard: Failed to resolve user profile");
      router.navigate(["/auth/login"]);
      return of(false);
    }),
  );
};
