# Notifications Feature Documentation

## Overview

The Notifications feature provides users with real-time updates about their Dâr activities, account events, system alerts, and action items. Users can view, filter, mark as read, and interact with notifications directly from a dedicated notifications page.

## Feature Components

### 1. Notifications Page Component

**Location:** `platform-front/src/app/features/dashboard/pages/notifications/`

**Files:**
- `notifications.component.ts` - Component logic and notification management
- `notifications.component.html` - Template with notification list and filters
- `notifications.component.scss` - Styles and animations

### 2. Key Features

#### A. Notification Categories
Notifications are organized into categories:
- **Today** - Notifications from the last 24 hours
- **Yesterday** - Notifications from 24-48 hours ago
- **Older** - Notifications older than 48 hours

#### B. Notification Types
- **Action Required** - Requires immediate user action (contributions due, invitations)
- **Success** - Positive events (goals achieved, payments received)
- **Alert** - Security or warning notifications (login detected, verification needed)
- **General** - Standard updates (new members, Dâr invitations)
- **System** - Platform updates and maintenance notices

#### C. Filtering & Tabs
Four main tabs for filtering:
1. **All** - Shows all notifications with total count
2. **Unread** - Only unread notifications with badge count
3. **Action Required** - Notifications requiring user action
4. **Archived** - Previously archived notifications

#### D. Notification Cards
Each notification displays:
- **Icon** - Type-specific icon with color coding
- **Title** - Bold, descriptive heading
- **Message** - Detailed notification text (supports HTML formatting)
- **Timestamp** - Relative time (e.g., "2h ago", "Yesterday")
- **Unread Indicator** - Visual dot for unread notifications
- **Action Buttons** - Primary and secondary action buttons
- **More Options** - Delete/archive menu (appears on hover)

#### E. Interactive Elements
- **Click to Mark as Read** - Clicking notification marks it as read
- **Action Buttons** - Navigate to relevant pages or execute actions
- **Mark All as Read** - Bulk action to mark all as read
- **Settings** - Configure notification preferences
- **Load More** - Pagination for older notifications

## Routing

### Route Configuration

```typescript
{
  path: "notifications",
  component: NotificationsComponent,
  data: { title: "Notifications - TonTin" }
}
```

### Access Points

Users can access notifications from:
1. **Sidebar** - "Notifications" link in Account section (with unread badge)
2. **Top Bar** - Bell icon in header (with notification dot)
3. **Direct URL** - `/dashboard/client/notifications`

## Data Structures

### Notification Interface
```typescript
interface Notification {
  id: number;
  type: "action" | "success" | "alert" | "general" | "system";
  icon: string;                    // Material icon name
  iconColor: string;                // Tailwind color class
  title: string;                    // Notification heading
  message: string;                  // Detailed message (HTML allowed)
  timestamp: string;                // ISO 8601 timestamp
  isRead: boolean;                  // Read status
  category?: string;                // "today" | "yesterday" | "older"
  actionButton?: {
    label: string;
    route?: string;                 // Navigation route
    action?: () => void;            // Custom action function
  };
  secondaryButton?: {
    label: string;
    route?: string;
    action?: () => void;
  };
}
```

### NotificationTab Type
```typescript
type NotificationTab = "all" | "unread" | "action" | "archived";
```

## Key Methods

### Component Methods

#### `ngOnInit()`
- Initializes component
- Loads notifications from service (TODO: backend integration)

#### `setTab(tab: NotificationTab)`
- Switches between notification filter tabs
- Updates `activeTab` property

#### `markAsRead(notificationId: number)`
- Marks a single notification as read
- Updates local state
- Calls API endpoint (TODO)

#### `markAllAsRead()`
- Marks all notifications as read in bulk
- Updates all notifications in the list
- Calls bulk update API (TODO)

#### `deleteNotification(notificationId: number)`
- Removes a notification from the list
- Calls delete API endpoint (TODO)

#### `archiveNotification(notificationId: number)`
- Archives a notification
- Moves to archived category
- Calls archive API endpoint (TODO)

#### `onNotificationClick(notification: Notification)`
- Handles notification card click
- Marks as read if unread
- Navigates to associated route if available

#### `onActionButtonClick(event, notification, button)`
- Handles action button clicks
- Prevents event bubbling
- Executes custom action or navigates to route

#### `declineInvitation(notificationId: number)`
- Declines a Dâr invitation
- Deletes the notification
- Calls API to reject invitation (TODO)

#### `openNotificationSettings()`
- Opens notification settings modal or page (TODO)

#### `loadOlderNotifications()`
- Loads more historical notifications
- Implements pagination (TODO)

### Computed Properties

#### `filteredNotifications`
Returns notifications filtered by:
- Active tab selection
- Search query (if implemented)
- Read/unread status

#### `notificationsByCategory`
Returns object with notifications grouped by:
- `today` - Last 24 hours
- `yesterday` - 24-48 hours ago
- `older` - More than 48 hours ago

