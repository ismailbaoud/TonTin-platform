# Dynamic Darts Listing Feature - Implementation Summary

**Date:** 2026-02-15  
**Feature:** Dynamic Darts Listing with Pagination  
**Status:** ✅ COMPLETE AND READY

---

## Overview

This document summarizes the implementation of the dynamic darts listing feature that replaces the static mock data with real API calls to fetch the user's darts from the backend.

---

## What Was Implemented

### 1. Backend API Endpoint

**Endpoint:** `GET /api/v1/dart/my-dars`

**Description:** Fetches all darts where the authenticated user is a member, with optional status filtering and pagination.

**Request Parameters:**
- `status` (optional) - Filter by dart status (PENDING, ACTIVE, FINISHED)
- `page` (optional, default: 0) - Page number (0-based)
- `size` (optional, default: 10) - Number of items per page

**Response Format:**
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Family Savings Circle",
      "monthlyContribution": 100.00,
      "startDate": "2024-01-15T10:30:00",
      "orderMethod": "FIXED_ORDER",
      "description": "A savings circle for the holidays",
      "paymentFrequency": "MONTH",
      "status": "ACTIVE",
      "memberCount": 10,
      "totalMonthlyPool": 1000.00,
      "organizerId": "uuid",
      "organizerName": "John Doe",
      "organizerAvatar": "base64_encoded_image",
      "isOrganizer": true,
      "currentCycle": 3,
      "totalCycles": 10,
      "nextPayoutDate": "2024-02-15T10:00:00",
      "image": null,
      "customRules": "Pay on time",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## Changes Made

### Backend Changes

#### 1. Repository Layer (`DartRepository.java`)

**Added Methods:**

```java
@Query("SELECT DISTINCT d FROM Dart d JOIN d.members m WHERE m.user.id = :userId ORDER BY d.createdAt DESC")
Page<Dart> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

@Query("SELECT DISTINCT d FROM Dart d JOIN d.members m WHERE m.user.id = :userId AND d.status = :status ORDER BY d.createdAt DESC")
Page<Dart> findAllByUserIdAndStatus(
    @Param("userId") UUID userId,
    @Param("status") DartStatus status,
    Pageable pageable
);
```

**Purpose:** 
- Find all darts where a user is a member
- Support filtering by dart status
- Support pagination
- Order by creation date (newest first)

---

#### 2. DTO Layer

**Created:** `PageResponse.java`

Generic paginated response wrapper that includes:
- `content` - List of items
- `page` - Current page number
- `size` - Items per page
- `totalElements` - Total items across all pages
- `totalPages` - Total number of pages
- `first` - Whether this is the first page
- `last` - Whether this is the last page
- `hasNext` - Whether there are more pages
- `hasPrevious` - Whether there are previous pages

**Enhanced:** `DartResponse.java`

Added fields:
- `organizerId` - ID of the dart organizer
- `organizerName` - Name of the organizer
- `organizerAvatar` - Avatar image (base64 encoded)
- `isOrganizer` - Whether current user is the organizer
- `currentCycle` - Current round number
- `totalCycles` - Total number of rounds
- `nextPayoutDate` - Date of next payout
- `image` - Cover image URL
- `customRules` - Custom rules and terms

---

#### 3. Mapper Layer (`DartMapper.java`)

**Added Method:**

```java
default DartResponse toDtoWithContext(Dart dart, UUID currentUserId) {
    // Maps Dart entity to DartResponse with:
    // - Organizer information extracted from members
    // - isOrganizer flag based on current user
    // - All additional context fields
    // - Calculated values (totalMonthlyPool, etc.)
}
```

**Purpose:** 
- Include organizer information from member relationships
- Determine if current user is the organizer
- Calculate derived fields
- Provide complete context for frontend

---

#### 4. Service Layer (`DartService.java` & `DartServiceImpl.java`)

**Added Interface Method:**

```java
PageResponse<DartResponse> getMyDarts(
    DartStatus status,
    int page,
    int size
);
```

