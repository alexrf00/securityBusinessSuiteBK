// path: business/src/main/java/com/securitybusinesssuite/business/service/ValidationService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Invoice;
import com.securitybusinesssuite.data.entity.Receipt;
import com.securitybusinesssuite.data.entity.ReceiptAllocation;
import java.util.List;

public interface ValidationService {
    void validateRNCFormat(String rnc);
    void validateClientRequirements(Client client);
    void validateInvoiceRequirements(Invoice invoice, Client client);
    void validateReceiptAllocation(Receipt receipt, List<ReceiptAllocation> allocations);
    void validateInvoiceAmounts(Invoice invoice);
    void validatePaymentMethodDetails(Client.PaymentMethod method, String checkNumber, String bankName, String referenceNumber);
}