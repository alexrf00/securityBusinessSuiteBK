// path: data/src/main/java/com/securitybusinesssuite/data/entity/InvoiceItem.java
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
public class InvoiceItem {
    private UUID id;
    private UUID invoiceId;
    private String serviceCode;
    private String description;
    private Client.SecurityService serviceType;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal itbisRate;
    private BigDecimal itbisAmount;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal lineSubtotal;
    private BigDecimal lineTotal;
    private LocalDateTime createdAt;
    private UUID createdBy;
}