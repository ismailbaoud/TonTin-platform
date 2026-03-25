import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import {
  AuthService,
  UserProfileUpdateRequest,
  UserResponse,
} from "../../../../auth/services/auth.service";
import { ThemeService } from "../../../../../core/services/theme.service";

interface UserProfile {
  userName: string;
  email: string;
  avatar: string;
}

interface SecuritySettings {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

interface AppSettings {
  darkMode: boolean;
}

type SettingsTab = "profile" | "security" | "appearance";

@Component({
  selector: "app-profile",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./profile.component.html",
  styleUrl: "./profile.component.scss",
})
export class ProfileComponent implements OnInit {
  activeTab: SettingsTab = "profile";
  isLoading = false;
  lastSaved: Date | null = null;
  avatarFile: File | null = null;

  // User profile data
  userProfile: UserProfile = {
    userName: "",
    email: "",
    avatar: "",
  };

  // Security settings
  securitySettings: SecuritySettings = {
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  };

  // App settings
  appSettings: AppSettings = {
    darkMode: false,
  };

  // Backup for cancel functionality
  private originalProfile: UserProfile = { ...this.userProfile };
  private originalAppSettings: AppSettings = { ...this.appSettings };
  private currentUser: UserResponse | null = null;

  constructor(
    private router: Router,
    private authService: AuthService,
    private themeService: ThemeService,
  ) {}

