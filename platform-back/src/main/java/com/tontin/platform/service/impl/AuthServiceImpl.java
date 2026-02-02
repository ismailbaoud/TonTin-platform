package com.tontin.platform.service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tontin.platform.config.CustomUserDetails;
import com.tontin.platform.config.JwtService;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.user.UserRole;
import com.tontin.platform.domain.enums.user.UserStatus;
import com.tontin.platform.dto.auth.User.UserResponse;
import com.tontin.platform.dto.auth.login.request.LoginRequest;
import com.tontin.platform.dto.auth.login.response.LoginResponse;
import com.tontin.platform.dto.auth.register.request.RegisterRequest;
import com.tontin.platform.helper.TokenUtil;
import com.tontin.platform.mapper.user.UserMapper;
import com.tontin.platform.repository.UserRepository;
import com.tontin.platform.service.AuthService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;

    @Override
    public LoginResponse login(LoginRequest request) {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        var jwt = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return LoginResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    public UserResponse register(RegisterRequest request, String siteURL)
            throws UnsupportedEncodingException, MessagingException {
        User user = new User();
        user.setCreationDate(LocalDateTime.now());
        user.setEmail(request.getEmail());
        user.setRole(UserRole.ROLE_CLIENT);
        user.setStatus(UserStatus.PENDING);
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        String randomCode = TokenUtil.generate(64);
        System.out.println(randomCode);
        user.setVerificationCode(randomCode);
        User userRes = userRepository.save(user);
        sendVerificationEmail(user, siteURL);
        return userMapper.toDto(userRes);

    }

    private void sendVerificationEmail(User user, String siteURL)
            throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "example@example.com";
        String senderName = "TonTin";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "TonTin.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getUserName());
        String verifyURL = siteURL + "/api/v1/auth/verify?code=" + user.getVerificationCode();

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
        userRepository.save(user);
        return "User activated succussfully ! \n welcome Mr." + user.getUserName();
    }

    @Override
    public void logout() {

    }

}
