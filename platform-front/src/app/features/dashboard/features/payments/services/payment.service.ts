import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable, BehaviorSubject, tap, of } from "rxjs";
import { environment } from "../../../../../../environments/environment";

export interface PaymentMethod {
  id: number;
  type: "card" | "bank_account" | "mobile_money" | "wallet";
  provider?: string;
  last4?: string;
  brand?: string;
  accountName?: string;
  isDefault: boolean;
  isVerified: boolean;
  expiryMonth?: number;
  expiryYear?: number;
  createdDate: string;
}

export interface Payment {
  id: string | number;
  darId: string | number;
  darName: string;
  payerMemberId?: string;
  payerUserId?: string;
  payerUserName?: string;
  payerEmail?: string;
  userId: number;
  amount: number;
  fee: number;
  totalAmount: number;
  type: "contribution" | "payout" | "refund" | "penalty";
  status: "pending" | "processing" | "completed" | "failed" | "cancelled";
  paymentMethodId?: number;
  paymentMethodType?: string;
  transactionId?: string;
  referenceNumber?: string;
  description?: string;
  cycleNumber?: number;
  scheduledDate?: string;
  completedDate?: string;
  createdDate: string;
  failureReason?: string;
}

export interface Contribution {
  id: number;
  darId: number;
  darName: string;
  userId: number;
  userName: string;
  cycleNumber: number;
  amount: number;
  dueDate: string;
  paidDate?: string;
  status: "pending" | "paid" | "overdue" | "waived";
  paymentId?: number;
  reminderSent: boolean;
}

export interface Wallet {
  id: number;
  userId: number;
  balance: number;
  currency: string;
  pendingAmount: number;
  totalContributed: number;
  totalReceived: number;
  totalWithdrawn: number;
  lastUpdated: string;
}

export interface WalletTransaction {
  id: number;
  walletId: number;
  type: "deposit" | "withdrawal" | "contribution" | "payout" | "refund" | "fee";
  amount: number;
  balanceBefore: number;
  balanceAfter: number;
  status: "pending" | "completed" | "failed";
  description: string;
  referenceId?: string;
  referenceType?: string;
  createdDate: string;
}

export interface MakePaymentRequest {
  darId: string;
  contributionId?: number;
  amount: number;
  paymentMethodId: number;
  cycleNumber: number;
  savePaymentMethod?: boolean;
  notes?: string;
}

export interface AddPaymentMethodRequest {
  type: "card" | "bank_account" | "mobile_money" | "wallet";
  provider?: string;
  cardNumber?: string;
  expiryMonth?: number;
  expiryYear?: number;
  cvv?: string;
  accountNumber?: string;
  accountName?: string;
  bankCode?: string;
  phoneNumber?: string;
  setAsDefault?: boolean;
}

export interface WithdrawRequest {
  amount: number;
  paymentMethodId: number;
  notes?: string;
}

export interface PaymentSummary {
  totalContributions: number;
  totalPayouts: number;
  pendingContributions: number;
  overdueContributions: number;
  nextPaymentDue?: {
    darId: string;
    darName: string;
    amount: number;
    dueDate: string;
  };
  recentPayments: Payment[];
}

export interface FeeCalculation {
  baseAmount: number;
  platformFee: number;
  processingFee: number;
  totalFee: number;
  totalAmount: number;
  feePercentage: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  /** Backend PageResponse uses `page` (0-based) */
  page?: number;
  pageNumber?: number;
  size?: number;
  pageSize?: number;
  last: boolean;
  first: boolean;
}

export interface CreatePaymentIntentResponse {
  clientSecret: string;
  paymentId: string;
}

export interface CanPayResponse {
  canPay: boolean;
}

/** GET /v1/payments/config/stripe-public — publishable key from backend (.env) */
export interface StripePublicConfigResponse {
  publishableKey: string;
}

/** Monthly bar for reports chart (API: GET /v1/payments/chart/monthly) */
export interface MonthlyChartPoint {
  month: string;
  value: number;
}

