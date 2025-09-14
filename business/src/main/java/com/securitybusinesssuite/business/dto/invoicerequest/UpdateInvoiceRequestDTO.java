package com.securitybusinesssuite.business.dto.invoicerequest;

import com.securitybusinesssuite.data.entity.Invoice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateInvoiceRequestDTO {
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Invoice type is required")
    private Invoice.InvoiceType invoiceType;

    @Valid
    private List<InvoiceItemRequestDTO> items;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String notes;
}