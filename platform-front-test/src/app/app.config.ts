import { ApplicationConfig, importProvidersFrom } from "@angular/core";
import { provideRouter } from "@angular/router";
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from "@angular/common/http";
import { provideAnimations } from "@angular/platform-browser/animations";
import { routes } from "./app.routes";
import { CoreModule } from "./core/core.module";

/**
 * Application Configuration
 *
 * This file contains the main configuration for the Angular application.
 * It sets up providers for routing, HTTP client, animations, and the Core module.
 *
 * Key Features:
 * - Router configuration with lazy loading
 * - HTTP client with interceptors
 * - Browser animations
 * - Core module providers (singleton services, guards, interceptors)
 */
export const appConfig: ApplicationConfig = {
  providers: [
    // Routing
    provideRouter(routes),

    // HTTP Client with interceptors
    provideHttpClient(withInterceptorsFromDi()),

    // Animations
    provideAnimations(),

    // Core Module providers (guards, interceptors, singleton services)
    importProvidersFrom(CoreModule),
  ],
};
