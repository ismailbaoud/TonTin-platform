import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";

@Component({
  selector: "app-public-navbar",
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header class="fixed top-0 z-[100] w-full px-6 py-4">
      <nav
        class="mx-auto max-w-7xl bg-white/70 backdrop-blur-xl border border-white/40 shadow-[0_8px_32px_0_rgba(16,185,129,0.07)] rounded-2xl px-6 py-3 flex items-center justify-between"
      >
        <div class="flex items-center cursor-pointer" (click)="navigateHome()">
          <img src="assets/logo.png" alt="TonTin Logo" class="h-16 w-16" />
        </div>
        <div class="hidden md:flex items-center gap-10">
          <a
            routerLink="/dashboard"
            routerLinkActive="text-primary"
            [routerLinkActiveOptions]="{ exact: false }"
            class="text-sm font-semibold text-slate-600 hover:text-primary transition-colors cursor-pointer"
            >Dashboard</a
          >
          <a
            routerLink="/about"
            routerLinkActive="text-primary"
            [routerLinkActiveOptions]="{ exact: true }"
            class="text-sm font-semibold text-slate-600 hover:text-primary transition-colors cursor-pointer"
            >About Us</a
          >
          <a
            routerLink="/contact"
            routerLinkActive="text-primary"
            [routerLinkActiveOptions]="{ exact: true }"
            class="text-sm font-semibold text-slate-600 hover:text-primary transition-colors cursor-pointer"
            >Contact Us</a
          >
        </div>
        <div class="flex items-center gap-4">
          <button
            (click)="navigateToLogin()"
            class="hidden sm:block text-sm font-bold text-slate-700 hover:text-primary px-4 py-2 transition-colors"
          >
            Log In
          </button>
          <button
            (click)="navigateToRegister()"
            class="bg-slate-900 text-white px-6 py-2.5 rounded-xl text-sm font-bold hover:bg-primary transition-all shadow-lg hover:shadow-primary/25 active:scale-95"
          >
            Get Started
          </button>
        </div>
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
export class PublicNavbarComponent {
  constructor(private router: Router) {}

  navigateHome(): void {
    this.router.navigate(["/"]);
  }

  navigateToLogin(): void {
    this.router.navigate(["/auth/login"]);
  }

  navigateToRegister(): void {
    this.router.navigate(["/auth/register"]);
  }
}
