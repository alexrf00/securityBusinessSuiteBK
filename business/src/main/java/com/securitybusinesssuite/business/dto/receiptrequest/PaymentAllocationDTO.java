// path: business/src/main/java/com/securitybusinesssuite/business/dto/PaymentAllocationDTO.java
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
public class PaymentAllocationDTO {
    @NotNull(message = "Invoice ID is required")
    private UUID invoiceId;

    @NotNull(message = "Allocated amount is required")
    @DecimalMin(value = "0.01", message = "Allocated amount must be positive")
    private BigDecimal allocatedAmount;
}