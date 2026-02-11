import { Routes } from '@angular/router';

export const NOTIFICATIONS_ROUTES: Routes = [
  {
    path: 'notifications',
    loadComponent: () =>
      import('./pages/notifications.component').then((m) => m.NotificationsComponent),
    data: {
      title: 'Notifications - TonTin',
    },
  },
];
