// path: data/src/main/java/com/securitybusinesssuite/data/entity/Receipt.java
package com.securitybusinesssuite.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {
    private UUID id;
    private String receiptNumber;
    private UUID clientId;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private Client.PaymentMethod paymentMethod;
    private String currency;
    private String checkNumber;
    private String bankName;
    private String referenceNumber;
    private ReceiptStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Transient fields for relationships
    private Client client;
    private List<ReceiptAllocation> allocations;

    public enum ReceiptStatus {
        ACTIVE, CANCELLED, VOIDED
    }
}
