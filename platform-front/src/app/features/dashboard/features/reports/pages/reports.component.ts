import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { PaymentService, Payment, PaymentSummary } from "../../payments/services/payment.service";
import { DarService, Dar } from "../../dars/services/dar.service";

interface Transaction {
  id: string;
  darName: string;
  darInitials: string;
  darColor: string;
  date: string;
  type: string;
  amount: number;
  status: "paid" | "pending" | "completed";
}

interface StatCard {
  title: string;
  value: string;
  icon: string;
  bgColor: string;
  borderColor: string;
  iconColor: string;
  badge: string;
  badgeBg: string;
  badgeText: string;
  subtext?: string;
}

@Component({
  selector: "app-reports",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./reports.component.html",
  styleUrl: "./reports.component.scss",
})
export class ReportsComponent implements OnInit {
  activeTab: "dar-circles" | "personal-savings" = "dar-circles";

  dateRange = "This Month";
  /** null = all darts */
  selectedDartId: string | null = null;
  selectedStatus = "All Statuses";
  /** Populated from API for the Dâr filter */
  dars: { id: string; name: string }[] = [];
  private pageIndex = 0;

  statCards: StatCard[] = [];
  paymentSummary: PaymentSummary | null = null;

  chartData: { month: string; value: number; height: number }[] = [];

  paymentStatus = [
    { label: "Completed", percentage: 0, color: "bg-primary" },
    { label: "Pending", percentage: 0, color: "bg-amber-400" },
    { label: "Other", percentage: 0, color: "bg-gray-200" },
  ];

  transactions: Transaction[] = [];

  currentPage = 1;
  totalTransactions = 0;
  pageSize = 4;

  constructor(
    private paymentService: PaymentService,
    private darService: DarService,
  ) {}

  ngOnInit(): void {
    this.loadDars();
    this.refreshReportData();
  }

  setActiveTab(tab: "dar-circles" | "personal-savings"): void {
    this.activeTab = tab;
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.currentPage = 1;
    this.refreshReportData();
  }

  /** Reload summary, chart, and transaction page with current filters */
  private refreshReportData(): void {
    this.loadSummary();
    this.loadChart();
    this.loadTransactions();
  }

  exportReport(): void {
    this.downloadCSV();
  }

  downloadCSV(): void {
    const { start, end } = this.getDateRangeBounds();
    this.paymentService
      .downloadPaymentHistory(start, end, this.selectedDartId)
      .subscribe((blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "payment-report.csv";
        a.click();
        URL.revokeObjectURL(url);
      });
  }

  /** Dynamic conic gradient for the payment status donut */
  get maxChartValue(): number {
    return this.chartData.length
      ? Math.max(...this.chartData.map((d) => d.value))
      : 0;
  }

  get donutGradient(): string {
    const s = this.paymentStatus;
    const sum = s.reduce((a, b) => a + b.percentage, 0) || 1;
    const colors = ["#13ec5b", "#fbbf24", "#e2e8f0"];
    let acc = 0;
    const parts = s.map((x, i) => {
      const pct = (x.percentage / sum) * 100;
      const start = acc;
      acc += pct;
      return `${colors[i % colors.length]} ${start}% ${acc}%`;
    });
    return `conic-gradient(${parts.join(", ")})`;
  }

  printReport(): void {
    console.log('Printing report...');
    window.print();
  }

  getStatusClass(status: string): string {
    const statusClasses: { [key: string]: string } = {
      paid: "bg-green-100 text-green-700",
      pending: "bg-amber-100 text-amber-700",
      completed: "bg-green-100 text-green-700",
    };
    return statusClasses[status] || "bg-gray-100 text-gray-700";
  }

  getStatusLabel(status: string): string {
    const statusLabels: { [key: string]: string } = {
      paid: "Paid",
      pending: "Pending",
      completed: "Completed",
    };
    return statusLabels[status] || status;
  }

  formatAmount(amount: number, type: string): string {
    const formatted = amount.toFixed(2);
    return type === "Payout" ? `+$${formatted}` : `$${formatted}`;
  }

