# Complete Fix Summary - Dart Creation Issues

**Date:** 2026-02-15  
**Project:** TonTin Platform  
**Status:** ✅ ALL ISSUES RESOLVED

---

## Overview

This document summarizes all the fixes applied to resolve issues related to Dart (tontine/savings circle) creation in the TonTin platform. Three major issues were identified and resolved:

1. **Validation Error** - `@NotBlank` on enum field
2. **Bean Creation Error** - UserMapper bean not found
3. **Database Error** - Duplicate columns causing 500 error

---

## Issue #1: Validation Error on Dart Creation

### Problem
```
jakarta.validation.UnexpectedTypeException: HV000030: No validator could be found for constraint 'jakarta.validation.constraints.NotBlank' validating type 'com.tontin.platform.domain.enums.round.OrderMethod'. Check configuration for 'orderMethod'
```

### Root Cause
The `@NotBlank` annotation was incorrectly applied to the `orderMethod` field, which is an **enum type** (`OrderMethod`). The `@NotBlank` constraint only works with `String` and `CharSequence` types.

### Solution
Changed validation annotation from `@NotBlank` to `@NotNull` for enum fields.

**File:** `TonTin/platform-back/src/main/java/com/tontin/platform/domain/Dart.java`

**Before:**
```java
@NotBlank(message = "Order Method is required")
@Column(name = "Order Method", nullable = false, length = 50)
private OrderMethod orderMethod;;

@Enumerated(EnumType.STRING)
```

**After:**
```java
@NotNull(message = "Order Method is required")
@Enumerated(EnumType.STRING)
@Column(name = "order_method", nullable = false, length = 50)
private OrderMethod orderMethod;
```

**Additional Fixes:**
- Fixed column name from `"Order Method"` to `"order_method"` (proper snake_case)
- Removed duplicate semicolon
- Improved code formatting
- Updated `DartRequest.java` with proper formatting and fixed typos

### Validation Rules Reference

| Annotation    | Applicable Types                      | Use Case                          |
|---------------|---------------------------------------|-----------------------------------|
| `@NotNull`    | Any object type (including enums)     | Ensures field is not null         |
| `@NotBlank`   | String, CharSequence                  | Ensures string is not empty/blank |
| `@NotEmpty`   | String, Collection, Map, Array        | Ensures collection is not empty   |
| `@Size`       | String, Collection, Map, Array        | Validates size/length             |
| `@DecimalMin` | BigDecimal, BigInteger, Number types  | Validates minimum value           |

### Status
✅ **RESOLVED** - Build successful, validation works correctly

**Details:** See `VALIDATION_FIX_SUMMARY.md`

---

## Issue #2: UserMapper Bean Not Found

### Problem
```
***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 4 of constructor in com.tontin.platform.service.impl.AuthServiceImpl required a bean of type 'com.tontin.platform.mapper.UserMapper' that could not be found.
```

### Root Cause
The `UserMapper` interface was correctly defined with MapStruct annotations, but the **generated implementation class** (`UserMapperImpl`) was not being properly compiled or was missing from the classpath.

### Solution
Performed a **clean rebuild** of the project to regenerate MapStruct implementations.

```bash
cd TonTin/platform-back
./mvnw clean compile -DskipTests
```

### Verification
**Generated file:** `target/generated-sources/annotations/com/tontin/platform/mapper/UserMapperImpl.java`

```java
@Component
public class UserMapperImpl implements UserMapper {
    @Override