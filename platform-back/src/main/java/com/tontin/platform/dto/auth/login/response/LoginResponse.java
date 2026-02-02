package com.tontin.platform.dto.auth.login.response;

import com.tontin.platform.dto.auth.User.UserResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private final String token;
    private final String refreshToken;
    private final UserResponse user;
}
