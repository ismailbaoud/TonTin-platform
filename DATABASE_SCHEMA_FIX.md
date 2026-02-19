# Database Schema Fix Summary

**Date:** 2026-02-15  
**Issue:** 500 Internal Server Error when creating Dart  
**Root Cause:** Duplicate and inconsistent column names in `darts` table  
**Status:** ✅ RESOLVED

---

## Problem Description

When attempting to create a new Dart via the frontend, the following error occurred:

```
POST http://localhost:9090/api/v1/dart 500 (Internal Server Error)
```

### Root Cause Analysis

The database table `darts` had **duplicate columns** for the same logical field:

1. ✅ `order_method` - Correct column (snake_case)
2. ❌ `Order Method` - Incorrect column (space in name, from old code)
3. ❌ `allocation_method` - Old/deprecated column

This occurred because:
- Previous code used `@Column(name = "Order Method")` with a space
- Entity was updated to use `@Column(name = "order_method")` (proper naming)
- Hibernate's `ddl-auto=update` added the new column instead of renaming
- Old columns were never dropped

---

## Database Schema Before Fix

```sql
Table "public.darts"
       Column        |              Type              | Nullable |
----------------------+--------------------------------+----------+
 id                   | uuid                           | not null |
 created_at           | timestamp(6) without time zone | not null |
 updated_at           | timestamp(6) without time zone |          |
 version              | bigint                         |          |
 allocation_method    | character varying(50)          | not null | ❌ OLD
 monthly_contribution | numeric(19,2)                  | not null |
 name                 | character varying(100)         | not null |
 start_date           | timestamp(6) without time zone | not null |
 status               | character varying(20)          | not null |
 custom_rules         | character varying(255)         |          |
 Order Method         | character varying(50)          | not null | ❌ INVALID NAME
 description          | character varying(500)         |          |
 payment_frequency    | character varying(50)          | not null |
 order_method         | character varying(50)          | not null | ✅ CORRECT
```

**Problems:**
- 3 columns trying to store the same data
- `Order Method` with space violates SQL naming conventions
- Confusion for ORM mapping
- Potential data inconsistency

---

## Solution Applied

### Step 1: Drop Invalid Columns

```sql
-- Drop the column with space in name
ALTER TABLE darts DROP COLUMN IF EXISTS "Order Method";

-- Drop the old deprecated column
ALTER TABLE darts DROP COLUMN IF EXISTS allocation_method;
```

### Step 2: Verify Schema

```sql
\d darts
```

---

## Database Schema After Fix

```sql
Table "public.darts"
       Column        |              Type              | Nullable |
----------------------+--------------------------------+----------+
 id                   | uuid                           | not null |
 created_at           | timestamp(6) without time zone | not null |
 updated_at           | timestamp(6) without time zone |          |
 version              | bigint                         |          |
 monthly_contribution | numeric(19,2)                  | not null |
 name                 | character varying(100)         | not null |
 start_date           | timestamp(6) without time zone | not null |
 status               | character varying(20)          | not null |
 custom_rules         | character varying(255)         |          |
 description          | character varying(500)         |          |
 payment_frequency    | character varying(50)          | not null |
 order_method         | character varying(50)          | not null | ✅ ONLY ONE

Indexes:
    "darts_pkey" PRIMARY KEY, btree (id)

Check constraints:
    "darts_order_method_check" CHECK (order_method::text = ANY (ARRAY[
        'FIXED_ORDER'::character varying,
        'RANDOM_ONCE'::character varying,
        'BIDDING_MODEL'::character varying,
        'DYNAMIQUE_RANDOM'::character varying
    ]::text[]))
    
    "darts_status_check" CHECK (status::text = ANY (ARRAY[
        'PENDING'::character varying,
        'ACTIVE'::character varying,
        'FINISHED'::character varying
    ]::text[]))

Referenced by:
    TABLE "rounds" CONSTRAINT "fkegerx99dignwpaddc6wn1nxwn" 
        FOREIGN KEY (dart_id) REFERENCES darts(id)
    TABLE "members" CONSTRAINT "fkjlvivc7vdt8fvxo8a1xru8473" 
        FOREIGN KEY (dart_id) REFERENCES darts(id)
```

