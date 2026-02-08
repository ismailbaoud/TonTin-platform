import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";

@Component({
  selector: "app-auth-navbar",
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header
      class="fixed top-0 z-50 w-full px-6 py-4 bg-white border-b border-gray-100"
    >
      <nav class="mx-auto max-w-7xl flex items-center justify-between">
        <!-- Logo -->
        <div class="flex items-center cursor-pointer" (click)="navigateHome()">
          <img src="assets/logo.png" alt="TonTin Logo" class="h-16 w-16" />
        </div>

        <!-- Empty space for future items -->
        <div></div>
      </nav>
    </header>
  `,
  styles: [
    `
      :host {
        display: block;
      }
    `,
  ],
})
export class AuthNavbarComponent {
  constructor(private router: Router) {}

  navigateHome(): void {
    this.router.navigate(["/"]);
  }
}
