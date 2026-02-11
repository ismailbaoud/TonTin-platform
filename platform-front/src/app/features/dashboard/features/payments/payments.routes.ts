import { Routes } from '@angular/router';

export const PAYMENTS_ROUTES: Routes = [
  {
    path: 'pay-contribution',
    loadComponent: () =>
      import('./pages/pay-contribution.component').then((m) => m.PayContributionComponent),
    data: {
      title: 'Pay Contribution - TonTin',
    },
  },
  {
    path: 'pay-contribution/:id',
    loadComponent: () =>
      import('./pages/pay-contribution.component').then((m) => m.PayContributionComponent),
    data: {
      title: 'Pay Contribution - TonTin',
    },
  },
];
