package com.tontin.platform.dto.auth.login.request;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String token;
}
