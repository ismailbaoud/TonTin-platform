# Validation Annotations Quick Reference

## Common Validation Error: Enum Fields

### ❌ WRONG - This causes UnexpectedTypeException
```java
@NotBlank(message = "Order Method is required")
private OrderMethod orderMethod;  // OrderMethod is an ENUM!
```

### ✅ CORRECT - Use @NotNull for enums
```java
@NotNull(message = "Order Method is required")
@Enumerated(EnumType.STRING)
private OrderMethod orderMethod;
```

---

## Validation Annotation Cheat Sheet

| Field Type | Required Check | Additional Validation | Example |
|------------|---------------|----------------------|---------|
| **Enum** | `@NotNull` | ❌ No other constraints | `@NotNull OrderMethod method` |
| **String** | `@NotBlank` | `@Size`, `@Pattern`, `@Email` | `@NotBlank @Size(min=3) String name` |
| **Number** | `@NotNull` | `@Min`, `@Max`, `@DecimalMin` | `@NotNull @DecimalMin("0.01") BigDecimal amount` |
| **Collection** | `@NotEmpty` | `@Size` | `@NotEmpty @Size(max=10) List<String> items` |
| **Boolean** | `@NotNull` | ❌ No other constraints | `@NotNull Boolean active` |
| **Date/Time** | `@NotNull` | `@Past`, `@Future`, `@PastOrPresent` | `@NotNull @Future LocalDateTime startDate` |

---

## Key Rules

### 1. @NotBlank vs @NotNull vs @NotEmpty

| Annotation | Applicable To | Checks For | Example Use Case |
|------------|--------------|------------|------------------|
| `@NotBlank` | **String**, CharSequence | Not null AND not empty AND not whitespace | Names, emails, descriptions |
| `@NotNull` | **Any type** (Object, Enum, Number, Date) | Not null | Enums, numbers, dates, booleans |
| `@NotEmpty` | String, Collection, Map, Array | Not null AND size > 0 | Lists, sets, arrays |

### 2. When to Use Each

```java
// ✅ String fields - use @NotBlank (includes @NotNull + @NotEmpty + no whitespace)
@NotBlank(message = "Name is required")
private String name;

// ✅ Enum fields - ONLY @NotNull
@NotNull(message = "Status is required")
private DartStatus status;

// ✅ Collections - use @NotEmpty
@NotEmpty(message = "Members list cannot be empty")
private List<Member> members;

// ✅ Numbers - use @NotNull
@NotNull(message = "Amount is required")
@DecimalMin(value = "0.01", message = "Amount must be positive")
private BigDecimal amount;

// ✅ Dates - use @NotNull
@NotNull(message = "Start date is required")
private LocalDateTime startDate;
```

### 3. Common Mistakes to Avoid

#### ❌ MISTAKE 1: Using @NotBlank on Enum
```java
@NotBlank  // ❌ ERROR: Cannot use @NotBlank on enum!
private OrderMethod orderMethod;
```
**Fix:**
```java
@NotNull  // ✅ Correct
private OrderMethod orderMethod;
```

#### ❌ MISTAKE 2: Using @NotNull on String (when you want non-empty check)
```java
@NotNull  // ⚠️ Allows empty strings like ""
private String name;
```
**Fix:**
```java
@NotBlank  // ✅ Rejects null, "", "   "
private String name;
```

#### ❌ MISTAKE 3: Using @NotEmpty on primitive types
```java
@NotEmpty  // ❌ ERROR: Cannot use on int
private int age;
```
**Fix:**
```java
@Not