import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { PublicNavbarComponent } from "../../../shared/components/navigation/public-navbar.component";

@Component({
  selector: "app-about",
  standalone: true,
  imports: [CommonModule, RouterModule, PublicNavbarComponent],
  templateUrl: "./about.component.html",
  styleUrl: "./about.component.scss",
})
export class AboutComponent {
  constructor(private router: Router) {}

  navigateToDashboard(): void {
    this.router.navigate(["/dashboard"]);
  }

  navigateToContact(): void {
    this.router.navigate(["/contact"]);
  }

  navigateHome(): void {
    this.router.navigate(["/"]);
  }
}
