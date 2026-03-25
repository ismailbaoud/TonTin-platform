import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Subject, debounceTime, distinctUntilChanged } from "rxjs";
import { AuthService } from "../../../../auth/services/auth.service";
import { AdminService, AdminUser } from "../services/admin.service";

@Component({
  selector: "app-admin-users",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./admin-users.component.html",
})
export class AdminUsersComponent {
  searchTerm = "";
  roleFilter = "";
  statusFilter = "";
  loading = false;
  createLoading = false;
  actionLoadingUserId = "";
  errorMessage = "";
  successMessage = "";
  users: AdminUser[] = [];
  totalUsers = 0;
  verifiedUsers = 0;
  flaggedUsers = 0;
  newUserName = "";
  newUserEmail = "";
  newUserPassword = "";
  showCreateUserModal = false;
  private currentUserId = "";
  private search$ = new Subject<string>();

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
  ) {
    const current = this.authService.getStoredUser();
    this.currentUserId = current?.id || "";
    this.loadUsers();
    this.search$
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((query) => this.loadUsers(query));
  }

  openCreateUserModal(): void {
    this.errorMessage = "";
    this.successMessage = "";
    this.showCreateUserModal = true;
  }

  closeCreateUserModal(): void {
    this.showCreateUserModal = false;
  }

  onSearchChange(value: string): void {
    this.searchTerm = value;
    this.search$.next(value);
  }

  loadUsers(query: string = this.searchTerm): void {
    this.loading = true;
    this.errorMessage = "";
    this.adminService
      .getAdminUsers(
        0,
        100,
        query?.trim() || undefined,
        this.statusFilter || undefined,
      )
      .subscribe({
      next: (result) => {
        this.users = (result.content ?? []).filter(
          (u) => !this.currentUserId || u.id !== this.currentUserId,
        );
        this.totalUsers = this.users.length;
        this.verifiedUsers = this.users.filter(
          (u) => (u.status || "").toUpperCase() === "ACTIVE",
        ).length;
        this.flaggedUsers = this.users.filter(
          (u) => (u.status || "").toUpperCase() !== "ACTIVE",
        ).length;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = "Unable to load users right now.";
      },
    });
  }

  createUser(): void {
    if (!this.newUserName || !this.newUserEmail || !this.newUserPassword) {
      this.errorMessage = "Please fill username, email and password.";
      return;
    }

    this.createLoading = true;
    this.errorMessage = "";
    this.successMessage = "";
    this.authService
      .register({
        userName: this.newUserName.trim(),
        email: this.newUserEmail.trim(),
        password: this.newUserPassword,
      })
      .subscribe({
        next: () => {
          this.createLoading = false;
          this.successMessage = "User created successfully.";
          this.newUserName = "";
          this.newUserEmail = "";
          this.newUserPassword = "";
          this.loadUsers();
          this.showCreateUserModal = false;
        },
        error: (error) => {
          this.createLoading = false;
          this.errorMessage = error?.message || "Unable to create user.";
        },
      });
  }

  setUserStatus(user: AdminUser, status: string): void {
    if (!user.id) {
      return;
    }
    this.actionLoadingUserId = user.id;
    this.errorMessage = "";
    this.successMessage = "";
    this.adminService.updateUserStatus(user.id, status).subscribe({
      next: () => {
        this.actionLoadingUserId = "";
        this.successMessage = `User ${user.userName} ${status === "ACTIVE" ? "enabled" : "disabled"} successfully.`;
        this.loadUsers();
      },
      error: () => {
        this.actionLoadingUserId = "";
        this.errorMessage = "Unable to update user status.";
      },
    });
  }
}

