/**
 * Development Environment Configuration
 *
 * This file contains environment-specific settings for development.
 * Values here will be used when running the application in development mode.
 */
export const environment = {
  production: false,

  /**
   * API Configuration
   */
  apiUrl: 'http://localhost:3000/api',
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
    enableAnalytics: false,
    enableLogging: true,
    enableDebugMode: true,
    enablePerformanceMonitoring: false,
    enableErrorReporting: false,
  },

  /**
   * Logging Configuration
   */
  logging: {
    logLevel: 'DEBUG', // DEBUG, INFO, WARN, ERROR
    enableConsole: true,
    enableRemote: false,
    remoteLogUrl: '',
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
    enableEncryption: false,
    encryptionKey: '',
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
    defaultTTL: 300000, // 5 minutes in milliseconds
    maxSize: 100, // Maximum number of cached items
  },

  /**
   * Third-party Service Configuration
   */
  services: {
    google: {
      apiKey: '',
      analyticsId: '',
    },
    stripe: {
      publicKey: '',
    },
    sentry: {
      dsn: '',
    },
  },

  /**
   * WebSocket Configuration
   */
  websocket: {
    enabled: false,
    url: 'ws://localhost:3000',
    reconnectInterval: 5000,
    maxReconnectAttempts: 5,
  },

  /**
   * Upload Configuration
   */
  upload: {
    maxFileSize: 10485760, // 10MB in bytes
    allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'],
    uploadUrl: 'http://localhost:3000/api/upload',
  },
};
