// path: data/src/main/java/com/securitybusinesssuite/data/entity/ReceiptAllocation.java
package com.securitybusinesssuite.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptAllocation {
    private UUID id;
    private UUID receiptId;
    private UUID invoiceId;
    private BigDecimal allocatedAmount;
    private LocalDateTime createdAt;
    private UUID createdBy;

    // Transient fields
    private Invoice invoice;
}
