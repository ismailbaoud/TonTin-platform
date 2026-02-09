import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { PublicNavbarComponent } from "../../../shared/components/navigation/public-navbar.component";

@Component({
  selector: "app-landing",
  standalone: true,
  imports: [CommonModule, RouterModule, PublicNavbarComponent],
  templateUrl: "./landing.component.html",
  styleUrl: "./landing.component.scss",
})
export class LandingComponent implements OnInit {
  constructor(private router: Router) {
    console.log("🏠 Landing Page Component Constructor Called");
  }

  ngOnInit(): void {
    console.log("🏠 Landing Page Component Initialized - Route: '/'");
    console.log(
      "✅ You are viewing the landing page - No authentication required",
    );
  }

  navigateToLogin(): void {
    this.router.navigate(["/auth/login"]);
  }

  navigateToRegister(): void {
    this.router.navigate(["/auth/register"]);
  }

  launchApp(): void {
    this.router.navigate(["/auth/login"]);
  }

  scrollToSection(sectionId: string): void {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }
}
