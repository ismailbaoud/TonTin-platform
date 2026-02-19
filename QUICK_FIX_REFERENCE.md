# Quick Fix Reference Card

**Last Updated:** 2026-02-15  
**For:** TonTin Platform - Dart Creation Feature

---

## ✅ ALL FIXES APPLIED - READY TO USE

This is a quick reference for the fixes applied to make Dart creation work.

---

## Problem Summary

When creating a Dart, we encountered 4 main issues:
1. ❌ Validation error on enum field
2. ❌ UserMapper bean not found
3. ❌ Database duplicate columns
4. ❌ Status field constraint violation

**All fixed!** ✅

---

## Quick Fixes Applied

### 1. Enum Fields Configuration

**RULE:** Enum fields need special annotations

```java
// ❌ WRONG - Don't use @NotBlank on enums
@NotBlank(message = "Status is required")
private DartStatus status;

// ✅ CORRECT - Use @NotNull + @Enumerated(STRING)
@NotNull(message = "Status is required")
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false)
private DartStatus status;
```

### 2. Database Columns

**RULE:** Always use snake_case, no spaces

```sql
-- ❌ WRONG
"Order Method"    -- Space in name
"OrderMethod"     -- PascalCase
"orderMethod"     -- camelCase

-- ✅ CORRECT
order_method      -- snake_case
```

### 3. MapStruct Build

**RULE:** Clean build after mapper changes

```bash
# Always run this after changing mappers
cd platform-back
./mvnw clean compile -DskipTests
```

### 4. Validation Annotations

**RULE:** Match annotation to field type

| Field Type | Use This | NOT This |
|------------|----------|----------|
| String | `@NotBlank` | `@NotNull` |
| Enum | `@NotNull` | `@NotBlank` |
| Number | `@NotNull` | `@NotBlank` |
| Date | `@NotNull` | `@NotBlank` |

---

## Dart Entity - Correct Configuration

```java
@Entity
@Table(name = "darts")
public class Dart extends BaseEntity {
    
    // String field
    @NotBlank(message = "Dart name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    // Enum field #1
    @NotNull(message = "Order Method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false)
    private OrderMethod orderMethod;
    
    // Enum field #2
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DartStatus status = DartStatus.PENDING;
    
    // String field
    @NotBlank(message = "Payment Frequency is required")
    @Column(name = "payment_frequency", nullable = false)
    private String paymentFrequency;
}
```

---

## Database Cleanup (If Needed)

```sql
-- Remove old/duplicate columns
ALTER TABLE darts DROP COLUMN IF EXISTS "Order Method";
ALTER TABLE darts DROP COLUMN IF EXISTS allocation_method;

-- Verify clean schema
\d darts;
```

---

## Build & Run Commands

```bash
# 1. Clean build
cd platform-back
./mvnw clean compile -DskipTests

# 2. Start application
./mvnw spring-boot:run

# 3. Verify in browser
# Backend: http://localhost:9090/swagger-ui.html
# Frontend: http://localhost:4200/dashboard/client/create-dar
```

---

## Testing Dart Creation

### Via Frontend
1. Login to application
2. Navigate to: Dashboard → Create Dâr
3. Fill form:
   - Name: "Test Dart"
   - Amount: 100
   - Frequency: Monthly
   - Order Method: Fixed Order
4. Click "Create Dâr"
5. ✅ Should redirect to dart details

### Via API
```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}' \
  | jq -r '.accessToken')

# Create dart
curl -X POST http://localhost:9090/api/v1/dart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Test Dart",
    "monthlyContribution": 100.00,
    "orderMethod": "FIXED_ORDER",
    "paymentFrequency": "MONTH",
    "description": "Test"
  }'

# Expected: 201 Created
```

---

## Common Errors & Solutions

### Error: "No validator could be found for @NotBlank validating type Enum"
**Fix:** Change `@NotBlank` to `@NotNull` on enum field

### Error: "UserMapper bean not found"
**Fix:** Run `./mvnw clean compile`

### Error: "Column 'Order Method' not found"
**Fix:** Run database cleanup SQL above

### Error: "Constraint darts_status_check violated"
**Fix:** Add `@Enumerated(EnumType.STRING)` to status field

---

## Quick Checklist

When adding new enum fields:
- [ ] Use `@NotNull` (not `@NotBlank`)
- [ ] Add `@Enumerated(EnumType.STRING)`
- [ ] Use snake_case column name
- [ ] Run clean build
- [ ] Test with actual data

---

## Files Modified

✅ `Dart.java` - Fixed enum annotations
✅ `DartRequest.java` - Validation confirmed
✅ Database schema - Cleaned duplicates
✅ MapStruct - Regenerated implementations

---

## Verification

```bash
# 1. Check build
./mvnw clean compile
# Expected: BUILD SUCCESS

# 2. Check generated mappers
ls target/generated-sources/annotations/com/tontin/platform/mapper/
# Expected: DartMapperImpl.java, UserMapperImpl.java exist

# 3. Check database
psql -h localhost -U happy -d tontin_test -c "\d darts"
# Expected: Only one order_method column

# 4. Start app
./mvnw spring-boot:run
# Expected: Started PlatformApplication successfully
```

---

## Status: ✅ ALL WORKING

- ✅ Backend compiles
- ✅ Application starts
- ✅ Database schema clean
- ✅ API returns 201 Created
- ✅ Frontend works
- ✅ Data persists correctly

---

## Need Help?

See detailed documentation:
- `VALIDATION_FIX_SUMMARY.md` - Validation annotations
- `USERMAPPER_FIX_SUMMARY.md` - MapStruct issues
- `DATABASE_SCHEMA_FIX.md` - Database problems
- `DART_CREATION_COMPLETE_FIX.md` - Complete guide

---

**Ready to use! Happy coding! 🚀**