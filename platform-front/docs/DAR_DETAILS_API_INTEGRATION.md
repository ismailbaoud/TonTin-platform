# Dâr Details Page - API Integration Guide

## Overview

The Dâr Details component (`dar-details.component.ts`) is currently built with static mock data to allow UI development and testing without backend dependencies. This guide explains how to integrate it with real API endpoints when ready.

## Current Architecture

### Component Structure

```typescript
// Static mock data (easy to remove/replace)
private mockData: DarDetails = { ... };

// Main data loading method
loadDarDetails(): void {
  // Currently uses mock data with simulated delay
  // Ready to be replaced with API call
}
```

### Data Interfaces

The component uses simplified interfaces that can be mapped from API responses:

```typescript
interface Member {
  id: number;
  name: string;
  email: string;
  avatar: string;
  role: "organizer" | "member";
  turnDate: string;
  paymentStatus: "paid" | "pending" | "overdue" | "future";
}

interface DarDetails {
  id: number;
  name: string;
  image: string;
  status: "active" | "completed" | "pending";
  organizer: string;
  startDate: string;
  currentCycle: number;
  totalCycles: number;
  progress: number;
  totalMembers: number;
  monthlyPot: number;
  nextPayout: string;
  members: Member[];
}
```

## API Integration Steps

### Step 1: Uncomment the DarService Import

```typescript
// At the top of dar-details.component.ts
import {
  DarService,
  DarDetails as ApiDarDetails,
  Member as ApiMember,
  MemberRole,
  PaymentStatus,
} from "../services/dar.service";
```

### Step 2: Inject DarService in Constructor

```typescript
constructor(
  private route: ActivatedRoute,
  private router: Router,
  private darService: DarService,  // Add this
) {}
```

### Step 3: Replace loadDarDetails() Method

Replace the current mock implementation with:

```typescript
loadDarDetails(): void {
  this.isLoading = true;
  this.error = null;

  if (!this.darId) {
    this.error = "No Dâr ID provided";
    this.isLoading = false;
    return;
  }

  const darIdNum = parseInt(this.darId, 10);

  this.darService
    .getDarDetails(darIdNum)
    .pipe(
      takeUntil(this.destroy$),
      finalize(() => (this.isLoading = false))
    )
    .subscribe({
      next: (apiData) => {
        this.darDetails = this.mapApiDataToComponent(apiData);
      },
      error: (err) => {
        console.error("Error loading Dâr details:", err);
        this.error = err.error?.message || "Failed to load Dâr details. Please try again.";
      },
    });
}
```

### Step 4: Add Data Mapping Method

Create a method to map API response to component interface:

```typescript
private mapApiDataToComponent(apiData: ApiDarDetails): DarDetails {
  return {
    id: apiData.id,
    name: apiData.name,
    image: apiData.image || "https://via.placeholder.com/150",
    status: this.mapStatus(apiData.status),
    organizer: apiData.organizerName || "Unknown",
    startDate: this.formatDate(apiData.startDate),
    currentCycle: apiData.currentCycle || 0,
    totalCycles: apiData.totalCycles || 0,
    progress: this.calculateProgress(apiData.currentCycle, apiData.totalCycles),
    totalMembers: apiData.totalMembers || 0,
    monthlyPot: apiData.potSize || 0,
    nextPayout: this.formatDate(apiData.nextPayoutDate),
    members: this.mapMembers(apiData.members || []),
  };
}

private mapMembers(apiMembers: ApiMember[]): Member[] {
  return apiMembers.map((member) => ({
    id: member.id,
    name: member.userName,
    email: member.email,
    avatar: member.avatar || "https://via.placeholder.com/40",
    role: this.mapRole(member.role),
    turnDate: this.formatDate(member.turnDate),
    paymentStatus: this.mapPaymentStatus(member.paymentStatus),
  }));
}

private mapStatus(status: string): "active" | "completed" | "pending" {
  const statusLower = status.toLowerCase();
  if (statusLower === "active") return "active";
  if (statusLower === "completed") return "completed";
  return "pending";
}

private mapRole(role: MemberRole): "organizer" | "member" {
  return role === MemberRole.ORGANIZER ? "organizer" : "member";
}

private mapPaymentStatus(status: PaymentStatus): "paid" | "pending" | "overdue" | "future" {
  const statusStr = String(status).toLowerCase();
  if (statusStr === "paid") return "paid";
  if (statusStr === "pending") return "pending";
  if (statusStr === "overdue") return "overdue";
  return "future";
}

private calculateProgress(current: number, total: number): number {
  if (total === 0) return 0;
  return (current / total) * 100;
}

private formatDate(date: string | Date | null | undefined): string {
  if (!date) return "TBD";
  const dateObj = typeof date === "string" ? new Date(date) : date;
  return dateObj.toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}
```

