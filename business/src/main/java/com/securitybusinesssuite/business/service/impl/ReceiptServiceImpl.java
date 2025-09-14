// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/ReceiptServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.receiptrequest.CreateReceiptRequestDTO;
import com.securitybusinesssuite.business.dto.receiptrequest.PaymentAllocationDTO;
import com.securitybusinesssuite.business.dto.receiptrequest.UpdateReceiptRequestDTO;
import com.securitybusinesssuite.business.dto.receiptresponse.ReceiptAllocationResponseDTO;
import com.securitybusinesssuite.business.dto.receiptresponse.ReceiptResponseDTO;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;
import com.securitybusinesssuite.business.dto.search.ReceiptSearchCriteria;
import com.securitybusinesssuite.business.exception.BusinessException;
import com.securitybusinesssuite.business.service.InvoiceService;
import com.securitybusinesssuite.business.service.ReceiptService;
import com.securitybusinesssuite.business.service.ValidationService;
import com.securitybusinesssuite.data.entity.*;
import com.securitybusinesssuite.data.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptAllocationRepository receiptAllocationRepository;
    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final SequenceRepository sequenceRepository;
    private final ValidationService validationService;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public ReceiptResponseDTO createReceipt(CreateReceiptRequestDTO request, UUID createdBy) {
        // Validate client exists
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new BusinessException("Client not found"));

        // Validate payment method details
        validationService.validatePaymentMethodDetails(
                request.getPaymentMethod(),
                request.getCheckNumber(),
                request.getBankName(),
                request.getReferenceNumber()
        );

        // Generate receipt number
        String receiptNumber = generateReceiptNumber();

        // Create receipt entity
        Receipt receipt = Receipt.builder()
                .receiptNumber(receiptNumber)
                .clientId(request.getClientId())
                .issueDate(request.getIssueDate())
                .totalAmount(request.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .currency(request.getCurrency() != null ? request.getCurrency() : "DOP")
                .checkNumber(request.getCheckNumber())
                .bankName(request.getBankName())
                .referenceNumber(request.getReferenceNumber())
                .status(Receipt.ReceiptStatus.ACTIVE)
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();

        // Save receipt
        Receipt savedReceipt = receiptRepository.save(receipt);

        // Process allocations if provided
        if (request.getAllocations() != null && !request.getAllocations().isEmpty()) {
            allocatePayment(savedReceipt.getId(), request.getAllocations(), createdBy);
        }

        // Load full receipt for response
        return getReceipt(savedReceipt.getId());
    }

    @Override
    @Transactional
    public ReceiptResponseDTO updateReceipt(UUID id, UpdateReceiptRequestDTO request, UUID updatedBy) {
        Receipt existingReceipt = receiptRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Receipt not found"));

        if (existingReceipt.getStatus() == Receipt.ReceiptStatus.VOIDED) {
            throw new BusinessException("Cannot update a voided receipt");
        }

        // Validate payment method details
        validationService.validatePaymentMethodDetails(
                request.getPaymentMethod(),
                request.getCheckNumber(),
                request.getBankName(),
                request.getReferenceNumber()
        );

        // Update receipt fields
        existingReceipt.setIssueDate(request.getIssueDate());
        existingReceipt.setTotalAmount(request.getTotalAmount());
        existingReceipt.setPaymentMethod(request.getPaymentMethod());
        existingReceipt.setCurrency(request.getCurrency());
        existingReceipt.setCheckNumber(request.getCheckNumber());
        existingReceipt.setBankName(request.getBankName());
        existingReceipt.setReferenceNumber(request.getReferenceNumber());
        existingReceipt.setStatus(request.getStatus());
        existingReceipt.setNotes(request.getNotes());
        existingReceipt.setUpdatedBy(updatedBy);

        // Update receipt
        Receipt updatedReceipt = receiptRepository.update(existingReceipt);

        log.info("Receipt updated: {}", updatedReceipt.getReceiptNumber());
        return getReceipt(updatedReceipt.getId());
    }

    @Override
    public ReceiptResponseDTO getReceipt(UUID id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Receipt not found"));

        // Load related data
        Client client = clientRepository.findById(receipt.getClientId()).orElse(null);
        List<ReceiptAllocation> allocations = receiptAllocationRepository.findByReceiptId(id);

        // Load invoices for allocations
        for (ReceiptAllocation allocation : allocations) {
            Invoice invoice = invoiceRepository.findById(allocation.getInvoiceId()).orElse(null);
            if (invoice != null && client != null) {
                invoice.setClient(client);
            }
            allocation.setInvoice(invoice);
        }

        receipt.setClient(client);
        receipt.setAllocations(allocations);

        return convertToResponseDTO(receipt);
    }

    @Override
    public PagedResponseDTO<ReceiptResponseDTO> searchReceipts(ReceiptSearchCriteria criteria) {
        // Create sort
        Sort.Direction direction = "ASC".equalsIgnoreCase(criteria.getSortDirection()) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, criteria.getSortBy());

        // Create pageable
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        // Execute search
        Page<Receipt> receiptPage = receiptRepository.findByFilters(
                criteria.getClientId(),
                criteria.getStatus(),
                criteria.getFromDate(),
                criteria.getToDate(),
                pageable
        );

        // Convert to DTOs with client information
        List<ReceiptResponseDTO> receiptDTOs = receiptPage.getContent().stream()
                .map(receipt -> {
                    Client client = clientRepository.findById(receipt.getClientId()).orElse(null);
                    receipt.setClient(client);

                    List<ReceiptAllocation> allocations = receiptAllocationRepository.findByReceiptId(receipt.getId());
                    receipt.setAllocations(allocations);

                    return convertToResponseDTO(receipt);
                })
                .collect(Collectors.toList());

        return PagedResponseDTO.<ReceiptResponseDTO>builder()
                .content(receiptDTOs)
                .page(receiptPage.getNumber())
                .size(receiptPage.getSize())
                .totalElements(receiptPage.getTotalElements())
                .totalPages(receiptPage.getTotalPages())
                .first(receiptPage.isFirst())
                .last(receiptPage.isLast())
                .hasNext(receiptPage.hasNext())
                .hasPrevious(receiptPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public void voidReceipt(UUID id, String reason, UUID updatedBy) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Receipt not found"));

        if (receipt.getStatus() == Receipt.ReceiptStatus.VOIDED) {
            throw new BusinessException("Receipt is already voided");
        }

        // Remove all allocations and update invoice payment status
        List<ReceiptAllocation> allocations = receiptAllocationRepository.findByReceiptId(id);
        for (ReceiptAllocation allocation : allocations) {
            // Update invoice paid amount
            Invoice invoice = invoiceRepository.findById(allocation.getInvoiceId())
                    .orElseThrow(() -> new BusinessException("Invoice not found"));

            BigDecimal newPaidAmount = invoice.getPaidAmount().subtract(allocation.getAllocatedAmount());
            BigDecimal newBalance = invoice.getTotalAmount().subtract(newPaidAmount);

            invoice.setPaidAmount(newPaidAmount);
            invoice.setBalanceDue(newBalance);

            invoiceRepository.update(invoice);
            invoiceService.updateInvoicePaymentStatus(invoice.getId());

            // Delete allocation
            receiptAllocationRepository.deleteById(allocation.getId());
        }

        // Update receipt status
        receipt.setStatus(Receipt.ReceiptStatus.VOIDED);
        receipt.setNotes(receipt.getNotes() != null ?
                receipt.getNotes() + "\n\nVoided: " + reason : "Voided: " + reason);
        receipt.setUpdatedBy(updatedBy);

        receiptRepository.update(receipt);
        log.info("Receipt voided: {} - {}", receipt.getReceiptNumber(), reason);
    }

    @Override
    @Transactional
    public void allocatePayment(UUID receiptId, List<PaymentAllocationDTO> allocations, UUID createdBy) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new BusinessException("Receipt not found"));

        if (receipt.getStatus() != Receipt.ReceiptStatus.ACTIVE) {
            throw new BusinessException("Can only allocate payments for active receipts");
        }

        // Validate total allocation doesn't exceed receipt amount
        BigDecimal totalRequested = allocations.stream()
                .map(PaymentAllocationDTO::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentlyAllocated = receiptAllocationRepository.getTotalAllocatedByReceiptId(receiptId);
        BigDecimal available = receipt.getTotalAmount().subtract(currentlyAllocated);

        if (totalRequested.compareTo(available) > 0) {
            throw new BusinessException(String.format(
                    "Cannot allocate %.2f. Only %.2f available for allocation.",
                    totalRequested, available
            ));
        }

        // Process each allocation
        for (PaymentAllocationDTO allocationDto : allocations) {
            Invoice invoice = invoiceRepository.findById(allocationDto.getInvoiceId())
                    .orElseThrow(() -> new BusinessException("Invoice not found"));

            // Validate invoice belongs to same client
            if (!invoice.getClientId().equals(receipt.getClientId())) {
                throw new BusinessException("Invoice must belong to the same client as the receipt");
            }

            // Check if invoice needs payment
            if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Invoice " + invoice.getInvoiceNumber() + " is already fully paid");
            }

            // Don't allow overpayment of invoice
            if (allocationDto.getAllocatedAmount().compareTo(invoice.getBalanceDue()) > 0) {
                throw new BusinessException(String.format(
                        "Cannot allocate %.2f to invoice %s. Maximum allocation is %.2f",
                        allocationDto.getAllocatedAmount(), invoice.getInvoiceNumber(), invoice.getBalanceDue()
                ));
            }

            // Create allocation record
            ReceiptAllocation allocation = ReceiptAllocation.builder()
                    .receiptId(receiptId)
                    .invoiceId(allocationDto.getInvoiceId())
                    .allocatedAmount(allocationDto.getAllocatedAmount())
                    .createdBy(createdBy)
                    .build();

            receiptAllocationRepository.save(allocation);

            // Update invoice payment status
            BigDecimal newPaidAmount = invoice.getPaidAmount().add(allocationDto.getAllocatedAmount());
            BigDecimal newBalance = invoice.getTotalAmount().subtract(newPaidAmount);

            invoice.setPaidAmount(newPaidAmount);
            invoice.setBalanceDue(newBalance);

            invoiceRepository.update(invoice);
            invoiceService.updateInvoicePaymentStatus(invoice.getId());
        }

        log.info("Payment allocated for receipt: {} to {} invoices", receipt.getReceiptNumber(), allocations.size());
    }

    @Override
    public BigDecimal getAvailableAmount(UUID receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new BusinessException("Receipt not found"));

        BigDecimal allocated = receiptAllocationRepository.getTotalAllocatedByReceiptId(receiptId);
        return receipt.getTotalAmount().subtract(allocated != null ? allocated : BigDecimal.ZERO);
    }

    @Override
    public List<ReceiptResponseDTO> getClientReceipts(UUID clientId) {
        List<Receipt> receipts = receiptRepository.findByClientId(clientId);
        Client client = clientRepository.findById(clientId).orElse(null);

        return receipts.stream()
                .map(receipt -> {
                    receipt.setClient(client);
                    List<ReceiptAllocation> allocations = receiptAllocationRepository.findByReceiptId(receipt.getId());
                    receipt.setAllocations(allocations);
                    return convertToResponseDTO(receipt);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public String generateReceiptNumber() {
        int currentYear = LocalDate.now().getYear();

        ReceiptSequence sequence = sequenceRepository.findReceiptSequenceByYear(currentYear)
                .orElseGet(() -> {
                    ReceiptSequence newSequence = ReceiptSequence.builder()
                            .currentNumber(0)
                            .prefix("REC")
                            .year(currentYear)
                            .build();
                    return sequenceRepository.saveReceiptSequence(newSequence);
                });

        sequence.setCurrentNumber(sequence.getCurrentNumber() + 1);
        sequenceRepository.updateReceiptSequence(sequence);

        // Format: REC-2024-0001
        return String.format("%s-%d-%04d", sequence.getPrefix(), currentYear, sequence.getCurrentNumber());
    }

    private ReceiptResponseDTO convertToResponseDTO(Receipt receipt) {
        ReceiptResponseDTO dto = ReceiptResponseDTO.fromEntity(receipt);

        if (receipt.getAllocations() != null) {
            List<ReceiptAllocationResponseDTO> allocationDTOs = receipt.getAllocations().stream()
                    .map(ReceiptAllocationResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            dto.setAllocations(allocationDTOs);

            BigDecimal totalAllocated = receipt.getAllocations().stream()
                    .map(ReceiptAllocation::getAllocatedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dto.setAllocatedAmount(totalAllocated);
            dto.setAvailableAmount(receipt.getTotalAmount().subtract(totalAllocated));
        } else {
            dto.setAllocatedAmount(BigDecimal.ZERO);
            dto.setAvailableAmount(receipt.getTotalAmount());
        }

        return dto;
    }
}