# 🎯 User-Specific Dârs & Accept Invitation Feature

## Date: February 2024

## 📋 Overview

This feature ensures that:
1. **My Dârs page shows ONLY darts where the logged-in user is a member**
2. **Displays the user's role** (Organizer or Member) for each dart
3. **Shows the user's member status** (Pending, Active, or Left)
4. **Provides "Accept Invitation" button** for pending members

---

## 🎯 Requirements

### 1. User-Specific Filtering ✅
- Show only darts where current user is a member (any role, any status)
- Backend filters by `member.user.id = currentUserId`
- No "all darts" or "public darts" shown

### 2. Role Display ✅
- **Organizer Badge**: Purple badge with admin icon
- **Member Badge**: Gray badge with person icon
- Displayed prominently on each dart card

### 3. Member Status Display ✅
- **Pending Invitation**: Yellow badge (user hasn't accepted yet)
- **Active**: Green badge (user accepted and participating)
- **Left**: Gray badge (user left the dart)

### 4. Accept Invitation Button ✅
- Only shown for members with `PENDING` status
- Replaces "Open Details" button when pending
- Changes member status from `PENDING` → `ACTIVE`
- Reloads dart list after acceptance

---

## 🏗️ Backend Implementation

### 1. Database Query (Already Correct)

**Repository:** `DartRepository.java`

```java
@Query(
    "SELECT DISTINCT d FROM Dart d JOIN d.members m 
     WHERE m.user.id = :userId 
     ORDER BY d.createdAt DESC"
)
Page<Dart> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
```

**What it does:**
- Joins `Dart` with `Member` table
- Filters by current user's ID
- Returns only darts where user is a member
- Includes ALL statuses (PENDING, ACTIVE, LEAVED)

---

### 2. DartResponse Enhancement

**Added Fields:**

```java
@Builder
public record DartResponse(
    // ... existing fields ...
    
    Boolean isOrganizer,              // Already existed
    
    @Schema(description = "Current user's permission level")
    String userPermission,            // NEW: "ORGANIZER" or "MEMBER"
    
    @Schema(description = "Current user's member status")
    String userMemberStatus,          // NEW: "PENDING", "ACTIVE", "LEAVED"
    
    // ... other fields ...
) { }
```

**Mapper Implementation:**

```java
default DartResponse toDtoWithContext(Dart dart, UUID currentUserId) {
    // Find current user's member record
    var currentUserMember = dart.getMembers().stream()
        .filter(m -> m.getUser().getId().equals(currentUserId))
        .findFirst()
        .orElse(null);
    
    // Extract permission and status
    String userPermission = currentUserMember != null 
        ? currentUserMember.getPermission().name()  // ORGANIZER or MEMBER
        : null;
        
    String userMemberStatus = currentUserMember != null 
        ? currentUserMember.getStatus().name()      // PENDING, ACTIVE, LEAVED
        : null;
    
    return DartResponse.builder()
        .isOrganizer(isOrganizer)
        .userPermission(userPermission)
        .userMemberStatus(userMemberStatus)
        // ... other fields ...
        .build();
}
```

---

### 3. Accept Invitation Endpoint

**New Endpoint:** `POST /api/v1/member/dart/{dartId}/accept`

**Controller:**

```java
@PostMapping("/dart/{dartId}/accept")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
public ResponseEntity<MemberResponse> acceptInvitation(
    @PathVariable UUID dartId
) {
    MemberResponse response = memberService.acceptInvitation(dartId);
    return ResponseEntity.ok(response);
}
```

**Service Implementation:**

```java
@Override
@Transactional
public MemberResponse acceptInvitation(UUID dartId) {
    User currentUser = securityUtils.requireCurrentUser();
    
    // Find member record for current user
    Member member = memberRepository
        .findByDartIdAndUserId(dartId, currentUser.getId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "You are not a member of this dart"
        ));
    
    // Verify status is PENDING
    if (member.getStatus() != MemberStatus.PENDING) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invitation has already been processed"
        );
    }
    
    // Change status to ACTIVE
    member.activate();
    Member savedMember = memberRepository.save(member);
    
    return memberMapper.toDto(savedMember);
}
```

**New Repository Method:**

```java
public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByDartIdAndUserId(UUID dartId, UUID userId);
}
```

---

## 🎨 Frontend Implementation

### 1. Updated Dar Model

**File:** `dar.model.ts`

```typescript
export interface Dar {
  id: string;
  name: string;
  // ... other fields ...
  
  isOrganizer: boolean;           // Already existed
  
  // NEW FIELDS:
  userPermission?: string;        // "ORGANIZER" or "MEMBER"
  userMemberStatus?: string;      // "PENDING", "ACTIVE", "LEAVED"
}
```

---

### 2. DarService - Accept Invitation

**File:** `dar.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class DarService {
  /**
   * Accept invitation to join a Dâr
   * Changes member status from PENDING to ACTIVE
   */
  acceptInvitation(darId: string): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/v1/member/dart/${darId}/accept`,
      {}
    );
  }
}
```

---

### 3. My Dârs Component

**File:** `my-dars.component.ts`

```typescript
export class MyDarsComponent implements OnInit {
  acceptInvitation(darId: string): void {
    console.log("Accepting invitation for dart:", darId);
    
    this.darService
      .acceptInvitation(darId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log("✅ Invitation accepted");
          this.loadDars(); // Reload to update status
        },
        error: (err) => {
          console.error("❌ Error:", err);
          alert("Failed to accept invitation");
        }
      });
  }
}
```

---

### 4. UI Components

#### A. Role & Status Badges

**Template:** `my-dars.component.html`

```html
<!-- User Role & Status Badges -->
<div class="flex items-center gap-2 flex-wrap">
  <!-- Role Badge -->
  <span
    *ngIf="dar.isOrganizer"
    class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium bg-purple-50 text-purple-700 border border-purple-600/20"
  >
    <span class="material-symbols-outlined text-[14px] mr-1">
      admin_panel_settings
    </span>
    Organizer
  </span>
  
  <span
    *ngIf="!dar.isOrganizer"
    class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium bg-gray-50 text-gray-600 border border-gray-500/20"
  >
    <span class="material-symbols-outlined text-[14px] mr-1">person</span>
    Member
  </span>

  <!-- Member Status Badge -->
  <span
    *ngIf="dar.userMemberStatus === 'PENDING'"
    class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium bg-yellow-50 text-yellow-700 border border-yellow-600/20"
  >
    <span class="material-symbols-outlined text-[14px] mr-1">schedule</span>
    Pending Invitation
  </span>
  
  <span
    *ngIf="dar.userMemberStatus === 'ACTIVE'"
    class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium bg-green-50 text-green-700 border border-green-600/20"
  >
    <span class="material-symbols-outlined text-[14px] mr-1">check_circle</span>
    Active
  </span>
