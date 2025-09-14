// path: business/src/main/java/com/securitybusinesssuite/business/dto/InvoiceItemRequestDTO.java
package com.securitybusinesssuite.business.dto.invoicerequest;

import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class InvoiceItemRequestDTO {
    private String serviceCode = "SF-0001";

    @NotBlank(message = "Description is required")
    private String description = "SERVICIO SEGURIDAD PRIVADA";

    private Client.SecurityService serviceType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Discount percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100%")
    private BigDecimal discountPercentage = BigDecimal.ZERO;
}