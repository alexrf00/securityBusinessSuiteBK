// path: business/src/main/java/com/securitybusinesssuite/business/dto/ReceiptAllocationResponseDTO.java
package com.securitybusinesssuite.business.dto.receiptresponse;

import com.securitybusinesssuite.business.dto.invoiceresponse.InvoiceResponseDTO;
import com.securitybusinesssuite.data.entity.ReceiptAllocation;
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
public class ReceiptAllocationResponseDTO {
    private UUID id;
    private UUID receiptId;
    private UUID invoiceId;
    private InvoiceResponseDTO invoice;
    private BigDecimal allocatedAmount;
    private LocalDateTime createdAt;

    public static ReceiptAllocationResponseDTO fromEntity(ReceiptAllocation allocation) {
        return ReceiptAllocationResponseDTO.builder()
                .id(allocation.getId())
                .receiptId(allocation.getReceiptId())
                .invoiceId(allocation.getInvoiceId())
                .invoice(allocation.getInvoice() != null ? InvoiceResponseDTO.fromEntity(allocation.getInvoice()) : null)
                .allocatedAmount(allocation.getAllocatedAmount())
                .createdAt(allocation.getCreatedAt())
                .build();
    }
}