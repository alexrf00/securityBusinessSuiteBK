// path: business/src/main/java/com/securitybusinesssuite/business/dto/InvoiceResponseDTO.java
package com.securitybusinesssuite.business.dto.invoiceresponse;

import com.securitybusinesssuite.business.dto.ClientResponseDTO;
import com.securitybusinesssuite.data.entity.Invoice;
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
public class InvoiceResponseDTO {
    private UUID id;
    private String invoiceNumber;
    private String ncf;
    private Invoice.NCFType ncfType;
    private UUID clientId;
    private ClientResponseDTO client;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Invoice.InvoiceType invoiceType;
    private BigDecimal subtotal;
    private BigDecimal itbisAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal netAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceDue;
    private Invoice.InvoiceStatus status;
    private String notes;
    private List<InvoiceItemResponseDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InvoiceResponseDTO fromEntity(Invoice invoice) {
        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .ncf(invoice.getNcf())
                .ncfType(invoice.getNcfType())
                .clientId(invoice.getClientId())
                .client(invoice.getClient() != null ? ClientResponseDTO.fromEntity(invoice.getClient()) : null)
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .invoiceType(invoice.getInvoiceType())
                .subtotal(invoice.getSubtotal())
                .itbisAmount(invoice.getItbisAmount())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .netAmount(invoice.getNetAmount())
                .paidAmount(invoice.getPaidAmount())
                .balanceDue(invoice.getBalanceDue())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
