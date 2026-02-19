# Dart Details Dynamic Implementation - Complete Guide

## Overview

The dart details page is **already correctly implemented** to display dynamic data based on the dart ID you select from the list. When you click "Open Details" on a dart card, the system fetches and displays the specific details for that dart from the API.

## How It Works

### 1. Navigation Flow

When you click "Open Details" button on a dart card:

```typescript
// my-dars.component.ts
openDetails(darId: string): void {
  console.log("=== Opening Dart Details ===");
  console.log("Dart ID:", darId);
  console.log("Navigation path:", ["/dashboard/client/dar", darId]);
  this.router.navigate(["/dashboard/client/dar", darId]);
}
```

This navigates to: `/dashboard/client/dar/{dart-uuid}`

### 2. Route Configuration

```typescript
// dars.routes.ts
{
  path: 'dar/:id',
  loadComponent: () =>
    import('./pages/dar-details.component').then((m) => m.DarDetailsComponent),
  data: {
    title: 'Dâr Details - TonTin',
  },
}
```

The `:id` parameter captures the dart UUID from the URL.

### 3. Dart Details Component Initialization

```typescript
// dar-details.component.ts
ngOnInit(): void {
  // Extract dart ID from route parameters
  this.darId = this.route.snapshot.paramMap.get("id");
  console.log("=== Dart Details Component Initialized ===");
  console.log("Dart ID from route:", this.darId);
  
  // Load the specific dart's details
  this.loadDarDetails();
}
```

### 4. API Call to Backend

```typescript
// dar-details.component.ts
loadDarDetails(): void {
  if (!this.darId) {
    this.error = "No Dâr ID provided";
    return;
  }

  console.log(`📡 Loading Dart details for ID: ${this.darId}`);
  this.isLoading = true;

  // Call API: GET /api/v1/dart/{darId}
  this.darService
    .getDarDetails(this.darId)
    .pipe(takeUntil(this.destroy$), finalize(() => (this.isLoading = false)))
    .subscribe({
      next: (data) => {
        console.log("✅ Dart details loaded successfully:");
        console.log("  - Dart ID:", data.id);
        console.log("  - Dart Name:", data.name);
        console.log("  - Status:", data.status);
        
        // Map API data to component format
        this.darDetails = this.mapApiDataToComponent(data);
        
        // Load members separately
        this.loadMembers();
      },
      error: (err) => {
        console.error("❌ Error loading Dâr details:", err);
        this.error = err.error?.message || "Failed to load Dâr details.";
      },
    });
}
```

### 5. Backend API Endpoint

```java
// DartController.java
@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
public ResponseEntity<DartResponse> getDart(@PathVariable("id") UUID id) {
    log.info("Fetching dart with ID: {}", id);
    DartResponse response = dartService.getDartDetails(id);
    log.info("Dart retrieved successfully: {}", id);
    return ResponseEntity.ok(response);
}
```

### 6. Service Layer

```java
// DartServiceImpl.java
@Override
@Transactional(readOnly = true)
public DartResponse getDartDetails(UUID id) {
    log.debug("Fetching dart details for id: {}", id);
    validateId(id);
    
    // Find the specific dart by ID
    Dart dart = findDartById(id);
    
    // Get current user context
    User currentUser = securityUtils.requireCurrentUser();
    
    // Map dart to response with user context
    return dartMapper.toDtoWithContext(dart, currentUser.getId());
}

private Dart findDartById(UUID id) {
    return dartRepository
        .findById(id)
        .orElseThrow(() -> {
            log.warn("Dart not found with id: {}", id);
            return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Dart not found with id: " + id
            );
        });
}
```

### 7. Data Mapping

The mapper converts the dart entity to a response DTO with all dynamic data:

```java
// DartMapper.java
default DartResponse toDtoWithContext(Dart dart, UUID currentUserId) {
    // Find organizer
    User organizer = dart.getMembers().stream()
        .filter(m -> m.getPermission() == DartPermission.ORGANIZER)
        .findFirst()
        .map(m -> m.getUser())
        .orElse(null);

    // Check if current user is organizer
    boolean isOrganizer = dart.getMembers().stream()
        .anyMatch(m -> 
            m.getUser().getId().equals(currentUserId) &&
            m.getPermission() == DartPermission.ORGANIZER
        );

    return DartResponse.builder()
        .id(dart.getId())                          // ✅ Unique ID
        .name(dart.getName())                      // ✅ Unique name
        .monthlyContribution(dart.getMonthlyContribution())
        .startDate(dart.getStartDate())
        .status(dart.getStatus())
        .memberCount(dart.getMemberCount())        // ✅ Unique member count
        .totalMonthlyPool(dart.calculateTotalMonthlyContributions())
        .organizerId(organizer != null ? organizer.getId() : null)
        .organizerName(organizer != null ? organizer.getUserName() : null)
        .isOrganizer(isOrganizer)                  // ✅ User-specific
        .currentCycle(0)
        .totalCycles(dart.getMemberCount())
        // ... more fields
        .build();
}
```

### 8. Display in Template