  isPositiveAmount(type: string): boolean {
    return type === "Payout";
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.pageIndex--;
      this.loadTransactions();
    }
  }

  nextPage(): void {
    const totalPages = Math.ceil(this.totalTransactions / this.pageSize);
    if (this.currentPage < totalPages) {
      this.currentPage++;
      this.pageIndex++;
      this.loadTransactions();
    }
  }

  get paginationInfo(): string {
    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(this.currentPage * this.pageSize, this.totalTransactions);
    return `Showing ${start}-${end} of ${this.totalTransactions} transactions`;
  }

  get canGoPrevious(): boolean {
    return this.currentPage > 1;
  }

  get canGoNext(): boolean {
    return this.currentPage < Math.ceil(this.totalTransactions / this.pageSize);
  }

  private loadDars(): void {
    this.darService.getMyDars(undefined, 0, 100).subscribe({
      next: (res) => {
        const dars = res.content || [];
        this.dars = dars.map((d: Dar) => ({ id: d.id, name: d.name }));
      },
    });
  }

  /** ISO date yyyy-mm-dd for API query params */
  private getDateRangeBounds(): { start: string; end: string } {
    const now = new Date();
    const iso = (d: Date) => d.toISOString().slice(0, 10);

    switch (this.dateRange) {
      case "Last Month": {
        const start = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        const end = new Date(now.getFullYear(), now.getMonth(), 0);
        return { start: iso(start), end: iso(end) };
      }
      case "Last 3 Months": {
        const start = new Date(now.getFullYear(), now.getMonth() - 2, 1);
        return { start: iso(start), end: iso(now) };
      }
      case "This Year": {
        const start = new Date(now.getFullYear(), 0, 1);
        return { start: iso(start), end: iso(now) };
      }
      case "This Month":
      default: {
        const start = new Date(now.getFullYear(), now.getMonth(), 1);
        return { start: iso(start), end: iso(now) };
      }
    }
  }

  private loadChart(): void {
    const { start, end } = this.getDateRangeBounds();
    this.paymentService.getMonthlyChart(start, end, this.selectedDartId).subscribe({
      next: (points) => {
        const nums = points.map((p) => Number(p.value));
        const max = Math.max(1, ...nums);
        this.chartData = points.map((p) => {
          const v = Number(p.value);
          return {
            month: p.month,
            value: v,
            height: (v / max) * 100,
          };
        });
      },
      error: () => {
        this.chartData = [];
      },
    });
  }

  private loadSummary(): void {
    const { start, end } = this.getDateRangeBounds();
    this.paymentService.getPaymentSummary(start, end, this.selectedDartId).subscribe({
      next: (summary) => {
        this.paymentSummary = summary;
        const tc = Number(summary.totalContributions);
        const pc = Number(summary.pendingContributions);
        const tp = Number(summary.totalPayouts);
        this.statCards = [
          {
            title: "Total Contributed",
            value: `${tc.toFixed(2)}`,
            icon: "account_balance_wallet",
            bgColor: "bg-green-50",
            borderColor: "border-green-100",
            iconColor: "text-primary-dark",
            badge: "Live",
            badgeBg: "bg-green-100",
            badgeText: "text-green-700",
          },
          {
            title: "Pending Payments",
            value: `${pc.toFixed(2)}`,
            icon: "pending",
            bgColor: "bg-yellow-50",
            borderColor: "border-yellow-100",
            iconColor: "text-yellow-600",
            badge: "Open",
            badgeBg: "bg-yellow-100",
            badgeText: "text-yellow-700",
          },
          {
            title: "Expected Return",
            value: `${tp.toFixed(2)}`,
            icon: "payments",
            bgColor: "bg-blue-50",
            borderColor: "border-blue-100",
            iconColor: "text-blue-600",
            badge: "Payouts",
            badgeBg: "bg-blue-100",
            badgeText: "text-blue-700",
            subtext: summary.nextPaymentDue?.darName || "No upcoming payout",
          },
        ];
      },
    });
  }

  private loadTransactions(): void {
    const status =
      this.selectedStatus === "All Statuses"
        ? undefined
        : this.selectedStatus.toLowerCase();
    const { start, end } = this.getDateRangeBounds();
    this.paymentService
      .getPayments(
        this.pageIndex,
        this.pageSize,
        status,
        this.selectedDartId,
        start,
        end,
      )
      .subscribe({
        next: (res) => {
          this.totalTransactions = res.totalElements;
          this.transactions = (res.content || []).map((payment: Payment) => ({
            id: String(payment.id),
            darName: payment.darName || "Unknown Dâr",
            darInitials: this.toInitials(payment.darName || "D"),
            darColor: "bg-green-100 text-green-800",
            date: payment.createdDate
              ? new Date(payment.createdDate).toLocaleDateString()
              : "—",
            type: this.formatPaymentType(payment.type),
            amount: payment.amount,
            status: this.mapPaymentStatus(payment.status),
          }));
          this.updatePaymentStatus();
        },
        error: () => {
          this.transactions = [];
          this.totalTransactions = 0;
          this.updatePaymentStatus();
        },
      });
  }

  private updatePaymentStatus(): void {
    const total = this.transactions.length || 1;
    const completed = this.transactions.filter(
      (t) => t.status === "completed",
    ).length;
    const pending = this.transactions.filter((t) => t.status === "pending").length;
    const other = this.transactions.filter((t) => t.status === "paid").length;
    this.paymentStatus = [
      {
        label: "Completed",
        percentage: Math.round((completed / total) * 100),
        color: "bg-primary",
      },
      {
        label: "Pending",
        percentage: Math.round((pending / total) * 100),
        color: "bg-amber-400",
      },
      {
        label: "Other",
        percentage: Math.round((other / total) * 100),
        color: "bg-gray-200",
      },
    ];
  }

  private toInitials(name: string): string {
    return name
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase() || "")
      .join("");
  }

  private formatPaymentType(type: string): string {
    const typeMap: Record<string, string> = {
      contribution: "Contribution",
      payout: "Payout",
      refund: "Refund",
      penalty: "Penalty",
    };
    return typeMap[type] || type;
  }

  private mapPaymentStatus(status: string): "paid" | "pending" | "completed" {
    const s = (status || "").toLowerCase();
    if (s === "pending" || s === "processing") {
      return "pending";
    }
    if (s === "completed" || s === "cancelled" || s === "payed") {
      return "completed";
    }
    return "paid";
  }
}
