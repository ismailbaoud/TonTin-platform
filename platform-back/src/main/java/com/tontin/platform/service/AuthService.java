package com.tontin.platform.service;

import java.io.UnsupportedEncodingException;

import com.tontin.platform.dto.auth.User.UserResponse;
import com.tontin.platform.dto.auth.login.request.LoginRequest;
import com.tontin.platform.dto.auth.login.response.LoginResponse;
import com.tontin.platform.dto.auth.register.request.RegisterRequest;

import jakarta.mail.MessagingException;

public interface AuthService {
    public LoginResponse login(LoginRequest request);
    public UserResponse register(RegisterRequest request, String siteURL) throws UnsupportedEncodingException, MessagingException;
    public void logout();
    public String verify(String code);
}
