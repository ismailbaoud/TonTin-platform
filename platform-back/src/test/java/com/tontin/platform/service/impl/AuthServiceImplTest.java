package com.tontin.platform.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tontin.platform.config.JwtService;
import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.user.UserRole;
import com.tontin.platform.domain.enums.user.UserStatus;
import com.tontin.platform.dto.auth.register.request.RegisterRequest;
import com.tontin.platform.dto.auth.user.UserResponse;
import com.tontin.platform.mapper.UserMapper;
import com.tontin.platform.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private SecurityUtils securityUtils;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
            userRepository,
            authenticationManager,
            jwtService,
            passwordEncoder,
            userMapper,
            mailSender,
            securityUtils
        );
    }

    @Test
    void register_shouldSaveNewUserWithExpectedDefaults() {
        RegisterRequest request = new RegisterRequest("test_user", "TEST@MAIL.COM", "Password123@");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        when(userRepository.findByUserNameIgnoreCase("test_user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123@")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Mail @Value fields are unset in this unit test; sendVerificationEmail fails fast and is caught
        // (no stub needed for mailSender).

        UserResponse expected = UserResponse.builder().email("test@mail.com").userName("test_user").build();
        when(userMapper.toDto(any(User.class))).thenReturn(expected);

        UserResponse result = authService.register(request, "http://localhost:9090");

        assertNotNull(result);
        assertEquals("test@mail.com", result.email());
        assertEquals("test_user", result.userName());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("test@mail.com", saved.getEmail());
        assertEquals("test_user", saved.getUserName());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(UserRole.ROLE_CLIENT, saved.getRole());
        assertEquals(UserStatus.PENDING, saved.getStatus());
        assertEquals(0, saved.getPoints());
    }

    @Test
    void register_shouldThrowConflict_whenEmailExistsWithActiveStatus() {
        RegisterRequest request = new RegisterRequest("test_user", "test@mail.com", "Password123@");
        User existing = new User();
        existing.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> authService.register(request, "http://localhost:9090")
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void register_shouldReuseExistingPendingUser_whenEmailAlreadyExistsButNotActive() {
        RegisterRequest request = new RegisterRequest("test_user", "test@mail.com", "Password123@");

        User existingPending = new User();
        existingPending.setId(UUID.randomUUID());
        existingPending.setEmail("test@mail.com");
        existingPending.setUserName("test_user");
        existingPending.setStatus(UserStatus.PENDING);

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(existingPending));
        when(userRepository.findByUserNameIgnoreCase("test_user")).thenReturn(Optional.of(existingPending));
        when(passwordEncoder.encode("Password123@")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toDto(any(User.class)))
            .thenReturn(UserResponse.builder().email("test@mail.com").userName("test_user").build());

        UserResponse result = authService.register(request, "http://localhost:9090");

        assertNotNull(result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertSame(existingPending, saved);
        assertEquals(UserStatus.PENDING, saved.getStatus());
        assertEquals(UserRole.ROLE_CLIENT, saved.getRole());
        assertEquals("encoded-password", saved.getPassword());
        assertEquals(0, saved.getPoints());
    }
}
