import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { DashboardHomeComponent } from './pages/dashboard-home/dashboard-home.component';

/**
 * Dashboard Routing Module
 *
 * Defines routes for the dashboard feature module.
 * All routes are protected by AuthGuard.
 *
 * Route Structure:
 * - '' (empty): Dashboard home/overview page
 * - 'analytics': Analytics dashboard (example)
 * - 'reports': Reports dashboard (example)
 */
const routes: Routes = [
  {
    path: '',
    component: DashboardHomeComponent,
    canActivate: [AuthGuard],
    data: {
      title: 'Dashboard',
      breadcrumb: 'Dashboard'
    }
  },
  {
    path: 'analytics',
    loadComponent: () => import('./pages/dashboard-analytics/dashboard-analytics.component')
      .then(c => c.DashboardAnalyticsComponent),
    canActivate: [AuthGuard],
    data: {
      title: 'Analytics',
      breadcrumb: 'Analytics',
      roles: ['ADMIN', 'ANALYST']
    }
  },
  {
    path: 'reports',
    loadComponent: () => import('./pages/dashboard-reports/dashboard-reports.component')
      .then(c => c.DashboardReportsComponent),
    canActivate: [AuthGuard],
    data: {
      title: 'Reports',
      breadcrumb: 'Reports',
      permissions: ['VIEW_REPORTS']
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }
