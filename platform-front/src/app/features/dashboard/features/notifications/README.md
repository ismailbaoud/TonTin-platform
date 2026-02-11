# Notifications Feature Module

## 📋 Overview

This is the Notifications feature module for TonTin Platform. It handles all notification-related functionality including viewing, managing, and receiving real-time notifications.

**Current Status**: ✅ Hardcoded (Development Mode)  
**API Ready**: ✅ Yes - Easy to switch to backend integration

---

## 🏗️ Architecture

```
features/dashboard/features/notifications/
├── pages/
│   └── notifications.component.ts    # Notifications list page
├── services/
│   └── notification.service.ts       # Notification service (HARDCODED)
├── notifications.routes.ts           # Feature routing
└── README.md                         # This file
```

---

## 🎯 Features

### Current Implementation (Hardcoded)

- ✅ **Notification List**
  - View all notifications
  - Filter by type (Payment, System, Dâr Update)
  - Mark as read/unread
  - Delete notifications
  - Pagination support

- ✅ **Notification Types**
  - Payment notifications
  - Dâr updates
  - System announcements
  - Member activity
  - Admin messages

- ✅ **Real-time Badge**
  - Unread count display
  - Auto-update on new notifications
  - Badge in navigation

- ✅ **Mock Notification Service**
  - In-memory storage
  - CRUD operations
  - Status management
  - Mock real-time updates

---

## 🔄 Switching from Hardcoded to API

The notification service is **structured to make API integration simple**. Here's how:

### Current (Hardcoded) Code

```typescript
getNotifications(): Observable<Notification[]> {
  // Hardcoded implementation
  return new Observable<Notification[]>(observer => {
    setTimeout(() => {
      observer.next(this.mockNotifications);
    }, 500);
  });
}
```

### Switch to API (3 Steps)

#### Step 1: Inject HttpClient

```typescript
// In notification.service.ts
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';

constructor(private http: HttpClient) {}
```

#### Step 2: Replace Method Bodies

```typescript
// Get all notifications
getNotifications(): Observable<Notification[]> {
  return this.http.get<Notification[]>(`${environment.apiUrl}/notifications`);
}

// Get unread count
getUnreadCount(): Observable<number> {
  return this.http.get<number>(`${environment.apiUrl}/notifications/unread/count`);
}

// Mark as read
markAsRead(id: number): Observable<void> {
  return this.http.put<void>(`${environment.apiUrl}/notifications/${id}/read`, {});
}

// Mark all as read
markAllAsRead(): Observable<void> {
  return this.http.put<void>(`${environment.apiUrl}/notifications/read-all`, {});
}

// Delete notification
deleteNotification(id: number): Observable<void> {
  return this.http.delete<void>(`${environment.apiUrl}/notifications/${id}`);
}
```

#### Step 3: Update Environment Config

```typescript
// environment.development.ts
export const environment = {
  apiUrl: 'http://localhost:9090/api',  // Your Spring Boot backend
};
```

#### Step 4: Add WebSocket for Real-time (Optional)

```typescript
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { webSocket } from 'rxjs/webSocket';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private socket$ = webSocket(`${environment.wsUrl}/notifications`);

  subscribeToNotifications(): Observable<Notification> {
    return this.socket$.asObservable();
  }
}
```

**That's it!** The component doesn't need to change at all.

---

## 📝 API Endpoints (When Ready)

### Get All Notifications
```
GET /api/notifications
Authorization: Bearer {token}

Query Parameters:
- page: number (default: 0)
- size: number (default: 20)
- type: string (optional) - PAYMENT, SYSTEM, DAR_UPDATE
- read: boolean (optional) - filter by read status

Response (200):
{
  "data": [
    {
      "id": 1,
      "type": "PAYMENT",
      "title": "Payment Received",
      "message": "Your contribution for Family Savings has been received",
      "read": false,
      "createdAt": "2024-01-15T10:30:00Z",
      "relatedEntityType": "DAR",
      "relatedEntityId": 123
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "total": 50,
    "totalPages": 3
  }
}
```

### Get Unread Count
```
GET /api/notifications/unread/count
Authorization: Bearer {token}

Response (200):
{
  "count": 5
}
```

### Mark as Read
```
PUT /api/notifications/{id}/read
Authorization: Bearer {token}

Response (200):
{
  "id": 1,
  "read": true,
  "readAt": "2024-01-15T11:00:00Z"
}
```