**Improvements:**
- ✅ Clean schema with proper column names
- ✅ Only one `order_method` column
- ✅ Proper snake_case naming convention
- ✅ Check constraints ensure data integrity
- ✅ Foreign key relationships intact

---

## Code Alignment

### Entity Definition (Correct)

```java
@Entity
@Table(name = "darts")
public class Dart extends BaseEntity {
    
    @NotNull(message = "Order Method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50)
    private OrderMethod orderMethod;
    
    // ... other fields
}
```

**Key Points:**
- ✅ Uses `@Column(name = "order_method")` - proper snake_case
- ✅ `@Enumerated(EnumType.STRING)` for enum persistence
- ✅ `@NotNull` instead of `@NotBlank` (correct for enums)

### Mapper (Generated by MapStruct)

```java
@Component
public class DartMapperImpl implements DartMapper {
    
    @Override
    public Dart toEntity(DartRequest request) {
        Dart.DartBuilder dart = Dart.builder();
        dart.orderMethod(request.orderMethod());
        // ... other mappings
        return dart.build();
    }
}
```

---

## Testing Steps

### 1. Manual Database Connection Test

```bash
PGPASSWORD=happy psql -h localhost -U happy -d tontin_test \
  -c "SELECT column_name, data_type FROM information_schema.columns 
      WHERE table_name = 'darts' AND column_name LIKE '%method%';"
```

**Expected Output:**
```
  column_name  |     data_type
---------------+-------------------
 order_method  | character varying
(1 row)
```

### 2. Insert Test

```sql
INSERT INTO darts (
    id, 
    name, 
    monthly_contribution, 
    start_date, 
    status, 
    payment_frequency, 
    order_method, 
    created_at
) VALUES (
    gen_random_uuid(),
    'Test Dart',
    100.00,
    NOW(),
    'PENDING',
    'MONTH',
    'FIXED_ORDER',
    NOW()
);
```

**Expected:** Success ✅

### 3. API Test

```bash
# Login to get token
TOKEN=$(curl -s -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}' \
  | jq -r '.accessToken')

# Create Dart
curl -X POST http://localhost:9090/api/v1/dart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Test Savings Circle",
    "monthlyContribution": 100.00,
    "orderMethod": "FIXED_ORDER",
    "paymentFrequency": "MONTH",
    "description": "Test description"
  }'
```

**Expected:** 201 Created ✅

### 4. Frontend Test

1. Navigate to: `http://localhost:4200/dashboard/client/create-dar`
2. Fill in form:
   - Name: "Test Dart"
   - Monthly Amount: 100
   - Frequency: Monthly
   - Order Method: Fixed Order
3. Click "Create Dâr"

**Expected:** Success, redirect to dart details ✅

---

## Prevention Measures

### 1. Database Migration Strategy

Instead of using `hibernate.ddl-auto=update`, use proper migration tools:

**Flyway Migration Example:**

```sql
-- V1__create_darts_table.sql
CREATE TABLE darts (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    monthly_contribution NUMERIC(19,2) NOT NULL,
    order_method VARCHAR(50) NOT NULL,
    -- ... other fields
    CONSTRAINT darts_order_method_check 
        CHECK (order_method IN ('FIXED_ORDER', 'RANDOM_ONCE', 'BIDDING_MODEL', 'DYNAMIQUE_RANDOM'))
);
```

**Recommendation:** Add Flyway to `pom.xml`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### 2. Column Naming Convention

**Always use snake_case for database columns:**

| ❌ Incorrect | ✅ Correct |
|-------------|-----------|
| `Order Method` | `order_method` |
| `OrderMethod` | `order_method` |
| `order-method` | `order_method` |
| `orderMethod` | `order_method` |

### 3. Entity Field Mapping Rules

```java
// Implicit mapping (field name matches column)
@Column(name = "order_method")
private OrderMethod orderMethod;

// Explicit for clarity
@Column(name = "monthly_contribution")
private BigDecimal monthlyContribution;
```

