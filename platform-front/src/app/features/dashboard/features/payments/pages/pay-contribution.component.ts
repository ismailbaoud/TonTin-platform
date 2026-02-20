import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  AfterViewInit,
  ChangeDetectorRef,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { Subject, takeUntil, finalize, forkJoin, of } from "rxjs";
import { catchError } from "rxjs/operators";
import { PaymentService } from "../services/payment.service";
import { DarService } from "../../dars/services/dar.service";
import { RoundService } from "../../dars/services/round.service";
import type { DarDetails } from "../../dars/models";
import type { Round } from "../../dars/models";
import { environment } from "../../../../../../environments/environment";
import { loadStripe } from "@stripe/stripe-js";
import type {
  Stripe,
  StripeElements,
  StripeCardElement,
} from "@stripe/stripe-js";

@Component({
  selector: "app-pay-contribution",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./pay-contribution.component.html",
  styleUrl: "./pay-contribution.component.scss",
})
export class PayContributionComponent
  implements OnInit, OnDestroy, AfterViewInit
{
  @ViewChild("stripeCardMount") stripeCardMountRef!: ElementRef<HTMLDivElement>;

  private destroy$ = new Subject<void>();

  darId: string | null = null;
  darDetails: DarDetails | null = null;
  currentRound: Round | null = null;
  contributionAmount = 0;

  isLoading = true;
  isSubmitting = false;
  error: string | null = null;

  /** After createPaymentIntent we show card form */
  showStripeCard = false;
  clientSecret: string | null = null;
  /** Internal payment record UUID returned by createPaymentIntent — used for client-side confirmation */
  paymentId: string | null = null;
  stripeInstance: Stripe | null = null;
  stripeCardElement: StripeCardElement | null = null;
  stripeElements: StripeElements | null = null;

  /** All dârs for dropdown when no darId in route */
  darsList: { id: string; name: string }[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private darService: DarService,
    private roundService: RoundService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.darId = this.route.snapshot.paramMap.get("id");

    if (this.darId) {
      this.loadDarAndRound(this.darId);
    } else {
      this.loadDarsThenSelect();
    }
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.stripeCardElement && this.stripeCardMountRef?.nativeElement) {
      this.stripeCardElement.destroy();
    }
  }

  private get stripePublishableKey(): string {
    const env = environment as {
      external?: { stripePublishableKey?: string };
      services?: { stripe?: { publicKey?: string } };
    };
    return (
      env.external?.stripePublishableKey ??
      env.services?.stripe?.publicKey ??
      ""
    );
  }

  loadDarAndRound(id: string): void {
    this.isLoading = true;
    this.error = null;
    this.darDetails = null;
    this.currentRound = null;

    forkJoin({
      dar: this.darService.getDarDetails(id),
      round: this.roundService
        .getCurrentRound(id)
        .pipe(catchError(() => of(null))),
    })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: ({ dar, round }) => {
          this.darDetails = dar as DarDetails;
          this.currentRound = round;
          this.contributionAmount =
            typeof dar.monthlyContribution === "number"
              ? dar.monthlyContribution
              : Number(dar.monthlyContribution) || 0;
        },
        error: (err) => {
          console.error("Load dar/round failed:", err);
          if (
            err?.status === 404 ||
            err?.error?.message?.includes("current round")
          ) {
            this.error =
              "No current round to pay for this Dâr, or Dâr not found.";
          } else {
            this.error =
              err?.error?.message ?? "Failed to load Dâr. Please try again.";
          }
        },
      });
  }

  loadDarsThenSelect(): void {
    this.isLoading = true;
    this.error = null;
    this.darService
      .getMyDars("active", 0, 50)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (res) => {
          this.darsList = (res.content || []).map(
            (d: { id: string; name: string }) => ({
              id: d.id,
              name: d.name,
            }),
          );
          if (this.darsList.length === 0) {
            this.error = "You have no active Dârs. Create or join one first.";
          }
        },
        error: (err) => {
          console.error("Load dars failed:", err);
          this.error = "Failed to load your Dârs. Please try again.";
        },
      });
  }

  onSelectDar(darId: string): void {
    if (darId) {
      this.darId = darId;
      this.loadDarAndRound(darId);
    }
  }

  get roundDateFormatted(): string {
    if (!this.currentRound?.date) return "—";
    return new Date(this.currentRound.date).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  }

  get canPayWithStripe(): boolean {
    return !!(
      this.darId &&
      this.darDetails &&
      this.currentRound &&
      this.contributionAmount > 0 &&
      this.stripePublishableKey
    );
  }

  async startStripePayment(): Promise<void> {
    if (!this.darId || !this.canPayWithStripe) {
      this.error = "Select a Dâr and ensure a current round is available.";
      return;
    }

    this.isSubmitting = true;
    this.error = null;

    this.paymentService
      .createPaymentIntent(this.darId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isSubmitting = false)),
      )
      .subscribe({
        next: async (res) => {
          this.clientSecret = res.clientSecret;
          this.paymentId = res.paymentId ?? null;
          const key = this.stripePublishableKey;
          if (!key || key.startsWith("pk_test_mock")) {
            this.error =
              "Stripe is not configured. Add your Stripe publishable key in environment (e.g. environment.development.ts).";
            return;
          }
          try {
            const stripe = await loadStripe(key);
            if (!stripe) {
              this.error =
                "Could not load Stripe. Check your key (use pk_test_* for localhost; live keys require HTTPS).";
              return;
            }
            this.stripeInstance = stripe;
            // Show card container first so #stripeCardMount is in the DOM
            this.showStripeCard = true;
            this.cdr.detectChanges();
            // Mount after the view has rendered (next tick)
            setTimeout(() => {
              const mountEl = this.stripeCardMountRef?.nativeElement;
              if (!mountEl) {
                this.error =
                  "Could not display payment form. Please try again.";
                this.cdr.detectChanges();
                return;
              }
              this.stripeElements = stripe.elements();
              this.stripeCardElement = this.stripeElements.create("card", {
                style: {
                  base: {
                    fontSize: "16px",
                    color: "#1f2937",
                    "::placeholder": { color: "#9ca3af" },
                  },
                },
              });
              this.stripeCardElement.mount(mountEl);
              this.cdr.detectChanges();
            }, 0);
          } catch (e) {
            console.error("Stripe init error:", e);
            this.error = "Could not load payment form. Try again.";
          }
        },
        error: (err) => {
          console.error("Create payment intent failed:", err);
          this.error =
            err?.error?.message ??
            "Could not start payment. You may not be a payer for this round.";
        },
      });
  }

  async confirmStripePayment(): Promise<void> {
    if (
      !this.stripeInstance ||
      !this.clientSecret ||
      !this.stripeCardElement ||
      !this.darId
    ) {
      this.error = "Payment form is not ready.";
      return;
    }

    this.isSubmitting = true;
    this.error = null;

    const { error } = await this.stripeInstance.confirmCardPayment(
      this.clientSecret,
      { payment_method: { card: this.stripeCardElement } },
    );

    if (error) {
      this.isSubmitting = false;
      this.error = error.message ?? "Payment failed. Try again.";
      return;
    }

    // Stripe confirmed successfully on the client side.
    // Immediately tell the backend to mark this payment as PAYED — this is the
    // reliable synchronous path that works even when the Stripe webhook has not
    // yet fired (e.g. local dev without the Stripe CLI running).
    if (this.paymentId) {
      this.paymentService
        .confirmPaymentSuccess(this.paymentId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.isSubmitting = false;
            this.router.navigate(["/dashboard/client/dar", this.darId], {
              queryParams: { payment: "success" },
            });
          },
          error: (err) => {
            // The payment was confirmed on Stripe's side — don't block the user.
            // The webhook will eventually sync it; just navigate anyway.
            console.warn(
              "Could not confirm payment on backend (will sync via webhook):",
              err,
            );
            this.isSubmitting = false;
            this.router.navigate(["/dashboard/client/dar", this.darId], {
              queryParams: { payment: "success" },
            });
          },
        });
    } else {
      this.isSubmitting = false;
      this.router.navigate(["/dashboard/client/dar", this.darId], {
        queryParams: { payment: "success" },
      });
    }
  }

  backToDar(): void {
    if (this.darId) {
      this.router.navigate(["/dashboard/client/dar", this.darId]);
    } else {
      this.router.navigate(["/dashboard/client"]);
    }
  }
}
