import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable, BehaviorSubject, tap, interval } from "rxjs";
import { switchMap, filter } from "rxjs/operators";
import { environment } from "../../../../../../environments/environment";

export interface Notification {
  id: number;
  userId: number;
  type:
    | "payment_due"
    | "payment_received"
    | "payout_ready"
    | "dar_invitation"
    | "member_joined"
    | "member_left"
    | "tour_completed"
    | "reminder"
    | "system"
    | "trust_score"
    | "message";
  title: string;
  message: string;
  data?: any;
  priority: "low" | "medium" | "high" | "urgent";
  isRead: boolean;
  isArchived: boolean;
  actionUrl?: string;
  actionLabel?: string;
  icon?: string;
  imageUrl?: string;
  relatedEntityType?: "dar" | "payment" | "user" | "contribution";
  relatedEntityId?: number;
  createdDate: string;
  readDate?: string;
  expiresAt?: string;
}

export interface NotificationPreferences {
  id: number;
  userId: number;
  emailNotifications: boolean;
  pushNotifications: boolean;
  smsNotifications: boolean;
  paymentReminders: boolean;
  darUpdates: boolean;
  memberActivity: boolean;
  marketingEmails: boolean;
  weeklyDigest: boolean;
  instantAlerts: boolean;
  quietHoursEnabled: boolean;
  quietHoursStart?: string;
  quietHoursEnd?: string;
}

export interface NotificationSettings {
  channels: {
    email: boolean;
    push: boolean;
    sms: boolean;
  };
  types: {
    paymentDue: boolean;
    paymentReceived: boolean;
    payoutReady: boolean;
    darInvitation: boolean;
    memberActivity: boolean;
    tourCompleted: boolean;
    reminders: boolean;
    systemAlerts: boolean;
    trustScore: boolean;
    messages: boolean;
  };
  frequency: "instant" | "daily" | "weekly";
  quietHours: {
    enabled: boolean;
    start: string;
    end: string;
  };
}

export interface NotificationSummary {
  unreadCount: number;
  totalCount: number;
  unreadByType: {
    [key: string]: number;
  };
  hasUrgent: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
  last: boolean;
  first: boolean;
}

