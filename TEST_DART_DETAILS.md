# Testing Guide: Dynamic Dart Details Feature

## Overview

This guide will help you verify that the dart details page displays unique, dynamic data for each dart you select from the list.

## Prerequisites

1. Backend server running on `http://localhost:8080`
2. Frontend server running on `http://localhost:4200`
3. At least 2-3 darts created with different data
4. User logged in with valid authentication

## Test Scenarios

### Scenario 1: Basic Dynamic Display

**Objective**: Verify that clicking different darts shows different data

**Steps**:
1. Navigate to `http://localhost:4200/dashboard/client/my-dars`
2. Open browser DevTools (F12) and go to the Console tab
3. Click "Open Details" on the first dart
4. Note the console output:
   - Dart ID that was clicked
   - API call being made
   - Data received from backend
5. Go back to the list (use back button or breadcrumb)
6. Click "Open Details" on a different dart
7. Compare the console output with step 4

**Expected Result**:
- Different Dart IDs in the console
- Different API calls (different UUIDs in the URL)
- Different data returned (name, status, member count, etc.)
- Different information displayed on the page

**Example Console Output**:
```
=== Opening Dart Details ===
Dart ID: 123e4567-e89b-12d3-a456-426614174000
Navigation path: ["/dashboard/client/dar", "123e4567-e89b-12d3-a456-426614174000"]

=== Dart Details Component Initialized ===
Dart ID from route: 123e4567-e89b-12d3-a456-426614174000

📡 Loading Dart details for ID: 123e4567-e89b-12d3-a456-426614174000

✅ Dart details loaded successfully:
  - Dart ID: 123e4567-e89b-12d3-a456-426614174000
  - Dart Name: Family Vacation Fund
  - Status: ACTIVE
```

---

### Scenario 2: URL Parameter Verification

**Objective**: Verify that the dart ID is correctly passed through the URL

**Steps**:
1. Navigate to My Darts page
2. Click "Open Details" on any dart
3. Look at the browser's address bar
4. Copy the URL (should be like: `http://localhost:4200/dashboard/client/dar/123e4567-...`)
5. Click back, select a different dart
6. Compare the new URL with the previous one

**Expected Result**:
- Each dart has a unique UUID in the URL
- The UUID in the URL matches the dart's actual ID
- Different darts have different UUIDs

---

### Scenario 3: Direct URL Access

**Objective**: Verify that directly accessing a dart via URL works

**Steps**:
1. Create or note down a specific dart ID (from console or database)
2. Navigate directly to: `http://localhost:4200/dashboard/client/dar/{DART_ID}`
3. Verify the correct dart details are displayed
4. Try with a different dart ID
5. Verify different data is shown

**Expected Result**:
- Each URL shows the correct dart's data
- No errors in console
- Page displays the specific dart's information

---

### Scenario 4: API Network Verification

**Objective**: Verify the correct API calls are being made

