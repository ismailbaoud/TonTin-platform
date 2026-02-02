import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { Router } from '@angular/router';
import { LoggerService } from '../services/logger/logger.service';
import { NotificationService } from '../services/notification/notification.service';

/**
 * Error Interceptor
 *
 * Centralized error handling for all HTTP requests.
 * Provides consistent error handling, logging, and user notifications.
 *
 * Key Features:
 * - Catches and processes HTTP errors
 * - Logs errors for debugging
 * - Shows user-friendly error messages
 * - Handles different error status codes
 * - Implements retry logic for failed requests
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private readonly MAX_RETRIES = 2;
  private readonly RETRY_DELAY = 1000;

  constructor(
    private router: Router,
    private logger: LoggerService,
    private notificationService: NotificationService
  ) {}

  /**
   * Intercept HTTP requests and handle errors
   */
  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      // Retry failed requests (only for GET requests)
      retry({
        count: request.method === 'GET' ? this.MAX_RETRIES : 0,
        delay: this.RETRY_DELAY
      }),
      catchError((error: HttpErrorResponse) => {
        return this.handleError(error, request);
      })
    );
  }

  /**
   * Handle HTTP errors based on status code
   */
  private handleError(
    error: HttpErrorResponse,
    request: HttpRequest<unknown>
  ): Observable<never> {
    let errorMessage = 'An unexpected error occurred';
    let errorTitle = 'Error';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Network Error: ${error.error.message}`;
      errorTitle = 'Connection Error';
      this.logger.error('Client-side error:', error.error.message);
    } else {
      // Backend returned an unsuccessful response code
      errorMessage = this.getServerErrorMessage(error);
      errorTitle = this.getErrorTitle(error.status);

      this.logger.error(
        `Backend returned code ${error.status}`,
        `URL: ${request.url}`,
        `Message: ${errorMessage}`
      );

      // Handle specific error codes
      this.handleSpecificErrors(error.status);
    }

    // Show notification to user (except for 401 - handled by auth interceptor)
    if (error.status !== 401) {
      this.notificationService.error(errorMessage, errorTitle);
    }

    // Return error for component-level handling
    return throwError(() => ({
      status: error.status,
      message: errorMessage,
      error: error.error,
      timestamp: new Date()
    }));
  }

  /**
   * Extract error message from server response
   */
  private getServerErrorMessage(error: HttpErrorResponse): string {
    // Try to extract message from various possible error structures
    if (error.error?.message) {
      return error.error.message;
    }

    if (error.error?.error) {
      return typeof error.error.error === 'string'
        ? error.error.error
        : JSON.stringify(error.error.error);
    }

    if (error.message) {
      return error.message;
    }

    // Default messages based on status code
    switch (error.status) {
      case 0:
        return 'Unable to connect to the server. Please check your internet connection.';
      case 400:
        return 'Invalid request. Please check your input and try again.';
      case 401:
        return 'You are not authorized. Please log in.';
      case 403:
        return 'You do not have permission to access this resource.';
      case 404:
        return 'The requested resource was not found.';
      case 408:
        return 'Request timeout. Please try again.';
      case 409:
        return 'A conflict occurred. The resource may have been modified.';
      case 422:
        return 'Validation error. Please check your input.';
      case 429:
        return 'Too many requests. Please wait and try again.';
      case 500:
        return 'Internal server error. Please try again later.';
      case 502:
        return 'Bad gateway. The server is temporarily unavailable.';
      case 503:
        return 'Service unavailable. Please try again later.';
      case 504:
        return 'Gateway timeout. The server took too long to respond.';
      default:
        return `An error occurred (Status: ${error.status})`;
    }
  }

  /**
   * Get error title based on status code
   */
  private getErrorTitle(status: number): string {
    if (status >= 500) {
      return 'Server Error';
    } else if (status >= 400) {
      return 'Request Error';
    } else if (status === 0) {
      return 'Connection Error';
    }
    return 'Error';
  }

  /**
   * Handle specific error codes with custom logic
   */
  private handleSpecificErrors(status: number): void {
    switch (status) {
      case 401:
        // Handled by auth interceptor
        break;
      case 403:
        // Redirect to unauthorized page
        this.router.navigate(['/unauthorized']);
        break;
      case 404:
        // Could redirect to 404 page if needed
        // this.router.navigate(['/not-found']);
        break;
      case 500:
      case 502:
      case 503:
        // Could redirect to maintenance page
        // this.router.navigate(['/maintenance']);
        break;
      default:
        // No specific action
        break;
    }
  }
}
