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
import com.bryan.service.AuthService;
import com.bryan.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final JavaMailSender mailSender;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public AuthResponse signup(com.bryan.dto.request.SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_USER);

        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        saveRefreshToken(user, refreshToken);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtUtils.generateAccessToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        refreshTokenRepository.deleteByUser(user);
        saveRefreshToken(user, refreshToken);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }



    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {

        try {
            jwtUtils.validateRefreshToken(request.refreshToken());
        } catch (JwtException e) {
            throw new BadRequestException("Invalid refresh token: " + e.getMessage());
        }

        RefreshToken saved = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));


        if (saved.isExpired()) {
            refreshTokenRepository.delete(saved);
            throw new BadRequestException("Refresh token expired, please login again");
        }

        User user = saved.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        refreshTokenRepository.delete(saved);

        String newAccessToken = jwtUtils.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        saveRefreshToken(user, newRefreshToken);

        return new AuthResponse(newAccessToken, newRefreshToken, user.getEmail(), user.getRole().name());
    }

    @Override
    public void logout(String refreshToken) {

        try {
            jwtUtils.extractEmail(refreshToken);
        } catch (JwtException e) {
            return;
        }

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            sendResetEmail(user.getEmail(), resetToken);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (LocalDateTime.now().isAfter(user.getResetPasswordExpiresAt())) {
            throw new BadRequestException("Reset token expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiresAt(null);
        userRepository.save(user);

        refreshTokenRepository.deleteByUser(user);
    }



    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plus(refreshTokenExpiration, ChronoUnit.MILLIS));
        refreshTokenRepository.save(refreshToken);
    }

    private void sendResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset your password");
        message.setText("Click the link to reset your password (valid 15 minutes):\n\n"
                + "https://yourapp.com/reset-password?token=" + token);
        mailSender.send(message);
    }
}
