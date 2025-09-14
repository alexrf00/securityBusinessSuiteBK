// path: business/src/main/java/com/securitybusinesssuite/business/dto/searchDTO/ReceiptSearchCriteria.java
package com.securitybusinesssuite.business.dto.search;

import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Receipt;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;
@Data
public class ReceiptSearchCriteria {
    private UUID clientId;
    private String clientName;
    private Receipt.ReceiptStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Client.PaymentMethod paymentMethod;
    private String receiptNumber;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}