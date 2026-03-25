import { Routes } from "@angular/router";
import { authGuard } from "./features/auth/guards/auth.guard";
import { guestGuard } from "./features/auth/guards/guest.guard";
import { roleGuard } from "./core/guards/role.guard";

export const routes: Routes = [
  {
    path: "",
    loadComponent: () =>
      import("./features/public/home/landing.component").then(
        (m) => m.LandingComponent,
      ),
    data: {
      title: "TonTin - The Premium Dâr Management Standard",
    },
  },
  {
    path: "about",
    loadComponent: () =>
      import("./features/public/about/about.component").then(
        (m) => m.AboutComponent,
      ),
    data: {
      title: "About Us - TonTin",
    },
  },
  {
    path: "contact",
    loadComponent: () =>
      import("./features/public/contact/contact.component").then(
        (m) => m.ContactComponent,
      ),
    data: {
      title: "Contact Us - TonTin",
    },
  },
  {
    path: "auth/register",
    loadComponent: () =>
      import("./features/auth/pages/register/register.component").then(
        (m) => m.RegisterComponent,
      ),
    canActivate: [guestGuard],
    data: {
      title: "Create Account - TonTin",
    },
  },
  {
    path: "auth/login",
    loadComponent: () =>
      import("./features/auth/pages/login/login.component").then(
        (m) => m.LoginComponent,
      ),
    canActivate: [guestGuard],
    data: {
      title: "Login - TonTin",
    },
  },
  {
    path: "auth/reset-password",
    loadComponent: () =>
      import("./features/auth/pages/reset-password/reset-password.component").then(
        (m) => m.ResetPasswordComponent,
      ),
    canActivate: [guestGuard],
    data: {
      title: "Reset Password - TonTin",
    },
  },
  {
    path: "dashboard/client",
    loadComponent: () =>
      import("./shared/layouts/client-layout/client-layout.component").then(
        (m) => m.ClientLayoutComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ["ROLE_CLIENT", "ROLE_ADMIN"],
    },
    children: [
      {
        path: "",
        loadComponent: () =>
          import("./features/dashboard/features/overview/pages/client.component").then(
            (m) => m.ClientComponent,
          ),
        data: {
          title: "Client Dashboard - TonTin",
        },
      },
      {
        path: "my-dars",
        loadComponent: () =>
          import("./features/dashboard/features/dars/pages/my-dars.component").then(
            (m) => m.MyDarsComponent,
          ),
        data: {
          title: "My Dârs - TonTin",
        },
      },
      {
        path: "create-dar",
        loadComponent: () =>
          import("./features/dashboard/features/dars/pages/create-dar.component").then(
            (m) => m.CreateDarComponent,
          ),
        data: {
          title: "Create Dâr - TonTin",
        },
      },
      {
        path: "reports",
        loadComponent: () =>
          import("./features/dashboard/features/reports/pages/reports.component").then(
            (m) => m.ReportsComponent,
          ),
        data: {
          title: "Reports - TonTin",
        },
      },
      {
        path: "dar/:id",
        loadComponent: () =>
          import("./features/dashboard/features/dars/pages/dar-details.component").then(
            (m) => m.DarDetailsComponent,
          ),
        data: {
          title: "Dâr Details - TonTin",
        },
      },
      {
        path: "pay-contribution",
        loadComponent: () =>
          import("./features/dashboard/features/payments/pages/pay-contribution.component").then(
            (m) => m.PayContributionComponent,
          ),
        data: {
          title: "Pay Contribution - TonTin",
        },
      },
      {
        path: "pay-contribution/:id",
        loadComponent: () =>
          import("./features/dashboard/features/payments/pages/pay-contribution.component").then(
            (m) => m.PayContributionComponent,
          ),
        data: {
          title: "Pay Contribution - TonTin",
        },
      },
      {
        path: "profile",
        loadComponent: () =>
          import("./features/dashboard/features/profile/pages/profile.component").then(
            (m) => m.ProfileComponent,
          ),
        data: {
          title: "Profile Settings - TonTin",
        },
      },
    ],
  },
  {
    path: "dashboard/admin",
    loadComponent: () =>
      import("./shared/layouts/client-layout/client-layout.component").then(
        (m) => m.ClientLayoutComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: {
      title: "Admin Dashboard - TonTin",
      roles: ["ROLE_ADMIN"],
    },
    children: [
      {
        path: "",
        loadComponent: () =>
          import(
            "./features/dashboard/features/admin/pages/admin.component"
          ).then((m) => m.AdminComponent),
        data: {
          title: "Admin Dashboard - TonTin",
        },
      },
      {
        path: "users",
        loadComponent: () =>
          import(
            "./features/dashboard/features/admin/pages/admin-users.component"
          ).then((m) => m.AdminUsersComponent),
        data: {
          title: "User Management - TonTin",
        },
      },
      {
        path: "dars",
        loadComponent: () =>
          import(
            "./features/dashboard/features/admin/pages/admin-dars.component"
          ).then((m) => m.AdminDarsComponent),
        data: {
          title: "Dâr Management - TonTin",
        },
      },
      {
        path: "transactions",
        loadComponent: () =>
          import(
            "./features/dashboard/features/admin/pages/admin-transactions.component"
          ).then((m) => m.AdminTransactionsComponent),
        data: {
          title: "Transaction Management - TonTin",
        },
      },
      {
        path: "payments",
        loadComponent: () =>
          import(
            "./features/dashboard/features/admin/pages/admin-payments.component"
          ).then((m) => m.AdminPaymentsComponent),
        data: {
          title: "Payments - TonTin",
        },
      },
      {
        path: "settings",
        loadComponent: () =>
          import(
            "./features/dashboard/features/admin/pages/admin-settings.component"
          ).then((m) => m.AdminSettingsComponent),
        data: {
          title: "Admin Settings - TonTin",
        },
      },
    ],
  },
  {
    path: "**",
    redirectTo: "",
    pathMatch: "full",
  },
];
