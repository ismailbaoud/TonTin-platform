import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

// Interceptors
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { ErrorInterceptor } from './interceptors/error.interceptor';
import { LoadingInterceptor } from './interceptors/loading.interceptor';

// Guards
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

// Services
import { AuthService } from './services/auth/auth.service';
import { StorageService } from './services/storage/storage.service';
import { LoggerService } from './services/logger/logger.service';
import { NotificationService } from './services/notification/notification.service';
import { LoadingService } from './services/loading/loading.service';

/**
 * Core Module
 *
 * This module should be imported only once in the AppModule.
 * It contains singleton services, interceptors, and guards that are used throughout the application.
 *
 * Key Features:
 * - Singleton services (Auth, Storage, Logger, etc.)
 * - HTTP Interceptors
 * - Route Guards
 * - Prevents re-import with constructor check
 */
@NgModule({
  declarations: [],
  imports: [
    CommonModule
  ],
  providers: [
    // HTTP Client with interceptors
    provideHttpClient(withInterceptorsFromDi()),

    // HTTP Interceptors
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LoadingInterceptor,
      multi: true
    },

    // Guards
    AuthGuard,
    RoleGuard,

    // Services
    AuthService,
    StorageService,
    LoggerService,
    NotificationService,
    LoadingService
  ]
})
export class CoreModule {
  /**
   * Prevents re-import of the CoreModule
   * Ensures it's only imported once in AppModule
   */
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error(
        'CoreModule is already loaded. Import it in the AppModule only.'
      );
    }
  }
}
