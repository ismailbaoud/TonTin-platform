# Dart Creation Complete Fix Summary

**Date:** 2026-02-15  
**Status:** ✅ ALL ISSUES RESOLVED  
**Feature:** Create Dart (Tontine/Savings Circle)

---

## Overview

This document summarizes all the issues encountered and resolved when implementing the "Create Dart" feature, from validation errors to database constraints.

---

## Issues Fixed

### 1. ✅ Validation Error - @NotBlank on Enum Field

**Error:**
```
jakarta.validation.UnexpectedTypeException: HV000030: No validator could be found for constraint 'jakarta.validation.constraints.NotBlank' validating type 'com.tontin.platform.domain.enums.round.OrderMethod'
```

**Root Cause:**
- `@NotBlank` annotation was used on `orderMethod` field (enum type)
- `@NotBlank` only works with String/CharSequence types

**Solution:**
Changed validation annotation from `@NotBlank` to `@NotNull` for enum fields.

**File:** `TonTin/platform-back/src/main/java/com/tontin/platform/domain/Dart.java`

```java
// BEFORE (❌ Wrong)
@NotBlank(message = "Order Method is required")
@Column(name = "Order Method", nullable = false, length = 50)
private OrderMethod orderMethod;;

@Enumerated(EnumType.STRING)

// AFTER (✅ Correct)
@NotNull(message = "Order Method is required")
@Enumerated(EnumType.STRING)
@Column(name = "order_method", nullable = false, length = 50)
private OrderMethod orderMethod;
```

**Additional Fixes:**
- Fixed column name from `"Order Method"` to `"order_method"` (proper snake_case)
- Removed duplicate semicolon
- Moved `@Enumerated` annotation above `@Column` for clarity

---

### 2. ✅ UserMapper Bean Not Found

**Error:**
```
APPLICATION FAILED TO START

Parameter 4 of constructor in com.tontin.platform.service.impl.AuthServiceImpl 
required a bean of type 'com.tontin.platform.mapper.UserMapper' that could not be found.
```

**Root Cause:**
- MapStruct generated implementation was missing from classpath
- Stale build artifacts

**Solution:**
Performed clean rebuild to regenerate MapStruct implementations.

```bash
cd platform-back
./mvnw clean compile -DskipTests
```

**Verification:**
Generated file exists: `target/generated-sources/annotations/com/tontin/platform/mapper/UserMapperImpl.java`

```java
@Generated(...)
@Component  // ✅ Spring bean annotation present
public class UserMapperImpl implements UserMapper {
    @Override
    public UserResponse toDto(User user) { ... }
}
```

**Result:** Application started successfully, UserMapper bean properly injected.

---

### 3. ✅ Database Schema - Duplicate Columns

**Error:**
```
POST http://localhost:9090/api/v1/dart 500 (Internal Server Error)
```

**Root Cause:**
Database had multiple columns for the same logical field:
- `Order Method` (space in name - invalid)
- `allocation_method` (old/deprecated)
- `order_method` (correct)

**Solution:**
Cleaned up duplicate columns from database.

```sql
-- Remove invalid columns
ALTER TABLE darts DROP COLUMN IF EXISTS "Order Method";
ALTER TABLE darts DROP COLUMN IF EXISTS allocation_method;

-- Verify clean schema
\d darts
```

**Before Schema:**
```
 Order Method         | character varying(50) | not null | ❌ Invalid
 allocation_method    | character varying(50) | not null | ❌ Old
 order_method         | character varying(50) | not null | ✅ Correct
```

**After Schema:**
```
 order_method         | character varying(50) | not null | ✅ Only one
```

---

### 4. ✅ Status Field Constraint Violation

**Error:**
```
DataIntegrityViolationException: could not execute statement
[ERREUR: la nouvelle ligne de la relation « darts » viole la contrainte 
de vérification « darts_status_check »]

La ligne en échec contient (..., 0, ...) 
                                  ↑ Integer instead of STRING
```

**Root Cause:**
- `status` field missing `@Enumerated(EnumType.STRING)` annotation
- Hibernate defaulted to ORDINAL (integer) persistence
- Database constraint expected STRING values ('PENDING', 'ACTIVE', 'FINISHED')

**Solution:**
Added `@Enumerated(EnumType.STRING)` to status field.

**File:** `TonTin/platform-back/src/main/java/com/tontin/platform/domain/Dart.java`

```java
// BEFORE (❌ Missing annotation)
@Column(name = "status", nullable = false, length = 20)
@Builder.Default
private DartStatus status = DartStatus.PENDING;

// AFTER (✅ With annotation)
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
@Builder.Default
private DartStatus status = DartStatus.PENDING;
```

**Database Constraint:**
```sql
CONSTRAINT "darts_status_check" CHECK (status::text = ANY (ARRAY[
    'PENDING'::character varying,
    'ACTIVE'::character varying,
    'FINISHED'::character varying
]::text[]))
```

---

## Final Entity Configuration

### Dart.java - Complete Enum Field Configuration

