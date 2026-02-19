# ✅ Start Dart Feature - Complete Implementation

## Date: February 15, 2024

## Overview

Added the ability for organizers to manually start a dart by clicking a "Start Dâr" button. The start date is now `null` when creating a dart and is only set when the organizer clicks the Start button.

## Backend Changes ✅

### 1. DartServiceImpl.java
**Changed dart creation to set startDate to null:**
```java
// BEFORE:
dart.setStartDate(LocalDateTime.now());

// AFTER:
dart.setStartDate(null); // Will be set when organizer starts the dart
```

### 2. DartService.java (Interface)
**Added new method:**
```java
/**
 * Start a dart (organizer only)
 * Sets the start date and changes status to ACTIVE if minimum members reached
 */
DartResponse startDart(UUID id);
```

### 3. DartServiceImpl.java
**Implemented startDart method with validation:**
- ✅ Verifies user is organizer
- ✅ Checks dart is not already started
- ✅ Validates minimum members (at least 2)
- ✅ Sets startDate to current time
- ✅ Changes status from PENDING to ACTIVE
- ✅ Returns updated dart

```java
@Override
@Transactional
public DartResponse startDart(UUID id) {
    // Verify organizer
    // Check not already started
    // Validate minimum members
    dart.setStartDate(LocalDateTime.now());
    dart.setStatus(DartStatus.ACTIVE);
    return save and return
}
```

### 4. DartController.java
**Added new endpoint:**
```
POST /api/v1/dart/{id}/start
Authorization: Bearer token
```

**Response Codes:**
- `200` - Dart started successfully
- `400` - Minimum members not met
- `403` - Not organizer
- `404` - Dart not found
- `409` - Dart already started

## Frontend Changes ✅

### 1. DarService
**Method already exists:**
```typescript
startDar(darId: string): Observable<Dar> {
  return this.http.post<Dar>(`${this.apiUrl}/${darId}/start`, {});
}
```

### 2. dar-details.component.ts
**Added:**
- `isOrganizer` property to track if current user is organizer
- `startDart()` method to call API and start the dart

```typescript
isOrganizer = false;

startDart(): void {
  if (!confirm("Are you sure you want to start this Dâr?")) return;
  
  this.darService.startDar(this.darId!)
    .subscribe({
      next: (response) => {
        console.log("✅ Dart started successfully");
        this.loadDarDetails(); // Reload to show updated status
        alert("Dâr started successfully!");
      },
      error: (err) => {
        console.error("❌ Error starting dart");
        alert(err.error?.message || "Failed to start Dâr");
      }
    });
}
```

### 3. dar-details.component.html
**Added Start button (only visible to organizers when dart is pending):**
```html
<button
  *ngIf="darDetails.status === 'pending' && isOrganizer"
  (click)="startDart()"
  class="...bg-green-600..."
>
  <span class="material-symbols-outlined">play_arrow</span>
  Start Dâr
</button>
```

## How It Works

### Creating a Dart:
1. User creates a new dart
2. Backend sets `startDate = null`
3. Backend sets `status = PENDING`
4. Frontend shows "Not started" for start date
5. **"Start Dâr" button is visible** to organizer

### Starting a Dart:
1. Organizer clicks "Start Dâr" button
2. Confirmation dialog appears
3. If confirmed, API call: `POST /dart/{id}/start`
4. Backend validates:
   - User is organizer ✓
   - Dart not already started ✓
   - Minimum 2 members ✓
5. Backend sets:
   - `startDate = now()`
   - `status = ACTIVE`
6. Frontend reloads dart details
7. Status changes from PENDING → ACTIVE
8. Start date shows actual date
9. **"Start Dâr" button disappears**

## UI Changes

### Before Starting:
```
Status: [PENDING]
Start Date: Not started
[Start Dâr] [Invite Member] [Share Link]  <- Start button visible
```

### After Starting:
```
Status: [ACTIVE]
Start Date: Feb 15, 2024
[Invite Member] [Share Link]  <- Start button gone
```

## Validation Rules

### Minimum Requirements to Start:
- ✅ User must be organizer
- ✅ Dart must be in PENDING status
- ✅ Dart must have at least 2 members
- ✅ Dart must not already be started

### Error Messages:
- "Only organizers can start a dart" (403)
- "Dart has already been started" (409)
- "Cannot start dart: minimum 2 members required" (400)

## Testing Steps

### 1. Create a Dart
```bash
POST /api/v1/dart
{
  "name": "Test Dart",
  "monthlyContribution": 100,
  ...
}
```
**Expected:** 
- `startDate` is `null`
- `status` is `PENDING`

### 2. View Dart Details
Navigate to dart details page

**Expected:**
- Shows "Not started" for start date
- Shows [PENDING] status badge
- Shows "Start Dâr" button (green, with play icon)

### 3. Try to Start Without Minimum Members
Click "Start Dâr" button

**Expected:**
- Error: "Cannot start dart: minimum 2 members required"

### 4. Add Another Member
Invite and add at least one more member

### 5. Start the Dart
Click "Start Dâr" button → Confirm

**Expected:**
- Success message
- Page reloads
- Status changes to [ACTIVE]
- Start date shows current date
- "Start Dâr" button disappears

### 6. Try to Start Again
API call: `POST /dart/{id}/start`

**Expected:**
- Error 409: "Dart has already been started"

## Files Modified

### Backend:
1. `DartServiceImpl.java` - Set startDate to null, added startDart method
2. `DartService.java` - Added startDart method signature
3. `DartController.java` - Added POST /start endpoint

### Frontend:
1. `dar-details.component.ts` - Added isOrganizer flag and startDart method
2. `dar-details.component.html` - Added Start button
3. `dar.service.ts` - Method already existed

## Build Status

### Backend:
```
✅ mvn compile - Success
✅ No compilation errors
```

### Frontend:
```
✅ npm run build - Success
✅ Application bundle generation complete
✅ No errors
```

## Summary

✅ **Feature Complete!**

- Darts are created with `startDate = null`
- Status starts as `PENDING`
- Only organizers can start darts
- Start button only shows when dart is pending
- Minimum 2 members required to start
- Starting sets current date and changes status to ACTIVE
- Full validation and error handling
- Clear UI feedback

**The dart lifecycle is now:**
1. CREATE → status: PENDING, startDate: null
2. **START** → status: ACTIVE, startDate: now()
3. COMPLETE → status: FINISHED

Ready to test! 🚀
