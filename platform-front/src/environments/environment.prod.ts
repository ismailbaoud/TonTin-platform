/**
 * Production Environment Configuration
 *
 * This file contains environment-specific settings for production.
 * Values here will be used when running the application in production mode.
 */
export const environment = {
  production: true,

  /**
   * API Configuration
   */
  apiUrl: 'https://api.yourdomain.com/api',
  apiVersion: 'v1',
  apiTimeout: 30000, // 30 seconds

  /**
   * Authentication Configuration
   */
  auth: {
    tokenKey: 'auth_token',
    refreshTokenKey: 'refresh_token',
    tokenExpirationBuffer: 300, // 5 minutes in seconds
    enableAutoRefresh: true,
  },

  /**
   * Feature Flags
   */
  features: {
    enableAnalytics: true,
    enableLogging: true,
    enableDebugMode: false,
    enablePerformanceMonitoring: true,
    enableErrorReporting: true,
  },

  /**
   * Logging Configuration
   */
  logging: {
    logLevel: 'WARN', // DEBUG, INFO, WARN, ERROR
    enableConsole: false,
    enableRemote: true,
    remoteLogUrl: 'https://api.yourdomain.com/logs',
  },

  /**
   * Application Configuration
   */
  app: {
    name: 'Advanced Angular App',
    version: '1.0.0',
    defaultLanguage: 'en',
    supportedLanguages: ['en', 'fr', 'es'],
    dateFormat: 'yyyy-MM-dd',
    timeFormat: 'HH:mm:ss',
  },

  /**
   * Storage Configuration
   */
  storage: {
    type: 'localStorage', // 'localStorage' or 'sessionStorage'
    enableEncryption: true,
    encryptionKey: 'YOUR_ENCRYPTION_KEY_HERE',
  },

  /**
   * Pagination Configuration
   */
  pagination: {
    defaultPageSize: 10,
    pageSizeOptions: [5, 10, 25, 50, 100],
  },

  /**
   * Cache Configuration
   */
  cache: {
    enabled: true,
    defaultTTL: 600000, // 10 minutes in milliseconds
    maxSize: 200, // Maximum number of cached items
  },

  /**
   * Third-party Service Configuration
   */
  services: {
    google: {
      apiKey: 'YOUR_GOOGLE_API_KEY',
      analyticsId: 'YOUR_GA_TRACKING_ID',
    },
    stripe: {
      publicKey: 'YOUR_STRIPE_PUBLIC_KEY',
    },
    sentry: {
      dsn: 'YOUR_SENTRY_DSN',
    },
  },

  /**
   * WebSocket Configuration
   */
  websocket: {
    enabled: true,
    url: 'wss://api.yourdomain.com',
    reconnectInterval: 5000,
    maxReconnectAttempts: 5,
  },

  /**
   * Upload Configuration
   */
  upload: {
    maxFileSize: 10485760, // 10MB in bytes
    allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'],
    uploadUrl: 'https://api.yourdomain.com/api/upload',
  },
};
