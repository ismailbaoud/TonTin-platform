import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import {
  AuthService,
  UserResponse,
} from "../../../../auth/services/auth.service";
import { DarService } from "../../dars/services/dar.service";
import { Dar } from "../../dars/models";
import {
  DarStatus,
  getDarStatusLabel,
  getDarStatusColor,
} from "../../dars/enums/dar-status.enum";

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
  dars: Dar[] = [];
  isLoadingDars = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private darService: DarService,
  ) {}

  ngOnInit(): void {
    this.loadUserData();
    this.loadDars();
  }

  private loadDars(): void {
    this.isLoadingDars = true;
    this.darService.getMyDars(undefined, 0, 10).subscribe({
      next: (res) => {
        this.dars = res.content ?? [];
        this.isLoadingDars = false;
      },
      error: () => {
        this.dars = [];
        this.isLoadingDars = false;
      },
    });
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

  navigateToDarDetails(darId: string): void {
    this.router.navigate(["/dashboard/client/dar", darId]);
  }

  navigateToPayment(darId: string): void {
    this.router.navigate(["/dashboard/client/pay-contribution", darId]);
  }

  getStatusLabel(status: string): string {
    const normalized = (status || "").toLowerCase() as DarStatus;
    return getDarStatusLabel(normalized) || status || "—";
  }

  getStatusColor(status: string): string {
    const normalized = (status || "").toLowerCase() as DarStatus;
    return getDarStatusColor(normalized) || "bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300";
  }
}