### 4. Code Review Checklist

- [ ] Column names use snake_case
- [ ] No spaces or special characters in column names
- [ ] `@Column(name = "...")` explicitly specified
- [ ] Enum fields use `@Enumerated(EnumType.STRING)`
- [ ] Validation annotations match field types
- [ ] No duplicate column mappings

---

## SQL Cleanup Script

If you need to clean up the database for other environments:

```sql
-- Backup table first (optional but recommended)
CREATE TABLE darts_backup AS SELECT * FROM darts;

-- Drop invalid columns
ALTER TABLE darts DROP COLUMN IF EXISTS "Order Method";
ALTER TABLE darts DROP COLUMN IF EXISTS allocation_method;

-- Verify schema
\d darts

-- If everything looks good, you can drop the backup
-- DROP TABLE darts_backup;
```

---

## Impact Summary

| Area | Before | After | Status |
|------|--------|-------|--------|
| **Database Columns** | 3 columns for order method | 1 clean column | ✅ Fixed |
| **Column Naming** | Mixed (space, snake_case) | Consistent snake_case | ✅ Fixed |
| **API Endpoint** | 500 Error | 201 Created | ✅ Fixed |
| **Frontend** | Creation fails | Works correctly | ✅ Fixed |
| **Data Integrity** | Potential inconsistency | Check constraints enforced | ✅ Fixed |

---

## Related Files Modified

1. ✅ `Dart.java` - Updated `@Column(name = "order_method")`
2. ✅ `DartRequest.java` - Validation annotations corrected
3. ✅ Database Schema - Cleaned up duplicate columns
4. ✅ `DartMapper.java` - Regenerated with correct mappings

---

## Environment-Specific Notes

### Development Environment
- Database: PostgreSQL 17.6
- User: `happy`
- Database: `tontin_test`
- Port: 5432
- Hibernate DDL: `update` (consider changing to `validate`)

### Production Recommendations

1. **Use Flyway/Liquibase** for schema migrations
2. **Set `hibernate.ddl-auto=validate`** (never `update` or `create`)
3. **Test migrations** in staging before production
4. **Backup database** before schema changes
5. **Use database versioning** for rollback capability

---

## Verification Checklist

- [x] Old columns dropped from database
- [x] Only `order_method` column exists
- [x] Column name matches entity field mapping
- [x] Check constraints present and correct
- [x] Foreign key relationships intact
- [x] API endpoint responds with 201
- [x] Frontend can create darts successfully
- [x] No validation errors in backend logs

---

## Troubleshooting Guide

### Issue: "Column 'Order Method' does not exist"

**Solution:**
```sql
ALTER TABLE darts DROP COLUMN IF EXISTS "Order Method";
```

### Issue: "Column 'order_method' does not exist"

**Solution:** Restart application with `hibernate.ddl-auto=update` to auto-create column, or manually:
```sql
ALTER TABLE darts ADD COLUMN order_method VARCHAR(50) NOT NULL DEFAULT 'FIXED_ORDER';
```

### Issue: Constraint violation on insert

**Solution:** Ensure enum value is valid:
```sql
-- Check valid values
SELECT constraint_name, check_clause 
FROM information_schema.check_constraints 
WHERE constraint_name LIKE '%order_method%';
```

---

## Conclusion

The 500 error when creating darts was caused by duplicate and improperly named columns in the database schema. By cleaning up the schema and aligning it with the entity definition, the issue is now resolved. The application can successfully create darts with the correct `order_method` enum values.

**Final Status:** ✅ **COMPLETE AND TESTED**

---

## References

- PostgreSQL Naming Conventions: https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
- Hibernate Column Mapping: https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#basic
- Spring Data JPA: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
- Flyway Migrations: https://flywaydb.org/documentation/

---

**Last Updated:** 2026-02-15  
**Database:** tontin_test (PostgreSQL 17.6)  
**Application:** TonTin Platform v0.0.1-SNAPSHOT