import { Injectable } from "@angular/core";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { environment } from "../../../../environments/environment.development";

/**
 * Authentication Service
 *
 * This service handles authentication logic with the backend API.
 * Manages login, registration, logout, and token operations.
 */

// Models/Interfaces matching backend DTOs
export interface RegisterRequest {
  userName: string;
  email: string;
  password: string;
}

export interface RegisterResponse {
  id: string;
  userName: string;
  email: string;
  creationDate: string;
  emailConfirmed: boolean;
  accountAccessFileCount: number;
  resetPasswordDate: string | null;
  role: string;
  picture: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: UserResponse;
}

export interface UserResponse {
  id: string;
  userName: string;
  email: string;
  creationDate: string;
  emailConfirmed: boolean;
  accountAccessFileCount: number;
  resetPasswordDate: string | null;
  role: string;
  picture: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface RefreshTokenRequest {
  token: string;
}

export interface AuthenticationResponse {
  token: string;
  refreshToken: string;
}

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Register a new user
   *
   * @param data Registration data containing username, email, and password
   * @returns Observable with user response
   */
  register(data: {
    userName: string;
    email: string;
    password: string;
  }): Observable<RegisterResponse> {
    const registerRequest: RegisterRequest = {
      userName: data.userName,
      email: data.email,
      password: data.password,
    };

    console.log("📝 Registering user:", registerRequest.userName);

    return this.http
      .post<RegisterResponse>(
        `${this.apiUrl}/v1/auth/register`,
        registerRequest,
      )
      .pipe(
        tap((response) => {
          console.log("✅ Registration successful:", response.userName);
        }),
        catchError(this.handleError),
      );
  }

  /**
   * Login user
   *
   * @param data Login credentials (email/username and password)
   * @returns Observable with login response containing tokens and user data
   */
  login(data: {
    emailOrUsername: string;
    password: string;
  }): Observable<LoginResponse> {
    // Transform the frontend format to backend format
    const loginRequest: LoginRequest = {
      email: data.emailOrUsername.includes("@")
        ? data.emailOrUsername
        : `${data.emailOrUsername}@tontin.com`,
      password: data.password,
    };

    console.log("🔐 Logging in user:", loginRequest.email);

    return this.http
      .post<LoginResponse>(`${this.apiUrl}/v1/auth/login`, loginRequest)
      .pipe(
        tap((response) => {
          console.log("✅ Login successful:", response.user.userName);
          // Store tokens in localStorage
          this.storeTokens(response.token, response.refreshToken);
          this.storeUser(response.user);
        }),
        catchError(this.handleError),
      );
  }

  /**
   * Logout user
   *
   * Clears local tokens and calls backend logout endpoint
   */
  logout(): Observable<{ message: string }> {
    console.log("👋 Logging out user");

    return this.http
      .post<{ message: string }>(`${this.apiUrl}/v1/auth/logout`, {})
      .pipe(
        tap(() => {
          this.clearTokens();
          console.log("✅ Logout successful");
        }),
        catchError((error) => {
          // Even if API call fails, clear local tokens
          this.clearTokens();
          return throwError(() => error);
        }),
      );
  }

  /**
   * Refresh authentication token
   *
   * @param refreshToken The refresh token
   * @returns Observable with new tokens
   */
  refreshToken(refreshToken: string): Observable<AuthenticationResponse> {
    const request: RefreshTokenRequest = { token: refreshToken };

    console.log("🔄 Refreshing token");

    return this.http
      .post<AuthenticationResponse>(
        `${this.apiUrl}/v1/auth/refresh-token`,
        request,
      )
      .pipe(
        tap((response) => {
          console.log("✅ Token refreshed successfully");
          this.storeTokens(response.token, response.refreshToken);
        }),
        catchError(this.handleError),
      );
  }

