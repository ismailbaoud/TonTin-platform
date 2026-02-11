import { Routes } from '@angular/router';

export const OVERVIEW_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/client.component').then((m) => m.ClientComponent),
    data: {
      title: 'Client Dashboard - TonTin',
    },
  },
];
