import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from "@angular/forms";
import { Router, RouterModule } from "@angular/router";
import { AuthService } from "../../services/auth.service";
import { AuthNavbarComponent } from "../../../../shared/components/navigation/auth-navbar.component";

@Component({
  selector: "app-reset-password",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    AuthNavbarComponent,
  ],
  templateUrl: "./reset-password.component.html",
  styleUrls: ["./reset-password.component.scss"],
})
export class ResetPasswordComponent implements OnInit {
  resetForm!: FormGroup;
  isSubmitting = false;
  errorMessage = "";
  successMessage = "";

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.resetForm = this.fb.group({
      emailOrUsername: ["", [Validators.required]],
    });
  }

  get emailOrUsername() {
    return this.resetForm.get("emailOrUsername");
  }

  onSubmit(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = "";
    this.successMessage = "";

    const emailOrUsername = this.resetForm.value.emailOrUsername;

    // TODO: Replace with actual API call
    setTimeout(() => {
      this.isSubmitting = false;
      this.successMessage =
        "If an account exists with this email/username, you will receive a password reset link shortly.";
      this.resetForm.reset();

      // Redirect to login after 3 seconds
      setTimeout(() => {
        this.router.navigate(["/auth/login"]);
      }, 3000);
    }, 1500);

    // Uncomment when backend is ready:
    /*
    this.authService.resetPassword(emailOrUsername).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = 'If an account exists with this email/username, you will receive a password reset link shortly.';
        this.resetForm.reset();

        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 3000);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Unable to process your request. Please try again.';
      }
    });
    */
  }

  onBackToLogin(): void {
    this.router.navigate(["/auth/login"]);
  }
}
