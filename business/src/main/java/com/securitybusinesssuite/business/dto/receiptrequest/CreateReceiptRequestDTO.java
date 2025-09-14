// path: business/src/main/java/com/securitybusinesssuite/business/dto/CreateReceiptRequestDTO.java
package com.securitybusinesssuite.business.dto.receiptrequest;

import com.securitybusinesssuite.data.entity.Client;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateReceiptRequestDTO {
    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Payment method is required")
    private Client.PaymentMethod paymentMethod;

    private String currency = "DOP";

    // Payment method specific fields
    private String checkNumber;
    private String bankName;
    private String referenceNumber;

    private String notes;

    @Valid
    private List<PaymentAllocationDTO> allocations;
}
