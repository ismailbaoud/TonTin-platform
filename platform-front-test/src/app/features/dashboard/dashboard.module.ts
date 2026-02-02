import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { SharedModule } from '../../shared/shared.module';

// Pages (Smart Components)
import { DashboardHomeComponent } from './pages/dashboard-home/dashboard-home.component';

// Components (Presentational Components)
import { DashboardStatsCardComponent } from './components/dashboard-stats-card/dashboard-stats-card.component';
import { DashboardChartComponent } from './components/dashboard-chart/dashboard-chart.component';
import { DashboardRecentActivityComponent } from './components/dashboard-recent-activity/dashboard-recent-activity.component';
import { DashboardQuickActionsComponent } from './components/dashboard-quick-actions/dashboard-quick-actions.component';

// Services
import { DashboardService } from './services/dashboard.service';
import { DashboardStateService } from './services/dashboard-state.service';

/**
 * Dashboard Feature Module
 *
 * This module encapsulates all dashboard-related functionality.
 * It follows the smart/dumb component pattern and is lazy-loaded.
 *
 * Key Features:
 * - Overview statistics and KPIs
 * - Interactive charts and graphs
 * - Recent activity feed
 * - Quick action buttons
 * - Real-time data updates
 *
 * Architecture:
 * - pages/: Smart/Container components with business logic
 * - components/: Dumb/Presentational components (UI only)
 * - services/: Feature-specific services
 * - models/: Feature-specific interfaces and types
 */
@NgModule({
  declarations: [
    // Smart Components (Pages)
    DashboardHomeComponent,

    // Dumb Components (Presentational)
    DashboardStatsCardComponent,
    DashboardChartComponent,
    DashboardRecentActivityComponent,
    DashboardQuickActionsComponent
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    SharedModule // Provides reusable components, directives, and pipes
  ],
  providers: [
    // Feature-scoped services
    DashboardService,
    DashboardStateService
  ]
})
export class DashboardModule {
  constructor() {
    // Module loaded successfully
    console.log('Dashboard module loaded');
  }
}
