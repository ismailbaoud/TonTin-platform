import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth/auth.service';
import { StorageService } from '../services/storage/storage.service';

/**
 * Auth Interceptor
 *
 * Intercepts all HTTP requests and adds authentication token to the headers.
 * Also handles token refresh if the token is expired.
 *
 * Key Features:
 * - Adds Bearer token to Authorization header
 * - Excludes authentication endpoints from token injection
 * - Handles token refresh on 401 errors
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;

  constructor(
    private authService: AuthService,
    private storageService: StorageService
  ) {}

  /**
   * Intercept HTTP requests and add authentication token
   */
  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    // Skip token injection for authentication endpoints
    if (this.isAuthEndpoint(request.url)) {
      return next.handle(request);
    }

    // Clone request and add token if available
    const token = this.storageService.getToken();
    if (token) {
      request = this.addToken(request, token);
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle 401 Unauthorized errors
        if (error.status === 401 && !this.isRefreshing) {
          return this.handle401Error(request, next);
        }

        return throwError(() => error);
      })
    );
  }

  /**
   * Add authentication token to request headers
   */
  private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  /**
   * Handle 401 error by attempting to refresh the token
   */
  private handle401Error(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    this.isRefreshing = true;

    return this.authService.refreshToken().pipe(
      switchMap((response: any) => {
        this.isRefreshing = false;
        const newToken = response.token || response.accessToken;

        if (newToken) {
          this.storageService.setToken(newToken);
          return next.handle(this.addToken(request, newToken));
        }

        // If no token received, logout user
        this.authService.logout();
        return throwError(() => new Error('Token refresh failed'));
      }),
      catchError((error) => {
        this.isRefreshing = false;
        this.authService.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Check if the URL is an authentication endpoint
   */
  private isAuthEndpoint(url: string): boolean {
    const authEndpoints = [
      '/auth/login',
      '/auth/register',
      '/auth/refresh',
      '/auth/forgot-password',
      '/auth/reset-password'
    ];

    return authEndpoints.some(endpoint => url.includes(endpoint));
  }
}
