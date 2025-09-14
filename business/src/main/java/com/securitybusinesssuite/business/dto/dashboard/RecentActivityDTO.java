// path: business/src/main/java/com/securitybusinesssuite/business/dto/RecentActivityDTO.java
package com.securitybusinesssuite.business.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    private UUID id;
    private ActivityType type;
    private String description;
    private String clientName;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    public enum ActivityType {
        CLIENT_CREATED, INVOICE_CREATED, PAYMENT_RECEIVED, INVOICE_OVERDUE
    }
}