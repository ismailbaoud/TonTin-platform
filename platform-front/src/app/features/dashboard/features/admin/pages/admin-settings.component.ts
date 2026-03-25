import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { AuthService, UserResponse } from "../../../../auth/services/auth.service";

@Component({
  selector: "app-admin-settings",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./admin-settings.component.html",
})
export class AdminSettingsComponent {
  currentUser: UserResponse | null = null;
  adminUserName = "";
  platformName = "TonTin Global";
  supportEmail = "";
  platformFee = 1.5;
  loading = false;
  successMessage = "";
  errorMessage = "";

  constructor(private authService: AuthService) {
    this.loadCurrentUser();
  }

  loadCurrentUser(): void {
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.adminUserName = user.userName;
        this.supportEmail = user.email;
      },
    });
  }

  saveSettings(): void {
    this.loading = true;
    this.successMessage = "";
    this.errorMessage = "";
    this.authService
      .updateCurrentUserProfile({
        userName: this.adminUserName,
      })
      .subscribe({
        next: (user) => {
          this.currentUser = user;
          this.loading = false;
          this.successMessage = "Settings saved successfully.";
        },
        error: () => {
          this.loading = false;
          this.errorMessage = "Unable to save settings right now.";
        },
      });
  }
}