@Injectable({
  providedIn: "root",
})
export class PaymentService {
  private apiUrl = `${environment.apiUrl}/payments`;
  private apiV1Url = `${environment.apiUrl}/v1/payments`;
  /** Reports, summary, CSV — aligned with Spring `/api/v1/payments` */
  private apiV1Payments = `${environment.apiUrl}/v1/payments`;
  private walletApiUrl = `${environment.apiUrl}/wallet`;
  private paymentMethodsApiUrl = `${environment.apiUrl}/payment-methods`;

  private walletSubject = new BehaviorSubject<Wallet | null>(null);
  public wallet$ = this.walletSubject.asObservable();

  private paymentMethodsSubject = new BehaviorSubject<PaymentMethod[]>([]);
  public paymentMethods$ = this.paymentMethodsSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Stripe publishable key from the API (backed by STRIPE_PUBLISHABLE_KEY in platform-back/.env).
   */
  getStripePublicConfig(): Observable<StripePublicConfigResponse> {
    return this.http.get<StripePublicConfigResponse>(
      `${this.apiV1Url}/config/stripe-public`,
    );
  }

  /**
   * Get payment summary for current user (filtered by period / dart)
   */
  getPaymentSummary(
    startDate?: string,
    endDate?: string,
    dartId?: string | null,
  ): Observable<PaymentSummary> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set("startDate", startDate);
    }
    if (endDate) {
      params = params.set("endDate", endDate);
    }
    if (dartId) {
      params = params.set("dartId", dartId);
    }
    return this.http.get<PaymentSummary>(`${this.apiV1Payments}/summary`, {
      params,
    });
  }

  /**
   * Admin/global payment summary (all users).
   */
  getAdminPaymentSummary(
    startDate?: string,
    endDate?: string,
    dartId?: string | null,
  ): Observable<PaymentSummary> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set("startDate", startDate);
    }
    if (endDate) {
      params = params.set("endDate", endDate);
    }
    if (dartId) {
      params = params.set("dartId", dartId);
    }
    return this.http.get<PaymentSummary>(`${this.apiV1Payments}/admin/summary`, {
      params,
    });
  }

  /**
   * Monthly contribution totals for the reports chart
   */
  getMonthlyChart(
    startDate?: string,
    endDate?: string,
    dartId?: string | null,
  ): Observable<MonthlyChartPoint[]> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set("startDate", startDate);
    }
    if (endDate) {
      params = params.set("endDate", endDate);
    }
    if (dartId) {
      params = params.set("dartId", dartId);
    }
    return this.http.get<MonthlyChartPoint[]>(
      `${this.apiV1Payments}/chart/monthly`,
      { params },
    );
  }

  /**
   * Get payment history (paginated)
   */
  getPayments(
    page: number = 0,
    size: number = 20,
    status?: string,
    darId?: string | null,
    startDate?: string,
    endDate?: string,
  ): Observable<PaginatedResponse<Payment>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (status) {
      params = params.set("status", status);
    }
    if (darId) {
      params = params.set("dartId", darId);
    }
    if (startDate) {
      params = params.set("startDate", startDate);
    }
    if (endDate) {
      params = params.set("endDate", endDate);
    }

    return this.http.get<PaginatedResponse<Payment>>(`${this.apiV1Payments}/me`, {
      params,
    });
  }

  /**
   * Admin/global payment history (all users).
   */
  getAdminPayments(
    page: number = 0,
    size: number = 20,
    status?: string,
    darId?: string | null,
    startDate?: string,
    endDate?: string,
  ): Observable<PaginatedResponse<Payment>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (status) {
      params = params.set("status", status);
    }
    if (darId) {
      params = params.set("dartId", darId);
    }
    if (startDate) {
      params = params.set("startDate", startDate);
    }
    if (endDate) {
      params = params.set("endDate", endDate);
    }

    return this.http.get<PaginatedResponse<Payment>>(
      `${this.apiV1Payments}/admin/list`,
      { params },
    );
  }

  /**
   * Get a specific payment by ID
   */
  getPayment(paymentId: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/${paymentId}`);
  }

  /**
   * Get contributions for a specific Dâr
   */
  getContributions(
    darId: number,
    page: number = 0,
    size: number = 20,
    status?: string,
  ): Observable<PaginatedResponse<Contribution>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (status) {
      params = params.set("status", status);
    }

    return this.http.get<PaginatedResponse<Contribution>>(
      `${this.apiUrl}/contributions/dar/${darId}`,
      { params },
    );
  }

  /**
   * Get user's contributions across all Dârs
   */
  getMyContributions(
    page: number = 0,
    size: number = 20,
    status?: string,
  ): Observable<PaginatedResponse<Contribution>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (status) {
      params = params.set("status", status);
    }

    return this.http.get<PaginatedResponse<Contribution>>(
      `${this.apiUrl}/contributions/my`,
      { params },
    );
  }

  /**
   * Create a Stripe PaymentIntent for contribution (current round of the dart).
   * Use the returned clientSecret with Stripe.js to confirm payment.
   */
  createPaymentIntent(dartId: string): Observable<CreatePaymentIntentResponse> {
    return this.http.post<CreatePaymentIntentResponse>(
      `${this.apiV1Url}/create-intent`,
      { dartId },
    );
  }

  /**
   * Check if current user can pay for a dart right now.
   */
  canPay(dartId: string): Observable<CanPayResponse> {
    const params = new HttpParams().set("dartId", dartId);
    return this.http.get<CanPayResponse>(`${this.apiV1Url}/can-pay`, { params });
  }

  /**
   * Confirm a payment as successfully completed using our internal payment record UUID.
   *
   * Call this immediately after stripe.confirmCardPayment() resolves successfully.
   * This guarantees the payment is marked PAYED on the backend even when the Stripe
   * webhook has not fired yet (e.g. in local development without the Stripe CLI).
   *
   * The operation is idempotent — calling it on an already-PAYED payment is a no-op.
   *
   * @param paymentId  UUID returned by createPaymentIntent
   */
  confirmPaymentSuccess(paymentId: string): Observable<{ status: string }> {
    return this.http.post<{ status: string }>(
      `${this.apiV1Url}/${paymentId}/confirm-success`,
      {},
    );
  }

  /**
   * Make a contribution payment (legacy / non-Stripe flow)
   */
  makePayment(request: MakePaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/contribute`, request);
  }

  /**
   * Calculate payment fees
   */
  calculateFees(
    amount: number,
    paymentMethodType: string,
  ): Observable<FeeCalculation> {
    const params = new HttpParams()
      .set("amount", amount.toString())
      .set("paymentMethodType", paymentMethodType);

    return this.http.get<FeeCalculation>(`${this.apiUrl}/calculate-fees`, {
      params,
    });
  }

  /**
   * Retry a failed payment
   */
  retryPayment(
    paymentId: number,
    paymentMethodId?: number,
  ): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/${paymentId}/retry`, {
      paymentMethodId,
    });
  }

  /**
   * Cancel a pending payment
   */
  cancelPayment(paymentId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${paymentId}/cancel`, {});
  }

  /**
   * Request a refund
   */
  requestRefund(paymentId: number, reason: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${paymentId}/refund`, {
      reason,
    });
  }

  /**
   * Get user's wallet
   */
  getWallet(): Observable<Wallet> {
    return this.http
      .get<Wallet>(this.walletApiUrl)
      .pipe(tap((wallet) => this.walletSubject.next(wallet)));
  }

  /**
   * Get wallet transactions
   */
  getWalletTransactions(
    page: number = 0,
    size: number = 20,
    type?: string,
  ): Observable<PaginatedResponse<WalletTransaction>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (type) {
      params = params.set("type", type);
    }

    return this.http.get<PaginatedResponse<WalletTransaction>>(
      `${this.walletApiUrl}/transactions`,
      { params },
    );
  }

  /**
   * Deposit funds to wallet
   */
  depositToWallet(
    amount: number,
    paymentMethodId: number,
  ): Observable<WalletTransaction> {
    return this.http
      .post<WalletTransaction>(`${this.walletApiUrl}/deposit`, {
        amount,
        paymentMethodId,
      })
      .pipe(tap(() => this.getWallet().subscribe()));
  }

  /**
   * Withdraw funds from wallet
   */
  withdrawFromWallet(request: WithdrawRequest): Observable<WalletTransaction> {
    return this.http
      .post<WalletTransaction>(`${this.walletApiUrl}/withdraw`, request)
      .pipe(tap(() => this.getWallet().subscribe()));
  }

  /**
   * Get all payment methods
   */
  getPaymentMethods(): Observable<PaymentMethod[]> {
    return this.http
      .get<PaymentMethod[]>(this.paymentMethodsApiUrl)
      .pipe(tap((methods) => this.paymentMethodsSubject.next(methods)));
  }

  /**
   * Add a new payment method
   */
  addPaymentMethod(
    request: AddPaymentMethodRequest,
  ): Observable<PaymentMethod> {
    return this.http
      .post<PaymentMethod>(this.paymentMethodsApiUrl, request)
      .pipe(
        tap((method) => {
          const current = this.paymentMethodsSubject.value;
          this.paymentMethodsSubject.next([...current, method]);
        }),
      );
  }

  /**
   * Remove a payment method
   */
  removePaymentMethod(paymentMethodId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.paymentMethodsApiUrl}/${paymentMethodId}`)
      .pipe(
        tap(() => {
          const current = this.paymentMethodsSubject.value;
          this.paymentMethodsSubject.next(
            current.filter((m) => m.id !== paymentMethodId),
          );
        }),
      );
  }

  /**
   * Set default payment method
   */
  setDefaultPaymentMethod(paymentMethodId: number): Observable<void> {
    return this.http
      .put<void>(
        `${this.paymentMethodsApiUrl}/${paymentMethodId}/set-default`,
        {},
      )
      .pipe(
        tap(() => {
          const current = this.paymentMethodsSubject.value;
          const updated = current.map((m) => ({
            ...m,
            isDefault: m.id === paymentMethodId,
          }));
          this.paymentMethodsSubject.next(updated);
        }),
      );
  }

  /**
   * Verify a payment method
   */
  verifyPaymentMethod(
    paymentMethodId: number,
    verificationData: any,
  ): Observable<PaymentMethod> {
    return this.http.post<PaymentMethod>(
      `${this.paymentMethodsApiUrl}/${paymentMethodId}/verify`,
      verificationData,
    );
  }

  /**
   * Send payment reminder to a member
   */
  sendPaymentReminder(
    darId: number,
    userId: number,
    contributionId: number,
  ): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/send-reminder`, {
      darId,
      userId,
      contributionId,
    });
  }

  /**
   * Get payment receipt
   */
  getPaymentReceipt(paymentId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${paymentId}/receipt`, {
      responseType: "blob",
    });
  }

  /**
   * Download payment history as CSV
   */
  downloadPaymentHistory(
    startDate?: string,
    endDate?: string,
    dartId?: string | null,
  ): Observable<Blob> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set("startDate", startDate);
    }
    if (endDate) {
      params = params.set("endDate", endDate);
    }
    if (dartId) {
      params = params.set("dartId", dartId);
    }

    return this.http.get(`${this.apiV1Payments}/export`, {
      params,
      responseType: "blob",
    });
  }

  /**
   * Check if user has pending payments for a Dâr
   */
  hasPendingPayments(
    darId: number,
  ): Observable<{ hasPending: boolean; count: number }> {
    return this.http.get<{ hasPending: boolean; count: number }>(
      `${this.apiUrl}/pending-check/${darId}`,
    );
  }

  /**
   * Clear cached data
   */
  clearCache(): void {
    this.walletSubject.next(null);
    this.paymentMethodsSubject.next([]);
  }
}
