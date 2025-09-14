// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/ValidationServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.config.DominicanTaxConfig;
import com.securitybusinesssuite.business.exception.BusinessException;
import com.securitybusinesssuite.business.service.ValidationService;
import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Invoice;
import com.securitybusinesssuite.data.entity.Receipt;
import com.securitybusinesssuite.data.entity.ReceiptAllocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final DominicanTaxConfig taxConfig;

    @Override
    public void validateRNCFormat(String rnc) {
        if (rnc == null || rnc.trim().isEmpty()) {
            return; // RNC is optional for some client types
        }

        Pattern rncPattern = Pattern.compile(DominicanTaxConfig.RNC_PATTERN);
        if (!rncPattern.matcher(rnc.trim()).matches()) {
            throw new BusinessException("RNC must be in format XXX-XXXXXXX-X (e.g., 123-1234567-8)");
        }
    }

    @Override
    public void validateClientRequirements(Client client) {
        // Validate RNC format
        validateRNCFormat(client.getRnc());

        // Business rules for different client types
        switch (client.getClientType()) {
            case SRL:
                if (client.getRnc() == null || client.getRnc().trim().isEmpty()) {
                    throw new BusinessException("SRL clients must have a valid RNC");
                }
                if (!client.isRequiresNcf()) {
                    throw new BusinessException("SRL clients must require NCF");
                }
                if (!client.isRequiresRnc()) {
                    throw new BusinessException("SRL clients must require RNC validation");
                }
                break;

            case PERSONA_FISICA:
                // Physical persons may or may not have RNC depending on their business activities
                if (client.getRnc() != null && !client.getRnc().trim().isEmpty()) {
                    if (!client.isRequiresRnc()) {
                        throw new BusinessException("If RNC is provided, client must require RNC validation");
                    }
                }
                break;

            case CONSUMIDOR_FINAL:
                if (client.getRnc() != null && !client.getRnc().trim().isEmpty()) {
                    throw new BusinessException("Final consumers should not have RNC");
                }
                if (client.isRequiresNcf()) {
                    throw new BusinessException("Final consumers do not require NCF");
                }
                break;
        }

        // Validate contract dates
        if (client.isHasContract()) {
            if (client.getContractStartDate() == null) {
                throw new BusinessException("Contract start date is required when client has contract");
            }
            if (client.getContractEndDate() == null && !client.isAutoRenewal()) {
                throw new BusinessException("Contract end date is required when auto-renewal is disabled");
            }
            if (client.getContractStartDate() != null && client.getContractEndDate() != null) {
                if (client.getContractStartDate().isAfter(client.getContractEndDate())) {
                    throw new BusinessException("Contract start date cannot be after end date");
                }
            }
        }

        // Validate hourly rate
        if (client.getHourlyRate() != null && client.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Hourly rate must be positive");
        }

        // Validate business name
        if (client.getBusinessName() == null || client.getBusinessName().trim().isEmpty()) {
            throw new BusinessException("Business name is required");
        }
    }

    @Override
    public void validateInvoiceRequirements(Invoice invoice, Client client) {
        // Validate due date
        if (invoice.getDueDate() != null && invoice.getIssueDate() != null) {
            if (invoice.getDueDate().isBefore(invoice.getIssueDate())) {
                throw new BusinessException("Due date cannot be before issue date");
            }
        }

        // Validate NCF requirements
        if (client.isRequiresNcf() && (invoice.getNcf() == null || invoice.getNcf().trim().isEmpty())) {
            throw new BusinessException("NCF is required for this client type");
        }

        if (!client.isRequiresNcf() && invoice.getNcf() != null && !invoice.getNcf().trim().isEmpty()) {
            throw new BusinessException("NCF should not be provided for this client type");
        }

        // Validate amounts
        validateInvoiceAmounts(invoice);
    }

    @Override
    public void validateReceiptAllocation(Receipt receipt, List<ReceiptAllocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            return; // No allocations to validate
        }

        BigDecimal totalAllocated = allocations.stream()
                .map(ReceiptAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAllocated.compareTo(receipt.getTotalAmount()) > 0) {
            throw new BusinessException(
                    String.format("Total allocated amount (%.2f) cannot exceed receipt amount (%.2f)",
                            totalAllocated, receipt.getTotalAmount())
            );
        }

        // Validate individual allocations
        for (ReceiptAllocation allocation : allocations) {
            if (allocation.getAllocatedAmount() == null || allocation.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Allocation amount must be positive");
            }
        }
    }

    @Override
    public void validateInvoiceAmounts(Invoice invoice) {
        // Validate that all amounts are non-negative
        if (invoice.getSubtotal() != null && invoice.getSubtotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Subtotal cannot be negative");
        }

        if (invoice.getItbisAmount() != null && invoice.getItbisAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("ITBIS amount cannot be negative");
        }

        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Discount amount cannot be negative");
        }

        if (invoice.getTotalAmount() != null && invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Total amount must be positive");
        }

        if (invoice.getPaidAmount() != null && invoice.getPaidAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Paid amount cannot be negative");
        }

        // Validate that paid amount doesn't exceed total amount
        if (invoice.getPaidAmount() != null && invoice.getTotalAmount() != null) {
            if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) > 0) {
                throw new BusinessException("Paid amount cannot exceed total amount");
            }
        }
    }

    @Override
    public void validatePaymentMethodDetails(Client.PaymentMethod method, String checkNumber, String bankName, String referenceNumber) {
        switch (method) {
            case CHEQUE:
                if (checkNumber == null || checkNumber.trim().isEmpty()) {
                    throw new BusinessException("Check number is required for check payments");
                }
                if (bankName == null || bankName.trim().isEmpty()) {
                    throw new BusinessException("Bank name is required for check payments");
                }
                break;

            case TRANSFERENCIA_BANCARIA:
                if (referenceNumber == null || referenceNumber.trim().isEmpty()) {
                    throw new BusinessException("Reference number is required for bank transfer payments");
                }
                if (bankName == null || bankName.trim().isEmpty()) {
                    throw new BusinessException("Bank name is required for bank transfer payments");
                }
                break;

            case EFECTIVO:
                // No additional validation required for cash payments
                break;
        }
    }
}