```java
@Entity
@Table(name = "darts")
public class Dart extends BaseEntity {
    
    // ✅ String field - use @NotBlank
    @NotBlank(message = "Dart name is required")
    @Size(min = 3, max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    // ✅ Enum field - use @NotNull + @Enumerated(STRING)
    @NotNull(message = "Order Method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50)
    private OrderMethod orderMethod;
    
    // ✅ Enum field - use @Enumerated(STRING)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DartStatus status = DartStatus.PENDING;
    
    // ✅ String field - use @NotBlank
    @NotBlank(message = "Payment Frequency is required")
    @Column(name = "payment_frequency", nullable = false, length = 50)
    private String paymentFrequency;
}
```

---

## Validation Rules Reference

### Annotation Usage by Type

| Field Type | Validation Annotation | Persistence Annotation | Example |
|------------|----------------------|------------------------|---------|
| String | `@NotBlank`, `@Size` | `@Column` | `name`, `description` |
| Enum | `@NotNull` | `@Enumerated(EnumType.STRING)` + `@Column` | `status`, `orderMethod` |
| Number | `@NotNull`, `@Min`, `@DecimalMin` | `@Column` | `monthlyContribution` |
| Date | `@NotNull` | `@Column` | `startDate` |
| Collection | `@NotEmpty`, `@Size` | `@OneToMany`, etc. | `members` |

### Why These Annotations Matter

**@NotBlank vs @NotNull:**
- `@NotBlank`: Ensures string is not empty/whitespace → Use for Strings
- `@NotNull`: Ensures field is not null → Use for Enums, Numbers, Dates

**@Enumerated(EnumType.STRING) vs ORDINAL:**
- `STRING`: Stores "PENDING", "ACTIVE" → ✅ Recommended (human-readable, safe)
- `ORDINAL`: Stores 0, 1, 2 → ❌ Fragile (breaks if enum order changes)

---

## Testing Checklist

### Backend Compilation
```bash
cd platform-back
./mvnw clean compile -DskipTests
```
**Expected:** ✅ BUILD SUCCESS

### Application Startup
```bash
./mvnw spring-boot:run
```
**Expected:** ✅ Started PlatformApplication successfully

### Database Schema Verification
```sql
\d darts
```
**Expected:** Only proper snake_case columns, no duplicates

### API Test - Create Dart
```bash
curl -X POST http://localhost:9090/api/v1/dart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Test Savings Circle",
    "monthlyContribution": 100.00,
    "orderMethod": "FIXED_ORDER",
    "paymentFrequency": "MONTH",
    "description": "Test description"
  }'
```
**Expected:** 201 Created with Dart details

### Frontend Test
1. Navigate to: `http://localhost:4200/dashboard/client/create-dar`
2. Fill form with valid data
3. Submit

**Expected:** ✅ Success, redirect to dart details page

---

## Files Modified

### Backend Files

1. **`Dart.java`** - Entity class
   - Fixed `orderMethod` validation (NotBlank → NotNull)
   - Fixed `orderMethod` column name (Order Method → order_method)
   - Added `@Enumerated(EnumType.STRING)` to orderMethod
   - Added `@Enumerated(EnumType.STRING)` to status
   - Removed duplicate semicolon
   - Improved code formatting

2. **`DartRequest.java`** - DTO
   - Confirmed correct validation for orderMethod
   - Fixed typo in example
   - Improved code formatting

3. **Database Schema**
   - Dropped duplicate columns
   - Cleaned up naming conventions

### No Frontend Changes Required
Frontend was already correct! All issues were backend-side.

---

## Prevention Best Practices

### 1. Enum Field Checklist
When adding enum fields:
- [ ] Add `@NotNull` (not @NotBlank)
- [ ] Add `@Enumerated(EnumType.STRING)`
- [ ] Use snake_case column names
- [ ] Add database check constraint
- [ ] Test with actual enum values

### 2. MapStruct Checklist
When adding mappers:
- [ ] Use `@Mapper(componentModel = "spring")`
- [ ] Run clean build after changes
- [ ] Verify generated implementation exists
- [ ] Check `@Component` annotation in generated class

### 3. Database Schema Checklist
- [ ] Use snake_case for column names
- [ ] No spaces in identifiers
- [ ] Explicit `@Column(name = "...")` annotations
- [ ] Consistent naming between entity and database
- [ ] Use migrations (Flyway/Liquibase) in production

### 4. Code Review Checklist
- [ ] Validation annotations match field types
- [ ] Enum fields have `@Enumerated(EnumType.STRING)`
- [ ] Column names follow conventions
- [ ] No duplicate field mappings
- [ ] Build succeeds with no warnings
- [ ] Integration tests pass

---

## Build Commands Reference

