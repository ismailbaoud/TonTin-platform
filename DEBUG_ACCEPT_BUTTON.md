# 🔍 Debug Guide - Accept Invitation Button Not Showing

## Quick Fix Applied ✅

**Issue:** The `userMemberStatus` field was not being mapped from API response to the component.

**Fix:** Updated `my-dars.component.ts` to include `userMemberStatus` and `userPermission` in the mapping.

---

## 🧪 How to Test Now

### Step 1: Start Application
```bash
# Backend
cd platform-back
./mvnw spring-boot:run

# Frontend (new terminal)
cd platform-front
npm start
```

### Step 2: Open Browser Console
1. Open browser: http://localhost:4200
2. Press **F12** to open DevTools
3. Go to **Console** tab

### Step 3: Login and Check Logs

**Login** and navigate to My Dârs page. You should see detailed logs:

```javascript
📊 API Response: {content: Array(3), page: 0, ...}
📋 Darts received: 3

Dart 1: Family Savings Circle
  - isOrganizer: true
  - userPermission: ORGANIZER
  - userMemberStatus: ACTIVE

Dart 2: Office Savings
  - isOrganizer: false
  - userPermission: MEMBER
  - userMemberStatus: PENDING  ← This one should show button

Dart 3: Community Fund
  - isOrganizer: false
  - userPermission: MEMBER
  - userMemberStatus: ACTIVE

✅ Mapped dars: [
  {name: "Family Savings Circle", userMemberStatus: "ACTIVE", isOrganizer: true},
  {name: "Office Savings", userMemberStatus: "PENDING", isOrganizer: false},
  {name: "Community Fund", userMemberStatus: "ACTIVE", isOrganizer: false}
]
```

---

## ✅ What to Look For

### If Button Shows (SUCCESS ✅)
You should see:
- Dart card with **⚫ Member** badge
- Yellow badge: **🟡 Pending Invitation**
- Green button: **"Accept Invitation"**

### If Button Still Missing (DEBUG 🔍)

#### Check 1: Is userMemberStatus being received?
Look in console logs for:
```javascript
userMemberStatus: "PENDING"  // Should be present
```

If it's `undefined` or `null`:
- ❌ Backend not returning the field
- **Fix:** Check backend `DartMapper.java` lines 65-68

#### Check 2: Is it the correct value?
The button shows when:
```typescript
dar.userMemberStatus === 'PENDING' && !dar.isOrganizer
```

Check console:
```javascript
// Should be:
userMemberStatus: "PENDING"
isOrganizer: false

// NOT:
userMemberStatus: "ACTIVE"  // Already accepted
isOrganizer: true           // Organizers auto-accept
```

#### Check 3: HTML rendering
In browser console, run:
```javascript
// Check if dars have the field
console.log('Dars:', this.dars);

// Check specific dart
const pendingDar = this.dars.find(d => d.userMemberStatus === 'PENDING');
console.log('Pending dart:', pendingDar);
```

---

## 🔧 Manual Backend Test

Test if backend returns `userMemberStatus`:

```bash
# 1. Login
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your-email@example.com",
    "password": "your-password"
  }'

# 2. Copy the token from response
TOKEN="paste_token_here"

# 3. Get darts
curl -X GET http://localhost:9090/api/v1/dart/my-dars \
  -H "Authorization: Bearer $TOKEN"

# 4. Check response includes these fields:
# {
#   "content": [{
#     "id": "...",
#     "name": "...",
#     "userPermission": "MEMBER",      ← Should be present
#     "userMemberStatus": "PENDING"    ← Should be present
#   }]
# }
```

### If Backend Missing Fields

**Check file:** `platform-back/src/main/java/com/tontin/platform/mapper/DartMapper.java`

**Lines 65-68 should have:**
```java
.userPermission(userPermission)
.userMemberStatus(userMemberStatus)
```

If missing, add them and rebuild:
```bash
cd platform-back
./mvnw clean install
./mvnw spring-boot:run
```

---

## 🎯 Test Scenario: Create Pending Member

### As Organizer:
1. Login as User A
2. Create a new dart
3. Go to dart details → Members tab
4. Invite User B (this creates member with status PENDING)

### As Invited User:
1. **Logout** User A
2. **Login** as User B
3. Navigate to **My Dârs**
4. **Look for the dart** you were invited to
5. **Check console logs** - should show `userMemberStatus: "PENDING"`
6. **Look for button** - green "Accept Invitation" button

---

## 🐛 Common Issues

### Issue 1: userMemberStatus is undefined
**Cause:** Backend not returning the field

**Fix:**
1. Check `DartMapper.java` has the fields
2. Rebuild backend
3. Restart backend
4. Clear browser cache (Ctrl+Shift+R)

### Issue 2: userMemberStatus is "ACTIVE" but expecting "PENDING"
**Cause:** Member already accepted invitation

**Fix:**
- This is correct behavior
- Member is already active
- No button should show (already accepted)
- To test: create a NEW invitation

### Issue 3: Console shows no logs
**Cause:** Frontend not updated

**Fix:**
```bash
cd platform-front
# Kill the dev server (Ctrl+C)
npm start
# Hard refresh browser (Ctrl+Shift+R)
```

---

## ✅ Success Criteria

**Button will show when ALL these are true:**

1. ✅ `dar.userMemberStatus === 'PENDING'`
2. ✅ `dar.isOrganizer === false`
3. ✅ User is logged in
4. ✅ Frontend received the data
5. ✅ Mapping includes the fields

**Visual confirmation:**
```
┌────────────────────────────────┐
│ Office Savings                 │
│                                │
│ ⚫ Member  🟡 Pending          │ ← These badges
│    Invitation                  │
│                                │
│ Contribution: $500/Month       │
│ Pot Size: $3000               │
│                                │
│ ┌──────────────────────────┐  │
│ │  Accept Invitation       │  │ ← This button (GREEN)
│ └──────────────────────────┘  │
└────────────────────────────────┘
```

---

## 🎉 After Fix Applied

**Files changed:**
1. `my-dars.component.ts` - Added `userMemberStatus` to interface and mapping
2. Added console logging for debugging

**What to do:**
1. Restart frontend (Ctrl+C, then `npm start`)
2. Hard refresh browser (Ctrl+Shift+R)
3. Open console (F12)
4. Navigate to My Dârs
5. Check console logs
6. Look for green "Accept Invitation" button on pending darts

---

## 📞 Still Not Working?

**Share these details:**

1. Console logs (copy from browser console)
2. Backend API response:
   ```bash
   curl -X GET http://localhost:9090/api/v1/dart/my-dars \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```
3. Database check:
   ```sql
   SELECT d.name, m.status, m.permission, u.user_name
   FROM members m
   JOIN darts d ON m.dart_id = d.id
   JOIN users u ON m.user_id = u.id
   WHERE u.id = 'YOUR_USER_ID';
   ```

---

**Status:** ✅ Fix Applied - Ready to Test
**Next:** Restart frontend and check console logs