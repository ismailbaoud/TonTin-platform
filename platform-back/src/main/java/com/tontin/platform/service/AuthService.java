package com.tontin.platform.service;

import com.tontin.platform.dto.auth.login.request.LoginRequest;
import com.tontin.platform.dto.auth.login.response.LoginResponse;
import com.tontin.platform.dto.auth.register.request.RegisterRequest;
import com.tontin.platform.dto.auth.user.UserProfileUpdateRequest;
import com.tontin.platform.dto.auth.user.UserResponse;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface AuthService {
    public LoginResponse login(LoginRequest request);
    public UserResponse register(RegisterRequest request, String siteURL)
        throws UnsupportedEncodingException, MessagingException;
    public UserResponse getCurrentUserProfile();
    public UserResponse updateCurrentUserProfile(
        UserProfileUpdateRequest request
    );
    public void logout();
    public String verify(String code);
}
