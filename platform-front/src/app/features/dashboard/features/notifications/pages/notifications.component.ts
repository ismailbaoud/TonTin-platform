import { Component, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { Subject, takeUntil, finalize } from "rxjs";
import {
  NotificationService,
  Notification as ApiNotification,
} from "../services/notification.service";

interface Notification {
  id: number;
  type: "action" | "success" | "alert" | "general" | "system";
  icon: string;
  iconColor: string;
  title: string;
  message: string;
  timestamp: string;
  isRead: boolean;
  actionButton?: {
    label: string;
    route?: string;
    action?: () => void;
  };
  secondaryButton?: {
    label: string;
    route?: string;
    action?: () => void;
  };
  category?: string;
}

type NotificationTab = "all" | "unread" | "action" | "archived";

@Component({
  selector: "app-notifications",
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: "./notifications.component.html",
  styleUrl: "./notifications.component.scss",
})
export class NotificationsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  activeTab: NotificationTab = "all";
  searchQuery = "";
  isLoading = false;
  error: string | null = null;

  unreadCount = 0;
  currentPage = 0;
  pageSize = 20;
  totalPages = 0;
  hasMore = false;

  // Real notifications from service
  notifications: Notification[] = [];

  // Mock notifications data (fallback)
  mockNotifications: Notification[] = [
    {
      id: 1,
      type: "action",
      icon: "savings",
      iconColor: "text-primary",
      title: "Contribution Due: Family Circle",
      message:
        'Your monthly payment of <span class="text-gray-900 dark:text-white font-medium">$200</span> is due tomorrow. Please ensure your wallet has sufficient funds.',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), // 2 hours ago
      isRead: false,
      actionButton: {
        label: "Pay Now",
        route: "/dashboard/client/pay-contribution/1",
      },
      category: "today",
    },
    {
      id: 2,
      type: "success",
      icon: "celebration",
      iconColor: "text-primary",
      title: "Goal Achieved!",
      message:
        'Congratulations! You\'ve successfully saved <span class="text-gray-900 dark:text-white font-medium">$5,000</span> for your "Europe Trip" goal.',
      timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(), // 4 hours ago
      isRead: false,
      actionButton: {
        label: "View Savings",
        route: "/dashboard/client",
      },
      category: "today",
    },
    {
      id: 3,
      type: "alert",
      icon: "security",
      iconColor: "text-red-500",
      title: "New Login Detected",
      message: "A new login from Chrome on Windows was detected in London, UK.",
      timestamp: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(), // 6 hours ago
      isRead: true,
      actionButton: {
        label: "Review Activity",
        route: "/dashboard/client",
      },
      category: "today",
    },
    {
      id: 4,
      type: "general",
      icon: "group",
      iconColor: "text-gray-900 dark:text-white",
      title: "Dâr Invitation",
      message: 'James invited you to join "Office Saving Circle".',
      timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), // Yesterday
      isRead: true,
      actionButton: {
        label: "View Details",
        route: "/dashboard/client/dar/2",
      },
      secondaryButton: {
        label: "Decline",
        action: () => this.declineInvitation(4),
      },
      category: "yesterday",
    },
    {
      id: 5,
      type: "system",
      icon: "info",
      iconColor: "text-gray-900 dark:text-white",
      title: "Platform Maintenance",
      message:
        "Scheduled maintenance is complete. All systems are operational.",
      timestamp: new Date(Date.now() - 27 * 60 * 60 * 1000).toISOString(), // Yesterday
      isRead: true,
      category: "yesterday",
    },
    {
      id: 6,
      type: "action",
      icon: "payments",
      iconColor: "text-primary",
      title: "Payment Received",
      message: "Sarah sent $50 for the Weekend Trip Dâr.",
      timestamp: new Date(Date.now() - 28 * 60 * 60 * 1000).toISOString(),
      isRead: true,
      actionButton: {
        label: "View Transaction",
        route: "/dashboard/client/dar/3",
      },
      category: "yesterday",
    },
    {
      id: 7,
      type: "general",
      icon: "person_add",
      iconColor: "text-primary",
      title: "New Member Joined",
      message: "Michael Brown joined your Family Savings Circle.",
      timestamp: new Date(Date.now() - 48 * 60 * 60 * 1000).toISOString(), // 2 days ago
      isRead: true,
      category: "older",
    },
    {
      id: 8,
      type: "success",
      icon: "check_circle",
      iconColor: "text-primary",
      title: "Contribution Confirmed",
      message: "Your $200 contribution to Office Tech Dâr has been processed.",
      timestamp: new Date(Date.now() - 72 * 60 * 60 * 1000).toISOString(), // 3 days ago
      isRead: true,
      actionButton: {
        label: "View Receipt",
        route: "/dashboard/client",
      },
      category: "older",
    },
  ];

  constructor(
    private router: Router,
    private notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
    this.loadUnreadCount();
    // Start polling for new notifications
    this.notificationService.startPolling();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    // Stop polling when component is destroyed
    this.notificationService.stopPolling();
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.error = null;

    const isRead =
      this.activeTab === "unread"
        ? false
        : this.activeTab === "all"
          ? undefined
          : undefined;

    this.notificationService
      .getNotifications(this.currentPage, this.pageSize, undefined, isRead)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (response) => {
          const newNotifications = this.mapApiNotificationsToComponent(
            response.content,
          );

          if (this.currentPage === 0) {
            this.notifications = newNotifications;
          } else {
            this.notifications = [...this.notifications, ...newNotifications];
          }

          this.totalPages = response.totalPages;
          this.hasMore = !response.last;
        },
        error: (err) => {
          console.error("Error loading notifications:", err);
          this.error = "Failed to load notifications. Please try again.";
          // Fallback to mock data on error
          if (this.currentPage === 0) {
            this.notifications = this.mockNotifications;
          }
        },
      });
  }

  loadUnreadCount(): void {
    this.notificationService
      .getUnreadCount()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (count) => {
          this.unreadCount = count;
        },
        error: (err) => {
          console.error("Error loading unread count:", err);
        },
      });
  }

  private mapApiNotificationsToComponent(
    apiNotifications: ApiNotification[],
  ): Notification[] {
    return apiNotifications.map((notif) => ({
      id: notif.id,
      type: this.mapNotificationType(notif.type),
      icon:
        notif.icon || this.notificationService.getNotificationIcon(notif.type),
      iconColor: this.getIconColorForType(notif.type),
      title: notif.title,
      message: notif.message,
      timestamp: notif.createdDate,
      isRead: notif.isRead,
      actionButton: notif.actionUrl
        ? {
            label: notif.actionLabel || "View",
            route: notif.actionUrl,
          }
        : undefined,
      category: this.categorizeByDate(notif.createdDate),
    }));
  }

  private mapNotificationType(
    apiType: string,
  ): "action" | "success" | "alert" | "general" | "system" {
    const typeMap: {
      [key: string]: "action" | "success" | "alert" | "general" | "system";
    } = {
      payment_due: "action",
      payment_received: "success",
      payout_ready: "success",
      dar_invitation: "action",
      member_joined: "general",
      member_left: "general",
      tour_completed: "success",
      reminder: "action",
      system: "system",
      trust_score: "general",
      message: "general",
    };
    return typeMap[apiType] || "general";
  }

  private getIconColorForType(type: string): string {
    const colorMap: { [key: string]: string } = {
      payment_due: "text-orange-500",
      payment_received: "text-green-500",
      payout_ready: "text-primary",
      dar_invitation: "text-blue-500",
      member_joined: "text-green-500",
      member_left: "text-gray-500",
      tour_completed: "text-green-500",
      reminder: "text-orange-500",
      system: "text-gray-900 dark:text-white",
      trust_score: "text-primary",
      message: "text-blue-500",
    };
    return colorMap[type] || "text-gray-900 dark:text-white";
  }

  private categorizeByDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return "today";
    if (diffDays === 1) return "yesterday";
    return "older";
  }

  get filteredNotifications(): Notification[] {
    let filtered = this.notifications;

    // Filter by tab
    switch (this.activeTab) {
      case "unread":
        filtered = filtered.filter((n) => !n.isRead);
        break;
      case "action":
        filtered = filtered.filter(
          (n) => n.type === "action" && n.actionButton,
        );
        break;
      case "archived":
        // TODO: Implement archived notifications
        filtered = [];
        break;
      case "all":
      default:
        break;
    }

    // Filter by search query
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (n) =>
          n.title.toLowerCase().includes(query) ||
          n.message.toLowerCase().includes(query),
      );
    }

    return filtered;
  }

  get notificationsByCategory(): {
    today: Notification[];
    yesterday: Notification[];
    older: Notification[];
  } {
    const notifications = this.filteredNotifications;
    return {
      today: notifications.filter((n) => n.category === "today"),
      yesterday: notifications.filter((n) => n.category === "yesterday"),
      older: notifications.filter((n) => n.category === "older"),
    };
  }

  get displayedUnreadCount(): number {
    return this.notifications.filter((n) => !n.isRead).length;
  }

  get actionRequiredCount(): number {
    return this.notifications.filter(
      (n) => n.type === "action" && n.actionButton && !n.isRead,
    ).length;
  }

  get totalCount(): number {
    return this.notifications.length;
  }

  setTab(tab: NotificationTab): void {
    this.activeTab = tab;
    this.currentPage = 0;
    this.loadNotifications();
  }

  markAsRead(notificationId: number): void {
    const notification = this.notifications.find(
      (n) => n.id === notificationId,
    );
    if (notification) {
      notification.isRead = true;

      this.notificationService
        .markAsRead(notificationId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.loadUnreadCount();
          },
          error: (err) => {
            console.error("Error marking notification as read:", err);
            // Revert on error
            notification.isRead = false;
          },
        });
    }
  }

  markAllAsRead(): void {
    this.notificationService
      .markAllAsRead()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notifications.forEach((n) => (n.isRead = true));
          this.unreadCount = 0;
        },
        error: (err) => {
          console.error("Error marking all as read:", err);
          alert("Failed to mark all notifications as read. Please try again.");
        },
      });
  }

  deleteNotification(notificationId: number): void {
    const index = this.notifications.findIndex((n) => n.id === notificationId);
    if (index > -1) {
      const notification = this.notifications[index];

      this.notificationService
        .deleteNotification(notificationId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.notifications.splice(index, 1);
            if (!notification.isRead) {
              this.unreadCount = Math.max(0, this.unreadCount - 1);
            }
          },
          error: (err) => {
            console.error("Error deleting notification:", err);
            alert("Failed to delete notification. Please try again.");
          },
        });
    }
  }

  archiveNotification(notificationId: number): void {
    const index = this.notifications.findIndex((n) => n.id === notificationId);
    if (index > -1) {
      const notification = this.notifications[index];

      this.notificationService
        .archiveNotification(notificationId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.notifications.splice(index, 1);
          },
          error: (err) => {
            console.error("Error archiving notification:", err);
            alert("Failed to archive notification. Please try again.");
          },
        });
    }
  }

  onNotificationClick(notification: Notification): void {
    // Mark as read when clicked
    if (!notification.isRead) {
      this.markAsRead(notification.id);
    }

    // Navigate to route if action button has route
    if (notification.actionButton?.route) {
      this.router.navigate([notification.actionButton.route]);
    }
  }

  onActionButtonClick(
    event: Event,
    notification: Notification,
    button: "primary" | "secondary",
  ): void {
    event.stopPropagation(); // Prevent notification click event

    const actionButton =
      button === "primary"
        ? notification.actionButton
        : notification.secondaryButton;

    if (!actionButton) return;

    // Execute action if defined
    if (actionButton.action) {
      actionButton.action();
    }

    // Navigate to route if defined
    if (actionButton.route) {
      this.router.navigate([actionButton.route]);
    }
  }

  declineInvitation(notificationId: number): void {
    // For now, just delete the notification
    // In the future, this could call a specific decline invitation API
    this.deleteNotification(notificationId);
  }

  openNotificationSettings(): void {
    this.router.navigate(["/dashboard/client/profile"], {
      queryParams: { tab: "notifications" },
    });
  }

  loadOlderNotifications(): void {
    this.loadMore();
  }

  getRelativeTime(timestamp: string): string {
    const now = new Date();
    const notificationTime = new Date(timestamp);
    const diffInMs = now.getTime() - notificationTime.getTime();
    const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

    if (diffInMinutes < 1) {
      return "Just now";
    } else if (diffInMinutes < 60) {
      return `${diffInMinutes}m ago`;
    } else if (diffInHours < 24) {
      return `${diffInHours}h ago`;
    } else if (diffInDays === 1) {
      return "Yesterday";
    } else if (diffInDays < 7) {
      return `${diffInDays} days ago`;
    } else {
      return notificationTime.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year:
          notificationTime.getFullYear() !== now.getFullYear()
            ? "numeric"
            : undefined,
      });
    }
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery = input.value;
    // Client-side filtering is sufficient for now
    // Server-side search can be implemented later if needed
  }

  getIconBackgroundClass(notification: Notification): string {
    switch (notification.type) {
      case "action":
        return "bg-primary/10";
      case "success":
        return "bg-gray-100 dark:bg-gray-800";
      case "alert":
        return "bg-red-50 dark:bg-red-900/20";
      case "general":
        return "bg-gray-100 dark:bg-gray-800";
      case "system":
        return "bg-gray-100 dark:bg-gray-800";
      default:
        return "bg-gray-100 dark:bg-gray-800";
    }
  }

  loadMore(): void {
    if (this.hasMore && !this.isLoading) {
      this.currentPage++;
      this.loadNotifications();
    }
  }

  refresh(): void {
    this.currentPage = 0;
    this.loadNotifications();
    this.loadUnreadCount();
  }
}
