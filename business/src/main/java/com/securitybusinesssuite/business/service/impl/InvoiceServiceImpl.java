// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/InvoiceServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.config.DominicanTaxConfig;
import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.invoicerequest.CreateInvoiceRequestDTO;
import com.securitybusinesssuite.business.dto.invoicerequest.InvoiceItemRequestDTO;
import com.securitybusinesssuite.business.dto.invoicerequest.UpdateInvoiceRequestDTO;
import com.securitybusinesssuite.business.dto.invoiceresponse.InvoiceItemResponseDTO;
import com.securitybusinesssuite.business.dto.invoiceresponse.InvoiceResponseDTO;
import com.securitybusinesssuite.business.dto.search.InvoiceSearchCriteria;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;
import com.securitybusinesssuite.business.exception.BusinessException;
import com.securitybusinesssuite.business.service.*;
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
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ClientRepository clientRepository;
    private final SequenceRepository sequenceRepository;
    private final CalculationService calculationService;
    private final NCFService ncfService;
    private final ValidationService validationService;
    private final DominicanTaxConfig taxConfig;

    @Override
    @Transactional
    public InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO request, UUID createdBy) {
        // Validate client exists
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new BusinessException("Client not found"));

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Generate NCF if required
        String ncf = null;
        Invoice.NCFType ncfType = null;
        if (ncfService.isNCFRequired(client)) {
            ncf = ncfService.generateNCF(client);
            ncfType = ncfService.determineNCFType(client);
        }

        // Create invoice entity
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .ncf(ncf)
                .ncfType(ncfType)
                .clientId(request.getClientId())
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .invoiceType(request.getInvoiceType())
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .status(Invoice.InvoiceStatus.PENDING)
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();

        // Create invoice items
        List<InvoiceItem> items = request.getItems().stream()
                .map(itemDto -> createInvoiceItem(itemDto, createdBy))
                .collect(Collectors.toList());

        // Calculate totals
        invoice = calculationService.calculateInvoiceTotals(invoice, items);

        // Validate invoice
        validationService.validateInvoiceRequirements(invoice, client);

        // Save invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Save invoice items
        for (InvoiceItem item : items) {
            item.setInvoiceId(savedInvoice.getId());
            item = calculationService.calculateLineItem(item);
            invoiceItemRepository.save(item);
        }

        // Load full invoice with items for response
        savedInvoice.setClient(client);
        savedInvoice.setItems(invoiceItemRepository.findByInvoiceId(savedInvoice.getId()));

        log.info("Invoice created: {} for client: {}", savedInvoice.getInvoiceNumber(), client.getBusinessName());
        return convertToResponseDTO(savedInvoice);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO updateInvoice(UUID id, UpdateInvoiceRequestDTO request, UUID updatedBy) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Invoice not found"));

        if (existingInvoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BusinessException("Cannot update a paid invoice");
        }

        if (existingInvoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new BusinessException("Cannot update a cancelled invoice");
        }

        Client client = clientRepository.findById(existingInvoice.getClientId())
                .orElseThrow(() -> new BusinessException("Client not found"));

        // Update invoice fields
        existingInvoice.setDueDate(request.getDueDate());
        existingInvoice.setInvoiceType(request.getInvoiceType());
        existingInvoice.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        existingInvoice.setNotes(request.getNotes());
        existingInvoice.setUpdatedBy(updatedBy);

        // Update invoice items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Delete existing items
            invoiceItemRepository.deleteByInvoiceId(id);

            // Create new items
            List<InvoiceItem> items = request.getItems().stream()
                    .map(itemDto -> {
                        InvoiceItem item = createInvoiceItem(itemDto, updatedBy);
                        item.setInvoiceId(id);
                        item = calculationService.calculateLineItem(item);
                        return invoiceItemRepository.save(item);
                    })
                    .collect(Collectors.toList());

            // Recalculate totals
            existingInvoice = calculationService.calculateInvoiceTotals(existingInvoice, items);
        }

        // Validate updated invoice
        validationService.validateInvoiceRequirements(existingInvoice, client);

        // Update invoice
        Invoice updatedInvoice = invoiceRepository.update(existingInvoice);
        updatedInvoice.setClient(client);
        updatedInvoice.setItems(invoiceItemRepository.findByInvoiceId(id));

        log.info("Invoice updated: {}", updatedInvoice.getInvoiceNumber());
        return convertToResponseDTO(updatedInvoice);
    }

    @Override
    public InvoiceResponseDTO getInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Invoice not found"));

        // Load related data
        Client client = clientRepository.findById(invoice.getClientId())
                .orElseThrow(() -> new BusinessException("Client not found"));
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);

        invoice.setClient(client);
        invoice.setItems(items);

        return convertToResponseDTO(invoice);
    }

    @Override
    public PagedResponseDTO<InvoiceResponseDTO> searchInvoices(InvoiceSearchCriteria criteria) {
        // Create sort
        Sort.Direction direction = "ASC".equalsIgnoreCase(criteria.getSortDirection()) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, criteria.getSortBy());

        // Create pageable
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        // Execute search
        Page<Invoice> invoicePage = invoiceRepository.findByFilters(
                criteria.getClientId(),
                criteria.getStatus(),
                criteria.getFromDate(),
                criteria.getToDate(),
                criteria.getMinAmount(),
                criteria.getMaxAmount(),
                pageable
        );

        // Convert to DTOs with client information
        List<InvoiceResponseDTO> invoiceDTOs = invoicePage.getContent().stream()
                .map(invoice -> {
                    Client client = clientRepository.findById(invoice.getClientId()).orElse(null);
                    invoice.setClient(client);
                    return convertToResponseDTO(invoice);
                })
                .collect(Collectors.toList());

        return PagedResponseDTO.<InvoiceResponseDTO>builder()
                .content(invoiceDTOs)
                .page(invoicePage.getNumber())
                .size(invoicePage.getSize())
                .totalElements(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .first(invoicePage.isFirst())
                .last(invoicePage.isLast())
                .hasNext(invoicePage.hasNext())
                .hasPrevious(invoicePage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public void cancelInvoice(UUID id, String reason, UUID updatedBy) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Invoice not found"));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BusinessException("Cannot cancel a paid invoice");
        }

        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new BusinessException("Invoice is already cancelled");
        }

        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
        invoice.setNotes(invoice.getNotes() != null ?
                invoice.getNotes() + "\n\nCancelled: " + reason : "Cancelled: " + reason);
        invoice.setUpdatedBy(updatedBy);

        invoiceRepository.update(invoice);
        log.info("Invoice cancelled: {} - {}", invoice.getInvoiceNumber(), reason);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO recalculateInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Invoice not found"));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BusinessException("Cannot recalculate a paid invoice");
        }

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);

        // Recalculate line items
        items = items.stream()
                .map(calculationService::calculateLineItem)
                .map(invoiceItemRepository::update)
                .collect(Collectors.toList());

        // Recalculate invoice totals
        invoice = calculationService.calculateInvoiceTotals(invoice, items);
        invoice = invoiceRepository.update(invoice);

        // Load related data for response
        Client client = clientRepository.findById(invoice.getClientId()).orElse(null);
        invoice.setClient(client);
        invoice.setItems(items);

        log.info("Invoice recalculated: {}", invoice.getInvoiceNumber());
        return convertToResponseDTO(invoice);
    }

    @Override
    public List<InvoiceResponseDTO> getOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices();
        return overdueInvoices.stream()
                .map(invoice -> {
                    Client client = clientRepository.findById(invoice.getClientId()).orElse(null);
                    invoice.setClient(client);
                    return convertToResponseDTO(invoice);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponseDTO> getClientInvoices(UUID clientId) {
        List<Invoice> invoices = invoiceRepository.findByClientId(clientId);
        Client client = clientRepository.findById(clientId).orElse(null);

        return invoices.stream()
                .map(invoice -> {
                    invoice.setClient(client);
                    return convertToResponseDTO(invoice);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponseDTO> getUnpaidInvoices(UUID clientId) {
        List<Invoice> unpaidInvoices = invoiceRepository.findByClientId(clientId).stream()
                .filter(invoice -> invoice.getStatus() == Invoice.InvoiceStatus.PENDING ||
                        invoice.getStatus() == Invoice.InvoiceStatus.OVERDUE)
                .filter(invoice -> invoice.getBalanceDue() != null &&
                        invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        Client client = clientRepository.findById(clientId).orElse(null);

        return unpaidInvoices.stream()
                .map(invoice -> {
                    invoice.setClient(client);
                    return convertToResponseDTO(invoice);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInvoicePaymentStatus(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Invoice not found"));

        // Determine new status based on payment
        Invoice.InvoiceStatus newStatus;
        if (invoice.getBalanceDue() == null || invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            newStatus = Invoice.InvoiceStatus.PAID;
        } else if (invoice.getDueDate().isBefore(LocalDate.now())) {
            newStatus = Invoice.InvoiceStatus.OVERDUE;
        } else {
            newStatus = Invoice.InvoiceStatus.PENDING;
        }

        if (invoice.getStatus() != newStatus) {
            invoice.setStatus(newStatus);
            invoiceRepository.update(invoice);
            log.info("Invoice status updated: {} -> {}", invoice.getInvoiceNumber(), newStatus);
        }
    }

    @Transactional
    public String generateInvoiceNumber() {
        int currentYear = LocalDate.now().getYear();

        InvoiceSequence sequence = sequenceRepository.findInvoiceSequenceByYear(currentYear)
                .orElseGet(() -> {
                    InvoiceSequence newSequence = InvoiceSequence.builder()
                            .currentNumber(0)
                            .year(currentYear)
                            .build();
                    return sequenceRepository.saveInvoiceSequence(newSequence);
                });

        sequence.setCurrentNumber(sequence.getCurrentNumber() + 1);
        sequenceRepository.updateInvoiceSequence(sequence);

        // Format: INV-2024-0001
        return String.format("INV-%d-%04d", currentYear, sequence.getCurrentNumber());
    }

    private InvoiceItem createInvoiceItem(InvoiceItemRequestDTO dto, UUID createdBy) {
        return InvoiceItem.builder()
                .serviceCode(dto.getServiceCode() != null ? dto.getServiceCode() : taxConfig.getDefaultServiceCode())
                .description(dto.getDescription() != null ? dto.getDescription() : taxConfig.getDefaultServiceDescription())
                .serviceType(dto.getServiceType())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .discountPercentage(dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : BigDecimal.ZERO)
                .itbisRate(taxConfig.getItbisRate())
                .createdBy(createdBy)
                .build();
    }

    private InvoiceResponseDTO convertToResponseDTO(Invoice invoice) {
        InvoiceResponseDTO dto = InvoiceResponseDTO.fromEntity(invoice);

        if (invoice.getItems() != null) {
            List<InvoiceItemResponseDTO> itemDTOs = invoice.getItems().stream()
                    .map(InvoiceItemResponseDTO::fromEntity)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }
}