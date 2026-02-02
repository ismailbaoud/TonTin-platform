package com.tontin.platform.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tontin.platform.config.JwtService;
import com.tontin.platform.dto.auth.User.UserResponse;
import com.tontin.platform.dto.auth.login.request.LoginRequest;
import com.tontin.platform.dto.auth.login.request.RefreshTokenRequest;
import com.tontin.platform.dto.auth.login.response.AuthenticationResponse;
import com.tontin.platform.dto.auth.login.response.LoginResponse;
import com.tontin.platform.dto.auth.register.request.RegisterRequest;
import com.tontin.platform.service.impl.AuthServiceImpl;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok().body(authService.login(request));
    } 

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request, HttpServletRequest site) throws UnsupportedEncodingException, MessagingException  {
        return ResponseEntity.ok().body(authService.register(request, getSiteUrl(site)));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("code") String code) {
        return ResponseEntity.ok().body(authService.verify(code));
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok().body("logout with success");
    }
    
    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(@RequestBody RefreshTokenRequest token) {

        String username = jwtService.extractUsername(token.getToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.isRefreshTokenValid(token.getToken(), userDetails)) {
            String newAccessToken = jwtService.generateToken(userDetails);

            return new AuthenticationResponse(newAccessToken, token.getToken());
        }

        throw new RuntimeException("Invalid refresh token");
    }
    
    private String getSiteUrl(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }
    

}
