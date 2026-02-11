package com.neumarket.service;

import com.neumarket.enums.Role;
import com.neumarket.dto.request.*;
import com.neumarket.dto.response.AuthResponse;
import com.neumarket.exception.BadRequestException;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.exception.UnauthorizedException;
import com.neumarket.model.User;
import com.neumarket.repository.UserRepository;
import com.neumarket.security.JwtTokenProvider;
import com.neumarket.security.UserPrincipal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final AuthenticationManager authenticationManager;
  private final EmailService emailService;

  @Value("${app.verification-code-expiry-minutes}")
  private int verificationCodeExpiryMinutes;

  @Transactional
  public void signup(SignupRequest request) {
    String email = request.getEmail().toLowerCase().trim();

    // Check if the user already exists
    Optional<User> existingUser = userRepository.findByEmail(email);

    if (existingUser.isPresent()) {
      User user = existingUser.get();

      // If the user is already verified, block the signup
      if (user.getEmailVerified()) {
        throw new BadRequestException("Email already registered");
      }

      // If not verified, update their details and resend the code
      // This handles cases where they might have entered a wrong name or password first
      user.setName(request.getName().trim());
      user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

      String newCode = generateVerificationCode();
      user.setVerificationCode(newCode);
      user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));

      userRepository.save(user);
      emailService.sendVerificationEmail(email, newCode);

      log.info("Unverified user updated details and resent code to: {}", email);
      return; // Exit early so we don't try to create a duplicate row
    }

    // Original logic for completely new users
    String verificationCode = generateVerificationCode();

    User user = User.builder()
        .email(email)
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .name(request.getName().trim())
        .emailVerified(false)
        .verificationCode(verificationCode)
        .verificationCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes))
        .role(com.neumarket.enums.Role.USER)
        .build();

    userRepository.save(user);
    emailService.sendVerificationEmail(email, verificationCode);
    log.info("New user registered: {}", email);
  }

  @Transactional
  public AuthResponse verifyEmail(VerifyEmailRequest request) {
    String email = request.getEmail().toLowerCase().trim();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getEmailVerified()) {
      throw new BadRequestException("Email already verified");
    }
    if (!request.getCode().equals(user.getVerificationCode())) {
      throw new BadRequestException("Invalid verification code");
    }
    if (LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
      throw new BadRequestException("Verification code expired");
    }

    user.setEmailVerified(true);
    user.setVerificationCode(null);
    user.setVerificationCodeExpiry(null);
    userRepository.save(user);

    String token = tokenProvider.generateToken(UserPrincipal.create(user));
    return buildAuthResponse(user, token);
  }

  public AuthResponse login(LoginRequest request) {
    String email = request.getEmail().toLowerCase().trim();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    if (user.getBlocked()) {
      throw new BadRequestException("Your account has been blocked.");
    }

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, request.getPassword())
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String token = tokenProvider.generateToken(authentication);

    return buildAuthResponse(user, token);
  }

  @Transactional
  public void resendVerificationCode(String email) {
    User user = userRepository.findByEmail(email.toLowerCase().trim())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getEmailVerified()) {
      throw new BadRequestException("Email already verified");
    }

    String code = generateVerificationCode();
    user.setVerificationCode(code);
    user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
    userRepository.save(user);

    emailService.sendVerificationEmail(user.getEmail(), code);
  }

  @Transactional
  public void forgotPassword(ForgotPasswordRequest request) {
    User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    String resetCode = generateVerificationCode();
    user.setPasswordResetCode(resetCode);
    user.setPasswordResetCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
    userRepository.save(user);

    emailService.sendPasswordResetEmail(user.getEmail(), resetCode);
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getPasswordResetCode() == null || !user.getPasswordResetCode().equals(request.getCode())) {
      throw new BadRequestException("Invalid reset code");
    }

    if (LocalDateTime.now().isAfter(user.getPasswordResetCodeExpiry())) {
      throw new BadRequestException("Reset code expired");
    }

    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    user.setPasswordResetCode(null);
    user.setPasswordResetCodeExpiry(null);
    userRepository.save(user);
  }

  @Transactional
  public void changePassword(ChangePasswordRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
      throw new BadRequestException("Incorrect current password");
    }

    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  private String generateVerificationCode() {
    SecureRandom random = new SecureRandom();
    return String.valueOf(100000 + random.nextInt(900000));
  }

  private AuthResponse buildAuthResponse(User user, String token) {
    return AuthResponse.builder()
        .token(token)
        .tokenType("Bearer")
        .expiresIn(tokenProvider.getExpirationInSeconds())
        .user(AuthResponse.UserSummaryResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .profilePicUrl(user.getProfilePicUrl())
            .role(user.getRole())
            .emailVerified(user.getEmailVerified())
            .build())
        .build();
  }
}