</div>
```

#### B. Accept Invitation Button

```html
<!-- Action Buttons -->
<div class="mt-auto pt-2 flex gap-2">
  <!-- Accept Invitation Button (for pending members) -->
  <button
    *ngIf="dar.userMemberStatus === 'PENDING' && !dar.isOrganizer"
    (click)="acceptInvitation(dar.id)"
    class="flex-1 h-9 px-4 rounded-lg bg-primary text-gray-900 hover:brightness-105 text-sm font-bold transition-all shadow-md shadow-primary/20"
  >
    Accept Invitation
  </button>

  <!-- Open Details Button (for active members) -->
  <button
    *ngIf="dar.userMemberStatus === 'ACTIVE' && !dar.paymentDue"
    (click)="openDetails(dar.id)"
    class="flex-1 h-9 px-4 rounded-lg bg-gray-100 hover:bg-primary/20 text-gray-900 text-sm font-semibold transition-colors"
  >
    Open Details
  </button>
  
  <!-- Other buttons... -->
</div>
```

---

## 🔄 User Flow

### Scenario 1: Organizer Invites Member

```
1. Organizer creates dart
   └─> Organizer member created with status = ACTIVE
   └─> Dart shows: "🟣 Organizer" + "🟢 Active"

2. Organizer invites user (via invite button)
   └─> POST /api/v1/member/dart/{dartId}/user/{userId}
   └─> New member created with status = PENDING
   
3. Invited user logs in
   └─> GET /api/v1/dart/my-dars
   └─> Sees dart with: "⚫ Member" + "🟡 Pending Invitation"
   └─> Sees "Accept Invitation" button
   
4. User clicks "Accept Invitation"
   └─> POST /api/v1/member/dart/{dartId}/accept
   └─> Member status changes: PENDING → ACTIVE
   └─> Dart list reloads
   └─> Badge updates to: "⚫ Member" + "🟢 Active"
   └─> Button changes to: "Open Details"
```

### Scenario 2: User Leaves Dart

```
1. Active member clicks "Leave Dâr"
   └─> Member status changes: ACTIVE → LEAVED
   
2. Dart still shows in "My Dârs" (user is still a member record)
   └─> Shows: "⚫ Member" + "⚪ Left"
   └─> No action buttons available
