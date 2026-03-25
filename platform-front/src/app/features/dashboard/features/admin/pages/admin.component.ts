import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { AuthService, UserResponse } from "../../../../auth/services/auth.service";
import { DarService } from "../../dars/services/dar.service";
import { PaymentService, PaymentSummary } from "../../payments/services/payment.service";
import { AdminService } from "../services/admin.service";

@Component({
  selector: "app-admin",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./admin.component.html",
  styleUrl: "./admin.component.scss",
})
export class AdminComponent implements OnInit {
  currentUser: UserResponse | null = null;
  activeDarsCount = 0;
  totalMembers = 0;
  totalUsers = 0;
  stripePublishableKey = "";
  paymentSummary: PaymentSummary | null = null;
  loadError = "";

  constructor(
    private authService: AuthService,
    private darService: DarService,
    private paymentService: PaymentService,
    private adminService: AdminService,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getStoredUser();
    this.adminService.getAdminDars(0, 100, "ACTIVE").subscribe({
      next: (res) => {
        const dars = res.content || [];
        this.activeDarsCount = dars.length;
        this.totalMembers = dars.reduce(
          (sum, dar) => sum + (dar.memberCount || 0),
          0,
        );
      },
      error: () => {
        this.loadError = "Unable to load some admin metrics right now.";
      },
    });
    this.adminService.getAdminUsers(0, 1).subscribe({
      next: (res) => {
        this.totalUsers = res.totalElements ?? 0;
      },
      error: () => {
        this.loadError = "Unable to load some admin metrics right now.";
      },
    });
    this.paymentService.getPaymentSummary().subscribe({
      next: (summary) => {
        this.paymentSummary = summary;
      },
      error: () => {
        this.loadError = "Unable to load some admin metrics right now.";
      },
    });
    this.paymentService.getStripePublicConfig().subscribe({
      next: (config) => {
        this.stripePublishableKey = config.publishableKey || "";
      },
    });
  }
}
