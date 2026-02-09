import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { PublicNavbarComponent } from "../../../shared/components/navigation/public-navbar.component";

@Component({
  selector: "app-contact",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, PublicNavbarComponent],
  templateUrl: "./contact.component.html",
  styleUrl: "./contact.component.scss",
})
export class ContactComponent {
  // Form data
  contactForm = {
    name: "",
    email: "",
    subject: "",
    message: "",
  };

  isSubmitting = false;
  submitSuccess = false;
  submitError = false;

  constructor(private router: Router) {}

  navigateToDashboard(): void {
    this.router.navigate(["/dashboard"]);
  }

  navigateToAbout(): void {
    this.router.navigate(["/about"]);
  }

  navigateHome(): void {
    this.router.navigate(["/"]);
  }

  async onSubmit(): Promise<void> {
    if (
      !this.contactForm.name ||
      !this.contactForm.email ||
      !this.contactForm.message
    ) {
      return;
    }

    this.isSubmitting = true;
    this.submitError = false;

    try {
      // TODO: Replace with actual API call
      await new Promise((resolve) => setTimeout(resolve, 1500));

      console.log("Contact form submitted:", this.contactForm);

      this.submitSuccess = true;
      this.resetForm();

      // Hide success message after 5 seconds
      setTimeout(() => {
        this.submitSuccess = false;
      }, 5000);
    } catch (error) {
      console.error("Error submitting contact form:", error);
      this.submitError = true;
    } finally {
      this.isSubmitting = false;
    }
  }

  resetForm(): void {
    this.contactForm = {
      name: "",
      email: "",
      subject: "",
      message: "",
    };
  }
}
