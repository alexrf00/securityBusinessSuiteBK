// path: business/src/main/java/com/securitybusinesssuite/business/dto/InvoiceItemResponseDTO.java
package com.securitybusinesssuite.business.dto.invoiceresponse;

import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.InvoiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemResponseDTO {
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

    public static InvoiceItemResponseDTO fromEntity(InvoiceItem item) {
        return InvoiceItemResponseDTO.builder()
                .id(item.getId())
                .invoiceId(item.getInvoiceId())
                .serviceCode(item.getServiceCode())
                .description(item.getDescription())
                .serviceType(item.getServiceType())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .itbisRate(item.getItbisRate())
                .itbisAmount(item.getItbisAmount())
                .discountPercentage(item.getDiscountPercentage())
                .discountAmount(item.getDiscountAmount())
                .lineSubtotal(item.getLineSubtotal())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
