# 🚀 Quick Test Guide - New Features

## What's New?

1. ✅ **Member Status Display** - See if you've accepted invitations
2. ✅ **Role Badges** - Know if you're an Organizer or Member
3. ✅ **Accept Invitation Button** - One-click invitation acceptance

---

## 🏃 Quick Start (5 Minutes)

### Prerequisites
```bash
# Backend running on port 9090
# Frontend running on port 4200
# PostgreSQL running
```

---

## 📝 Test Scenario: Invite & Accept

### Step 1: Create a Dart (Organizer)

1. **Login** as User A:
   - Email: `userA@example.com`
   - Password: `password123`

2. **Create New Dâr**:
   - Navigate to: http://localhost:4200/dashboard/client/my-dars
   - Click "Create New Dâr"
   - Fill form:
     - Name: "Test Savings Circle"
     - Monthly Contribution: 100
     - Order Method: FIXED_ORDER
     - Payment Frequency: MONTH
   - Click "Create"

3. **Verify Your Role**:
   - You should see your new dart
   - Badge shows: 🟣 **Organizer** + 🟢 **Active**

---

### Step 2: Invite a Member (Organizer)

1. **Click "Open Details"** on your dart

2. **Go to "Members" tab**

3. **Click "Invite Member"**

4. **Search for User B**:
   - Type: "userB" or their email
   - Click "Invite" on their profile

5. **Check member list**:
   - User B should appear with 🟡 **Pending Invitation**

---

### Step 3: Accept Invitation (Member)

1. **Logout** from User A

2. **Login** as User B:
   - Email: `userB@example.com`
   - Password: `password123`

3. **Navigate to My Dârs**:
   - You should see "Test Savings Circle"
   - Badges show: ⚫ **Member** + 🟡 **Pending Invitation**
   - Button shows: **"Accept Invitation"**

4. **Click "Accept Invitation"**

5. **Wait for page reload**

6. **Verify Update**:
   - Badge changes to: ⚫ **Member** + 🟢 **Active**
   - Button changes to: **"Open Details"**

7. **Click "Open Details"**:
   - You can now see dart details
   - You're listed as Active member

---

## 🎯 What to Look For

### My Dârs Page

#### For Organizers:
```
┌─────────────────────────────┐
│ Test Savings Circle         │
│                             │
│ 🟣 Organizer  🟢 Active    │  ← Badges
│                             │
│ [Open Details]              │  ← Button
└─────────────────────────────┘
```

#### For Pending Members:
```
┌─────────────────────────────┐
│ Test Savings Circle         │
│                             │
│ ⚫ Member  🟡 Pending       │  ← Badges
│   Invitation                │
│                             │
│ [Accept Invitation]         │  ← Button (Green)
└─────────────────────────────┘
```

#### For Active Members:
```
┌─────────────────────────────┐
│ Test Savings Circle         │
│                             │
│ ⚫ Member  🟢 Active        │  ← Badges
│                             │
│ [Open Details]              │  ← Button (Gray)
└─────────────────────────────┘
```

---

## 🔍 Verification Checklist

### Backend Verification

```bash
# Check if backend returns user context
curl -X GET http://localhost:9090/api/v1/dart/my-dars \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": "...",
      "name": "Test Savings Circle",
      "isOrganizer": false,
      "userPermission": "MEMBER",
      "userMemberStatus": "PENDING"
    }
  ]
}
```

---

### Frontend Console Logs

**Open browser console (F12)** and look for:

```javascript
// When viewing My Dârs
📋 Member statuses: [
  { name: "Test Savings Circle", status: "PENDING" }
]

// When accepting invitation
=== Accepting Invitation ===
Dart ID: abc-123
✅ Invitation accepted successfully

// After reload
📋 Member statuses: [
  { name: "Test Savings Circle", status: "ACTIVE" }
]
```

---

## 🧪 API Testing (Optional)

### Test 1: Check My Darts
```bash
# Login
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "userB@example.com",
    "password": "password123"
  }'

# Save the token
TOKEN="<paste_token_here>"

# Get my darts
curl -X GET http://localhost:9090/api/v1/dart/my-dars \
  -H "Authorization: Bearer $TOKEN"
```

### Test 2: Accept Invitation
```bash
# Accept invitation
curl -X POST http://localhost:9090/api/v1/member/dart/{dartId}/accept \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK
# Response: Updated member details
```

