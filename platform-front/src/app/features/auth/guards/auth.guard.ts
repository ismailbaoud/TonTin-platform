import { inject } from "@angular/core";
import { Router, CanActivateFn } from "@angular/router";
import { AuthService } from "../services/auth.service";

/**
 * Auth Guard
 *
 * Protects routes that require authentication.
 * Redirects unauthenticated users to the login page.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    console.log("✅ Auth Guard: User is authenticated");
    return true;
  }

  console.log(
    "❌ Auth Guard: User is not authenticated, redirecting to landing page",
  );
  router.navigate(["/"], {
    queryParams: { returnUrl: state.url },
  });
  return false;
};
