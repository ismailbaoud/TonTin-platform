import { Routes } from '@angular/router';

export const TRUST_RANKINGS_ROUTES: Routes = [
  {
    path: 'trust-rankings',
    loadComponent: () =>
      import('./pages/trust-rankings.component').then((m) => m.TrustRankingsComponent),
    data: {
      title: 'Trust Rankings - TonTin',
    },
  },
];
