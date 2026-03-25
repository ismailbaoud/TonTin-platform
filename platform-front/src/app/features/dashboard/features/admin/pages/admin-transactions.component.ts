import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { PaymentService, Payment } from "../../payments/services/payment.service";

@Component({
  selector: "app-admin-transactions",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./admin-transactions.component.html",
})
export class AdminTransactionsComponent {
  loading = false;
  errorMessage = "";
  statusFilter = "";
  typeFilter = "";
  search = "";
  payments: Payment[] = [];

  constructor(private paymentService: PaymentService) {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.loading = true;
    this.errorMessage = "";
    this.paymentService.getAdminPayments(0, 100, this.statusFilter || undefined).subscribe({
      next: (res) => {
        this.payments = res.content ?? [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = "Unable to load transactions right now.";
      },
    });
  }

  get filteredPayments(): Payment[] {
    const query = this.search.trim().toLowerCase();
    return this.payments.filter((payment) => {
      const matchesType = !this.typeFilter || (payment.type || "").toLowerCase() === this.typeFilter.toLowerCase();
      const matchesQuery =
        !query ||
        String(payment.id).toLowerCase().includes(query) ||
        (payment.darName || "").toLowerCase().includes(query) ||
        (payment.payerUserName || "").toLowerCase().includes(query) ||
        (payment.payerEmail || "").toLowerCase().includes(query);
      return matchesType && matchesQuery;
    });
  }
}