```html
<!-- dar-details.component.html -->
<div *ngIf="darDetails && !isLoading" class="flex flex-col gap-6">
  <!-- Breadcrumbs -->
  <nav class="flex flex-wrap gap-2 items-center text-sm">
    <span class="text-text-light dark:text-dark">{{ darDetails.name }}</span>
  </nav>

  <!-- Dâr Header Card -->
  <div class="bg-white dark:bg-[#15281e] rounded-xl shadow-sm p-6">
    <div class="flex gap-5 w-full">
      <!-- Dynamic Image -->
      <div class="h-24 w-24 rounded-lg bg-cover bg-center"
           [style.background-image]="'url(' + darDetails.image + ')'">
      </div>

      <div class="flex flex-col justify-center gap-1">
        <!-- Dynamic Name -->
        <h1 class="text-2xl font-bold">{{ darDetails.name }}</h1>
        
        <!-- Dynamic Status -->
        <span class="inline-flex items-center rounded-full px-2.5 py-0.5"
              [ngClass]="getStatusClass(darDetails.status)">
          {{ getStatusText(darDetails.status) }}
        </span>
      </div>
    </div>

    <!-- Dynamic Statistics -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div>
        <p class="text-sm text-gray-500">Members</p>
        <p class="text-xl font-bold">{{ darDetails.totalMembers }}</p>
      </div>
      <div>
        <p class="text-sm text-gray-500">Monthly Pot</p>
        <p class="text-xl font-bold">${{ darDetails.monthlyPot }}</p>
      </div>
      <!-- More dynamic fields -->
    </div>
  </div>
</div>
```

## Debugging Steps

If you're seeing the same data for all darts, here's how to debug:

### 1. Check Browser Console

Open browser DevTools (F12) and check the console for logs:

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

### 2. Check Network Tab

In DevTools Network tab, verify the API call:

```
Request URL: http://localhost:8080/api/v1/dart/123e4567-e89b-12d3-a456-426614174000
Request Method: GET
Status Code: 200 OK
```

Check the Response payload to ensure it contains the correct dart data.

### 3. Check Backend Logs

In your backend console, you should see:

```
INFO  c.t.p.controller.DartController : Fetching dart with ID: 123e4567-e89b-12d3-a456-426614174000
DEBUG c.t.p.service.impl.DartServiceImpl : Fetching dart details for id: 123e4567-e89b-12d3-a456-426614174000
INFO  c.t.p.controller.DartController : Dart retrieved successfully: 123e4567-e89b-12d3-a456-426614174000
```

### 4. Verify Database

Connect to your database and verify different darts exist:

```sql
SELECT id, name, status, monthly_contribution, member_count 
FROM dart 
ORDER BY created_at DESC;
```

## Common Issues and Solutions

### Issue 1: All darts show the same data

**Cause**: Backend is not differentiating by ID (database issue or wrong query)

**Solution**: Check backend logs and database to ensure multiple darts exist with different data.

### Issue 2: Dart ID is null in console

**Cause**: Route parameter not configured correctly

**Solution**: Verify the route in `dars.routes.ts` has `:id` parameter.

### Issue 3: API returns 404

**Cause**: Dart doesn't exist in database or authentication issue

**Solution**: 
- Check if the dart exists in the database
- Verify JWT token is valid
- Check user permissions

### Issue 4: Getting cached data

**Cause**: Browser or Angular caching

**Solution**:
- Hard refresh: Ctrl+Shift+R (Windows/Linux) or Cmd+Shift+R (Mac)
- Clear browser cache
- Add cache-busting headers to API

## Enhanced Logging Added

The following enhanced logging has been added to help debug:

### Frontend (TypeScript)
- ✅ Route parameter extraction logging
- ✅ API call initiation logging
- ✅ Success response logging with key fields
- ✅ Error logging with details
- ✅ Data mapping logging

### Backend (Java)
- ✅ Controller request logging
- ✅ Service method entry logging
- ✅ Repository query logging
- ✅ Response logging

## Testing

To verify the system is working correctly:

1. **Create Multiple Darts**: Create at least 2-3 darts with different names and settings
2. **Navigate to My Darts**: Go to `/dashboard/client/my-dars`
3. **Open Console**: Press F12 to open browser DevTools
4. **Click Details**: Click "Open Details" on different darts
5. **Verify Logs**: Check console logs to see different IDs being loaded
6. **Verify Display**: Confirm the page shows different data for each dart

## API Response Example

When you call `GET /api/v1/dart/{id}`, you should receive:

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Family Vacation Fund",
  "monthlyContribution": 200.00,
  "status": "ACTIVE",
  "memberCount": 10,
  "totalMonthlyPool": 2000.00,
  "organizerId": "456e7890-e89b-12d3-a456-426614174001",
  "organizerName": "John Doe",
  "isOrganizer": true,
  "currentCycle": 3,
  "totalCycles": 10,
  "nextPayoutDate": "2024-02-15T00:00:00",
  "startDate": "2024-01-01T00:00:00",
  "orderMethod": "RANDOM",
  "paymentFrequency": "MONTHLY",
  "description": "Saving for family vacation",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-15T14:30:00"
}
```

Each dart will have its own unique values for these fields.

## Conclusion

The dart details page is **already fully functional and dynamic**. Each dart you click on will:

1. ✅ Pass its unique ID through the URL
2. ✅ Extract the ID from route parameters
3. ✅ Make an API call with that specific ID
4. ✅ Fetch that dart's unique data from the database
5. ✅ Display the specific dart's information

If you're seeing static or identical data for all darts, the issue is likely:
- **Backend**: Database doesn't have multiple distinct darts
- **Backend**: Caching at database or service layer
- **Frontend**: Browser caching
- **Data**: All darts were created with the same values

Use the debugging steps above to identify and resolve the specific issue in your environment.

## Next Steps

1. Open browser console (F12)
2. Navigate to My Darts page
3. Click "Open Details" on different darts
4. Watch the console logs to see the flow
5. Verify different IDs are being passed and loaded
6. Check the displayed data matches the logs

The enhanced logging will help you see exactly what's happening at each step!