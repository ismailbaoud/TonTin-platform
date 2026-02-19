# 🎯 Member Status - Quick Reference

## What is Member Status?

**Member Status** tracks whether a user has **accepted the invitation** to join a Dart.

## Values

| Status | Meaning | When It Happens |
|--------|---------|----------------|
| **PENDING** | Invited but not accepted | User is invited to dart |
| **ACTIVE** | Accepted and participating | User accepts invitation |
| **LEAVED** | Left or removed | User leaves or is removed |

## ⚠️ Important: Not Payment Status!

**Member Status ≠ Payment Status**

- ✅ Member Status = **Participation** (joined/not joined)
- ❌ Payment Status = **Contributions** (paid/not paid) - **Separate concept!**

### Example:
```
Member Status: ACTIVE    ← User accepted invitation
Payment Status: PENDING  ← User hasn't paid yet

These are TWO DIFFERENT things!
```

---

## Backend (Java)

### Entity
```java
@Entity
@Table(name = "members")
public class Member extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private MemberStatus status; // PENDING, ACTIVE, LEAVED
}
```

### Enum
```java
public enum MemberStatus {
    PENDING,  // Invited, not accepted
    ACTIVE,   // Accepted invitation
    LEAVED    // Left the dart
}
```

### API Response
```json
{
  "id": "123e4567-...",
  "user": {
    "id": "456e7890-...",
    "userName": "john_doe",
    "email": "john@example.com"
  },
  "permission": "MEMBER",
  "status": "PENDING",
  "joinedAt": "2024-01-15T10:30:00"
}
```

---

## Frontend (TypeScript)

### Enum
```typescript
export enum MemberStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  LEAVED = 'LEAVED'
}
```

### Model
```typescript
export interface Member {
  id: string;
  userId: string;
  userName: string;
  email: string;
  role: MemberRole;      // ORGANIZER or MEMBER
  status: MemberStatus;  // PENDING, ACTIVE, LEAVED
  joinedDate: string;
}
```

### Usage
```typescript
// ✅ CORRECT
member.status === MemberStatus.PENDING

// ❌ WRONG - Don't do this!
member.status === "paid"  // Wrong! Status is not about payments!
```

---

## UI Display

### Status Badges

**PENDING (Yellow):**
```html
<span class="bg-yellow-50 text-yellow-700">
  <icon>schedule</icon>
  Pending Invitation
</span>
```

**ACTIVE (Green):**
```html
<span class="bg-green-50 text-green-700">
  <icon>check_circle</icon>
  Active
</span>
```

**LEAVED (Gray):**
```html
<span class="bg-gray-50 text-gray-600">
  <icon>logout</icon>
  Left
</span>
```

---

## Common Operations

### 1. Check if member is active
```typescript
// TypeScript
if (member.status === MemberStatus.ACTIVE) {
  // Member can participate
}
```

```java
// Java
if (member.isActive()) {
  // Member can participate
}
```

### 2. Accept invitation
```typescript
// Update member status from PENDING to ACTIVE
PUT /api/v1/member/{memberId}
{
  "status": "ACTIVE"
}
```

### 3. Filter active members
```typescript
// TypeScript
const activeMembers = members.filter(
  m => m.status === MemberStatus.ACTIVE
);
```

```java
// Java
List<Member> activeMembers = dart.getActiveMembers();
```

---

## Workflow

```
1. Organizer invites user
   └─> Member created with status = PENDING

2. User receives invitation
   └─> Shows in UI as "Pending Invitation"

3. User accepts invitation
   └─> Status changes to ACTIVE
   └─> Shows in UI as "Active"

4. User leaves dart (or is removed)
   └─> Status changes to LEAVED
   └─> Shows in UI as "Left"
```

---

## Helper Functions

### Frontend
```typescript
// Check member status
import { MemberStatus } from '../enums';

function canParticipate(status: MemberStatus): boolean {
  return status === MemberStatus.ACTIVE;
}

function needsAcceptance(status: MemberStatus): boolean {
  return status === MemberStatus.PENDING;
}

function hasLeft(status: MemberStatus): boolean {
  return status === MemberStatus.LEAVED;
}
```

### Backend
```java
// Member entity methods
public boolean isActive() {
    return status == MemberStatus.ACTIVE;
}

public boolean isPending() {
    return status == MemberStatus.PENDING;
}

public boolean hasLeft() {
    return status == MemberStatus.LEAVED;
}
```

---

## Testing

### Scenario 1: Invite User
```bash
# POST /api/v1/member/dart/{dartId}/user/{userId}
{
  "permission": "MEMBER",
  "status": "ACTIVE"  # Or PENDING if requires acceptance
}

# Expected result:
# - Member created
# - Status = PENDING or ACTIVE
# - User can see invitation in UI
```

### Scenario 2: Check Member List
```bash
# GET /api/v1/member/dart/{dartId}

# Response:
[
  {
    "id": "...",
    "status": "ACTIVE",   ✅ Accepted
    "user": { "userName": "alice" }
  },
  {
    "id": "...",
    "status": "PENDING",  ⏳ Not accepted yet
    "user": { "userName": "bob" }
  }
]
```

---

## ⚠️ Common Mistakes

### ❌ DON'T
```typescript
// Wrong: Using member status for payment info
if (member.status === "paid") { }  // Member status is not "paid"!

// Wrong: Assuming ACTIVE means paid
const isPaid = member.status === MemberStatus.ACTIVE;  // NO!

// Wrong: Checking payment with member status
if (member.status === "PENDING") {
  showPaymentReminder();  // Wrong! PENDING means invitation not accepted
}
```

### ✅ DO
```typescript
// Correct: Using member status for participation
if (member.status === MemberStatus.ACTIVE) {
  // Member has accepted invitation
}

// Correct: Checking if needs to accept invitation
if (member.status === MemberStatus.PENDING) {
  showInvitationReminder();  // ✅ Correct!
}

// Correct: For payments, use separate Payment entity
if (payment.status === PaymentStatus.PENDING) {
  showPaymentReminder();  // ✅ Correct!
}
```

---

## Payment Status (Separate Concept)

When you need to track payments, use a **separate** Payment entity:

```typescript
// Separate Payment model (future implementation)
export interface Payment {
  id: string;
  memberId: string;
  roundNumber: number;
  status: PaymentStatus;  // PAID, PENDING, OVERDUE
  amount: number;
  dueDate: string;
}

// When displaying member with payment info
export interface MemberWithPayment extends Member {
  currentPayment?: Payment;
  paymentStatus?: PaymentStatus;
}
```

---

## Quick Decision Tree

```
Is this about invitation acceptance?
├─ YES → Use MemberStatus (PENDING/ACTIVE/LEAVED)
└─ NO → Is it about money/contributions?
         └─ YES → Use PaymentStatus (PAID/PENDING/OVERDUE)
                  (from separate Payment entity)
```

---

## Summary

| Concept | Purpose | Values | Entity |
|---------|---------|--------|--------|
| **Member Status** | Invitation acceptance | PENDING, ACTIVE, LEAVED | Member |
| **Payment Status** | Contribution tracking | PAID, PENDING, OVERDUE | Payment (separate) |

**Remember:** Member status = "Did they join?" NOT "Did they pay?"

---

**Last Updated:** February 2024
**Status:** ✅ Implemented and Working