---

## ❌ Troubleshooting

### Issue 1: Don't See "Accept Invitation" Button

**Check:**
- Are you logged in as the invited user?
- Is your member status "PENDING"?
- Are you NOT the organizer?

**Console check:**
```javascript
// In browser console
console.log(dar.userMemberStatus)  // Should be "PENDING"
console.log(dar.isOrganizer)       // Should be false
```

---

### Issue 2: Button Doesn't Work

**Check console for errors:**
```javascript
// Look for:
❌ Error accepting invitation: ...
```

**Common causes:**
- Member status is not PENDING
- User is not authenticated
- Backend not running

---

### Issue 3: Badges Not Showing

**Check:**
1. Backend returning `userPermission` and `userMemberStatus`
2. Frontend receiving the data
3. Console logs show correct values

**Debug:**
```javascript
// In browser console
console.log('All dars:', this.dars)
// Check if fields exist
```

---

## 🎨 Badge Reference

| Badge | Color | Icon | Meaning |
|-------|-------|------|---------|
| 🟣 Organizer | Purple | admin_panel_settings | You manage this dart |
| ⚫ Member | Gray | person | You're a participant |
| 🟡 Pending Invitation | Yellow | schedule | You haven't accepted yet |
| 🟢 Active | Green | check_circle | You're participating |
| ⚪ Left | Gray | logout | You left this dart |

---

## 🔄 Complete Flow Diagram

```
┌────────────────┐
│ User A Login   │ (Organizer)
└───────┬────────┘
        │
        v
┌────────────────┐
│ Create Dart    │ → Status: PENDING
└───────┬────────┘   Role: ORGANIZER
        │            MemberStatus: ACTIVE
        v
┌────────────────┐
│ Invite User B  │ → POST /api/v1/member/dart/{id}/user/{userId}
└───────┬────────┘   Creates member with status: PENDING
        │
        v
┌────────────────┐
│ User B Login   │ (Member)
└───────┬────────┘
        │
        v
┌────────────────┐
│ View My Dârs   │ → Shows dart with badges:
└───────┬────────┘   ⚫ Member + 🟡 Pending Invitation
        │            Button: "Accept Invitation"
        v
┌────────────────┐
│ Click Accept   │ → POST /api/v1/member/dart/{id}/accept
└───────┬────────┘   Changes status: PENDING → ACTIVE
        │
        v
┌────────────────┐
│ Page Reloads   │ → Shows updated badges:
└───────┬────────┘   ⚫ Member + 🟢 Active
        │            Button: "Open Details"
        v
┌────────────────┐
│ Open Details   │ → Full access to dart
└────────────────┘
```

---

## ⏱️ Expected Timings

| Action | Expected Time |
|--------|--------------|
| Login | 1-2 seconds |
| Create dart | 2-3 seconds |
| Invite member | 1-2 seconds |
| Accept invitation | 1-2 seconds |
| Page reload | 1-2 seconds |

---

## 📸 Screenshots to Verify

### 1. My Dârs - Pending Invitation
- [ ] Member badge visible
- [ ] Pending Invitation badge visible (yellow)
- [ ] Accept Invitation button visible (green)

### 2. After Accepting
- [ ] Active badge visible (green)
- [ ] Open Details button visible
- [ ] Accept button gone

### 3. Dart Details Page
- [ ] Member listed as Active
- [ ] Green check icon next to member name

---

## 🎯 Success Criteria

✅ **Test Passed If:**
1. Only your darts are shown (not all darts)
2. Role badge shows correctly (Organizer/Member)
3. Status badge shows correctly (Pending/Active)
4. Accept button appears for pending invitations
5. Clicking accept changes status to Active
6. Can open dart details after accepting

---

## 📞 Need Help?

**Check logs:**
```bash
# Backend logs
docker compose logs platform-back -f

# Or if running locally
tail -f platform-back/backend.log
```

**Check network:**
- Open browser DevTools (F12)
- Go to Network tab
- Look for failed requests
- Check response status codes

---

## 🎉 You're Done!

If all tests pass, you've successfully verified:
- ✅ User-specific dart filtering
- ✅ Role display (Organizer/Member)
- ✅ Member status display (Pending/Active)
- ✅ Invitation acceptance workflow

**Next:** Try with multiple users and multiple darts!

---

**Last Updated:** February 2024
**Estimated Test Time:** 5 minutes