import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { HttpRequest, HttpHandlerFn, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment.development';

/**
 * HTTP Interceptor for JWT Authentication
 *
 * This interceptor:
 * 1. Adds JWT token to all outgoing requests (except auth endpoints)
 * 2. Handles 401 errors by attempting to refresh the token
 * 3. Adds necessary headers for API communication
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  // Skip token addition for auth endpoints (login, register, refresh)
  const isAuthEndpoint = req.url.includes('/auth/login') ||
                         req.url.includes('/auth/register') ||
                         req.url.includes('/auth/refresh-token') ||
                         req.url.includes('/auth/verify');

  if (isAuthEndpoint) {
    // Just add content-type header for auth requests
    const authReq = req.clone({
      setHeaders: {
        'Content-Type': 'application/json'
      }
    });
    return next(authReq);
  }

  // Get token from localStorage
  const token = localStorage.getItem(environment.auth.tokenKey);

  // Clone the request and add authorization header if token exists
  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {
        'Content-Type': 'application/json',
        'Authorization': `${environment.auth.tokenPrefix} ${token}`
      }
    });
  } else {
    authReq = req.clone({
      setHeaders: {
        'Content-Type': 'application/json'
      }
    });
  }

  // Handle the request
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && token) {
        // Token expired or invalid - attempt to refresh
        console.warn('🔄 Token expired, attempting refresh...');

        const refreshToken = localStorage.getItem(environment.auth.refreshTokenKey);

        if (refreshToken) {
          // TODO: Implement token refresh logic
          // For now, just clear tokens and let user re-login
          localStorage.removeItem(environment.auth.tokenKey);
          localStorage.removeItem(environment.auth.refreshTokenKey);
          localStorage.removeItem(environment.auth.tokenExpiryKey);
          localStorage.removeItem(environment.auth.userKey);

          console.warn('⚠️ Token refresh not implemented. Please login again.');
        }
      }

      return throwError(() => error);
    })
  );
};

/**
 * Provide this interceptor in app.config.ts:
 *
 * import { provideHttpClient, withInterceptors } from '@angular/common/http';
 * import { authInterceptor } from './core/interceptors/auth.interceptor';
 *
 * export const appConfig: ApplicationConfig = {
 *   providers: [
 *     provideHttpClient(
 *       withInterceptors([authInterceptor])
 *     ),
 *     // ... other providers
 *   ]
 * };
 */
