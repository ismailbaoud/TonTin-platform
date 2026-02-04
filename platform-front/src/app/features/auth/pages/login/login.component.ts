import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from "@angular/forms";
import { Router, RouterModule, ActivatedRoute } from "@angular/router";
import { AuthService } from "../../services/auth.service";
import { AuthNavbarComponent } from "../../../../shared/components/navigation/auth-navbar.component";

@Component({
  selector: "app-login",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    AuthNavbarComponent,
  ],
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  showPassword = false;
  isSubmitting = false;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
  ) {
    console.log("LoginComponent constructor called");
  }

  ngOnInit(): void {
    console.log("LoginComponent ngOnInit called");
    this.initializeForm();
  }

  private initializeForm(): void {
    this.loginForm = this.fb.group({
      emailOrUsername: ["", [Validators.required]],
      password: ["", [Validators.required]],
    });
    console.log("Login form initialized:", this.loginForm);
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  get emailOrUsername() {
    return this.loginForm.get("emailOrUsername");
  }

  get password() {
    return this.loginForm.get("password");
  }

  onSubmit(): void {
    console.log("Form submitted");
    if (this.loginForm.invalid) {
      this.markAllFieldsAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const loginData = {
      emailOrUsername: this.loginForm.value.emailOrUsername,
      password: this.loginForm.value.password,
    };

    console.log("Login data:", loginData);

    // Call auth service
    this.authService.login(loginData).subscribe({
      next: (response) => {
        console.log("Login successful:", response);

        // Get return URL from query params or redirect based on role
        const returnUrl = this.route.snapshot.queryParams["returnUrl"];

        if (returnUrl) {
          this.router.navigateByUrl(returnUrl);
        } else {
          // Redirect based on user role (backend sends ROLE_ADMIN or ROLE_CLIENT)
          const userRole = response.user.role.toUpperCase();

          if (userRole === "ROLE_ADMIN" || userRole === "ADMIN") {
            this.router.navigate(["/dashboard/admin"]);
          } else {
            this.router.navigate(["/dashboard/client"]);
          }
        }
      },
      error: (error) => {
        console.error("Login failed:", error);
        this.errorMessage =
          error.message || "Login failed. Please check your credentials.";
        this.isSubmitting = false;
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  private markAllFieldsAsTouched(): void {
    Object.keys(this.loginForm.controls).forEach((key) => {
      this.loginForm.get(key)?.markAsTouched();
    });
  }

  navigateToRegister(): void {
    this.router.navigate(["/auth/register"]);
  }

  onForgotPassword(): void {
    this.router.navigate(["/auth/reset-password"]);
  }
}
