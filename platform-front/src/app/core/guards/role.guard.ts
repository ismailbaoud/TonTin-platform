import { inject } from "@angular/core";
import { Router, CanActivateFn, ActivatedRouteSnapshot } from "@angular/router";
import { AuthService } from "../../features/auth/services/auth.service";

/**
 * Role Guard
 *
 * Protects routes based on user roles.
 * Redirects users without the required role to their appropriate dashboard.
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.getStoredUser();

  if (!user) {
    console.log("❌ Role Guard: No user found, redirecting to login");
    router.navigate(["/auth/login"]);
    return false;
  }

  // Get required roles from route data
  const requiredRoles = route.data["roles"] as string[];

  if (!requiredRoles || requiredRoles.length === 0) {
    console.log("✅ Role Guard: No roles required");
    return true;
  }

  // Normalize user role (backend sends ROLE_ADMIN, ROLE_CLIENT)
  const userRole = user.role.toUpperCase();

  // Check if user has one of the required roles
  // Support both with and without ROLE_ prefix
  const hasRole = requiredRoles.some((role) => {
    const normalizedRole = role.toUpperCase();
    return (
      userRole === normalizedRole ||
      userRole === `ROLE_${normalizedRole}` ||
      `ROLE_${userRole}` === normalizedRole
    );
  });

  if (hasRole) {
    console.log(`✅ Role Guard: User has required role: ${userRole}`);
    return true;
  }

  console.log(
    `❌ Role Guard: User role ${userRole} not authorized, redirecting`,
  );

  // Redirect to appropriate dashboard based on user's actual role
  if (userRole === "ROLE_ADMIN" || userRole === "ADMIN") {
    router.navigate(["/dashboard/admin"]);
  } else {
    router.navigate(["/dashboard/client"]);
  }

  return false;
};