  /**
   * Get current authenticated user
   *
   * @returns Observable with current user data
   */
  getCurrentUser(): Observable<UserResponse> {
    console.log("👤 Fetching current user");

    return this.http.get<UserResponse>(`${this.apiUrl}/v1/auth/me`).pipe(
      tap((user) => {
        console.log("✅ Current user retrieved:", user.userName);
        this.storeUser(user);
      }),
      catchError(this.handleError),
    );
  }

  /**
   * Check if user is authenticated
   *
   * @returns true if user has valid token
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    // Check if token is expired
    const tokenExpiry = localStorage.getItem(environment.auth.tokenExpiryKey);
    if (tokenExpiry) {
      const expiryDate = new Date(tokenExpiry);
      return expiryDate > new Date();
    }

    return true;
  }

  /**
   * Get stored authentication token
   *
   * @returns JWT token or null
   */
  getToken(): string | null {
    return localStorage.getItem(environment.auth.tokenKey);
  }

  /**
   * Get stored refresh token
   *
   * @returns Refresh token or null
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(environment.auth.refreshTokenKey);
  }

  /**
   * Get stored user data
   *
   * @returns User object or null
   */
  getStoredUser(): UserResponse | null {
    const userJson = localStorage.getItem(environment.auth.userKey);
    if (userJson) {
      try {
        return JSON.parse(userJson);
      } catch (error) {
        console.error("Error parsing stored user data:", error);
        return null;
      }
    }
    return null;
  }

  /**
   * Store authentication tokens in localStorage
   *
   * @param token JWT access token
   * @param refreshToken JWT refresh token
   */
  private storeTokens(token: string, refreshToken: string): void {
    localStorage.setItem(environment.auth.tokenKey, token);
    localStorage.setItem(environment.auth.refreshTokenKey, refreshToken);

    // Calculate and store token expiry
    const expiryDate = new Date();
    expiryDate.setMinutes(
      expiryDate.getMinutes() + environment.auth.tokenExpirationMinutes,
    );
    localStorage.setItem(
      environment.auth.tokenExpiryKey,
      expiryDate.toISOString(),
    );
  }

  /**
   * Store user data in localStorage
   *
   * @param user User data
   */
  private storeUser(user: UserResponse): void {
    localStorage.setItem(environment.auth.userKey, JSON.stringify(user));
  }

  /**
   * Clear all authentication data from localStorage
   */
  private clearTokens(): void {
    localStorage.removeItem(environment.auth.tokenKey);
    localStorage.removeItem(environment.auth.refreshTokenKey);
    localStorage.removeItem(environment.auth.tokenExpiryKey);
    localStorage.removeItem(environment.auth.userKey);
  }

  /**
   * Handle HTTP errors
   *
   * @param error HTTP error response
   * @returns Observable that errors with user-friendly message
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = "An error occurred. Please try again.";

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      console.error("Client-side error:", error.error.message);
      errorMessage = "Network error. Please check your connection.";
    } else {
      // Backend error
      console.error(`Backend error: ${error.status}`, error.error);

      // Extract error message from backend response
      if (error.error && typeof error.error === "object") {
        if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.error) {
          errorMessage = error.error.error;
        } else if (error.error.errors && Array.isArray(error.error.errors)) {
          errorMessage = error.error.errors.join(", ");
        }
      } else if (typeof error.error === "string") {
        errorMessage = error.error;
      }

      // Handle specific status codes
      switch (error.status) {
        case 400:
          if (!error.error || !error.error.message) {
            errorMessage = "Invalid request. Please check your input!";
          }
          break;
        case 401:
          errorMessage = "Invalid credentials. Please try again.";
          break;
        case 403:
          errorMessage = "Access denied. Your account may not be active.";
          break;
        case 404:
          errorMessage = "Resource not found.";
          break;
        case 409:
          errorMessage = "User already exists with this email or username.";
          break;
        case 500:
          errorMessage = "Server error. Please try again later.";
          break;
        case 0:
          errorMessage =
            "Cannot connect to server. Please check if the backend is running.";
          break;
      }
    }

    return throwError(() => ({ message: errorMessage }));
  }
}
