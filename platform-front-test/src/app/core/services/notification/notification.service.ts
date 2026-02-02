import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

/**
 * Notification Type Enum
 */
export enum NotificationType {
  Success = 'success',
  Error = 'error',
  Warning = 'warning',
  Info = 'info'
}

/**
 * Notification Interface
 */
export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  duration?: number;
  dismissible?: boolean;
  timestamp: Date;
}

/**
 * Notification Service
 *
 * Manages application-wide notifications and toast messages.
 *
 * Key Features:
 * - Multiple notification types (success, error, warning, info)
 * - Auto-dismissible notifications
 * - Observable-based notification stream
 * - Notification queue management
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationSubject = new Subject<Notification>();
  private notifications: Notification[] = [];
  private readonly DEFAULT_DURATION = 5000; // 5 seconds
  private readonly MAX_NOTIFICATIONS = 5;

  /**
   * Observable stream of notifications
   */
  public notification$ = this.notificationSubject.asObservable();

  constructor() {}

  /**
   * Show success notification
   */
  success(message: string, title: string = 'Success', duration?: number): void {
    this.show({
      type: NotificationType.Success,
      title,
      message,
      duration: duration ?? this.DEFAULT_DURATION,
      dismissible: true
    });
  }

  /**
   * Show error notification
   */
  error(message: string, title: string = 'Error', duration?: number): void {
    this.show({
      type: NotificationType.Error,
      title,
      message,
      duration: duration ?? this.DEFAULT_DURATION,
      dismissible: true
    });
  }

  /**
   * Show warning notification
   */
  warning(message: string, title: string = 'Warning', duration?: number): void {
    this.show({
      type: NotificationType.Warning,
      title,
      message,
      duration: duration ?? this.DEFAULT_DURATION,
      dismissible: true
    });
  }

  /**
   * Show info notification
   */
  info(message: string, title: string = 'Info', duration?: number): void {
    this.show({
      type: NotificationType.Info,
      title,
      message,
      duration: duration ?? this.DEFAULT_DURATION,
      dismissible: true
    });
  }

  /**
   * Show notification with custom options
   */
  show(options: Partial<Notification>): void {
    const notification: Notification = {
      id: this.generateId(),
      type: options.type ?? NotificationType.Info,
      title: options.title ?? '',
      message: options.message ?? '',
      duration: options.duration,
      dismissible: options.dismissible ?? true,
      timestamp: new Date()
    };

    // Manage notification queue
    if (this.notifications.length >= this.MAX_NOTIFICATIONS) {
      this.notifications.shift();
    }

    this.notifications.push(notification);
    this.notificationSubject.next(notification);

    // Auto-dismiss if duration is set
    if (notification.duration && notification.duration > 0) {
      setTimeout(() => {
        this.dismiss(notification.id);
      }, notification.duration);
    }
  }

  /**
   * Dismiss notification by ID
   */
  dismiss(id: string): void {
    const index = this.notifications.findIndex(n => n.id === id);
    if (index !== -1) {
      this.notifications.splice(index, 1);
    }
  }

  /**
   * Clear all notifications
   */
  clearAll(): void {
    this.notifications = [];
  }

  /**
   * Get all active notifications
   */
  getAll(): Notification[] {
    return [...this.notifications];
  }

  /**
   * Generate unique notification ID
   */
  private generateId(): string {
    return `notification_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Show confirmation dialog (returns promise)
   */
  confirm(
    message: string,
    title: string = 'Confirm',
    confirmText: string = 'Confirm',
    cancelText: string = 'Cancel'
  ): Promise<boolean> {
    return new Promise((resolve) => {
      // This would typically integrate with a modal service
      // For now, using browser confirm
      const result = window.confirm(`${title}\n\n${message}`);
      resolve(result);
    });
  }
}
