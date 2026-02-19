# UserMapper Bean Fix Summary

**Date:** 2026-02-15  
**Issue:** `UserMapper` bean could not be found during application startup  
**Status:** ✅ RESOLVED

---

## Problem Description

When starting the Spring Boot application, the following error occurred:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 4 of constructor in com.tontin.platform.service.impl.AuthServiceImpl required a bean of type 'com.tontin.platform.mapper.UserMapper' that could not be found.

Action:

Consider defining a bean of type 'com.tontin.platform.mapper.UserMapper' in your configuration.
```

### Root Cause

The `UserMapper` interface was correctly defined with MapStruct annotations, but the **generated implementation class** (`UserMapperImpl`) was not being properly compiled or was missing from the classpath. This occurred because:

1. The MapStruct annotation processor may not have run during the previous compilation
2. The generated sources directory might not have been included in the classpath
3. A clean build was needed to regenerate the mapper implementations

---

## Solution

The issue was resolved by performing a **clean rebuild** of the project, which ensured that:

1. MapStruct annotation processor executed properly
2. `UserMapperImpl` was generated in `target/generated-sources/annotations/`
3. The generated implementation was included in the Spring application context

### Steps Taken

```bash
cd TonTin/platform-back
./mvnw clean compile -DskipTests
```

This command:
- ✅ Cleaned the `target/` directory
- ✅ Recompiled all source files
- ✅ Triggered MapStruct annotation processor
- ✅ Generated `UserMapperImpl.java` with `@Component` annotation

---

## Verification

### 1. Generated Mapper Implementation

**Location:** `platform-back/target/generated-sources/annotations/com/tontin/platform/mapper/UserMapperImpl.java`

**Content:**
```java
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-15T16:46:00+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.9"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.userName( user.getUserName() );
        userResponse.email( user.getEmail() );
        userResponse.creationDate( user.getCreationDate() );
        userResponse.emailConfirmed( user.getEmailConfirmed() );
        userResponse.accountAccessFileCount( user.getAccountAccessFileCount() );
        userResponse.resetPasswordDate( user.getResetPasswordDate() );
        userResponse.role( user.getRole() );
        byte[] picture = user.getPicture();
        if ( picture != null ) {
            userResponse.picture( Arrays.copyOf( picture, picture.length ) );
        }
        userResponse.status( user.getStatus() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.updatedAt( user.getUpdatedAt() );

        return userResponse.build();
    }
}
```

**Key Points:**
- ✅ `@Component` annotation is present (makes it a Spring bean)
- ✅ Implements `UserMapper` interface
- ✅ Properly maps all fields from `User` to `UserResponse`
- ✅ Handles null checks appropriately

### 2. Build Success

```
[INFO] BUILD SUCCESS
[INFO] Total time:  7.019 s
[INFO] Finished at: 2026-02-15T16:46:01Z
```

### 3. Application Startup Success

```
2026-02-15T16:47:36.534Z  INFO 952393 --- [platform] [  restartedMain] 
c.tontin.platform.PlatformApplication    : Started PlatformApplication in 7.347 seconds
```

**Result:** ✅ Application started successfully without UserMapper bean errors!

---

## MapStruct Configuration

### UserMapper Interface

**File:** `TonTin/platform-back/src/main/java/com/tontin/platform/mapper/UserMapper.java`

```java
package com.tontin.platform.mapper;

import org.mapstruct.Mapper;
import com.tontin.platform.domain.User;
import com.tontin.platform.dto.auth.user.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user); 
}
```

**Key Annotation:** `@Mapper(componentModel = "spring")`
- This tells MapStruct to generate a Spring component
- The generated implementation will have `@Component` annotation
- Spring will automatically detect and register it as a bean

### Maven Configuration

**File:** `pom.xml`

```xml
<dependencies>
    <!-- MapStruct dependency -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.6.3</version>
        <scope>compile</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>21</source>
                <target>21</target>
                <annotationProcessorPaths>
                    <!-- Lombok processor -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                    <!-- MapStruct processor -->
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.6.3</version>
                    </path>
                    <!-- Lombok-MapStruct binding -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Important Configuration Elements:**
1. **MapStruct dependency** - Provides annotations and base classes
2. **MapStruct processor** - Generates implementation classes during compilation
3. **Lombok-MapStruct binding** - Ensures compatibility between Lombok and MapStruct
4. **Annotation processor paths** - Tells Maven compiler plugin to run these processors

---

## How MapStruct Works

### 1. Developer Writes Interface
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);
}
```

### 2. Maven Compiler Plugin Runs Annotation Processors
During compilation, the MapStruct processor:
- Detects `@Mapper` annotated interfaces
- Analyzes source and target types
- Generates implementation code

### 3. Generated Implementation
```java
@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserResponse toDto(User user) {
        // Generated mapping code
    }
}
```

### 4. Spring Auto-Detection
- Spring component scanning finds `@Component` classes
- `UserMapperImpl` is registered as a bean
- Available for dependency injection

### 5. Dependency Injection
```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper; // ✅ Injected by Spring
}
```

---

## Other Mappers in Project

### DartMapper
**File:** `DartMapper.java`
```java
@Mapper(componentModel = "spring")
public interface DartMapper {
    Dart toEntity(DartRequest request);
    