#### `unreadCount`
Returns total count of unread notifications
Used for badge display

#### `actionRequiredCount`
Returns count of action-required unread notifications
Used for action tab badge

#### `totalCount`
Returns total number of notifications

### Helper Methods

#### `getRelativeTime(timestamp: string)`
Converts ISO timestamp to relative time string:
- "Just now" (< 1 minute)
- "2m ago" (< 1 hour)
- "3h ago" (< 24 hours)
- "Yesterday" (24-48 hours)
- "3 days ago" (< 1 week)
- "Jan 15" or "Jan 15, 2023" (older)

#### `getIconBackgroundClass(notification: Notification)`
Returns appropriate background class based on notification type:
- **Action**: `bg-primary/10` (light green)
- **Success**: `bg-gray-100 dark:bg-gray-800`
- **Alert**: `bg-red-50 dark:bg-red-900/20`
- **General**: `bg-gray-100 dark:bg-gray-800`
- **System**: `bg-gray-100 dark:bg-gray-800`

## UI/UX Features

### Responsive Design
- **Mobile**: Stacked layout, simplified actions
- **Tablet**: Hybrid layout with better spacing
- **Desktop**: Full-featured layout with hover effects

### Visual States
- **Unread**: Bold text, primary border-left, animated dot badge
- **Read**: Dimmed background, no border highlight
- **Hover**: Shadow elevation, show more options button
- **Active**: Highlight effect during interaction

### Animations
- **Slide In**: Notifications animate in from bottom
- **Fade In**: Smooth opacity transition
- **Pulse**: Unread indicator pulses
- **Ripple Effect**: Button press feedback
- **Slide Out**: Smooth deletion animation

### Dark Mode Support
- Full dark theme compatibility
- Proper contrast ratios
- Dark-mode-specific color variants
- Smooth theme transitions

### Empty States
- **No Notifications**: Friendly message with icon
- **No Results**: Clear feedback when filters show no results
- **Archived Empty**: Specific message for archived view

## Integration Points

### Backend API Endpoints (TODO)

```typescript
// Get all notifications
GET /api/notifications
Query params: page, limit, filter (all|unread|action|archived)
Response: { notifications: Notification[], total: number, unread: number }

// Get notification by ID
GET /api/notifications/:id

// Mark notification as read
PATCH /api/notifications/:id/read
Body: { isRead: boolean }

// Mark all as read
PATCH /api/notifications/mark-all-read

// Delete notification
DELETE /api/notifications/:id

// Archive notification
PATCH /api/notifications/:id/archive

// Get unread count
GET /api/notifications/unread-count
Response: { count: number }
```

### Services to Create

1. **NotificationService**
   - `getNotifications(filter?, page?, limit?)`
   - `getNotificationById(id)`
   - `markAsRead(id)`
   - `markAllAsRead()`
   - `deleteNotification(id)`
   - `archiveNotification(id)`
   - `getUnreadCount()`
   - `loadOlderNotifications()`

2. **WebSocket/SSE Integration** (Real-time)
   - Connect to notification stream
   - Push new notifications in real-time
   - Update unread count dynamically
   - Show toast for urgent notifications

## Mock Data

Currently includes 8 sample notifications:

1. **Contribution Due** (Action, Unread, Today)
   - Family Circle payment reminder
   - "Pay Now" button → `/dashboard/client/pay-contribution/1`

2. **Goal Achieved** (Success, Unread, Today)
   - Savings goal completion
   - "View Savings" button → `/dashboard/client`

3. **New Login Detected** (Alert, Read, Today)
   - Security notification
   - "Review Activity" button

4. **Dâr Invitation** (General, Read, Yesterday)
   - Invitation from James
   - "View Details" + "Decline" buttons

5. **Platform Maintenance** (System, Read, Yesterday)
   - System status update

6. **Payment Received** (Action, Read, Yesterday)
   - Sarah's payment notification
   - "View Transaction" button

7. **New Member Joined** (General, Read, Older)
   - Michael joined Family Savings

8. **Contribution Confirmed** (Success, Read, Older)
   - Payment processed confirmation
   - "View Receipt" button

## User Flows

### Flow 1: View and Interact with Notifications
1. User clicks bell icon in header or sidebar link
2. Lands on notifications page
3. Sees categorized list (Today, Yesterday, Older)
4. Clicks on a notification
5. Notification marked as read
6. User redirected to related page (if action button clicked)

### Flow 2: Handle Action Required Notification
1. User sees "4" badge on notifications bell
2. Navigates to notifications page
3. Clicks "Unread" or "Action Required" tab
4. Sees contribution due notification
5. Clicks "Pay Now" button
6. Redirected to payment page
7. Notification marked as read

### Flow 3: Manage Notifications
1. User opens notifications
2. Hovers over notification card
3. "More options" button appears
4. Clicks to delete notification
5. Notification removed with animation
6. Count badges updated

