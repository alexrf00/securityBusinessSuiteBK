// path: business/src/main/java/com/securitybusinesssuite/business/dto/ReceiptResponseDTO.java
package com.securitybusinesssuite.business.dto.receiptresponse;

import com.securitybusinesssuite.business.dto.ClientResponseDTO;
import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Receipt;
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
public class ReceiptResponseDTO {
    private UUID id;
    private String receiptNumber;
    private UUID clientId;
    private ClientResponseDTO client;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private Client.PaymentMethod paymentMethod;
    private String currency;
    private String checkNumber;
    private String bankName;
    private String referenceNumber;
    private Receipt.ReceiptStatus status;
    private String notes;
    private List<ReceiptAllocationResponseDTO> allocations;
    private BigDecimal allocatedAmount;
    private BigDecimal availableAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReceiptResponseDTO fromEntity(Receipt receipt) {
        return ReceiptResponseDTO.builder()
                .id(receipt.getId())
                .receiptNumber(receipt.getReceiptNumber())
                .clientId(receipt.getClientId())
                .client(receipt.getClient() != null ? ClientResponseDTO.fromEntity(receipt.getClient()) : null)
                .issueDate(receipt.getIssueDate())
                .totalAmount(receipt.getTotalAmount())
                .paymentMethod(receipt.getPaymentMethod())
                .currency(receipt.getCurrency())
                .checkNumber(receipt.getCheckNumber())
                .bankName(receipt.getBankName())
                .referenceNumber(receipt.getReferenceNumber())
                .status(receipt.getStatus())
                .notes(receipt.getNotes())
                .createdAt(receipt.getCreatedAt())
                .updatedAt(receipt.getUpdatedAt())
                .build();
    }
}