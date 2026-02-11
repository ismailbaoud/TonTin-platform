import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import {
  AuthService,
  UserResponse,
} from "../../../../auth/services/auth.service";

@Component({
  selector: "app-client",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./client.component.html",
  styleUrl: "./client.component.scss",
})
export class ClientComponent implements OnInit {
  currentUser: UserResponse | null = null;
  isLoading = false;

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

  // Navigation methods
  navigateToMyDars(): void {
    this.router.navigate(["/dashboard/client/my-dars"]);
  }

  navigateToCreateDar(): void {
    this.router.navigate(["/dashboard/client/create-dar"]);
  }

  navigateToReports(): void {
    this.router.navigate(["/dashboard/client/reports"]);
  }

  navigateToDarDetails(darId: number): void {
    this.router.navigate(["/dashboard/client/dar", darId]);
  }

  navigateToPayment(darId: number): void {
    this.router.navigate(["/dashboard/client/pay-contribution", darId]);
  }
}
