import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";

interface UserProfile {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  avatar: string;
  trustPoints: number;
}

interface SecuritySettings {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

interface AppSettings {
  emailNotifications: boolean;
  darInvites: boolean;
  darkMode: boolean;
}

type SettingsTab = "profile" | "security" | "notifications" | "appearance";

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
    firstName: "Alex",
    lastName: "Doe",
    email: "alex.doe@example.com",
    phone: "+1 (555) 000-0000",
    avatar:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuCdZOnZcE-XXa4KNlRVn6VLR8LfWhklrw5WihLxwW_rl8K_EEX5_EQwEv2pvJCxOGRiIv7F4OzeG1C5HnHKx5ohLPq0tnLBElfmXBcnyMkhBMjLbzBUjgIC2iaB7Jebcg1mnpOPCJ1U4Dk8v6W8ElJTPh6L-3Pj-P7HbYq9rxT_Z7GRsS43EihyGdDttXd2koR2GCQXF0_9wHw4Wg7QH8I56N-VaxeB4yQoQYp4pVwAAQb7BcIKwQqPD1t_SNcnN8FQiMTYaWqjxo4",
    trustPoints: 750,
  };

  // Security settings
  securitySettings: SecuritySettings = {
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  };

  // App settings
  appSettings: AppSettings = {
    emailNotifications: true,
    darInvites: true,
    darkMode: false,
  };

  // Backup for cancel functionality
  private originalProfile: UserProfile = { ...this.userProfile };
  private originalAppSettings: AppSettings = { ...this.appSettings };

  constructor(private router: Router) {}

  ngOnInit(): void {
    // TODO: Load user profile from service
    this.loadUserProfile();
    this.loadAppSettings();
  }

  loadUserProfile(): void {
    // TODO: Call API to get user profile
    console.log("Loading user profile...");
  }

  loadAppSettings(): void {
    // TODO: Call API to get app settings
    console.log("Loading app settings...");
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
      console.log("Avatar changed:", this.avatarFile.name);
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
      console.log("Avatar removed");
      // TODO: Call API to remove avatar
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

    // Simulate API call
    setTimeout(() => {
      console.log("Saving changes:", {
        profile: this.userProfile,
        security: this.securitySettings,
        settings: this.appSettings,
      });

      // TODO: Call API to save changes
      // API calls here...

      this.lastSaved = new Date();
      this.originalProfile = { ...this.userProfile };
      this.originalAppSettings = { ...this.appSettings };

      // Clear password fields
      this.securitySettings = {
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      };

      this.isLoading = false;
      alert("Changes saved successfully!");
    }, 1000);
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
        console.log("Changes cancelled");
      }
    }
  }

  onDeleteAccount(): void {
    const confirmation = prompt(
      'Are you sure you want to delete your account? This action cannot be undone. Type "DELETE" to confirm.',
    );
    if (confirmation === "DELETE") {
      console.log("Delete account requested");
      // TODO: Call API to delete account
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
    console.log("Sign out clicked");
    // TODO: Call auth service to log out
    this.router.navigate(["/auth/login"]);
  }

  toggleDarkMode(): void {
    this.appSettings.darkMode = !this.appSettings.darkMode;
    // TODO: Apply dark mode to the application
    console.log("Dark mode toggled:", this.appSettings.darkMode);
  }

  validateForm(): boolean {
    // Validate email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.userProfile.email)) {
      alert("Please enter a valid email address.");
      return false;
    }

    // Validate phone (basic)
    if (
      this.userProfile.phone &&
      this.userProfile.phone.length < 10
    ) {
      alert("Please enter a valid phone number.");
      return false;
    }

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
    return `${this.userProfile.firstName} ${this.userProfile.lastName}`;
  }

  get initials(): string {
    return `${this.userProfile.firstName.charAt(0)}${this.userProfile.lastName.charAt(0)}`.toUpperCase();
  }
}
