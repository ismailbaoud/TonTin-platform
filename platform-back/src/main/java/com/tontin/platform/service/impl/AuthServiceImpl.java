package com.tontin.platform.service.impl;

import com.tontin.platform.config.CustomUserDetails;
import com.tontin.platform.config.JwtService;
import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.user.UserRole;
import com.tontin.platform.domain.enums.user.UserStatus;
import com.tontin.platform.dto.auth.login.request.LoginRequest;
import com.tontin.platform.dto.auth.login.response.LoginResponse;
import com.tontin.platform.dto.auth.register.request.RegisterRequest;
import com.tontin.platform.dto.auth.user.UserProfileUpdateRequest;
import com.tontin.platform.dto.auth.user.UserResponse;
import com.tontin.platform.helper.TokenUtil;
import com.tontin.platform.mapper.UserMapper;
import com.tontin.platform.repository.UserRepository;
import com.tontin.platform.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final SecurityUtils securityUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );

        CustomUserDetails userDetails =
            (CustomUserDetails) authentication.getPrincipal();

        var jwt = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        return LoginResponse.builder()
            .token(jwt)
            .refreshToken(refreshToken)
            .user(userMapper.toDto(user))
            .build();
    }

    @Override
    public UserResponse register(RegisterRequest request, String siteURL) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An account with this email already exists."
            );
        }
        String userName = request.userName() != null ? request.userName().trim() : "";
        if (userName.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Username is required."
            );
        }
        if (userRepository.findByUserNameIgnoreCase(userName).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Username already taken."
            );
        }
        User user = new User();
        user.setEmail(request.email());
        user.setRole(UserRole.ROLE_CLIENT);
        user.setStatus(UserStatus.PENDING);
        user.setUserName(userName);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreationDate(LocalDateTime.now().toString());
        user.setEmailConfirmed(Boolean.FALSE);
        user.setAccountAccessFileCount(0);
        user.setResetPasswordDate(null);
        user.setPicture(null);
        String randomCode = TokenUtil.generate(64);
        user.setVerificationCode(randomCode);
        User userRes = userRepository.save(user);
        try {
            sendVerificationEmail(userRes, siteURL);
        } catch (Exception e) {
            // Do not fail registration if mail is misconfigured or unavailable
            log.warn(
                "Verification email could not be sent for {}: {}",
                userRes.getEmail(),
                e.getMessage()
            );
        }
        return userMapper.toDto(userRes);
    }

    @Override
    public UserResponse getCurrentUserProfile() {
        User user = securityUtils.requireCurrentUser();
        return userMapper.toDto(user);
    }

    @Override
    public UserResponse updateCurrentUserProfile(
        UserProfileUpdateRequest request
    ) {
        User user = securityUtils.requireCurrentUser();
        boolean updated = false;

        if (request.userName() != null) {
            String trimmedName = request.userName().trim();
            if (!trimmedName.isEmpty() && !trimmedName.equalsIgnoreCase(user.getUserName())) {
                var existing = userRepository.findByUserNameIgnoreCase(trimmedName);
                if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                    throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Username already taken."
                    );
                }
                user.setUserName(trimmedName);
                updated = true;
            }
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setResetPasswordDate(LocalDateTime.now());
            updated = true;
        }

        if (request.picture() != null) {
            if (request.picture().length == 0) {
                if (user.getPicture() != null && user.getPicture().length > 0) {
                    user.setPicture(null);
                    updated = true;
                }
            } else if (!Arrays.equals(request.picture(), user.getPicture())) {
                user.setPicture(request.picture());
                updated = true;
            }
        }

        if (!updated) {
            return userMapper.toDto(user);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    private void sendVerificationEmail(User user, String siteURL)
        throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "example@example.com";
        String senderName = "TonTin";
        String subject = "Please verify your registration";
        String content =
            "Dear [[name]],<br>" +
            "Please click the link below to verify your registration:<br>" +
            "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" +
            "Thank you,<br>" +
            "TonTin.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getUserName());
        String verifyURL =
            siteURL + "/api/v1/auth/verify?code=" + user.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
    }

    @Override
    public String verify(String code) {
        User user = userRepository.findByVerificationCode(code).orElseThrow();
        if (user == null) {
            return "User not found";
        } else if (user.getStatus() == UserStatus.ACTIVE) {
            return "User is already activated";
        }

        user.setVerificationCode("");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailConfirmed(Boolean.TRUE);
        userRepository.save(user);
        return (
            "User activated succussfully ! \n welcome Mr." + user.getUserName()
        );
    }

    @Override
    public void logout() {}
}
