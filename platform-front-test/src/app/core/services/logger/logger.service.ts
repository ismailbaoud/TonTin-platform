import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';

/**
 * Log Level Enum
 */
export enum LogLevel {
  Debug = 0,
  Info = 1,
  Warn = 2,
  Error = 3,
  None = 4
}

/**
 * Log Entry Interface
 */
export interface LogEntry {
  level: LogLevel;
  message: string;
  timestamp: Date;
  data?: any[];
  stack?: string;
  source?: string;
}

/**
 * Logger Service
 *
 * Provides centralized logging functionality for the entire application.
 * Supports different log levels, formatting, and can be extended to send logs to external services.
 *
 * Key Features:
 * - Multiple log levels (Debug, Info, Warn, Error)
 * - Environment-based log level filtering
 * - Structured log entries
 * - Console output with formatting
 * - Log history storage
 * - Can be extended to send logs to remote logging services
 */
@Injectable({
  providedIn: 'root'
})
export class LoggerService {
  private logLevel: LogLevel = LogLevel.Debug;
  private logHistory: LogEntry[] = [];
  private readonly MAX_HISTORY_SIZE = 100;
  private readonly ENABLE_CONSOLE_LOGS = true;
  private readonly ENABLE_REMOTE_LOGGING = false; // Enable to send logs to external service

  constructor() {
    this.initializeLogLevel();
  }

  /**
   * Initialize log level based on environment
   */
  private initializeLogLevel(): void {
    // In production, only log warnings and errors
    if (environment.production) {
      this.logLevel = LogLevel.Warn;
    } else {
      this.logLevel = LogLevel.Debug;
    }
  }

  /**
   * Set the minimum log level
   */
  setLogLevel(level: LogLevel): void {
    this.logLevel = level;
    this.info('Log level changed to:', LogLevel[level]);
  }

  /**
   * Get the current log level
   */
  getLogLevel(): LogLevel {
    return this.logLevel;
  }

  /**
   * Log debug message
   */
  debug(message: string, ...data: any[]): void {
    this.log(LogLevel.Debug, message, ...data);
  }

  /**
   * Log info message
   */
  info(message: string, ...data: any[]): void {
    this.log(LogLevel.Info, message, ...data);
  }

  /**
   * Log warning message
   */
  warn(message: string, ...data: any[]): void {
    this.log(LogLevel.Warn, message, ...data);
  }

  /**
   * Log error message
   */
  error(message: string, ...data: any[]): void {
    this.log(LogLevel.Error, message, ...data);
  }

  /**
   * Core logging method
   */
  private log(level: LogLevel, message: string, ...data: any[]): void {
    // Check if this log level should be processed
    if (level < this.logLevel) {
      return;
    }

    // Create log entry
    const logEntry: LogEntry = {
      level,
      message,
      timestamp: new Date(),
      data: data.length > 0 ? data : undefined,
      source: this.getCallerInfo()
    };

    // Add stack trace for errors
    if (level === LogLevel.Error) {
      logEntry.stack = new Error().stack;
    }

    // Store in history
    this.addToHistory(logEntry);

    // Output to console
    if (this.ENABLE_CONSOLE_LOGS) {
      this.logToConsole(logEntry);
    }

    // Send to remote logging service if enabled
    if (this.ENABLE_REMOTE_LOGGING && level >= LogLevel.Warn) {
      this.sendToRemoteLogger(logEntry);
    }
  }

  /**
   * Output log entry to console with formatting
   */
  private logToConsole(entry: LogEntry): void {
    const timestamp = this.formatTimestamp(entry.timestamp);
    const levelName = LogLevel[entry.level];
    const prefix = `[${timestamp}] [${levelName}]`;

    const style = this.getConsoleStyle(entry.level);
    const message = `${prefix} ${entry.message}`;

    switch (entry.level) {
      case LogLevel.Debug:
        if (entry.data) {
          console.debug(`%c${message}`, style, ...entry.data);
        } else {
          console.debug(`%c${message}`, style);
        }
        break;
      case LogLevel.Info:
        if (entry.data) {
          console.info(`%c${message}`, style, ...entry.data);
        } else {
          console.info(`%c${message}`, style);
        }
        break;
      case LogLevel.Warn:
        if (entry.data) {
          console.warn(`%c${message}`, style, ...entry.data);
        } else {
          console.warn(`%c${message}`, style);
        }
        break;
      case LogLevel.Error:
        if (entry.data) {
          console.error(`%c${message}`, style, ...entry.data);
        } else {
          console.error(`%c${message}`, style);
        }
        if (entry.stack) {
          console.error('Stack trace:', entry.stack);
        }
        break;
    }
  }

