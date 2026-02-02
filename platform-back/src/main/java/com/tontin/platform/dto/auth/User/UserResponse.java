package com.tontin.platform.dto.auth.User;

import java.time.LocalDateTime;

import com.tontin.platform.domain.enums.user.UserRole;
import com.tontin.platform.domain.enums.user.UserStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String userName;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime creationDate;
}
