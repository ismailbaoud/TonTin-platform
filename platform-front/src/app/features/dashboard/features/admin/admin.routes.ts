import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: 'dashboard/admin',
    loadComponent: () =>
      import('./pages/admin.component').then((m) => m.AdminComponent),
    data: {
      title: 'Admin Dashboard - TonTin',
      roles: ['ROLE_ADMIN'],
    },
  },
];
