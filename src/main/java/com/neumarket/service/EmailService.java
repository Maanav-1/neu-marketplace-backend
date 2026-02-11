package com.neumarket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.email.from}")
  private String fromEmail;

  @Async
  public void sendVerificationEmail(String toEmail, String code) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject("Verify your NEU Marketplace account");
      message.setText(buildVerificationEmailBody(code));

      mailSender.send(message);
      log.info("Verification email sent to: {}", toEmail);
    } catch (Exception e) {
      // In dev mode, just log the code
      log.warn("Failed to send email to {}. Verification code: {}", toEmail, code);
      log.error("Email error: ", e);
    }
  }

  private String buildVerificationEmailBody(String code) {
    return """
            Welcome to NEU Marketplace!
            
            Your verification code is: %s
            
            This code will expire in 15 minutes.
            
            If you didn't create an account, please ignore this email.
            
            Thanks,
            NEU Marketplace Team
            """.formatted(code);
  }

  @Async
  public void sendPasswordResetEmail(String toEmail, String resetToken) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject("Reset your NEU Marketplace password");
      message.setText(buildPasswordResetEmailBody(resetToken));

      mailSender.send(message);
      log.info("Password reset email sent to: {}", toEmail);
    } catch (Exception e) {
      log.warn("Failed to send password reset email to {}. Token: {}", toEmail, resetToken);
      log.error("Email error: ", e);
    }
  }

  private String buildPasswordResetEmailBody(String resetToken) {
    return """
            You requested to reset your password.
            
            Your password reset code is: %s
            
            This code will expire in 15 minutes.
            
            If you didn't request this, please ignore this email.
            
            Thanks,
            NEU Marketplace Team
            """.formatted(resetToken);
  }
}