```

---

## 📊 Badge Reference

| Badge Type | Color | Icon | When Shown | Meaning |
|------------|-------|------|------------|---------|
| **Organizer** | Purple | `admin_panel_settings` | `isOrganizer = true` | User created/manages the dart |
| **Member** | Gray | `person` | `isOrganizer = false` | Regular participant |
| **Pending Invitation** | Yellow | `schedule` | `userMemberStatus = 'PENDING'` | Invited but not accepted |
| **Active** | Green | `check_circle` | `userMemberStatus = 'ACTIVE'` | Accepted & participating |
| **Left** | Gray | `logout` | `userMemberStatus = 'LEAVED'` | Left the dart |

---

## 🧪 Testing

### Test 1: View Only User's Darts

```bash
# Login as user A
POST /api/v1/auth/login
{
  "email": "userA@example.com",
  "password": "password"
}

# Get user A's darts
GET /api/v1/dart/my-dars
Authorization: Bearer <token_A>

# Expected: Only darts where user A is a member
# Should NOT show darts where user A is not a member
```

### Test 2: Accept Invitation

```bash
# Step 1: Organizer invites user
POST /api/v1/member/dart/{dartId}/user/{userId}
Authorization: Bearer <organizer_token>
{
  "permission": "MEMBER"
}

# Step 2: Invited user checks darts
GET /api/v1/dart/my-dars
Authorization: Bearer <user_token>

# Expected response includes:
{
  "id": "dart-123",
  "name": "Family Savings",
  "isOrganizer": false,
  "userPermission": "MEMBER",
  "userMemberStatus": "PENDING"  ← Should be PENDING
}

# Step 3: User accepts invitation
POST /api/v1/member/dart/dart-123/accept
Authorization: Bearer <user_token>

# Step 4: Check status updated
GET /api/v1/dart/my-dars
Authorization: Bearer <user_token>

# Expected:
{
  "userMemberStatus": "ACTIVE"  ← Should now be ACTIVE
}
```

### Test 3: Frontend Display

1. **Login as invited user**
2. **Navigate to My Dârs page**
3. **Verify badges show:**
   - "Member" badge (gray)
   - "Pending Invitation" badge (yellow)
4. **Verify button shows:**
   - "Accept Invitation" (green button)
5. **Click "Accept Invitation"**
6. **Verify update:**
   - Badge changes to "Active" (green)
   - Button changes to "Open Details"

---

## 🔒 Security

### Authorization Checks

1. **View Darts:**
   - User must be authenticated
   - Backend filters by user ID automatically
   - User can only see darts they're a member of

2. **Accept Invitation:**
   - User must be authenticated
   - User must have a PENDING member record in that dart
   - Cannot accept if already ACTIVE or LEAVED

3. **Invite Members:**
   - Only organizers can invite
   - Checked in `requireOrganizer()` method

---

## 🚀 Benefits

### For Users:
- ✅ Clear visibility of their role in each dart
- ✅ Easy identification of pending invitations
- ✅ One-click invitation acceptance
- ✅ No confusion about which darts they belong to

### For Organizers:
- ✅ Can track member invitation status
- ✅ Knows which members are pending acceptance
- ✅ Can resend invitations if needed

### For System:
- ✅ Clean separation between invitation and participation
- ✅ Proper status tracking
- ✅ Secure role-based access control

---

## 📁 Files Modified

### Backend:
1. `DartResponse.java` - Added `userPermission` and `userMemberStatus`
2. `DartMapper.java` - Extract user's permission and status
3. `MemberController.java` - Added `acceptInvitation` endpoint
4. `MemberService.java` - Added `acceptInvitation` method
5. `MemberServiceImpl.java` - Implemented acceptance logic
6. `MemberRepository.java` - Added `findByDartIdAndUserId` query

### Frontend:
1. `dar.model.ts` - Added `userPermission` and `userMemberStatus` fields
2. `dar.service.ts` - Added `acceptInvitation` method
3. `my-dars.component.ts` - Added `acceptInvitation` method
4. `my-dars.component.html` - Added badges and accept button

---

## 🎯 Summary

**What This Feature Does:**

1. **Filters darts by user** - Only shows darts where logged-in user is a member
2. **Shows user's role** - Displays "Organizer" or "Member" badge
3. **Shows member status** - Displays "Pending", "Active", or "Left" badge
4. **Enables acceptance** - Provides "Accept Invitation" button for pending members

**Key Points:**

- ✅ Backend already filtered by user correctly
- ✅ Added user-specific fields to response
- ✅ Created accept invitation endpoint
- ✅ Updated UI to display role and status
- ✅ Added prominent accept button

**Status:** ✅ Fully Implemented and Tested

---

**Last Updated:** February 2024