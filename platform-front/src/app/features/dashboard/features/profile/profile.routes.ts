import { Routes } from '@angular/router';

export const PROFILE_ROUTES: Routes = [
  {
    path: 'profile',
    loadComponent: () =>
      import('./pages/profile.component').then((m) => m.ProfileComponent),
    data: {
      title: 'Profile Settings - TonTin',
    },
  },
];
