// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/EmailServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.backend.url}")
    private String backendUrl;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    public void sendVerificationEmail(String to, String firstName, String verificationToken) {
        String verificationLink = backendUrl + "/auth/verify-email?token=" + verificationToken;

        if ("dev".equals(activeProfile)) {
            log.info("=== EMAIL VERIFICATION LINK (DEV MODE) ===");
            log.info("To: {}", to);
            log.info("Link: {}", verificationLink);
            log.info("==========================================");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verify your email - Security Business Suite");
        message.setText(String.format("""
            Hi %s,
            
            Welcome to Security Business Suite! Please verify your email by clicking the link below:
            
            %s
            
            This link will expire in 24 hours.
            
            If you didn't create an account, please ignore this email.
            
            Best regards,
            Security Business Suite Team
            """, firstName, verificationLink));

        try {
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String firstName, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        if ("dev".equals(activeProfile)) {
            log.info("=== PASSWORD RESET LINK (DEV MODE) ===");
            log.info("To: {}", to);
            log.info("Link: {}", resetLink);
            log.info("=======================================");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset - Security Business Suite");
        message.setText(String.format("""
            Hi %s,
            
            We received a request to reset your password. Click the link below to create a new password:
            
            %s
            
            This link will expire in 1 hour.
            
            If you didn't request this, please ignore this email.
            
            Best regards,
            Security Business Suite Team
            """, firstName, resetLink));

        try {
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }
}