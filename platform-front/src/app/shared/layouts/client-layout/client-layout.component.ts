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
  isLoading = false;
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

    if (!this.currentUser) {
      // If no stored user, try to fetch from API
      this.isLoading = true;
      this.authService.getCurrentUser().subscribe({
        next: (user) => {
          this.currentUser = user;
          this.isLoading = false;
        },
        error: (error) => {
          console.error("Failed to load user data:", error);
          this.isLoading = false;
          // Redirect to login if user data cannot be loaded
          this.router.navigate(["/auth/login"]);
        },
      });
    }
  }

  onLogout(): void {
    if (confirm("Are you sure you want to logout?")) {
      this.isLoading = true;
      this.authService.logout().subscribe({
        next: () => {
          console.log("✅ Logout successful");
          this.router.navigate(["/auth/login"]);
        },
        error: (error) => {
          console.error("Logout error:", error);
          // Even if API fails, redirect to login
          this.router.navigate(["/auth/login"]);
        },
        complete: () => {
          this.isLoading = false;
        },
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
}