@Injectable({
  providedIn: "root",
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  private preferencesApiUrl = `${environment.apiUrl}/notification-preferences`;

  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  private preferencesSubject =
    new BehaviorSubject<NotificationPreferences | null>(null);
  public preferences$ = this.preferencesSubject.asObservable();

  private pollingInterval$ = interval(30000); // Poll every 30 seconds
  private isPolling = false;

  constructor(private http: HttpClient) {}

  /**
   * Get all notifications for current user
   */
  getNotifications(
    page: number = 0,
    size: number = 20,
    type?: string,
    isRead?: boolean,
  ): Observable<PaginatedResponse<Notification>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString())
      .set("sort", "createdDate,desc");

    if (type) {
      params = params.set("type", type);
    }
    if (isRead !== undefined) {
      params = params.set("isRead", isRead.toString());
    }

    return this.http
      .get<PaginatedResponse<Notification>>(this.apiUrl, { params })
      .pipe(
        tap((response) => {
          if (page === 0) {
            this.notificationsSubject.next(response.content);
          }
        }),
      );
  }

  /**
   * Get notification summary (counts)
   */
  getSummary(): Observable<NotificationSummary> {
    return this.http
      .get<NotificationSummary>(`${this.apiUrl}/summary`)
      .pipe(
        tap((summary) => this.unreadCountSubject.next(summary.unreadCount)),
      );
  }

  /**
   * Get unread count
   */
  getUnreadCount(): Observable<number> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/unread-count`).pipe(
      tap((response) => this.unreadCountSubject.next(response.count)),
      switchMap((response) => [response.count]),
    );
  }

  /**
   * Get a specific notification
   */
  getNotification(notificationId: number): Observable<Notification> {
    return this.http.get<Notification>(`${this.apiUrl}/${notificationId}`);
  }

  /**
   * Mark notification as read
   */
  markAsRead(notificationId: number): Observable<Notification> {
    return this.http
      .put<Notification>(`${this.apiUrl}/${notificationId}/read`, {})
      .pipe(
        tap((notification) => {
          this.updateNotificationInCache(notification);
          this.decrementUnreadCount();
        }),
      );
  }

  /**
   * Mark multiple notifications as read
   */
  markMultipleAsRead(notificationIds: number[]): Observable<void> {
    return this.http
      .put<void>(`${this.apiUrl}/mark-read`, { notificationIds })
      .pipe(
        tap(() => {
          const current = this.notificationsSubject.value;
          const updated = current.map((n) =>
            notificationIds.includes(n.id) ? { ...n, isRead: true } : n,
          );
          this.notificationsSubject.next(updated);
          this.getUnreadCount().subscribe();
        }),
      );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/mark-all-read`, {}).pipe(
      tap(() => {
        const current = this.notificationsSubject.value;
        const updated = current.map((n) => ({ ...n, isRead: true }));
        this.notificationsSubject.next(updated);
        this.unreadCountSubject.next(0);
      }),
    );
  }

  /**
   * Mark notification as unread
   */
  markAsUnread(notificationId: number): Observable<Notification> {
    return this.http
      .put<Notification>(`${this.apiUrl}/${notificationId}/unread`, {})
      .pipe(
        tap((notification) => {
          this.updateNotificationInCache(notification);
          this.incrementUnreadCount();
        }),
      );
  }

  /**
   * Archive notification
   */
  archiveNotification(notificationId: number): Observable<void> {
    return this.http
      .put<void>(`${this.apiUrl}/${notificationId}/archive`, {})
      .pipe(
        tap(() => {
          const current = this.notificationsSubject.value;
          this.notificationsSubject.next(
            current.filter((n) => n.id !== notificationId),
          );
        }),
      );
  }

  /**
   * Archive multiple notifications
   */
  archiveMultiple(notificationIds: number[]): Observable<void> {
    return this.http
      .put<void>(`${this.apiUrl}/archive-multiple`, { notificationIds })
      .pipe(
        tap(() => {
          const current = this.notificationsSubject.value;
          this.notificationsSubject.next(
            current.filter((n) => !notificationIds.includes(n.id)),
          );
        }),
      );
  }

  /**
   * Delete notification
   */
  deleteNotification(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`).pipe(
      tap(() => {
        const current = this.notificationsSubject.value;
        this.notificationsSubject.next(
          current.filter((n) => n.id !== notificationId),
        );
        const notification = current.find((n) => n.id === notificationId);
        if (notification && !notification.isRead) {
          this.decrementUnreadCount();
        }
      }),
    );
  }

  /**
   * Delete multiple notifications
   */
  deleteMultiple(notificationIds: number[]): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/delete-multiple`, {
        body: { notificationIds },
      })
      .pipe(
        tap(() => {
          const current = this.notificationsSubject.value;
          this.notificationsSubject.next(
            current.filter((n) => !notificationIds.includes(n.id)),
          );
          this.getUnreadCount().subscribe();
        }),
      );
  }

  /**
   * Delete all read notifications
   */
  deleteAllRead(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete-all-read`).pipe(
      tap(() => {
        const current = this.notificationsSubject.value;
        this.notificationsSubject.next(current.filter((n) => !n.isRead));
      }),
    );
  }

  /**
   * Get notification preferences
   */
  getPreferences(): Observable<NotificationPreferences> {
    return this.http
      .get<NotificationPreferences>(this.preferencesApiUrl)
      .pipe(tap((prefs) => this.preferencesSubject.next(prefs)));
  }

  /**
   * Update notification preferences
   */
  updatePreferences(
    preferences: Partial<NotificationPreferences>,
  ): Observable<NotificationPreferences> {
    return this.http
      .put<NotificationPreferences>(this.preferencesApiUrl, preferences)
      .pipe(tap((prefs) => this.preferencesSubject.next(prefs)));
  }

  /**
   * Test notification (send a test notification)
   */
  sendTestNotification(type: string): Observable<Notification> {
    return this.http.post<Notification>(`${this.apiUrl}/test`, { type });
  }

  /**
   * Get notifications by type
   */
  getNotificationsByType(
    type: string,
    page: number = 0,
    size: number = 20,
  ): Observable<PaginatedResponse<Notification>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString())
      .set("sort", "createdDate,desc");

    return this.http.get<PaginatedResponse<Notification>>(
      `${this.apiUrl}/by-type/${type}`,
      { params },
    );
  }

  /**
   * Get notifications by related entity
   */
  getNotificationsByEntity(
    entityType: string,
    entityId: number,
    page: number = 0,
    size: number = 20,
  ): Observable<PaginatedResponse<Notification>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString())
      .set("sort", "createdDate,desc");

    return this.http.get<PaginatedResponse<Notification>>(
      `${this.apiUrl}/by-entity/${entityType}/${entityId}`,
      { params },
    );
  }

  /**
   * Start polling for new notifications
   */
  startPolling(): void {
    if (this.isPolling) return;

    this.isPolling = true;
    this.pollingInterval$
      .pipe(
        filter(() => this.isPolling),
        switchMap(() => this.getUnreadCount()),
      )
      .subscribe();
  }

  /**
   * Stop polling for new notifications
   */
  stopPolling(): void {
    this.isPolling = false;
  }

  /**
   * Subscribe to real-time notifications (WebSocket/SSE)
   * TODO: Implement WebSocket/SSE connection
   */
  subscribeToRealtime(): void {
    // Placeholder for WebSocket/SSE implementation
    console.log("Real-time notifications not yet implemented");
  }

  /**
   * Unsubscribe from real-time notifications
   */
  unsubscribeFromRealtime(): void {
    // Placeholder for WebSocket/SSE cleanup
    console.log("Unsubscribing from real-time notifications");
  }

  /**
   * Request browser push notification permission
   */
  requestPushPermission(): Promise<NotificationPermission> {
    if (!("Notification" in window)) {
      console.warn("This browser does not support notifications");
      return Promise.resolve("denied");
    }

    return Notification.requestPermission();
  }

  /**
   * Check if push notifications are supported
   */
  isPushSupported(): boolean {
    return "Notification" in window;
  }

  /**
   * Get current push notification permission status
   */
  getPushPermissionStatus(): NotificationPermission {
    if (!("Notification" in window)) {
      return "denied";
    }
    return Notification.permission;
  }

  /**
   * Clear all cached notifications
   */
  clearCache(): void {
    this.notificationsSubject.next([]);
    this.unreadCountSubject.next(0);
    this.preferencesSubject.next(null);
  }

  /**
   * Helper: Update a notification in the cache
   */
  private updateNotificationInCache(notification: Notification): void {
    const current = this.notificationsSubject.value;
    const index = current.findIndex((n) => n.id === notification.id);
    if (index !== -1) {
      const updated = [...current];
      updated[index] = notification;
      this.notificationsSubject.next(updated);
    }
  }

  /**
   * Helper: Increment unread count
   */
  private incrementUnreadCount(): void {
    const current = this.unreadCountSubject.value;
    this.unreadCountSubject.next(current + 1);
  }

  /**
   * Helper: Decrement unread count
   */
  private decrementUnreadCount(): void {
    const current = this.unreadCountSubject.value;
    if (current > 0) {
      this.unreadCountSubject.next(current - 1);
    }
  }

  /**
   * Get notification icon based on type
   */
  getNotificationIcon(type: string): string {
    const iconMap: { [key: string]: string } = {
      payment_due: "schedule",
      payment_received: "payments",
      payout_ready: "account_balance_wallet",
      dar_invitation: "group_add",
      member_joined: "person_add",
      member_left: "person_remove",
      tour_completed: "check_circle",
      reminder: "notifications_active",
      system: "info",
      trust_score: "verified",
      message: "message",
    };
    return iconMap[type] || "notifications";
  }

  /**
   * Get notification color based on priority
   */
  getNotificationColor(priority: string): string {
    const colorMap: { [key: string]: string } = {
      low: "text-gray-600",
      medium: "text-blue-600",
      high: "text-orange-600",
      urgent: "text-red-600",
    };
    return colorMap[priority] || "text-gray-600";
  }
}
