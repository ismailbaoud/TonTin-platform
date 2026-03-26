import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule, RouterOutlet } from "@angular/router";
import {
  AuthService,
  UserResponse,
} from "../../../features/auth/services/auth.service";

@Component({
  selector: "app-client-layout",
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet],
  templateUrl: "./client-layout.component.html",
  styleUrl: "./client-layout.component.scss",
})
export class ClientLayoutComponent implements OnInit {
  currentUser: UserResponse | null = null;
  isLoadingUser = false;
  isLoggingOut = false;
  isMobileMenuOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadUserData();
  }

  private loadUserData(): void {
    this.currentUser = this.authService.getStoredUser();

    if (!this.currentUser || !this.currentUser.role) {
      // If no stored user, try to fetch from API
      this.isLoadingUser = true;
      this.authService.getCurrentUser().subscribe({
        next: (user) => {
          this.currentUser = user;
          this.isLoadingUser = false;
        },
        error: (error) => {
          console.error("Failed to load user data:", error);
          this.isLoadingUser = false;
          // Redirect to login if user data cannot be loaded
          this.router.navigate(["/auth/login"]);
        },
      });
    }
  }

  onLogout(): void {
    if (this.isLoggingOut) return;

    if (confirm("Are you sure you want to logout?")) {
      this.isLoggingOut = true;
      this.closeMobileMenu();

      // Clear local session first so guards treat user as logged out immediately.
      this.authService.clearLocalAuth();
      this.router.navigateByUrl("/auth/login", { replaceUrl: true }).finally(() => {
        // Hard fallback in case router navigation is interrupted by stale state.
        setTimeout(() => {
          if (!window.location.pathname.startsWith("/auth/login")) {
            window.location.assign("/auth/login");
          }
          this.isLoggingOut = false;
        }, 100);
      });
    }
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }

  getUserInitials(): string {
    if (!this.currentUser) return "?";

    const userName = this.currentUser.userName || "";
    const parts = userName.split(" ");

    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }

    return userName.substring(0, 2).toUpperCase();
  }

  getDisplayRole(): string {
    if (!this.currentUser) return "Unknown";

    const role = this.currentUser.role?.toUpperCase();
    if (role === "ROLE_ADMIN" || role === "ADMIN") {
      return "Administrator";
    }
    if (role === "ROLE_CLIENT" || role === "CLIENT") {
      return "Client";
    }
    return this.currentUser.role || "Unknown";
  }

  isAdmin(): boolean {
    const role = (this.currentUser?.role ?? "")
      .toString()
      .trim()
      .toUpperCase();
    return role === "ROLE_ADMIN" || role === "ADMIN";
  }
}
