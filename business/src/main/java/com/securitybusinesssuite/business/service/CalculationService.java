// path: business/src/main/java/com/securitybusinesssuite/business/service/CalculationService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.data.entity.Invoice;
import com.securitybusinesssuite.data.entity.InvoiceItem;
import java.math.BigDecimal;
import java.util.List;

public interface CalculationService {
    Invoice calculateInvoiceTotals(Invoice invoice, List<InvoiceItem> items);
    InvoiceItem calculateLineItem(InvoiceItem item);
    BigDecimal calculateITBIS(BigDecimal taxableAmount);
    BigDecimal calculateLineSubtotal(BigDecimal quantity, BigDecimal unitPrice);
    BigDecimal calculateLineDiscount(BigDecimal subtotal, BigDecimal discountPercentage);
    BigDecimal calculateLineTotal(BigDecimal subtotal, BigDecimal itbisAmount, BigDecimal discountAmount);
}