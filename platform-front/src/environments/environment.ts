/**
 * Production Environment Configuration
 *
 * This file contains environment-specific settings for the production environment.
 * These settings are used when building with `--configuration production`.
 */

export const environment = {
  production: true,
  envName: "production",

  // API Configuration
  apiUrl: "https://api.tontin.example.com/api",
  apiTimeout: 30000, // 30 seconds

  // Backend Endpoints
  endpoints: {
    auth: {
      login: "/auth/login",
      logout: "/auth/logout",
      register: "/auth/register",
      refresh: "/auth/refresh",
      resetPassword: "/auth/reset-password",
      changePassword: "/auth/change-password",
      verifyEmail: "/auth/verify-email",
    },
    users: "/users",
    profile: "/users/profile",
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
    enableDebugMode: false,
    enableLogging: false,
    enableDevTools: false,
    enableMockApi: false,
    enableServiceWorker: true,
  },

  // Logging Configuration
  logging: {
    level: "error", // 'debug' | 'info' | 'warn' | 'error'
    enableConsole: false,
    enableRemote: true,
    remoteUrl: "https://logs.tontin.example.com/api/logs",
  },

  // Cache Configuration
  cache: {
    enabled: true,
    defaultTTL: 15 * 60 * 1000, // 15 minutes in milliseconds
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
    timeOut: 3000,
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
    enabled: true,
    dsn: "https://your-sentry-dsn@sentry.io/project-id",
    environment: "production",
  },

  // Analytics
  analytics: {
    enabled: true,
    trackingId: "G-XXXXXXXXXX",
  },

  // External Services
  external: {
    googleMapsApiKey: "",
    stripePublicKey: "",
  },
};

export type Environment = typeof environment;
