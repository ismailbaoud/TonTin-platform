package com.tontin.platform.dto.auth.register.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String userName;
    private String email;
    private String password;
}