  /**
   * Get console style for log level
   */
  private getConsoleStyle(level: LogLevel): string {
    switch (level) {
      case LogLevel.Debug:
        return 'color: #9E9E9E; font-weight: normal;';
      case LogLevel.Info:
        return 'color: #2196F3; font-weight: normal;';
      case LogLevel.Warn:
        return 'color: #FF9800; font-weight: bold;';
      case LogLevel.Error:
        return 'color: #F44336; font-weight: bold;';
      default:
        return 'color: #000000; font-weight: normal;';
    }
  }

  /**
   * Format timestamp for display
   */
  private formatTimestamp(date: Date): string {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const milliseconds = date.getMilliseconds().toString().padStart(3, '0');
    return `${hours}:${minutes}:${seconds}.${milliseconds}`;
  }

  /**
   * Get caller information from stack trace
   */
  private getCallerInfo(): string {
    try {
      const stack = new Error().stack;
      if (!stack) return 'Unknown';

      const stackLines = stack.split('\n');
      // Skip first 4 lines (Error, getCallerInfo, log, and the actual log method)
      const callerLine = stackLines[4];

      if (callerLine) {
        // Extract file and line number
        const match = callerLine.match(/at\s+(.+?)\s+\((.+?):(\d+):(\d+)\)/);
        if (match) {
          const fileName = match[2].split('/').pop();
          const lineNumber = match[3];
          return `${fileName}:${lineNumber}`;
        }
      }
    } catch (error) {
      // Ignore errors in getting caller info
    }
    return 'Unknown';
  }

  /**
   * Add log entry to history
   */
  private addToHistory(entry: LogEntry): void {
    this.logHistory.push(entry);

    // Keep history size limited
    if (this.logHistory.length > this.MAX_HISTORY_SIZE) {
      this.logHistory.shift();
    }
  }

  /**
   * Get log history
   */
  getHistory(): LogEntry[] {
    return [...this.logHistory];
  }

  /**
   * Get log history filtered by level
   */
  getHistoryByLevel(level: LogLevel): LogEntry[] {
    return this.logHistory.filter(entry => entry.level === level);
  }

  /**
   * Clear log history
   */
  clearHistory(): void {
    this.logHistory = [];
    this.info('Log history cleared');
  }

  /**
   * Export log history as JSON
   */
  exportHistory(): string {
    return JSON.stringify(this.logHistory, null, 2);
  }

  /**
   * Export log history as CSV
   */
  exportHistoryAsCSV(): string {
    const headers = ['Timestamp', 'Level', 'Message', 'Source'];
    const rows = this.logHistory.map(entry => [
      entry.timestamp.toISOString(),
      LogLevel[entry.level],
      entry.message,
      entry.source || ''
    ]);

    const csv = [
      headers.join(','),
      ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
    ].join('\n');

    return csv;
  }

  /**
   * Send logs to remote logging service
   * This is a placeholder - implement actual remote logging logic
   */
  private sendToRemoteLogger(entry: LogEntry): void {
    // Example: Send to external logging service
    // this.http.post('/api/logs', entry).subscribe();

    // For now, just log that it would be sent
    if (!environment.production) {
      console.log('Would send to remote logger:', entry);
    }
  }

  /**
   * Log performance measurement
   */
  logPerformance(label: string, startTime: number): void {
    const duration = performance.now() - startTime;
    this.debug(`Performance [${label}]:`, `${duration.toFixed(2)}ms`);
  }

  /**
   * Create a performance marker
   */
  startPerformanceMeasure(label: string): number {
    const startTime = performance.now();
    this.debug(`Performance measurement started: ${label}`);
    return startTime;
  }

  /**
   * Log API call
   */
  logApiCall(method: string, url: string, duration?: number, statusCode?: number): void {
    const message = `API ${method} ${url}`;
    const data = [];

    if (statusCode) {
      data.push(`Status: ${statusCode}`);
    }

    if (duration !== undefined) {
      data.push(`Duration: ${duration.toFixed(2)}ms`);
    }

    if (statusCode && statusCode >= 400) {
      this.error(message, ...data);
    } else {
      this.debug(message, ...data);
    }
  }

  /**
   * Log navigation event
   */
  logNavigation(from: string, to: string): void {
    this.debug('Navigation:', `${from} → ${to}`);
  }

  /**
   * Group logs together
   */
  group(label: string): void {
    if (this.ENABLE_CONSOLE_LOGS && this.logLevel <= LogLevel.Debug) {
      console.group(label);
    }
  }

  /**
   * End log group
   */
  groupEnd(): void {
    if (this.ENABLE_CONSOLE_LOGS && this.logLevel <= LogLevel.Debug) {
      console.groupEnd();
    }
  }

  /**
   * Log table data
   */
  table(data: any): void {
    if (this.ENABLE_CONSOLE_LOGS && this.logLevel <= LogLevel.Debug) {
      console.table(data);
    }
  }
}
