// path: business/src/main/java/com/securitybusinesssuite/business/dto/CreateInvoiceRequestDTO.java
package com.securitybusinesssuite.business.dto.invoicerequest;

import com.securitybusinesssuite.data.entity.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateInvoiceRequestDTO {
    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;

    @NotNull(message = "Invoice type is required")
    private Invoice.InvoiceType invoiceType;

    @NotEmpty(message = "Invoice must have at least one item")
    @Valid
    private List<InvoiceItemRequestDTO> items;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String notes;
}