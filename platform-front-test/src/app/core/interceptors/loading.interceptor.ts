import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, finalize } from 'rxjs/operators';
import { LoadingService } from '../services/loading/loading.service';

/**
 * Loading Interceptor
 *
 * Tracks HTTP request loading state and updates the LoadingService.
 * This allows displaying loading indicators throughout the application.
 *
 * Key Features:
 * - Automatically shows/hides loading indicator for HTTP requests
 * - Supports excluding specific endpoints from loading indicator
 * - Tracks multiple concurrent requests
 * - Provides request-specific loading states
 */
@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  private totalRequests = 0;
  private completedRequests = 0;

  /**
   * URLs that should not trigger the global loading indicator
   */
  private readonly excludedUrls: string[] = [
    '/auth/refresh',
    '/health',
    '/ping',
    '/notifications/poll'
  ];

  constructor(private loadingService: LoadingService) {}

  /**
   * Intercept HTTP requests and track loading state
   */
  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // Check if this URL should be excluded from loading tracking
    if (this.shouldExclude(request.url)) {
      return next.handle(request);
    }

    // Increment total requests and show loading
    this.totalRequests++;
    this.loadingService.show();

    // Track individual request
    const requestId = this.generateRequestId(request);
    this.loadingService.addRequest(requestId);

    return next.handle(request).pipe(
      tap((event: HttpEvent<any>) => {
        // Update progress if supported
        if (event instanceof HttpResponse) {
          this.completedRequests++;
          this.updateProgress();
        }
      }),
      finalize(() => {
        // Remove this request from tracking
        this.loadingService.removeRequest(requestId);

        // Hide loading indicator when all requests are complete
        this.totalRequests--;
        if (this.totalRequests === 0) {
          this.loadingService.hide();
          this.completedRequests = 0;
        }
      })
    );
  }

  /**
   * Check if a URL should be excluded from loading tracking
   */
  private shouldExclude(url: string): boolean {
    return this.excludedUrls.some(excludedUrl => url.includes(excludedUrl));
  }

  /**
   * Generate a unique identifier for the request
   */
  private generateRequestId(request: HttpRequest<unknown>): string {
    return `${request.method}-${request.urlWithParams}-${Date.now()}`;
  }

  /**
   * Update loading progress percentage
   */
  private updateProgress(): void {
    if (this.totalRequests > 0) {
      const progress = Math.round((this.completedRequests / this.totalRequests) * 100);
      this.loadingService.setProgress(progress);
    }
  }
}
