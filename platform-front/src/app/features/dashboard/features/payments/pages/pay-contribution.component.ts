import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import {
  Subject,
  takeUntil,
  finalize,
  debounceTime,
  distinctUntilChanged,
} from "rxjs";
import {
  PaymentService,
  PaymentMethod as ApiPaymentMethod,
  MakePaymentRequest,
  FeeCalculation,
  Payment,
} from "../services/payment.service";
import { DarService, Dar as ApiDar } from "../../dars/services/dar.service";

interface Dar {
  id: number;
  name: string;
  currentCycle: number;
  totalCycles: number;
  beneficiary: {
    name: string;
    avatar: string;
  };
  potSize: number;
  dueDate: string;
  status: "pending" | "completed" | "late";
  contributionProgress: number;
  membersContributed: number;
  totalMembers: number;
  contributionAmount: number;
}

interface PaymentMethod {
  id: number;
  name: string;
  icon: string;
  type: string;
  balance?: string;
  processingTime?: string;
  isDefault: boolean;
}

interface ActivityRecord {
  date: string;
  cycle: string;
  beneficiary: {
    name: string;
    avatar: string;
  };
  status: string;
  amount: number;
}

@Component({
  selector: "app-pay-contribution",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./pay-contribution.component.html",
  styleUrl: "./pay-contribution.component.scss",
})
export class PayContributionComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private feeCalculationSubject$ = new Subject<void>();

  darId: string | null = null;
  selectedDarId: number | null = null;
  selectedPaymentMethodId: number | null = null;
  contributionAmount = 0;

  isLoading = false;
  isLoadingFees = false;
  isSubmitting = false;
  error: string | null = null;

  dars: Dar[] = [];
  paymentMethods: PaymentMethod[] = [];
  recentActivity: ActivityRecord[] = [];

  feeCalculation: FeeCalculation | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private darService: DarService,
  ) {}

  ngOnInit(): void {
    this.darId = this.route.snapshot.paramMap.get("id");

    // Load initial data
    this.loadDars();
    this.loadPaymentMethods();
    this.loadRecentActivity();

    // Setup fee calculation debounce
    this.feeCalculationSubject$
      .pipe(debounceTime(500), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(() => {
        this.calculateFees();
      });

    // If darId is provided, select it and load its contribution amount
    if (this.darId) {
      this.selectedDarId = parseInt(this.darId, 10);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.feeCalculationSubject$.complete();
  }

  loadDars(): void {
    this.isLoading = true;

    this.darService
      .getMyDars("active", 0, 50)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (response) => {
          this.dars = this.mapApiDarsToComponent(response.content);

          // If darId was provided, set the contribution amount
          if (this.selectedDarId) {
            const dar = this.dars.find((d) => d.id === this.selectedDarId);
            if (dar) {
              this.contributionAmount = dar.contributionAmount;
              this.triggerFeeCalculation();
            }
          }
        },
        error: (err) => {
          console.error("Error loading Dârs:", err);
          this.error = "Failed to load your Dârs. Please try again.";
        },
      });
  }

  loadPaymentMethods(): void {
    this.paymentService
      .getPaymentMethods()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (methods) => {
          this.paymentMethods = this.mapApiPaymentMethodsToComponent(methods);

          // Auto-select default payment method
          const defaultMethod = this.paymentMethods.find((m) => m.isDefault);
          if (defaultMethod) {
            this.selectedPaymentMethodId = defaultMethod.id;
            this.triggerFeeCalculation();
          } else if (this.paymentMethods.length > 0) {
            this.selectedPaymentMethodId = this.paymentMethods[0].id;
            this.triggerFeeCalculation();
          }
        },
        error: (err) => {
          console.error("Error loading payment methods:", err);
        },
      });
  }

  loadRecentActivity(): void {
    this.paymentService
      .getPayments(0, 10, "completed")
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.recentActivity = this.mapApiPaymentsToActivity(response.content);
        },
        error: (err) => {
          console.error("Error loading recent activity:", err);
        },
      });
  }

  private mapApiDarsToComponent(apiDars: ApiDar[]): Dar[] {
    return apiDars.map((dar) => ({
      id: dar.id,
      name: dar.name,
      currentCycle: dar.currentCycle,
      totalCycles: dar.totalCycles,
      beneficiary: {
        name: dar.nextPayoutRecipient || "TBD",
        avatar: dar.image || this.getDefaultAvatar(),
      },
      potSize: dar.potSize,
      dueDate: this.formatDate(dar.nextPayoutDate),
      status: dar.status === "active" ? "pending" : (dar.status as any),
      contributionProgress:
        dar.totalCycles > 0 ? (dar.currentCycle / dar.totalCycles) * 100 : 0,
      membersContributed: 0, // TODO: Get from API
      totalMembers: dar.totalMembers,
      contributionAmount: dar.contributionAmount,
    }));
  }

  private mapApiPaymentMethodsToComponent(
    apiMethods: ApiPaymentMethod[],
  ): PaymentMethod[] {
    return apiMethods.map((method) => ({
      id: method.id,
      name: this.getPaymentMethodName(method),
      icon: this.getPaymentMethodIcon(method.type),
      type: method.type,
      balance: method.type === "wallet" ? "Loading..." : undefined,
      processingTime: this.getProcessingTime(method.type),
      isDefault: method.isDefault,
    }));
  }

  private mapApiPaymentsToActivity(payments: Payment[]): ActivityRecord[] {
    return payments.slice(0, 3).map((payment) => ({
      date: this.formatDate(payment.completedDate || payment.createdDate),
      cycle: payment.cycleNumber
        ? `#${payment.cycleNumber} of ${payment.cycleNumber}`
        : "N/A",
      beneficiary: {
        name: payment.darName,
        avatar: this.getDefaultAvatar(),
      },
      status: this.capitalizeFirst(payment.status),
      amount: payment.amount,
    }));
  }

  private getPaymentMethodName(method: ApiPaymentMethod): string {
    if (method.type === "wallet") return "TonTin Wallet";
    if (method.type === "mobile_money")
      return `${method.provider || "Mobile"} Money`;
    if (method.type === "bank_account")
      return `Bank ${method.last4 ? "****" + method.last4 : "Account"}`;
    if (method.type === "card")
      return `${method.brand || "Card"} ${method.last4 ? "****" + method.last4 : ""}`;
    return "Payment Method";
  }

  private getPaymentMethodIcon(type: string): string {
    const iconMap: { [key: string]: string } = {
      wallet: "account_balance_wallet",
      mobile_money: "smartphone",
      bank_account: "account_balance",
      card: "credit_card",
    };
    return iconMap[type] || "payment";
  }

  private getProcessingTime(type: string): string {
    const timeMap: { [key: string]: string } = {
      wallet: "Instant",
      mobile_money: "Instant",
      card: "Instant",
      bank_account: "1-2 Days",
    };
    return timeMap[type] || "Unknown";
  }

  private getDefaultAvatar(): string {
    return "https://lh3.googleusercontent.com/aida-public/AB6AXuCDbAFe_orMUTMf0gYZD5360B1ZxfW3n5EsuUm8_gsqvf0kJvjn36SU8x9FLfZB80XaXeBgP4ktCNeu1XWFqA1VX4gPtOLQaW8NQebPvfP4puGa3hUJX_ki8RNYJ-f2ESmzGlsAW1Tsv8xnELAPxHzQ0T5aKI2jIT76UgxvDNvGY2T3ymx6caY4uRIKgTzHzFWBOXfPoQJo17AiAZ5A4ZxyTl1X--Ci-M1A8QCmMyude6bzImOyR9v5w7M_8jtwuSllvgu2oBrLQhc";
  }

  private formatDate(dateString?: string): string {
    if (!dateString) return "TBD";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  }

  private capitalizeFirst(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
  }

  get selectedDar(): Dar | undefined {
    return this.dars.find((d) => d.id === this.selectedDarId);
  }

  get selectedPaymentMethod(): PaymentMethod | undefined {
    return this.paymentMethods.find(
      (m) => m.id === this.selectedPaymentMethodId,
    );
  }

  get platformFee(): number {
    return this.feeCalculation?.platformFee || 0;
  }

  get processingFee(): number {
    return this.feeCalculation?.processingFee || 0;
  }

  get totalFee(): number {
    return this.feeCalculation?.totalFee || 0;
  }

  get totalPayment(): number {
    return this.feeCalculation?.totalAmount || this.contributionAmount;
  }

  get daysUntilDue(): number {
    const dar = this.selectedDar;
    if (!dar || !dar.dueDate) return 0;

    const due = new Date(dar.dueDate);
    const now = new Date();
    const diff = due.getTime() - now.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  get isEarlyContribution(): boolean {
    return this.daysUntilDue >= 3;
  }

  triggerFeeCalculation(): void {
    this.feeCalculationSubject$.next();
  }

  calculateFees(): void {
    if (!this.contributionAmount || this.contributionAmount <= 0) {
      this.feeCalculation = null;
      return;
    }

    if (!this.selectedPaymentMethod) {
      return;
    }

    this.isLoadingFees = true;

    this.paymentService
      .calculateFees(this.contributionAmount, this.selectedPaymentMethod.type)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoadingFees = false)),
      )
      .subscribe({
        next: (calculation) => {
          this.feeCalculation = calculation;
        },
        error: (err) => {
          console.error("Error calculating fees:", err);
          // Fallback to simple calculation
          const platformFee = this.contributionAmount * 0.005;
          const processingFee = this.contributionAmount * 0.02;
          this.feeCalculation = {
            baseAmount: this.contributionAmount,
            platformFee,
            processingFee,
            totalFee: platformFee + processingFee,
            totalAmount: this.contributionAmount + platformFee + processingFee,
            feePercentage: 2.5,
          };
        },
      });
  }

  onDarChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedDarId = parseInt(select.value, 10);

    const dar = this.selectedDar;
    if (dar) {
      this.contributionAmount = dar.contributionAmount;
      this.triggerFeeCalculation();
    }
  }

  onAmountChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.contributionAmount = parseFloat(input.value) || 0;
    this.triggerFeeCalculation();
  }

  onPaymentMethodChange(methodId: number): void {
    this.selectedPaymentMethodId = methodId;
    this.triggerFeeCalculation();
  }

  onPayNow(): void {
    if (!this.validatePayment()) {
      return;
    }

    const dar = this.selectedDar;
    if (!dar || !this.selectedPaymentMethodId) {
      this.error = "Please select a Dâr and payment method";
      return;
    }

    this.isSubmitting = true;
    this.error = null;

    const request: MakePaymentRequest = {
      darId: dar.id,
      amount: this.contributionAmount,
      paymentMethodId: this.selectedPaymentMethodId,
      cycleNumber: dar.currentCycle,
    };

    this.paymentService
      .makePayment(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isSubmitting = false)),
      )
      .subscribe({
        next: (payment) => {
          console.log("Payment processed successfully:", payment);

          // Show success message (you could add a toast service here)
          alert(
            `Payment of $${this.totalPayment.toFixed(2)} processed successfully!`,
          );

          // Navigate back to dar details
          this.router.navigate(["/dashboard/client/dar", dar.id]);
        },
        error: (err) => {
          console.error("Error processing payment:", err);
          this.error =
            err.error?.message ||
            "Failed to process payment. Please try again.";
        },
      });
  }

  private validatePayment(): boolean {
    if (!this.selectedDarId) {
      this.error = "Please select a Dâr";
      return false;
    }

    if (!this.contributionAmount || this.contributionAmount <= 0) {
      this.error = "Please enter a valid contribution amount";
      return false;
    }

    if (!this.selectedPaymentMethodId) {
      this.error = "Please select a payment method";
      return false;
    }

    this.error = null;
    return true;
  }

  onViewAllHistory(): void {
    this.router.navigate(["/dashboard/client/payment-history"]);
  }

  onAddPaymentMethod(): void {
    // TODO: Open add payment method modal
    console.log("Add payment method clicked");
  }
}
