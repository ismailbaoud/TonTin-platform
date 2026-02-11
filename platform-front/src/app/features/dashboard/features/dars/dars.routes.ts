import { Routes } from '@angular/router';

export const DARS_ROUTES: Routes = [
  {
    path: 'my-dars',
    loadComponent: () =>
      import('./pages/my-dars.component').then((m) => m.MyDarsComponent),
    data: {
      title: 'My Dârs - TonTin',
    },
  },
  {
    path: 'create-dar',
    loadComponent: () =>
      import('./pages/create-dar.component').then((m) => m.CreateDarComponent),
    data: {
      title: 'Create Dâr - TonTin',
    },
  },
  {
    path: 'dar/:id',
    loadComponent: () =>
      import('./pages/dar-details.component').then((m) => m.DarDetailsComponent),
    data: {
      title: 'Dâr Details - TonTin',
    },
  },
];
