import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import {
  PaymentService,
  PaymentSummary,
  Payment,
} from "../../payments/services/payment.service";

@Component({
  selector: "app-admin-payments",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./admin-payments.component.html",
})
export class AdminPaymentsComponent {
  loading = false;
  errorMessage = "";
  search = "";
  statusFilter = "";
  summary: PaymentSummary | null = null;
  payments: Payment[] = [];

  constructor(private paymentService: PaymentService) {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = "";
    this.paymentService.getAdminPaymentSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
      },
    });

    this.paymentService.getAdminPayments(0, 100, this.statusFilter || undefined).subscribe({
      next: (res) => {
        this.payments = res.content ?? [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = "Unable to load payments right now.";
      },
    });
  }

  get filteredPayments(): Payment[] {
    const query = this.search.trim().toLowerCase();
    return this.payments.filter(
      (payment) =>
        !query ||
        String(payment.id).toLowerCase().includes(query) ||
        (payment.darName || "").toLowerCase().includes(query) ||
        (payment.payerUserName || "").toLowerCase().includes(query) ||
        (payment.payerEmail || "").toLowerCase().includes(query),
    );
  }
}