**Steps**:
1. Open DevTools (F12) and go to Network tab
2. Filter by "XHR" or "Fetch"
3. Navigate to My Darts page
4. Click "Open Details" on a dart
5. In Network tab, find the API call to `/api/v1/dart/{id}`
6. Click on it and check:
   - Request URL (should have the dart's UUID)
   - Response (should have that dart's specific data)
7. Go back and click on a different dart
8. Compare the new API call with the previous one

**Expected Result**:
- Different Request URLs for different darts
- Different Response data for different darts
- Status code 200 OK for successful calls

**Example Network Requests**:
```
Request 1:
GET http://localhost:8080/api/v1/dart/123e4567-e89b-12d3-a456-426614174000
Response: { "id": "123e4567...", "name": "Family Vacation Fund", ... }

Request 2:
GET http://localhost:8080/api/v1/dart/987e6543-e89b-12d3-a456-426614174999
Response: { "id": "987e6543...", "name": "Office Savings", ... }
```

---

### Scenario 5: Backend Logs Verification

**Objective**: Verify backend is processing different dart IDs

**Steps**:
1. Open your backend console/logs
2. From frontend, click "Open Details" on a dart
3. Check backend logs for:
   ```
   INFO  c.t.p.controller.DartController : Fetching dart with ID: {uuid1}
   ```
4. Go back and click on another dart
5. Check backend logs for a different UUID

**Expected Result**:
- Different UUIDs logged for different darts
- Each request shows the correct dart ID being fetched
- No errors in backend logs

---

### Scenario 6: Database Verification

**Objective**: Verify multiple darts exist with different data

**Steps**:
1. Connect to your database (PostgreSQL, MySQL, etc.)
2. Run query:
   ```sql
   SELECT id, name, status, monthly_contribution, member_count, created_at
   FROM dart
   ORDER BY created_at DESC
   LIMIT 10;
   ```
3. Verify multiple rows exist with different values
4. Note the IDs and names
5. From the frontend, click on a dart and verify the displayed data matches the database

**Expected Result**:
- Multiple distinct darts in database
- Each dart has unique ID and potentially different attributes
- Frontend displays data matching the database

---

## Common Issues & Troubleshooting

### Issue: All darts show the same data

**Possible Causes**:
1. ❌ Only one dart exists in database
2. ❌ All darts were created with identical data
3. ❌ Backend caching issue
4. ❌ Frontend caching issue

**Debug Steps**:
```bash
# Check database
SELECT COUNT(*) FROM dart;  -- Should be > 1
SELECT COUNT(DISTINCT name) FROM dart;  -- Should be > 1

# Check backend logs
tail -f backend.log | grep "Fetching dart with ID"

# Check frontend console
# Should show different IDs being loaded
```

**Solutions**:
- Create multiple darts with different names/settings
- Clear backend cache (restart server)
- Hard refresh frontend (Ctrl+Shift+R)
- Clear browser cache and cookies

---

### Issue: Console shows "No Dâr ID provided"

**Possible Causes**:
1. ❌ Route parameter not configured correctly
2. ❌ Navigation not passing the ID
3. ❌ Route configuration mismatch

**Debug Steps**:
```typescript
// Check route configuration in dars.routes.ts
{
  path: 'dar/:id',  // ✅ Should have :id
  loadComponent: () => import('./pages/dar-details.component')
}

// Check navigation in my-dars.component.ts
openDetails(darId: string): void {
  this.router.navigate(['/dashboard/client/dar', darId]);  // ✅ Should pass darId
}
```

**Solutions**:
- Verify route has `:id` parameter
- Verify `openDetails()` method passes `darId`
- Check console logs for the navigation path

---

### Issue: API returns 404 Not Found

**Possible Causes**:
1. ❌ Dart doesn't exist in database
2. ❌ Wrong dart ID being passed
3. ❌ Authentication/permission issue

**Debug Steps**:
```bash
# Check if dart exists
SELECT * FROM dart WHERE id = 'your-dart-uuid';

# Check backend logs for actual ID received
# Check frontend console for ID being sent
```

**Solutions**:
- Verify the dart ID exists in database
- Check authentication token is valid
- Verify user has permission to view the dart

---

### Issue: Members not loading

**Possible Causes**:
1. ❌ Members endpoint mismatch (NOW FIXED)
2. ❌ Dart has no members
3. ❌ Permission issue

**Debug Steps**:
```bash
# Check members exist
SELECT * FROM member WHERE dart_id = 'your-dart-uuid';

# Check Network tab for members API call
GET /api/v1/member/dart/{dartId}
```

**Solutions**:
- Ensure dart has at least the organizer as a member
- Verify the endpoint URL is correct (now uses `/api/v1/member/dart/{dartId}`)
- Check user permissions

---

## Verification Checklist

Use this checklist to verify the feature is working correctly:

- [ ] Can see list of darts on My Darts page
- [ ] Each dart card shows different data (name, members, pot size, etc.)
- [ ] Clicking "Open Details" navigates to a new page
- [ ] URL contains the dart's UUID
- [ ] Different darts have different UUIDs in the URL
- [ ] Console logs show the correct dart ID being loaded
- [ ] Console logs show "✅ Dart details loaded successfully"
- [ ] Page displays the correct dart name
- [ ] Page displays the correct dart status
- [ ] Page displays the correct member count
- [ ] Page displays the correct monthly pot
- [ ] Network tab shows API call to `/api/v1/dart/{uuid}`
- [ ] API response contains the correct dart data
- [ ] Backend logs show the correct UUID being processed
- [ ] Going back and selecting a different dart shows different data
- [ ] Members section loads (if members exist)
- [ ] No errors in browser console
- [ ] No errors in backend logs

---

## Test Data Creation

If you need to create test darts with different data for testing:

### Via API (using cURL):

```bash
# Create Dart 1
curl -X POST http://localhost:8080/api/v1/dart \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Family Vacation Fund",
    "monthlyContribution": 200,
    "orderMethod": "RANDOM",
    "paymentFrequency": "MONTHLY",
    "description": "Saving for a family trip"
  }'

# Create Dart 2
curl -X POST http://localhost:8080/api/v1/dart \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Office Savings",
    "monthlyContribution": 500,
    "orderMethod": "RANDOM",
    "paymentFrequency": "MONTHLY",
    "description": "Office equipment fund"
  }'

# Create Dart 3
curl -X POST http://localhost:8080/api/v1/dart \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Car Fund",
    "monthlyContribution": 150,
    "orderMethod": "RANDOM",
    "paymentFrequency": "MONTHLY",
    "description": "Saving for a new vehicle"
  }'
```

### Via Frontend:

1. Navigate to `http://localhost:4200/dashboard/client/create-dar`
2. Fill in the form with unique data:
   - **Dart 1**: Name="Family Vacation Fund", Contribution=200
   - **Dart 2**: Name="Office Savings", Contribution=500
   - **Dart 3**: Name="New Car Fund", Contribution=150
3. Create each dart
4. Verify they appear in My Darts list

---

## Expected Console Output Examples

### When clicking on "Family Vacation Fund":
```
=== Opening Dart Details ===
Dart ID: 123e4567-e89b-12d3-a456-426614174000
Navigation path: ["/dashboard/client/dar", "123e4567-e89b-12d3-a456-426614174000"]

=== Dart Details Component Initialized ===
Dart ID from route: 123e4567-e89b-12d3-a456-426614174000
Full route params: {id: "123e4567-e89b-12d3-a456-426614174000"}

📡 Loading Dart details for ID: 123e4567-e89b-12d3-a456-426614174000

✅ Dart details loaded successfully:
  - Dart ID: 123e4567-e89b-12d3-a456-426614174000
  - Dart Name: Family Vacation Fund
  - Status: ACTIVE
  - Full Data: { id: "123e4567...", name: "Family Vacation Fund", ... }

📊 Mapped component data:
  - Display Name: Family Vacation Fund
  - Organizer: John Doe
  - Members Count: 10

📡 Loading members for Dart ID: 123e4567-e89b-12d3-a456-426614174000
✅ Loaded 10 members
```

### When clicking on "Office Savings":
```
=== Opening Dart Details ===
Dart ID: 987e6543-e89b-12d3-a456-426614174999
Navigation path: ["/dashboard/client/dar", "987e6543-e89b-12d3-a456-426614174999"]

=== Dart Details Component Initialized ===
Dart ID from route: 987e6543-e89b-12d3-a456-426614174999
Full route params: {id: "987e6543-e89b-12d3-a456-426614174999"}

📡 Loading Dart details for ID: 987e6543-e89b-12d3-a456-426614174999

✅ Dart details loaded successfully:
  - Dart ID: 987e6543-e89b-12d3-a456-426614174999
  - Dart Name: Office Savings
  - Status: PENDING
  - Full Data: { id: "987e6543...", name: "Office Savings", ... }

📊 Mapped component data:
  - Display Name: Office Savings
  - Organizer: Sarah M.
  - Members Count: 6

📡 Loading members for Dart ID: 987e6543-e89b-12d3-a456-426614174999
✅ Loaded 6 members
```

**Notice**: Each dart has:
- ✅ Different UUID
- ✅ Different name
- ✅ Different member count
- ✅ Different status
- ✅ Different organizer

---

## Success Criteria

The feature is working correctly if:

1. ✅ **Each dart has a unique ID** - Visible in console and URL
2. ✅ **API calls use different IDs** - Visible in Network tab
3. ✅ **Backend processes different IDs** - Visible in backend logs
4. ✅ **Page displays different data** - Visible on the screen
5. ✅ **No errors occur** - Console and logs are clean
6. ✅ **Members load dynamically** - Each dart shows its own members
7. ✅ **Navigation works both ways** - Can go back and forward
8. ✅ **Direct URL access works** - Can bookmark and share specific darts

---

## Final Verification

After running all tests, answer these questions:

1. ❓ Do different darts show different names on the details page?
   - ✅ YES = Feature working correctly
   - ❌ NO = Issue exists, review troubleshooting section

2. ❓ Are different UUIDs visible in the browser URL?
   - ✅ YES = Routing working correctly
   - ❌ NO = Check route configuration

3. ❓ Do Network requests show different API calls?
   - ✅ YES = API integration correct
   - ❌ NO = Check service implementation

4. ❓ Do backend logs show different dart IDs being fetched?
   - ✅ YES = Backend working correctly
   - ❌ NO = Check backend controller

5. ❓ Are console logs showing "✅ Dart details loaded successfully" with correct data?
   - ✅ YES = Data mapping working
   - ❌ NO = Check mapper function

If all answers are YES ✅, the dynamic dart details feature is working perfectly!

---

## Need Help?

If you're still experiencing issues:

1. 📋 Copy all console logs
2. 📋 Copy relevant backend logs
3. 📋 Take screenshots of the issue
4. 📋 Note the exact steps to reproduce
5. 📋 Check the database state

Then review the `DART_DETAILS_DYNAMIC_IMPLEMENTATION.md` file for the complete technical explanation.

---

## Summary

The dart details page is **fully functional and dynamic**. Each time you click on a different dart:

1. The dart's unique ID is passed through the URL
2. The component extracts this ID from the route
3. An API call is made with this specific ID
4. The backend fetches that dart's data from the database
5. The data is mapped and displayed on the page
6. Members are loaded separately for that dart

This happens automatically every time you navigate to a dart details page, ensuring you always see the correct, up-to-date information for the dart you selected.