import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { DarService, Dar } from "../../dars/services/dar.service";
import { AdminService, AdminUser } from "../services/admin.service";
import { AuthService } from "../../../../auth/services/auth.service";
import { CreateDarRequest } from "../../dars/models";
import { DarStatus } from "../../dars/enums/dar-status.enum";

@Component({
  selector: "app-admin-dars",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./admin-dars.component.html",
})
export class AdminDarsComponent {
  loading = false;
  errorMessage = "";
  statusFilter = "";
  filterText = "";
  dars: Dar[] = [];
  createLoading = false;
  actionLoadingDarId = "";
  newDarName = "";
  newDarContribution: number | null = null;
  newDarFrequency = "MONTH";
  newDarOrderMethod: CreateDarRequest["orderMethod"] = "FIXED_ORDER";
  newDarDescription = "";
  newDarCustomRules = "";
  showCreateDarModal = false;
  organizerUsers: AdminUser[] = [];
  selectedOrganizerUserId = "";
  private currentUserId = "";

  constructor(
    private darService: DarService,
    private adminService: AdminService,
    private authService: AuthService,
  ) {
    const current = this.authService.getStoredUser();
    this.currentUserId = current?.id || "";
    this.loadDars();
  }

  loadDars(): void {
    this.loading = true;
    this.errorMessage = "";
    this.adminService.getAdminDars(0, 100, this.statusFilter || undefined).subscribe({
      next: (res) => {
        this.dars = res.content ?? [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = "Unable to load dars right now.";
      },
    });
  }

  get filteredDars(): Dar[] {
    const query = this.filterText.trim().toLowerCase();
    if (!query) {
      return this.dars;
    }
    return this.dars.filter(
      (dar) =>
        dar.name.toLowerCase().includes(query) ||
        (dar.organizerName ?? "").toLowerCase().includes(query),
    );
  }

  get pendingDarsCount(): number {
    return this.dars.filter((d) => d.status === DarStatus.PENDING).length;
  }

  get completedDarsCount(): number {
    return this.dars.filter((d) => d.status === DarStatus.FINISHED).length;
  }

  get contributionVolume(): number {
    return this.dars.reduce((sum, d) => sum + (d.monthlyContribution || 0), 0);
  }

  createDar(): void {
    if (
      !this.newDarName ||
      !this.newDarContribution ||
      this.newDarContribution <= 0 ||
      !this.selectedOrganizerUserId
    ) {
      this.errorMessage =
        "Please fill name, contribution and organizer user before creating.";
      return;
    }

    this.createLoading = true;
    this.errorMessage = "";
    this.adminService
      .createAdminDar({
        organizerUserId: this.selectedOrganizerUserId,
        dart: {
        name: this.newDarName.trim(),
        monthlyContribution: this.newDarContribution,
        paymentFrequency: this.newDarFrequency,
        orderMethod: this.newDarOrderMethod,
          description: this.newDarDescription.trim() || undefined,
          customRules: this.newDarCustomRules.trim() || undefined,
        },
      })
      .subscribe({
        next: () => {
          this.createLoading = false;
          this.closeCreateDarModal();
          this.loadDars();
        },
        error: () => {
          this.createLoading = false;
          this.errorMessage = "Unable to create dar right now.";
        },
      });
  }

  openCreateDarModal(): void {
    this.showCreateDarModal = true;
    this.errorMessage = "";
    this.adminService.getAdminUsers(0, 100).subscribe({
      next: (res) => {
        this.organizerUsers = (res.content || []).filter(
          (u) => !!u.id && (!this.currentUserId || u.id !== this.currentUserId),
        );
      },
      error: () => {
        this.organizerUsers = [];
      },
    });
  }

  closeCreateDarModal(): void {
    this.showCreateDarModal = false;
    this.createLoading = false;
    this.newDarName = "";
    this.newDarContribution = null;
    this.newDarFrequency = "MONTH";
    this.newDarOrderMethod = "FIXED_ORDER";
    this.newDarDescription = "";
    this.newDarCustomRules = "";
    this.selectedOrganizerUserId = "";
  }

  startDar(darId: string): void {
    if (!darId) return;
    if (!confirm("Start this Dâr now?")) return;

    this.actionLoadingDarId = darId;
    this.errorMessage = "";

    this.darService.startDar(darId).subscribe({
      next: () => {
        this.actionLoadingDarId = "";
        this.loadDars();
      },
      error: (err) => {
        this.actionLoadingDarId = "";
        this.errorMessage = err?.error?.message || "Unable to start this Dâr.";
      },
    });
  }

  finishDar(darId: string): void {
    if (!darId) return;
    if (!confirm("Finish this Dâr now?")) return;

    this.actionLoadingDarId = darId;
    this.errorMessage = "";

    this.darService.finishDar(darId).subscribe({
      next: () => {
        this.actionLoadingDarId = "";
        this.loadDars();
      },
      error: (err) => {
        this.actionLoadingDarId = "";
        this.errorMessage = err?.error?.message || "Unable to finish this Dâr.";
      },
    });
  }

  cancelDar(darId: string): void {
    if (!darId) return;
    if (!confirm("Delete this Dâr? This action may be restricted.")) return;

    this.actionLoadingDarId = darId;
    this.errorMessage = "";

    this.darService.deleteDar(darId).subscribe({
      next: () => {
        this.actionLoadingDarId = "";
        this.loadDars();
      },
      error: (err) => {
        this.actionLoadingDarId = "";
        this.errorMessage = err?.error?.message || "Unable to delete this Dâr.";
      },
    });
  }
}

