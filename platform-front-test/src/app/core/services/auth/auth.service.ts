import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { map, tap, catchError, shareReplay } from 'rxjs/operators';
import { Router } from '@angular/router';
import { StorageService } from '../storage/storage.service';
import { LoggerService } from '../logger/logger.service';

/**
 * User Interface
 */
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  permissions: string[];
  avatar?: string;
  phoneNumber?: string;
  isActive: boolean;
  lastLogin?: Date;
}

/**
 * Login Request Interface
 */
export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

/**
 * Login Response Interface
 */
export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}

/**
 * Register Request Interface
 */
export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
}

/**
 * Auth Service
 *
 * Manages authentication state and operations throughout the application.
 *
 * Key Features:
 * - User authentication (login, logout, register)
 * - Token management (access token, refresh token)
 * - User session management
 * - Role and permission checking
 * - Auto-login on app initialization
 * - Observable-based state management
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/auth'; // Update with your API URL
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  /**
   * Current user as BehaviorSubject for reactive updates
   */
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  /**
   * Authentication state
   */
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    private storageService: StorageService,
    private logger: LoggerService
  ) {
    this.initializeAuth();
  }

  /**
   * Initialize authentication state from storage
   */
  private initializeAuth(): void {
    const user = this.storageService.getItem<User>(this.USER_KEY);
    const token = this.storageService.getToken();

    if (user && token && !this.isTokenExpired(token)) {
      this.currentUserSubject.next(user);
      this.isAuthenticatedSubject.next(true);
      this.logger.info('User session restored from storage');
    } else {
      this.clearAuthData();
    }
  }

  /**
   * Get current user value (synchronous)
   */
  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get authentication status (synchronous)
   */
  get isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  /**
   * Login user with email and password
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => {
        this.handleAuthSuccess(response);
        this.logger.info('User logged in successfully', response.user.email);
      }),
      catchError(error => {
        this.logger.error('Login failed', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Register new user
   */
  register(userData: RegisterRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/register`, userData).pipe(
      tap(response => {
        this.handleAuthSuccess(response);
        this.logger.info('User registered successfully', response.user.email);
      }),
      catchError(error => {
        this.logger.error('Registration failed', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Logout user
   */
  logout(): void {
    // Call logout endpoint if needed
    this.http.post(`${this.API_URL}/logout`, {}).pipe(
      catchError(error => {
        this.logger.error('Logout endpoint failed', error);
        return of(null);
      })
    ).subscribe();

    this.clearAuthData();
    this.router.navigate(['/auth/login']);
    this.logger.info('User logged out');
  }

  /**
   * Refresh authentication token
   */
  refreshToken(): Observable<any> {
    const refreshToken = this.storageService.getItem<string>(this.REFRESH_TOKEN_KEY);

    if (!refreshToken) {
      this.logger.error('No refresh token available');
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http.post<LoginResponse>(`${this.API_URL}/refresh`, { refreshToken }).pipe(
      tap(response => {
        this.storageService.setToken(response.token);
        if (response.refreshToken) {
          this.storageService.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
        }
        this.logger.info('Token refreshed successfully');
      }),
      catchError(error => {
        this.logger.error('Token refresh failed', error);
        this.clearAuthData();
        return throwError(() => error);
      }),
      shareReplay(1)
    );
  }

  /**
   * Get current user profile from server
   */
  getUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/profile`).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        this.storageService.setItem(this.USER_KEY, user);
        this.logger.info('User profile updated');
      }),
      catchError(error => {
        this.logger.error('Failed to fetch user profile', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Update user profile
   */
  updateProfile(userData: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/profile`, userData).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        this.storageService.setItem(this.USER_KEY, user);
        this.logger.info('User profile updated');
      }),
      catchError(error => {
        this.logger.error('Failed to update profile', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Change user password
   */
  changePassword(currentPassword: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.API_URL}/change-password`, {
      currentPassword,
      newPassword
    }).pipe(
      tap(() => {
        this.logger.info('Password changed successfully');
      }),
      catchError(error => {
        this.logger.error('Password change failed', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Request password reset
   */
  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.API_URL}/forgot-password`, { email }).pipe(
      tap(() => {
        this.logger.info('Password reset email sent to', email);
      }),
      catchError(error => {
        this.logger.error('Password reset request failed', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Reset password with token
   */
  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.API_URL}/reset-password`, {
      token,
      newPassword
    }).pipe(
      tap(() => {
        this.logger.info('Password reset successfully');
      }),
      catchError(error => {
        this.logger.error('Password reset failed', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: string): boolean {
    const user = this.currentUserValue;
    return user?.roles?.includes(role) ?? false;
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserValue;
    return roles.some(role => user?.roles?.includes(role)) ?? false;
  }

  /**
   * Check if user has all of the specified roles
   */
  hasAllRoles(roles: string[]): boolean {
    const user = this.currentUserValue;
    return roles.every(role => user?.roles?.includes(role)) ?? false;
  }

  /**
   * Check if user has specific permission
   */
  hasPermission(permission: string): boolean {
    const user = this.currentUserValue;
    return user?.permissions?.includes(permission) ?? false;
  }

  /**
   * Check if user has any of the specified permissions
   */
  hasAnyPermission(permissions: string[]): boolean {
    const user = this.currentUserValue;
    return permissions.some(permission => user?.permissions?.includes(permission)) ?? false;
  }

  /**
   * Check if user has all of the specified permissions
   */
  hasAllPermissions(permissions: string[]): boolean {
    const user = this.currentUserValue;
    return permissions.every(permission => user?.permissions?.includes(permission)) ?? false;
  }

  /**
   * Handle successful authentication
   */
  private handleAuthSuccess(response: LoginResponse): void {
    // Store tokens
    this.storageService.setToken(response.token);
    this.storageService.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);

    // Store user data
    this.storageService.setItem(this.USER_KEY, response.user);

    // Update observables
    this.currentUserSubject.next(response.user);
    this.isAuthenticatedSubject.next(true);
  }

  /**
   * Clear all authentication data
   */
  private clearAuthData(): void {
    this.storageService.removeToken();
    this.storageService.removeItem(this.REFRESH_TOKEN_KEY);
    this.storageService.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * Check if token is expired
   */
  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationDate = new Date(payload.exp * 1000);
      return expirationDate < new Date();
    } catch (error) {
      this.logger.error('Failed to parse token', error);
      return true;
    }
  }
}