### Step 5: Implement Action Methods with API Calls

Replace the placeholder methods with actual API calls:

```typescript
inviteMember(): void {
  // TODO: Open invite modal/dialog
  // When user submits:
  // const request: InviteMemberRequest = { darId: +this.darId!, email: userEmail };
  // this.darService.inviteMember(request).subscribe({
  //   next: () => { alert("Invitation sent!"); },
  //   error: (err) => { alert("Failed to send invitation"); }
  // });
}

shareLink(): void {
  if (!this.darId) return;
  
  this.darService.generateInviteCode(+this.darId)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (response) => {
        const link = `${window.location.origin}/join/${response.inviteCode}`;
        navigator.clipboard.writeText(link);
        alert("Link copied to clipboard!");
      },
      error: (err) => {
        console.error("Error generating invite code:", err);
        alert("Failed to generate share link");
      },
    });
}

remindMember(memberId: number): void {
  if (!this.darId) return;

  // Assuming there's a reminder endpoint
  this.darService.inviteMember({
    darId: +this.darId,
    userId: memberId
  })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: () => {
        alert("Reminder sent successfully!");
      },
      error: (err) => {
        console.error("Error sending reminder:", err);
        alert("Failed to send reminder");
      },
    });
}

openMemberOptions(memberId: number): void {
  // TODO: Implement options menu with actions like:
  // - Remove member (if organizer)
  // - View profile
  // - Send message
  // - Report member
}
```

## API Endpoints Expected

The component expects the following endpoints from `DarService`:

### 1. Get Dâr Details
```typescript
getDarDetails(darId: number): Observable<ApiDarDetails>
// GET /api/dars/{darId}
```

### 2. Generate Invite Code
```typescript
generateInviteCode(darId: number): Observable<{ inviteCode: string }>
// POST /api/dars/{darId}/generate-invite-code
```

### 3. Invite Member
```typescript
inviteMember(request: InviteMemberRequest): Observable<void>
// POST /api/dars/{darId}/invite
```

### 4. Get Members (if separate from details)
```typescript
getMembers(darId: number): Observable<ApiMember[]>
// GET /api/dars/{darId}/members
```

## Expected API Response Structure

### DarDetails Response
```json
{
  "id": 1,
  "name": "Family Savings 2024",
  "image": "https://...",
  "status": "ACTIVE",
  "organizerName": "Jane Doe",
  "startDate": "2023-10-01",
  "currentCycle": 2,
  "totalCycles": 12,
  "totalMembers": 12,
  "potSize": 1200,
  "nextPayoutDate": "2023-11-01",
  "members": [
    {
      "id": 1,
      "userName": "Jane Doe",
      "email": "jane@example.com",
      "avatar": "https://...",
      "role": "ORGANIZER",
      "turnDate": "2023-12-01",
      "paymentStatus": "PAID"
    }
  ]
}
```

## Testing Strategy

### Phase 1: Mock API (Current)
- ✅ UI fully functional with static data
- ✅ All interactions work (buttons, tabs, search)
- ✅ No backend required

### Phase 2: Development API
1. Replace `loadDarDetails()` with API call
2. Add error handling
3. Test with development backend
4. Add loading states

### Phase 3: Production API
1. Add retry logic
2. Add caching if needed
3. Optimize data fetching
4. Add real-time updates (WebSocket/polling)

## Error Handling

The component includes built-in error handling:

```typescript
// Error state is displayed in UI
this.error = "Failed to load Dâr details";

// User can dismiss error
error = null;

// Or retry loading
loadDarDetails();
```

## Progressive Enhancement

You can integrate APIs gradually:

1. **Start with GET only** - Display data from API
2. **Add mutations** - Implement invite, share, etc.
3. **Add real-time** - WebSocket for live updates
4. **Add optimistic updates** - Update UI before API confirms

## Migration Checklist