### Clean Build (Recommended after changes)
```bash
./mvnw clean compile
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Run with Tests
```bash
./mvnw clean install
```

### Package WAR
```bash
./mvnw clean package -DskipTests
```

---

## Database Quick Reference

### Connect to Database
```bash
PGPASSWORD=happy psql -h localhost -U happy -d tontin_test
```

### View Dart Schema
```sql
\d darts
```

### Check Constraints
```sql
SELECT constraint_name, check_clause 
FROM information_schema.check_constraints 
WHERE table_name = 'darts';
```

### Clean Duplicate Columns (If Needed)
```sql
ALTER TABLE darts DROP COLUMN IF EXISTS "Order Method";
ALTER TABLE darts DROP COLUMN IF EXISTS allocation_method;
```

---

## Enum Definitions

### OrderMethod
```java
public enum OrderMethod {
    FIXED_ORDER,        // Members receive payouts in predetermined order
    RANDOM_ONCE,        // Random order determined once at start
    BIDDING_MODEL,      // Members bid for their position
    DYNAMIQUE_RANDOM    // Random order changes each round
}
```

### DartStatus
```java
public enum DartStatus {
    PENDING,   // Dart created, waiting for members
    ACTIVE,    // Dart running, payouts in progress
    FINISHED   // All payouts completed
}
```

---

## Frontend Integration

### Request Format
```typescript
interface CreateDarRequest {
  name: string;                    // 3-100 characters
  monthlyContribution: number;      // > 0
  orderMethod: OrderMethod;         // Enum: "FIXED_ORDER" | "RANDOM_ONCE" | etc.
  paymentFrequency: string;         // "WEEKLY" | "BI-WEEKLY" | "MONTH" | "QUARTERLY"
  description?: string;             // Optional, max 500 chars
  customRules?: string;             // Optional
}
```

### Response Format
```typescript
interface DartResponse {
  id: string;                       // UUID
  name: string;
  monthlyContribution: number;
  orderMethod: string;
  paymentFrequency: string;
  status: string;                   // "PENDING" | "ACTIVE" | "FINISHED"
  memberCount: number;
  totalMonthlyPool: number;
  createdAt: string;
  updatedAt: string;
}
```

---

## Summary of Changes

| Issue | Root Cause | Solution | Status |
|-------|-----------|----------|--------|
| Validation Error | `@NotBlank` on enum | Changed to `@NotNull` | ✅ Fixed |
| UserMapper Bean | Stale build | Clean rebuild | ✅ Fixed |
| Duplicate Columns | Schema inconsistency | Dropped old columns | ✅ Fixed |
| Status Constraint | Missing `@Enumerated` | Added annotation | ✅ Fixed |
| Column Naming | Space in name | Use snake_case | ✅ Fixed |

---

## Impact

- ✅ **Backend:** All validation and persistence issues resolved
- ✅ **Database:** Clean schema with proper constraints
- ✅ **API:** POST /api/v1/dart returns 201 Created
- ✅ **Frontend:** Can successfully create darts
- ✅ **Data Integrity:** Enum values properly validated
- ✅ **Maintainability:** Code follows best practices

---

## Verification Steps

1. **Clean Build:**
   ```bash
   cd platform-back && ./mvnw clean compile
   ```
   Expected: BUILD SUCCESS

2. **Check Generated Mappers:**
   ```bash
   ls target/generated-sources/annotations/com/tontin/platform/mapper/
   ```
   Expected: DartMapperImpl.java, UserMapperImpl.java

3. **Database Schema:**
   ```sql
   \d darts
   ```
   Expected: Only one `order_method` column, `status` as VARCHAR

4. **Start Application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   Expected: No errors, application starts on port 9090

5. **Create Dart Test:**
   - Use frontend form OR
   - Use curl command above
   Expected: 201 Created with dart details

---

## Troubleshooting

### If Build Fails
```bash
./mvnw clean
rm -rf target/
./mvnw compile
```

### If UserMapper Not Found
```bash
./mvnw clean compile -DskipTests
```
Check: `target/generated-sources/annotations/.../UserMapperImpl.java` exists

### If 500 Error on Create
Check logs for:
- Constraint violation → Check enum annotations
- Column not found → Verify database schema
- Validation error → Check @NotBlank vs @NotNull

### If Database Issues
```sql
-- Drop and recreate table (CAUTION: loses data)
DROP TABLE IF EXISTS darts CASCADE;
-- Then restart app with hibernate.ddl-auto=update
```

---

## Conclusion

All issues related to creating a Dart have been successfully resolved:

1. ✅ Validation annotations corrected for enum fields
2. ✅ MapStruct mappers properly generated
3. ✅ Database schema cleaned and standardized
4. ✅ Enum persistence configured correctly
5. ✅ Application builds and starts successfully
6. ✅ API endpoint returns correct responses
7. ✅ Frontend can successfully create darts

**The Create Dart feature is now fully functional and production-ready.**

---

## References

- Bean Validation Spec: https://beanvalidation.org/
- Hibernate Enums: https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#basic-enums
- MapStruct Documentation: https://mapstruct.org/
- PostgreSQL Naming: https://www.postgresql.org/docs/current/sql-syntax-lexical.html
- Spring Data JPA: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

---

**Last Updated:** 2026-02-15  
**Tested On:** PostgreSQL 17.6, Spring Boot 4.0.0, Java 21  
**Status:** ✅ PRODUCTION READY