### Flow 4: Bulk Actions
1. User sees multiple unread notifications
2. Clicks "Mark all read" button
3. All notifications marked as read
4. Unread badge count clears
5. Visual states update

## Notification Badge Logic

### Header Bell Icon
- Shows red dot if `unreadCount > 0`
- Pulsing animation for new notifications
- Displays count in tooltip (optional)

### Sidebar Link
- Shows red badge with count if `unreadCount > 0`
- Badge disappears when all marked as read

### Tab Badges
- **All Tab**: Shows total count (gray badge)
- **Unread Tab**: Shows unread count (green/primary badge)
- **Action Required Tab**: Shows action count (red badge)

## Styling

### Color Scheme
- **Primary**: `#13ec5b` (Green) - Action buttons, unread indicators
- **Success**: Green variants - Success notifications
- **Warning/Alert**: Red variants - Alert notifications
- **Neutral**: Gray variants - General and system notifications

### Typography
- **Title**: 16px, Bold, Gray-900/White
- **Message**: 14px, Normal, Gray-600/Gray-400
- **Timestamp**: 12px, Medium, Gray-500
- **Badges**: 11-12px, Bold

### Spacing
- Card padding: 16px
- Gap between cards: 16px
- Icon size: 48px (12x12 container)
- Badge size: Minimum 20px width

### Border & Shadows
- Card border: 1px solid gray-200/gray-800
- Unread left border: 4px solid primary
- Shadow on hover: `shadow-md`
- Border radius: 12px (xl)

## Accessibility

- **Keyboard Navigation**: Tab through notifications and buttons
- **Screen Readers**: ARIA labels on all interactive elements
- **Focus States**: Clear focus indicators
- **Color Contrast**: WCAG AA compliant
- **Semantic HTML**: Proper heading hierarchy
- **Alt Text**: Descriptive icon labels

## Performance Optimization

1. **Lazy Loading**: Load notifications page on demand
2. **Virtual Scrolling**: For large notification lists (TODO)
3. **Pagination**: Load older notifications incrementally
4. **Caching**: Store recent notifications in localStorage
5. **Debouncing**: For search/filter operations

## Security Considerations

1. **XSS Prevention**: Sanitize notification messages (Angular's built-in sanitizer)
2. **Authentication**: Protected by authGuard
3. **Authorization**: Users only see their own notifications
4. **Rate Limiting**: Limit notification API requests
5. **Input Validation**: Validate all user actions

## Testing Checklist

### Unit Tests
- [ ] Component initialization
- [ ] Tab switching logic
- [ ] Mark as read functionality
- [ ] Delete notification
- [ ] Filter notifications
- [ ] Relative time calculation
- [ ] Badge count calculations

### Integration Tests
- [ ] API calls for notifications
- [ ] Mark all as read
- [ ] Navigation to action routes
- [ ] Real-time updates (WebSocket)
- [ ] Pagination

### E2E Tests
- [ ] View notifications page
- [ ] Click notification and navigate
- [ ] Mark all as read
- [ ] Delete notification
- [ ] Filter by tab
- [ ] Responsive behavior

## Future Enhancements

1. **Real-time Updates**
   - WebSocket integration
   - Push notifications
   - Live badge count updates

2. **Notification Preferences**
   - Settings page for notification types
   - Email notifications toggle
   - SMS notifications toggle
   - Do Not Disturb mode

3. **Advanced Filtering**
   - Filter by Dâr
   - Filter by date range
   - Filter by type
   - Search notifications

4. **Notification Groups**
   - Group related notifications
   - Expandable groups
   - Bulk actions on groups

5. **Rich Notifications**
   - Images and thumbnails
   - Action previews
   - Embedded forms
   - Quick reply

6. **Notification History**
   - Export to CSV
   - Search full history
   - Analytics dashboard

7. **Smart Notifications**
   - AI-powered priority sorting
   - Suggested actions
   - Notification summaries

8. **Multi-device Sync**
   - Sync read status across devices
   - Device-specific settings
   - Cross-platform notifications

## Browser Support

- Chrome/Edge (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Dependencies

- **Angular**: ^17.0.0
- **Tailwind CSS**: ^3.0.0
- **Material Symbols**: For icons
- **RxJS**: For reactive programming
- **Angular Router**: For navigation

## Troubleshooting

### Notifications Not Loading
- Check authentication state
- Verify API endpoint configuration
- Check browser console for errors
- Ensure proper CORS headers

### Badge Count Not Updating
- Refresh notification service
- Check WebSocket connection
- Verify unread count calculation
- Clear browser cache

### Action Buttons Not Working
- Verify route configuration
- Check navigation guards
- Inspect action function bindings
- Test with browser DevTools

## Performance Metrics

- **Initial Load**: < 1 second
- **Notification Render**: < 100ms per notification
- **Mark as Read**: < 500ms
- **Filter Switch**: < 200ms
- **Real-time Update**: < 1 second latency

---

**Last Updated:** 2024
**Version:** 1.0.0
**Status:** ✅ Complete - Ready for Backend Integration