    @Mapping(target = "totalMonthlyPool", 
             expression = "java(dart.calculateTotalMonthlyContributions())")
    DartResponse toDto(Dart dart);
}
```
- ✅ Uses `@Mapper(componentModel = "spring")`
- ✅ Generated implementation: `DartMapperImpl`
- ✅ Status: Working correctly

### MemberMapper
**File:** `MemberMapper.java`
```java
@Component
public class MemberMapper {
    public Member toEntity(MemberRequest request) { ... }
    public MemberResponse toDto(Member member) { ... }
}
```
- ⚠️ Uses manual `@Component` annotation (not MapStruct)
- ✅ Manually written implementation
- ✅ Status: Working correctly (different approach)

**Note:** `MemberMapper` doesn't use MapStruct because it has custom mapping logic that requires manual implementation.

---

## Troubleshooting Guide

### If UserMapper Bean Error Occurs Again

#### Step 1: Clean and Rebuild
```bash
cd platform-back
./mvnw clean compile
```

#### Step 2: Verify Generated Files
```bash
find target/generated-sources -name "*UserMapper*"
```
Expected output:
```
target/generated-sources/annotations/com/tontin/platform/mapper/UserMapperImpl.java
```

#### Step 3: Check Generated Implementation
```bash
cat target/generated-sources/annotations/com/tontin/platform/mapper/UserMapperImpl.java
```
Verify it has `@Component` annotation.

#### Step 4: Verify MapStruct Dependency
```bash
./mvnw dependency:tree | grep mapstruct
```
Expected output should show:
```
[INFO] +- org.mapstruct:mapstruct:jar:1.6.3:compile
```

#### Step 5: Check Annotation Processor Configuration
```bash
grep -A 20 "maven-compiler-plugin" pom.xml
```
Verify `mapstruct-processor` is in `<annotationProcessorPaths>`.

#### Step 6: IDE Issues
If using IntelliJ IDEA or Eclipse:
- **IntelliJ:** File → Invalidate Caches → Invalidate and Restart
- **Eclipse:** Project → Clean → Clean All Projects
- Ensure annotation processing is enabled in IDE settings

---

## Prevention Checklist

To prevent this issue in the future:

- [ ] Always run `./mvnw clean compile` after pulling major changes
- [ ] Ensure `target/generated-sources` is not in `.gitignore`
- [ ] Keep MapStruct and Lombok versions compatible
- [ ] Verify annotation processor configuration in `pom.xml`
- [ ] Run full build after updating dependencies
- [ ] Check IDE annotation processing settings
- [ ] Include generated sources in IDE module paths

---

## Related Configuration Files

### Files Verified as Correct

1. ✅ `pom.xml` - MapStruct dependencies and annotation processors configured
2. ✅ `UserMapper.java` - Interface with proper `@Mapper` annotation
3. ✅ `UserMapperImpl.java` - Generated implementation with `@Component`
4. ✅ `AuthServiceImpl.java` - Service correctly injects `UserMapper`

### No Changes Required

No code changes were needed to fix this issue. The problem was resolved by:
- Cleaning the build directory
- Regenerating MapStruct implementations
- Ensuring proper annotation processing

---

## Build Commands Reference

### Full Clean Build
```bash
./mvnw clean install -DskipTests
```

### Compile Only
```bash
./mvnw clean compile
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Package WAR
```bash
./mvnw clean package -DskipTests
```

### Verify Dependencies
```bash
./mvnw dependency:tree
```

---

## Testing

### Unit Test (Future Enhancement)
```java
@SpringBootTest
class UserMapperTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    void testUserMapperBeanExists() {
        assertThat(userMapper).isNotNull();
    }
    
    @Test
    void testToDto_MapsAllFields() {
        User user = User.builder()
            .id(1L)
            .userName("testuser")
            .email("test@example.com")
            .build();
            
        UserResponse response = userMapper.toDto(user);
        
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }
}
```

---

## Impact

### Services Using UserMapper

1. **AuthServiceImpl** - Authentication service
   - Uses `userMapper.toDto()` to convert User entities to DTOs
   - Required for login/registration responses

### Expected Behavior After Fix

- ✅ Application starts successfully
- ✅ UserMapper bean is available in Spring context
- ✅ AuthServiceImpl can inject and use UserMapper
- ✅ User authentication returns proper UserResponse DTOs

---

## Summary

| Aspect | Status |
|--------|--------|
| **Root Cause** | Missing generated MapStruct implementation |
| **Solution** | Clean rebuild to regenerate implementations |
| **Build Status** | ✅ Success |
| **Application Startup** | ✅ Success |
| **UserMapper Bean** | ✅ Found and injected |
| **Code Changes** | ❌ None required |
| **Configuration Changes** | ❌ None required |

---

## Conclusion

The UserMapper bean error was successfully resolved by performing a clean rebuild of the project. The MapStruct annotation processor regenerated the `UserMapperImpl` class with the proper `@Component` annotation, allowing Spring to detect and register it as a bean. The application now starts successfully and all mapper beans are properly injected.

**Status:** ✅ **COMPLETE AND VERIFIED**

---

## References

- MapStruct Documentation: https://mapstruct.org/
- Spring Component Scanning: https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html
- Maven Compiler Plugin: https://maven.apache.org/plugins/maven-compiler-plugin/
- Lombok-MapStruct Integration: https://github.com/projectlombok/lombok/wiki/Using-Lombok-with-MapStruct

---

**Last Updated:** 2026-02-15  
**Verified By:** Build successful, application started, no errors