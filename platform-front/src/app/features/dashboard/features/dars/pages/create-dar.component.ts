import { Component, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { Subject, takeUntil, finalize } from "rxjs";
import {
  DarService,
  CreateDarRequest,
  DarFrequency,
} from "../services/dar.service";

@Component({
  selector: "app-create-dar",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./create-dar.component.html",
  styleUrl: "./create-dar.component.scss",
})
export class CreateDarComponent implements OnDestroy {
  private destroy$ = new Subject<void>();

  // Form fields
  darName = "";
  description = "";
  monthlyAmount = 0;
  frequency: DarFrequency = DarFrequency.MONTHLY;
  orderMethod: "FIXED_ORDER" | "RANDOM_ONCE" | "BIDDING_MODEL" | "DYNAMIQUE_RANDOM" = "FIXED_ORDER";
  rules = "";

  isSubmitting = false;
  error: string | null = null;

  constructor(
    private router: Router,
    private darService: DarService,
  ) { }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSubmit(event: Event): void {
    event.preventDefault();

    // Validate form
    if (!this.darName || this.darName.trim().length === 0) {
      this.error = "Please enter a Dâr name";
      return;
    }

    if (this.monthlyAmount <= 0) {
      this.error = "Please enter a valid contribution amount";
      return;
    }

    if (!this.orderMethod) {
      this.error = "Please select an order method";
      return;
    }

    this.error = null;
    this.isSubmitting = true;

    // Map frequency to backend expected format (e.g. 'monthly' -> 'MONTH')
    let paymentFrequency = "MONTH";
    switch (this.frequency) {
      case DarFrequency.WEEKLY:
        paymentFrequency = "WEEKLY";
        break;
      case DarFrequency.BI_WEEKLY:
        paymentFrequency = "BI-WEEKLY";
        break;
      case DarFrequency.MONTHLY:
        paymentFrequency = "MONTH";
        break;
      case DarFrequency.QUARTERLY:
        paymentFrequency = "QUARTERLY";
        break;
      default:
        paymentFrequency = "MONTH";
    }

    const request: CreateDarRequest = {
      name: this.darName.trim(),
      monthlyContribution: this.monthlyAmount,
      paymentFrequency: paymentFrequency,
      orderMethod: this.orderMethod,
      customRules: this.rules.trim() || undefined,
    };

    this.darService
      .createDar(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isSubmitting = false)),
      )
      .subscribe({
        next: (dar) => {
          console.log("Dâr created successfully:", dar);
          // Navigate to the newly created Dâr details page
          this.router.navigate(["/dashboard/client/dar", dar.id]);
        },
        error: (err) => {
          console.error("Error creating Dâr:", err);
          this.error =
            err.error?.message || "Failed to create Dâr. Please try again.";
        },
      });
  }

  onCancel(): void {
    this.router.navigate(["/dashboard/client/my-dars"]);
  }
}
