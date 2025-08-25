// path: business/src/main/java/com/securitybusinesssuite/business/service/EmailService.java
package com.securitybusinesssuite.business.service;

public interface EmailService {
    void sendVerificationEmail(String to, String firstName, String verificationToken);
    void sendPasswordResetEmail(String to, String firstName, String resetToken);
}