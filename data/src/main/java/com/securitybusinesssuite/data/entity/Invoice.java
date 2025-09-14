// path: data/src/main/java/com/securitybusinesssuite/data/entity/Invoice.java
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
public class Invoice {
    private UUID id;
    private String invoiceNumber;
    private String ncf;
    private NCFType ncfType;
    private UUID clientId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceType invoiceType;
    private BigDecimal subtotal;
    private BigDecimal itbisAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal netAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private InvoiceStatus status;
    private String notes;
    private String dgiiTrackId;
    private String dgiiStatus;
    private String ecfPayload; // JSON string
    private String qrHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    // Transient fields for relationships
    private Client client;
    private List<InvoiceItem> items;

    public enum InvoiceType {
        CREDITO, CONTADO
    }

    public enum InvoiceStatus {
        PENDING, PAID, OVERDUE, CANCELLED
    }

    public enum NCFType {
        B01("B01"), B02("B02");

        private final String prefix;

        NCFType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