- [ ] Backend API endpoints are ready and documented
- [ ] API response matches expected structure
- [ ] DarService methods are implemented
- [ ] Error handling is tested
- [ ] Loading states are verified
- [ ] Replace `mockData` with API calls
- [ ] Add data mapping functions
- [ ] Test all user interactions
- [ ] Add proper error messages
- [ ] Test edge cases (empty data, errors, slow network)
- [ ] Remove or comment out mock data
- [ ] Update component documentation

## Notes

- **Mock data is preserved** in git history for reference
- **All methods are documented** with TODO comments
- **Interfaces are flexible** and can be adjusted to match API
- **Error handling is built-in** and ready to use
- **Loading states work** with both mock and real data

## Invite Member Modal

### Overview

The invite member modal is a popup dialog that allows organizers to search for users and invite them to the Dâr. It supports:
- Real-time search as you type
- Display of search results with user info
- Ability to invite multiple users without closing the modal
- "User not found" feedback when no results match
- Loading states for search and invite actions

### Current Implementation (Static)

```typescript
// Modal state
showInviteModal = false;
inviteSearchQuery = "";
searchResults: Array<{ id: number; name: string; email: string; avatar: string }> = [];
isSearching = false;
invitingUserId: number | null = null;

// Mock users for search
private mockUsers = [/* ... */];
```

### API Integration for Invite Modal

#### 1. Search Users Endpoint

Replace the `searchUsers()` method:

```typescript
searchUsers(): void {
  if (!this.inviteSearchQuery.trim()) {
    this.searchResults = [];
    return;
  }

  this.isSearching = true;

  // Get current member IDs to exclude them
  const memberIds = this.darDetails?.members.map((m) => m.id) || [];

  this.darService
    .searchUsers(this.inviteSearchQuery)
    .pipe(
      takeUntil(this.destroy$),
      finalize(() => (this.isSearching = false))
    )
    .subscribe({
      next: (users) => {
        // Filter out users who are already members
        this.searchResults = users.filter(
          (u) => !memberIds.includes(u.id)
        );
      },
      error: (err) => {
        console.error("Error searching users:", err);
        this.searchResults = [];
      },
    });
}
```

#### 2. Invite User Endpoint

Replace the `inviteUser()` method:

```typescript
inviteUser(userId: number): void {
  if (!this.darId) return;

  this.invitingUserId = userId;

  this.darService
    .inviteMember({ darId: +this.darId, userId })
    .pipe(
      takeUntil(this.destroy$),
      finalize(() => (this.invitingUserId = null))
    )
    .subscribe({
      next: () => {
        // Remove invited user from search results
        this.searchResults = this.searchResults.filter(
          (u) => u.id !== userId
        );
        
        // Show success message
        const user = this.searchResults.find((u) => u.id === userId);
        alert(`Invitation sent successfully!`);
        
        // Optionally reload member list
        this.loadDarDetails();
      },
      error: (err) => {
        console.error("Error inviting member:", err);
        alert(
          err.error?.message || "Failed to send invitation. Please try again."
        );
      },
    });
}
```

### Expected API Endpoints

#### Search Users
```typescript
searchUsers(query: string): Observable<UserSearchResult[]>
// GET /api/users/search?q={query}
```

**Response:**
```json
[
  {
    "id": 101,
    "name": "John Smith",
    "email": "john.smith@example.com",
    "avatar": "https://..."
  }
]
```

#### Invite Member
```typescript
inviteMember(request: InviteMemberRequest): Observable<void>
// POST /api/dars/{darId}/invite
```

**Request:**
```json
{
  "darId": 1,
  "userId": 101
}
```

### Features

1. **Real-time Search** - Searches as user types (with 300ms debounce)
2. **User Filtering** - Excludes users already in the Dâr
3. **Multiple Invites** - Modal stays open to invite more users
4. **Loading States** - Shows spinner during search and invite
5. **Empty States** - Clear feedback for no search, no results
6. **Success Feedback** - Alert after successful invitation
7. **Error Handling** - Displays errors from API

### User Experience Flow

1. Click "Invite Member" button → Modal opens
2. Type in search box → Real-time search triggers
3. See search results → User info displayed
4. Click "Invite" on a user → Invitation sent
5. User removed from results → Can search for more
6. Click "Done" → Modal closes

## Support

For questions or issues during API integration, refer to:
- `dar.service.ts` - Service documentation
- `dar.model.ts` - Type definitions
- API documentation from backend team
- Invite modal implementation in `dar-details.component.ts` (lines 43-418)