  ngOnInit(): void {
    const storedUser = this.authService.getStoredUser();
    if (storedUser) {
      this.currentUser = storedUser;
      this.userProfile = this.mapUserToProfile(storedUser);
      this.originalProfile = { ...this.userProfile };
    }
    this.loadUserProfile();
    this.loadAppSettings();
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.userProfile = this.mapUserToProfile(user);
        this.originalProfile = { ...this.userProfile };
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        alert(error?.message || "Failed to load profile.");
      },
    });
  }

  loadAppSettings(): void {
    this.appSettings.darkMode = this.themeService.isDarkMode();
    this.originalAppSettings = { ...this.appSettings };
  }

  setTab(tab: SettingsTab): void {
    this.activeTab = tab;
  }

  onAvatarChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.avatarFile = input.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        if (e.target?.result) {
          this.userProfile.avatar = e.target.result as string;
        }
      };
      reader.readAsDataURL(this.avatarFile);
    }
  }

  onRemoveAvatar(): void {
    if (
      confirm(
        "Are you sure you want to remove your profile picture? A default avatar will be used.",
      )
    ) {
      this.userProfile.avatar = "";
      this.avatarFile = null;
    }
  }

  onChangePhoto(): void {
    const fileInput = document.createElement("input");
    fileInput.type = "file";
    fileInput.accept = "image/*";
    fileInput.onchange = (e) => this.onAvatarChange(e);
    fileInput.click();
  }

  onSaveChanges(): void {
    this.isLoading = true;

    // Validate form
    if (!this.validateForm()) {
      this.isLoading = false;
      return;
    }

    this.buildUpdateRequest()
      .then((request) => {
        this.authService.updateCurrentUserProfile(request).subscribe({
          next: (updatedUser) => {
            this.currentUser = updatedUser;
            this.userProfile = this.mapUserToProfile(updatedUser);
            this.lastSaved = new Date();
            this.originalProfile = { ...this.userProfile };
            this.originalAppSettings = { ...this.appSettings };
            this.securitySettings = {
              currentPassword: "",
              newPassword: "",
              confirmPassword: "",
            };
            this.isLoading = false;
            alert("Changes saved successfully.");
          },
          error: (error) => {
            this.isLoading = false;
            alert(error?.message || "Failed to save profile changes.");
          },
        });
      })
      .catch((error) => {
        this.isLoading = false;
        alert(error?.message || "Failed to process avatar image.");
      });
  }

  onCancel(): void {
    if (this.hasUnsavedChanges()) {
      if (
        confirm(
          "You have unsaved changes. Are you sure you want to cancel?",
        )
      ) {
        this.userProfile = { ...this.originalProfile };
        this.appSettings = { ...this.originalAppSettings };
        this.securitySettings = {
          currentPassword: "",
          newPassword: "",
          confirmPassword: "",
        };
      }
    }
  }

  onDeleteAccount(): void {
    const confirmation = prompt(
      'Are you sure you want to delete your account? This action cannot be undone. Type "DELETE" to confirm.',
    );
    if (confirmation === "DELETE") {
      alert(
        "Account deletion request submitted. You will receive a confirmation email.",
      );
    }
  }

  onSignOut(): void {
    if (this.hasUnsavedChanges()) {
      if (
        !confirm(
          "You have unsaved changes. Are you sure you want to sign out?",
        )
      ) {
        return;
      }
    }
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(["/auth/login"]);
      },
      error: () => {
        this.router.navigate(["/auth/login"]);
      },
    });
  }

  toggleDarkMode(): void {
    this.themeService.setDarkMode(this.appSettings.darkMode);
  }

  validateForm(): boolean {
    // Validate password if changing
    if (this.securitySettings.newPassword) {
      if (!this.securitySettings.currentPassword) {
        alert("Please enter your current password.");
        return false;
      }
      if (this.securitySettings.newPassword.length < 8) {
        alert("New password must be at least 8 characters long.");
        return false;
      }
      if (
        this.securitySettings.newPassword !==
        this.securitySettings.confirmPassword
      ) {
        alert("New passwords do not match.");
        return false;
      }
      if (this.securitySettings.currentPassword === this.securitySettings.newPassword) {
        alert("New password must be different from current password.");
        return false;
      }
    }

    return true;
  }

  hasUnsavedChanges(): boolean {
    return (
      JSON.stringify(this.userProfile) !==
        JSON.stringify(this.originalProfile) ||
      JSON.stringify(this.appSettings) !==
        JSON.stringify(this.originalAppSettings) ||
      this.securitySettings.currentPassword !== "" ||
      this.securitySettings.newPassword !== "" ||
      this.securitySettings.confirmPassword !== ""
    );
  }

  getLastSavedText(): string {
    if (!this.lastSaved) {
      return "Not saved yet";
    }

    const now = new Date();
    const diffMs = now.getTime() - this.lastSaved.getTime();
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) {
      return "Just saved";
    } else if (diffMins === 1) {
      return "Last saved: 1 minute ago";
    } else if (diffMins < 60) {
      return `Last saved: ${diffMins} minutes ago`;
    } else {
      return `Last saved: ${this.lastSaved.toLocaleTimeString()}`;
    }
  }

  get fullName(): string {
    return this.userProfile.userName || "";
  }

  get initials(): string {
    const parts = (this.userProfile.userName || "")
      .trim()
      .split(/\s+/)
      .filter(Boolean);
    const first = parts[0]?.charAt(0) || "";
    const second = parts[1]?.charAt(0) || "";
    return `${first}${second}`.toUpperCase() || "U";
  }

  private mapUserToProfile(user: UserResponse): UserProfile {
    const avatar = user.picture ? `data:image/jpeg;base64,${user.picture}` : "";
    return {
      userName: user.userName || "",
      email: user.email || "",
      avatar,
    };
  }

  private buildUsername(): string {
    return this.userProfile.userName.trim() || this.currentUser?.userName || "";
  }

  private async buildUpdateRequest(): Promise<UserProfileUpdateRequest> {
    const request: UserProfileUpdateRequest = {
      userName: this.buildUsername(),
    };

    if (this.securitySettings.newPassword) {
      request.currentPassword = this.securitySettings.currentPassword;
      request.password = this.securitySettings.newPassword;
    }

    if (this.avatarFile) {
      request.picture = await this.readFileAsNumberArray(this.avatarFile);
    } else if (!this.userProfile.avatar && this.currentUser?.picture) {
      request.picture = [];
    }

    return request;
  }

  private readFileAsNumberArray(file: File): Promise<number[]> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const buffer = reader.result as ArrayBuffer;
        resolve(Array.from(new Uint8Array(buffer)));
      };
      reader.onerror = () => reject(new Error("Could not read file."));
      reader.readAsArrayBuffer(file);
    });
  }
}
