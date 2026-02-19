# Domain Layer Best Practices Implementation

## Overview
This document outlines the improvements made to the domain layer of the TonTin platform to align with JPA/Hibernate best practices and clean code principles.

---

## Changes Summary

### 1. **Base Entity Pattern**
Created `BaseEntity` abstract class to centralize common entity concerns:

- **Audit Fields**: `createdAt`, `updatedAt` with automatic timestamp management via `@CreationTimestamp` and `@UpdateTimestamp`
- **Optimistic Locking**: `@Version` field to prevent concurrent update conflicts
- **Primary Key**: Centralized UUID-based ID generation strategy
- **Base equals/hashCode**: Default implementation based on entity ID

**Benefits**:
- DRY (Don't Repeat Yourself) principle
- Consistent auditing across all entities
- Built-in optimistic locking support
- Reduced boilerplate code

---

### 2. **Entity Refactoring**

#### **User Entity**
- ✅ Extended `BaseEntity`
- ✅ Separated `@Entity` and `@Table` annotations
- ✅ Added complete Lombok annotations (`@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- ✅ Proper validation constraints (`@NotBlank`, `@Email`, `@Size`)
- ✅ Business key equals/hashCode based on `email` (natural key)
- ✅ Enhanced cascade and fetch type configuration
- ✅ Added business methods: `activate()`, `suspend()`, `disable()`, `isVerified()`, `isPending()`
- ✅ Bidirectional relationship helper methods
- ✅ Custom `toString()` without circular references
- ✅ Default values for `role` and `status` using `@Builder.Default`
- ✅ Removed redundant `creationDate` field (using `createdAt` from `BaseEntity`)

#### **Dart Entity**
- ✅ Extended `BaseEntity`
- ✅ Separated `@Entity` and `@Table` annotations
- ✅ Fixed field name: `member` → `members` (proper plural)
- ✅ Added validation constraints with detailed messages
- ✅ Proper `BigDecimal` column definition with precision/scale
- ✅ Business key equals/hashCode based on `name` and `startDate`
- ✅ Enhanced cascade types: `CascadeType.ALL` with `orphanRemoval = true`
- ✅ Added helper methods: `addMember()`, `removeMember()`, `clearMembers()`
- ✅ Business methods: `activate()`, `finish()`, `isActive()`, `getMemberCount()`, `getActiveMembers()`, `calculateTotalMonthlyContributions()`
- ✅ Default values with `@Builder.Default`

#### **Member Entity**
- ✅ Extended `BaseEntity`
- ✅ Fixed critical typo: `permession` → `permission` (throughout the codebase)
- ✅ Separated `@Entity` and `@Table` annotations
- ✅ Added `nullable = false` and `unique = true` constraints
- ✅ Business key equals/hashCode based on `user` and `dart` composite key
- ✅ Proper fetch types (`LAZY`) for performance
- ✅ Added business methods: `activate()`, `leave()`, `promoteToOrganizer()`, `demoteToMember()`, `getDaysSinceJoining()`
- ✅ Status check methods: `isActive()`, `isPending()`, `hasLeft()`, `isOrganizer()`, `isMember()`

#### **Loggin Entity**
- ✅ Added missing Lombok annotations (`@NoArgsConstructor`, `@AllArgsConstructor`)
- ✅ Separated `@Entity` and `@Table` annotations
- ✅ Added database indexes for common query patterns:
  - `idx_logs_timestamp` - for time-based queries
  - `idx_logs_user_email` - for user activity tracking
  - `idx_logs_event` - for event filtering
  - `idx_logs_status` - for status filtering
  - `idx_logs_request_id` - for request tracing
- ✅ Added `errorMessage` and `stackTrace` fields for better error tracking
- ✅ Business key equals/hashCode based on `requestId`
- ✅ Added business methods: `isSuccess()`, `isFailure()`, `hasError()`
- ✅ Comprehensive validation constraints

---

### 3. **Enum Improvements**

#### **Fixed Typo**
- Renamed `DartPermession` → `DartPermission`

#### **Documentation**
Added comprehensive JavaDoc documentation to all enums:
- `DartPermission`: ORGANIZER, MEMBER
- `DartStatus`: PENDING, ACTIVE, FINISHED
- `MemberStatus`: PENDING, ACTIVE, LEAVED
- `UserRole`: ROLE_ADMIN, ROLE_CLIENT
- `UserStatus`: PENDING, ACTIVE, SUSPENDED, DISABLED, DELETED
- `LogsLevel`: TRACE, DEBUG, INFO, WARN, ERROR

---

## Best Practices Implemented

### 1. **Separation of Concerns**
- Separated `@Entity` from `@Table` annotations
- Clear distinction between JPA entity mapping and table configuration

### 2. **Validation**
- Bean Validation annotations at domain level (`@NotBlank`, `@NotNull`, `@Email`, `@Size`, etc.)
- Meaningful validation messages for better error handling

### 3. **Relationship Management**
- Proper cascade types based on relationship ownership
- Lazy loading by default for performance
- Helper methods for bidirectional relationships to maintain consistency
- `orphanRemoval = true` where appropriate

### 4. **equals() and hashCode()**
- Business key-based implementations (not based on `id`)
- Prevents issues with entities not yet persisted (null ID)
- `User`: based on `email` (natural key)
- `Dart`: based on `name` + `startDate`
- `Member`: based on `user` + `dart` (composite business key)
- `Loggin`: based on `requestId`

### 5. **toString() Methods**
- Custom implementations avoiding circular references
- Includes only essential fields
- Uses lazy-loaded association IDs instead of full objects

### 6. **Business Logic in Domain**
- Rich domain model with behavior, not just data containers
- State transition methods with validation
- Query methods for common checks
- Calculation methods for derived values

### 7. **Immutability Where Appropriate**
- Final fields where applicable
- Builder pattern for object construction
- Default values via `@Builder.Default`

### 8. **Database Optimization**
- Strategic indexes on `Loggin` entity for query performance
- Proper column definitions (length, nullable, unique)
- `BigDecimal` with precision and scale for monetary values
- TEXT columns for potentially large strings

### 9. **Auditing**
- Automatic timestamp tracking via Hibernate annotations
- Version field for optimistic locking
- Centralized in `BaseEntity`

---

## Migration Impact

### **Breaking Changes**
⚠️ The following changes require code updates in other layers:

1. **DartPermession → DartPermission**
   - Update all references in services, repositories, DTOs, and controllers
   - Update database enum values if already populated

2. **Dart.member → Dart.members**
   - Update all code accessing the members collection

3. **Member.permession → Member.permission**
   - Update all property references

4. **User.creationDate removed**
   - Use `createdAt` from `BaseEntity` instead
   - Requires database migration to rename column

### **Database Migration Required**
```sql
-- Rename user creation_date column
ALTER TABLE users RENAME COLUMN creation_date TO created_at;

-- Add missing audit columns
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE users ADD COLUMN version BIGINT DEFAULT 0;

ALTER TABLE darts ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE darts ADD COLUMN version BIGINT DEFAULT 0;

ALTER TABLE members ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE members ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE members ADD COLUMN version BIGINT DEFAULT 0;

-- Rename member permission column
ALTER TABLE members RENAME COLUMN permession TO permission;

-- Add indexes to logs table
CREATE INDEX idx_logs_timestamp ON logs(timestamp);
CREATE INDEX idx_logs_user_email ON logs(user_email);
CREATE INDEX idx_logs_event ON logs(event);
CREATE INDEX idx_logs_status ON logs(status);
CREATE INDEX idx_logs_request_id ON logs(request_id);

-- Add error tracking columns to logs
ALTER TABLE logs ADD COLUMN error_message TEXT;
ALTER TABLE logs ADD COLUMN stack_trace TEXT;
```

---

## Next Steps

### **Immediate Actions**
1. ✅ Update all references to renamed fields/classes
2. ✅ Run database migrations
3. ✅ Update DTOs and Mappers
4. ✅ Update service layer implementations
5. ✅ Run tests to verify changes

### **Future Enhancements**
- Consider adding Spring Data JPA Auditing with `@CreatedBy` and `@ModifiedBy`
- Add domain events for important state changes
- Implement soft delete pattern if needed
- Add custom validation annotations for complex business rules
- Consider adding database views for complex queries
- Implement event sourcing for audit trail if needed

---

## Code Quality Improvements

### **Before**
```java
@Entity(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(unique = true)
    private String email;
    
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
    
    @OneToOne(mappedBy = "user")
    private Member member;
}
```

### **After**
```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, 
              fetch = FetchType.LAZY, orphanRemoval = true)
    private Member member;
    
    // Business methods
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.verificationCode = null;
    }
    
    public boolean isVerified() {
        return status == UserStatus.ACTIVE;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return email != null && email.equals(user.email);
    }
}
```

---

## Benefits Summary

1. **Maintainability**: Centralized common concerns, reduced code duplication
2. **Performance**: Proper lazy loading, strategic indexes, optimistic locking
3. **Data Integrity**: Validation constraints, proper cascade types, business logic enforcement
4. **Developer Experience**: Rich domain models, clear business methods, comprehensive documentation
5. **Testing**: Easier to test business logic in isolated domain entities
6. **Debugging**: Better toString() implementations, clear error messages
7. **Scalability**: Optimistic locking prevents concurrency issues
8. **Auditing**: Automatic timestamp tracking for compliance and debugging

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Author**: Platform Development Team