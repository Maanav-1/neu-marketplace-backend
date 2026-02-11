package com.neumarket.controller;

import com.neumarket.dto.request.*;
import com.neumarket.dto.response.ApiResponse;
import com.neumarket.dto.response.AuthResponse;
import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import com.neumarket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * Register a new user with NEU email
   * POST /api/auth/signup
   */
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest request) {
    authService.signup(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("Verification code sent to your email. Please check your inbox."));
  }

  /**
   * Verify email with 6-digit code
   * POST /api/auth/verify
   */
  @PostMapping("/verify")
  public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
    AuthResponse response = authService.verifyEmail(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Login with email and password
   * POST /api/auth/login
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Resend verification code
   * POST /api/auth/resend-code?email=xxx
   */
  @PostMapping("/resend-code")
  public ResponseEntity<ApiResponse> resendVerificationCode(@RequestParam String email) {
    authService.resendVerificationCode(email);
    return ResponseEntity.ok(ApiResponse.success("New verification code sent to your email."));
  }

  /**
   * Request password reset
   * POST /api/auth/forgot-password
   */
  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request);
    return ResponseEntity.ok(ApiResponse.success("Password reset code sent to your email."));
  }

  /**
   * Reset password with code
   * POST /api/auth/reset-password
   */
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.ok(ApiResponse.success("Password reset successful. You can now login with your new password."));
  }

  /**
   * Change password (authenticated users)
   * POST /api/auth/change-password
   */
  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      @CurrentUser UserPrincipal currentUser) {
    authService.changePassword(request, currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
  }
}