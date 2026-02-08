/**
 * Development Environment Configuration
 *
 * This file contains environment-specific settings for the development environment.
 * These settings are used when running `ng serve` or building with `--configuration development`.
 */

export const environment = {
  production: false,
  envName: "development",

  // API Configuration
  apiUrl: "http://localhost:9090/api",
  apiTimeout: 30000, // 30 seconds

  // Backend Endpoints
  endpoints: {
    auth: {
      login: "/v1/auth/login",
      logout: "/v1/auth/logout",
      register: "/v1/auth/register",
      refresh: "/v1/auth/refresh-token",
      resetPassword: "/v1/auth/reset-password",
      changePassword: "/v1/auth/change-password",
      verifyEmail: "/v1/auth/verify",
      me: "/v1/auth/me",
    },
    users: "/v1/users",
    profile: "/v1/users/profile",
    // Add your backend endpoints here
  },

  // Authentication
  auth: {
    tokenKey: "tontin_token",
    refreshTokenKey: "tontin_refresh_token",
    tokenExpiryKey: "tontin_token_expiry",
    userKey: "tontin_user",
    tokenPrefix: "Bearer",
    tokenExpirationMinutes: 60,
  },

  // Feature Flags
  features: {
    enableDebugMode: true,
    enableLogging: true,
    enableDevTools: true,
    enableMockApi: false,
    enableServiceWorker: false,
  },

  // Logging Configuration
  logging: {
    level: "debug", // 'debug' | 'info' | 'warn' | 'error'
    enableConsole: true,
    enableRemote: false,
    remoteUrl: "",
  },

  // Cache Configuration
  cache: {
    enabled: false,
    defaultTTL: 5 * 60 * 1000, // 5 minutes in milliseconds
  },

  // Pagination
  pagination: {
    defaultPageSize: 10,
    pageSizeOptions: [5, 10, 20, 50, 100],
  },

  // File Upload
  upload: {
    maxFileSize: 10 * 1024 * 1024, // 10MB in bytes
    allowedFileTypes: [
      "image/jpeg",
      "image/png",
      "image/gif",
      "application/pdf",
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    ],
  },

  // Toast Notifications
  toast: {
    timeOut: 5000,
    positionClass: "toast-top-right",
    preventDuplicates: true,
    closeButton: true,
    progressBar: true,
  },

  // Date/Time Format
  dateFormat: {
    short: "MM/dd/yyyy",
    medium: "MMM dd, yyyy",
    long: "MMMM dd, yyyy",
    full: "EEEE, MMMM dd, yyyy",
    dateTime: "MM/dd/yyyy HH:mm:ss",
    time: "HH:mm:ss",
  },

  // Internationalization
  i18n: {
    defaultLanguage: "en",
    availableLanguages: ["en", "fr", "es"],
    fallbackLanguage: "en",
  },

  // Error Tracking (e.g., Sentry)
  errorTracking: {
    enabled: false,
    dsn: "",
    environment: "development",
  },

  // Analytics
  analytics: {
    enabled: false,
    trackingId: "",
  },

  // External Services
  external: {
    googleMapsApiKey: "",
    stripePublicKey: "",
  },
};

export type Environment = typeof environment;