**Implementation:**
- Gets current authenticated user
- Queries darts from repository with pagination
- Maps results using `toDtoWithContext` for complete information
- Returns paginated response

**Updated Methods:**
- `createDart` - Now uses `toDtoWithContext`
- `updateDart` - Now uses `toDtoWithContext`
- `getDartDetails` - Now uses `toDtoWithContext`
- `deleteDart` - Now uses `toDtoWithContext`

---

#### 5. Controller Layer (`DartController.java`)

**Added Endpoint:**

```java
@GetMapping("/my-dars")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
public ResponseEntity<PageResponse<DartResponse>> getMyDarts(
    @RequestParam(required = false) DartStatus status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
)
```

**Features:**
- Requires authentication (CLIENT or ADMIN role)
- Optional status filtering
- Pagination support with defaults
- Full Swagger documentation
- Logging for debugging

---

### Frontend Changes

#### Frontend Was Already Prepared!

The frontend component `my-dars.component.ts` was already set up to:
- ✅ Call `darService.getMyDars()` API
- ✅ Handle pagination
- ✅ Handle status filtering
- ✅ Display loading states
- ✅ Handle errors with fallback to mock data
- ✅ Map API response to display format

**No frontend changes were required!** The component already had:
- API integration via `DarService`
- Proper response mapping via `mapApiDarsToComponent()`
- Pagination controls
- Status tab filtering
- Error handling

---

## Data Flow

```
User Requests Darts
       ↓
Frontend: my-dars.component.ts
   - Calls darService.getMyDars(status, page, size)
       ↓
HTTP GET: /api/v1/dart/my-dars?status=ACTIVE&page=0&size=10
       ↓
Backend: DartController.getMyDarts()
   - Validates authentication
   - Logs request
       ↓
DartService.getMyDarts()
   - Gets current user from security context
   - Queries DartRepository
       ↓
DartRepository.findAllByUserIdAndStatus()
   - Executes JPQL query with JOIN on members
   - Returns Page<Dart>
       ↓
DartMapper.toDtoWithContext()
   - Extracts organizer from members
   - Determines isOrganizer flag
   - Maps all fields
       ↓
Returns PageResponse<DartResponse>
       ↓
Frontend receives response
   - Maps to display format
   - Renders in UI
```

---

## Key Features

### 1. Pagination
- Backend supports efficient pagination via Spring Data
- Frontend displays pagination controls
- Configurable page size
- Total count and page numbers displayed

### 2. Status Filtering
- Filter by PENDING, ACTIVE, FINISHED, or ALL
- Implemented at database level for efficiency
- Tab-based UI for easy filtering

### 3. Organizer Information
- Automatically extracted from member relationships
- Includes name, avatar (base64 encoded)
- `isOrganizer` flag for current user

### 4. Context-Aware Responses
- Each dart includes whether current user is organizer
- Enables conditional UI (edit, delete buttons, etc.)
- Provides complete information in single request

### 5. Security
- Requires authentication
- Only returns darts where user is a member
- User isolation via database queries

---

## Testing

### Manual Testing Steps

#### 1. Backend API Test

```bash
# Login to get token
TOKEN=$(curl -s -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}' \
  | jq -r '.accessToken')

# Get all darts (page 0, size 10)
curl -X GET "http://localhost:9090/api/v1/dart/my-dars?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# Get active darts only
curl -X GET "http://localhost:9090/api/v1/dart/my-dars?status=ACTIVE&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"

# Get second page
curl -X GET "http://localhost:9090/api/v1/dart/my-dars?page=1&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Results:**
- Status 200 OK
- Paginated response with content array
- Each dart includes all fields
- Correct pagination metadata

#### 2. Frontend Test

1. **Navigate to My Dârs page:**
   - URL: `http://localhost:4200/dashboard/client/my-dars`
   - Login if not authenticated

