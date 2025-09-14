// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/CalculationServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.config.DominicanTaxConfig;
import com.securitybusinesssuite.business.service.CalculationService;
import com.securitybusinesssuite.data.entity.Invoice;
import com.securitybusinesssuite.data.entity.InvoiceItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {

    private final DominicanTaxConfig taxConfig;

    @Override
    public Invoice calculateInvoiceTotals(Invoice invoice, List<InvoiceItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalItbis = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;

        // Calculate totals from line items
        for (InvoiceItem item : items) {
            InvoiceItem calculatedItem = calculateLineItem(item);

            subtotal = subtotal.add(calculatedItem.getLineSubtotal());
            totalItbis = totalItbis.add(calculatedItem.getItbisAmount());
            totalDiscounts = totalDiscounts.add(calculatedItem.getDiscountAmount());
        }

        // Apply invoice-level discount if any
        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalDiscounts = totalDiscounts.add(invoice.getDiscountAmount());
        }

        // Calculate final amounts
        BigDecimal netAmount = subtotal.subtract(totalDiscounts);
        BigDecimal totalAmount = netAmount.add(totalItbis);
        BigDecimal balanceDue = totalAmount.subtract(invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO);

        // Set calculated values
        invoice.setSubtotal(subtotal);
        invoice.setItbisAmount(totalItbis);
        invoice.setDiscountAmount(totalDiscounts);
        invoice.setNetAmount(netAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setBalanceDue(balanceDue);

        return invoice;
    }

    @Override
    public InvoiceItem calculateLineItem(InvoiceItem item) {
        // Calculate line subtotal
        BigDecimal lineSubtotal = calculateLineSubtotal(item.getQuantity(), item.getUnitPrice());

        // Calculate discount amount
        BigDecimal discountAmount = calculateLineDiscount(lineSubtotal,
                item.getDiscountPercentage() != null ? item.getDiscountPercentage() : BigDecimal.ZERO);

        // Calculate taxable amount (subtotal minus discount)
        BigDecimal taxableAmount = lineSubtotal.subtract(discountAmount);

        // Calculate ITBIS
        BigDecimal itbisAmount = calculateITBIS(taxableAmount);

        // Calculate line total
        BigDecimal lineTotal = calculateLineTotal(lineSubtotal, itbisAmount, discountAmount);

        // Set calculated values
        item.setLineSubtotal(lineSubtotal);
        item.setDiscountAmount(discountAmount);
        item.setItbisRate(taxConfig.getItbisRate());
        item.setItbisAmount(itbisAmount);
        item.setLineTotal(lineTotal);

        return item;
    }

    @Override
    public BigDecimal calculateITBIS(BigDecimal taxableAmount) {
        if (taxableAmount == null || taxableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return taxableAmount.multiply(taxConfig.getItbisRate())
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateLineSubtotal(BigDecimal quantity, BigDecimal unitPrice) {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }

        return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateLineDiscount(BigDecimal subtotal, BigDecimal discountPercentage) {
        if (subtotal == null || discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return subtotal.multiply(discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateLineTotal(BigDecimal subtotal, BigDecimal itbisAmount, BigDecimal discountAmount) {
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (itbisAmount == null) itbisAmount = BigDecimal.ZERO;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;

        return subtotal.add(itbisAmount).subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }
}