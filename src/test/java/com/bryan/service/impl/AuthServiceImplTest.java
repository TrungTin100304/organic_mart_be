package com.bryan.service.impl;

import com.bryan.dto.request.ForgotPasswordRequest;
import com.bryan.dto.request.LoginRequest;
import com.bryan.dto.request.RefreshTokenRequest;
import com.bryan.dto.request.ResetPasswordRequest;
import com.bryan.dto.response.AuthResponse;
import com.bryan.entity.RefreshToken;
import com.bryan.entity.Role;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.repository.RefreshTokenRepository;
import com.bryan.repository.UserRepository;
import com.bryan.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailsServiceImpl userDetailsService;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);

        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setPasswordHash("hashed_password");
        user.setRole(Role.USER);
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest("test@gmail.com", "password");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password(), user.getPasswordHash())).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);

        when(jwtUtils.generateAccessToken(userDetails)).thenReturn("access_token");
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn("refresh_token");

        AuthResponse resp = authService.login(req);

        assertEquals("access_token", resp.accessToken());
        assertEquals("refresh_token", resp.refreshToken());
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refresh_success() {
        RefreshTokenRequest req = new RefreshTokenRequest("valid_token");
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken("valid_token");
        token.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByToken(req.refreshToken())).thenReturn(Optional.of(token));

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);

        when(jwtUtils.generateAccessToken(userDetails)).thenReturn("new_access");
        when(jwtUtils.generateRefreshToken(userDetails)).thenReturn("new_refresh");

        AuthResponse resp = authService.refresh(req);

        assertEquals("new_access", resp.accessToken());
        assertEquals("new_refresh", resp.refreshToken());
        verify(refreshTokenRepository).delete(token);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refresh_expiredToken() {
        RefreshTokenRequest req = new RefreshTokenRequest("expired_token");
        RefreshToken token = new RefreshToken();
        token.setToken("expired_token");
        token.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken(req.refreshToken())).thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> authService.refresh(req));
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void logout_success() {
        RefreshToken token = new RefreshToken();
        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        authService.logout("token");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void forgotPassword_success() {
        ForgotPasswordRequest req = new ForgotPasswordRequest("test@gmail.com");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(user));

        authService.forgotPassword(req);

        assertNotNull(user.getResetPasswordToken());
        assertNotNull(user.getResetPasswordExpiresAt());
        verify(userRepository).save(user);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void resetPassword_success() {
        user.setResetPasswordToken("valid_reset_token");
        user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(10));

        ResetPasswordRequest req = new ResetPasswordRequest("valid_reset_token", "newPassword");

        when(userRepository.findByResetPasswordToken(req.token())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(req.newPassword())).thenReturn("new_hashed");

        authService.resetPassword(req);

        assertEquals("new_hashed", user.getPasswordHash());
        assertNull(user.getResetPasswordToken());
        assertNull(user.getResetPasswordExpiresAt());
        verify(userRepository).save(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void resetPassword_expiredToken() {
        user.setResetPasswordToken("expired_reset_token");
        user.setResetPasswordExpiresAt(LocalDateTime.now().minusMinutes(1));

        ResetPasswordRequest req = new ResetPasswordRequest("expired_reset_token", "newPassword");

        when(userRepository.findByResetPasswordToken(req.token())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> authService.resetPassword(req));
    }
}
