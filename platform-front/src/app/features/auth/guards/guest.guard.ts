import { inject } from "@angular/core";
import { Router, CanActivateFn } from "@angular/router";
import { AuthService } from "../services/auth.service";

/**
 * Guest Guard
 *
 * Protects routes that should only be accessible to unauthenticated users (login, register).
 * Redirects authenticated users to their appropriate dashboard based on their role.
 */
export const guestGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    console.log("✅ Guest Guard: User is not authenticated, allowing access");
    return true;
  }

  console.log(
    "❌ Guest Guard: User is already authenticated, redirecting to dashboard",
  );

  // Get user role and redirect to appropriate dashboard
  const user = authService.getStoredUser();

  if (user) {
    const userRole = user.role.toUpperCase();

    // Check if user is admin (handle both ROLE_ADMIN and ADMIN)
    if (userRole === "ROLE_ADMIN" || userRole === "ADMIN") {
      router.navigate(["/dashboard/admin"]);
    } else {
      router.navigate(["/dashboard/client"]);
    }
  } else {
    // Fallback to client dashboard if user data not found
    router.navigate(["/dashboard/client"]);
  }

  return false;
};