### Mark All as Read
```
PUT /api/notifications/read-all
Authorization: Bearer {token}

Response (200):
{
  "message": "All notifications marked as read",
  "count": 5
}
```

### Delete Notification
```
DELETE /api/notifications/{id}
Authorization: Bearer {token}

Response (204): No Content
```

### WebSocket Connection (Real-time)
```
WS /ws/notifications
Authorization: Bearer {token}

Message Format:
{
  "id": 1,
  "type": "PAYMENT",
  "title": "Payment Received",
  "message": "Your contribution has been received",
  "read": false,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

## 📦 Models/Interfaces

All TypeScript interfaces are defined in `notification.service.ts`:

```typescript
interface Notification {
  id: number;
  type: 'PAYMENT' | 'SYSTEM' | 'DAR_UPDATE' | 'MEMBER_ACTIVITY' | 'ADMIN';
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  readAt?: string;
  relatedEntityType?: 'DAR' | 'PAYMENT' | 'USER';
  relatedEntityId?: number;
  actionUrl?: string;
}

interface NotificationPreferences {
  emailNotifications: boolean;
  pushNotifications: boolean;
  smsNotifications: boolean;
  notifyOnPayment: boolean;
  notifyOnDarUpdate: boolean;
  notifyOnMemberActivity: boolean;
}
```

**These should match your Spring Boot DTOs** - Update them if your backend uses different field names.

---

## 🎨 Components

### Notifications Component
- Lists all notifications
- Filters by type and read status
- Mark as read functionality
- Delete notifications
- Navigate to related entities
- Real-time updates

---

## 🔐 Security & Permissions

### Current (Hardcoded)
- Basic mock notifications
- No real permission checks

### When Connecting to API
- ✅ JWT token authentication
- ✅ User-specific notifications only
- ✅ Role-based notification types
- ✅ Secure WebSocket connection
- ✅ Rate limiting on notifications

---

## 🚀 Next Steps

### To Complete Notifications Feature

1. **Add Real-time Updates**
   - WebSocket integration
   - Live notification delivery
   - Push notifications
   - Desktop notifications

2. **Add Notification Preferences**
   - User settings page
   - Email/SMS/Push toggles
   - Notification type preferences
   - Frequency settings

3. **Add Advanced Features**
   - Notification grouping
   - Bulk actions
   - Search functionality
   - Export notifications

4. **Connect to Backend API**
   - Follow "Switching to API" guide above
   - Test with real Spring Boot backend
   - Handle API errors properly
   - Implement WebSocket connection

5. **Add Analytics**
   - Notification open rates
   - User engagement metrics
   - Most common notification types

---

## 📚 Related Files

- **Routes**: `src/app/app.routes.ts`
- **Environment Config**: `src/environments/environment.development.ts`
- **Auth Service**: `src/app/features/auth/services/auth.service.ts`
- **Layout Component**: `src/app/shared/layouts/client-layout/client-layout.component.ts`

---

## 💡 Tips

### Development
```bash
# Access Notifications page
http://localhost:4200/dashboard/client/notifications
```

### Debugging
```typescript
// Enable debug logs in notification.service.ts
console.log('🔔 Fetching notifications:', notifications);
console.log('✅ Notification marked as read:', id);
```

### Testing Real-time Updates
```typescript
// In notification.service.ts, simulate new notification
simulateNewNotification() {
  const newNotif: Notification = {
    id: Date.now(),
    type: 'PAYMENT',
    title: 'New Payment',
    message: 'Payment received',
    read: false,
    createdAt: new Date().toISOString()
  };
  this.notifications$.next([newNotif, ...this.notifications]);
}
```

---

## 🤝 Contributing

When adding new notification features:

1. Keep the **hardcoded/API switch pattern**
2. Add **loading states** for better UX
3. Include **error handling**
4. Add **real-time updates** support
5. Update this **README**

---

## ✅ Checklist for API Integration

- [ ] Backend API is running (`http://localhost:9090`)
- [ ] API endpoints match the ones documented above
- [ ] Environment file has correct `apiUrl`
- [ ] HttpClient is injected in notification.service.ts
- [ ] All methods use `http.get/post/put/delete()`
- [ ] Error responses match expected format
- [ ] CORS is configured on backend
- [ ] WebSocket is configured (optional)
- [ ] Test all CRUD operations
- [ ] Handle API errors properly
- [ ] Add loading states
- [ ] Add error messages
- [ ] Test real-time updates

---

**Status**: Ready for development ✅  
**Last Updated**: February 2025  
**Version**: 1.0.0