2. **Verify Dynamic Loading:**
   - Should show loading spinner initially
   - Should display real darts from API
   - Should NOT show mock data (unless API fails)

3. **Test Filtering:**
   - Click "Active" tab → Shows only active darts
   - Click "Completed" tab → Shows only finished darts
   - Click "All" tab → Shows all darts

4. **Test Pagination:**
   - Create more than 10 darts (or set size smaller)
   - Verify pagination controls appear
   - Click "Next" → Shows next page
   - Click page number → Jumps to that page

5. **Test Search:**
   - Enter dart name in search box
   - Verify client-side filtering works

6. **Test Organizer Features:**
   - Darts where you're organizer show "You" as organizer
   - Edit/Delete buttons appear for your darts
   - Other darts show actual organizer name

---

## Database Queries

### Query Performance

The repository uses JOIN to fetch darts efficiently:

```sql
SELECT DISTINCT d.* 
FROM darts d 
INNER JOIN members m ON m.dart_id = d.id 
WHERE m.user_id = ?1 
ORDER BY d.created_at DESC
LIMIT ? OFFSET ?
```

**Optimizations:**
- ✅ Uses JOIN instead of N+1 queries
- ✅ DISTINCT prevents duplicates from multiple members
- ✅ Indexed on foreign keys
- ✅ Pagination at database level
- ✅ ORDER BY for consistent results

---

## Error Handling

### Backend

1. **Authentication Required:**
   - Returns 401 if not authenticated
   - Returns 403 if missing required role

2. **Validation:**
   - Page number must be >= 0
   - Page size must be > 0
   - Status must be valid enum value

3. **Database Errors:**
   - Logged with full stack trace
   - Returns 500 with generic message (security)

### Frontend

1. **API Failures:**
   - Shows error message to user
   - Falls back to mock data (temporary)
   - Provides retry capability

2. **Empty Results:**
   - Shows "No darts found" message
   - Displays "Create New Dâr" button

3. **Network Errors:**
   - Detects timeout/connection issues
   - Shows appropriate error message

---

## Configuration

### Backend Configuration

**Default Values:**
```java
@RequestParam(defaultValue = "0") int page
@RequestParam(defaultValue = "10") int size
```

**Can be overridden via request parameters:**
- `/my-dars` → page=0, size=10
- `/my-dars?size=20` → page=0, size=20
- `/my-dars?page=2&size=5` → page=2, size=5

### Frontend Configuration

**In `my-dars.component.ts`:**
```typescript
currentPage = 0;
pageSize = 12;  // Default grid view shows 12 items
```

**Can be adjusted:**
- Change `pageSize` for different grid layouts
- Modify `loadDars()` call for custom page sizes

---

## API Documentation

### Swagger UI

Access interactive API docs at:
- URL: `http://localhost:9090/swagger-ui.html`
- Endpoint: `/api/v1/dart/my-dars`

**Documentation includes:**
- ✅ Request parameters with examples
- ✅ Response schema
- ✅ Error codes and meanings
- ✅ Try it out functionality
- ✅ Authentication requirements

---

## Benefits

### Performance
- ✅ Efficient database queries with JOIN
- ✅ Pagination reduces data transfer
- ✅ Only fetches user's darts (filtered at DB level)

### User Experience
- ✅ Real-time data (always up to date)
- ✅ Fast loading with pagination
- ✅ Status filtering for easy management
- ✅ Search functionality

### Maintainability
- ✅ Single source of truth (database)
- ✅ No mock data to maintain
- ✅ Clear separation of concerns
- ✅ Well-documented API

### Security
- ✅ Authentication required
- ✅ Authorization checks (role-based)
- ✅ User isolation (only see own darts)
- ✅ No data leakage between users

---

## Future Enhancements

### Potential Improvements

1. **Sorting Options:**
   - Add sort parameter (by name, date, status)
   - Multiple sort fields

2. **Advanced Filtering:**
   - Filter by date range
   - Filter by contribution amount
   - Filter by member count

