// path: business/src/main/java/com/securitybusinesssuite/business/dto/searchDTO/InvoiceSearchCriteria.java
package com.securitybusinesssuite.business.dto.search;

import com.securitybusinesssuite.data.entity.Invoice;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Data
public class InvoiceSearchCriteria {
    private UUID clientId;
    private String clientName;
    private Invoice.InvoiceStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String invoiceNumber;
    private String ncf;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}