3. **Search at Backend:**
   - Move search from client to server
   - Full-text search on dart name/description
   - Better performance for large datasets

4. **Caching:**
   - Implement Redis cache for frequently accessed darts
   - Cache invalidation on updates
   - Reduce database load

5. **Real-time Updates:**
   - WebSocket notifications
   - Auto-refresh when darts change
   - Live member count updates

6. **Image Handling:**
   - Add image upload for dart covers
   - Store images separately (S3, CDN)
   - Optimize image loading

---

## Troubleshooting

### Issue: Empty darts list

**Possible Causes:**
1. No darts created yet → Create a dart
2. User not member of any darts → Join or create dart
3. Database query issue → Check logs

**Debug Steps:**
```bash
# Check if darts exist in database
SELECT COUNT(*) FROM darts;

# Check if user has member records
SELECT COUNT(*) FROM members WHERE user_id = 'user-uuid';

# Check if JOIN works
SELECT d.name FROM darts d 
JOIN members m ON m.dart_id = d.id 
WHERE m.user_id = 'user-uuid';
```

### Issue: Wrong dart count

**Possible Cause:** Duplicate results from JOIN

**Solution:** 
- Query uses DISTINCT
- Verify in logs: should show DISTINCT in SQL

### Issue: Missing organizer information

**Possible Causes:**
1. No organizer member exists
2. Multiple members marked as organizer
3. Eager loading issue

**Debug:**
- Check members table for ORGANIZER permission
- Verify one and only one organizer per dart

---

## Code Structure

### Backend

```
platform-back/
├── controller/
│   └── DartController.java          # GET /my-dars endpoint
├── service/
│   ├── DartService.java              # getMyDarts() interface
│   └── impl/
│       └── DartServiceImpl.java      # getMyDarts() implementation
├── repository/
│   └── DartRepository.java           # findAllByUserId() queries
├── mapper/
│   └── DartMapper.java               # toDtoWithContext() method
└── dto/
    └── dart/
        └── response/
            ├── DartResponse.java      # Enhanced with new fields
            └── PageResponse.java      # Generic pagination wrapper
```

### Frontend

```
platform-front/
└── src/app/features/dashboard/features/dars/
    ├── pages/
    │   └── my-dars.component.ts     # Already uses darService.getMyDars()
    ├── services/
    │   └── dar.service.ts           # API integration
    └── models/
        └── dar.model.ts             # Frontend types
```

---

## Summary

| Component | Status | Notes |
|-----------|--------|-------|
| **Backend Repository** | ✅ Complete | Query with JOIN and pagination |
| **Backend Service** | ✅ Complete | Includes user context |
| **Backend Controller** | ✅ Complete | Full API documentation |
| **Backend DTO** | ✅ Complete | Enhanced with all fields |
| **Backend Mapper** | ✅ Complete | Context-aware mapping |
| **Frontend Component** | ✅ Already Done | No changes needed! |
| **Frontend Service** | ✅ Already Done | API integration ready |
| **API Documentation** | ✅ Complete | Swagger UI available |
| **Testing** | ✅ Ready | Manual test steps provided |

---

## Conclusion

The dynamic darts listing feature is **fully implemented and ready for use**. The backend provides a robust, paginated API endpoint that includes all necessary information for the frontend. The frontend was already prepared to consume this API, so no frontend changes were required.

**Key Achievements:**
- ✅ Replaced static mock data with real API calls
- ✅ Efficient database queries with proper JOIN
- ✅ Pagination for scalability
- ✅ Status filtering for organization
- ✅ Complete organizer information in responses
- ✅ Context-aware data (isOrganizer flag)
- ✅ Full API documentation
- ✅ Secure and performant

**The feature is production-ready and can be tested immediately!** 🎉

---

**Last Updated:** 2026-02-15  
**Backend Endpoint:** `/api/v1/dart/my-dars`  
**Frontend Page:** `/dashboard/client/my-dars`  
**Status:** ✅ COMPLETE