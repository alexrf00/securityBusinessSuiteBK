// path: web/src/main/java/com/securitybusinesssuite/web/controller/InvoiceController.java
package com.securitybusinesssuite.web.controller;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.invoicerequest.CreateInvoiceRequestDTO;
import com.securitybusinesssuite.business.dto.invoicerequest.UpdateInvoiceRequestDTO;
import com.securitybusinesssuite.business.dto.invoiceresponse.InvoiceResponseDTO;
import com.securitybusinesssuite.business.dto.search.InvoiceSearchCriteria;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;
import com.securitybusinesssuite.business.service.InvoiceService;
import com.securitybusinesssuite.data.entity.Invoice;
import com.securitybusinesssuite.web.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(
            @Valid @RequestBody CreateInvoiceRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {

        InvoiceResponseDTO invoice = invoiceService.createInvoice(request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoice(@PathVariable UUID id) {
        InvoiceResponseDTO invoice = invoiceService.getInvoice(id);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvoiceRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {

        InvoiceResponseDTO invoice = invoiceService.updateInvoice(id, request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(invoice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelInvoice(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal) {

        String reason = request != null ? request.get("reason") : "Cancelled by user";
        invoiceService.cancelInvoice(id, reason, UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(Map.of("message", "Invoice cancelled successfully"));
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<InvoiceResponseDTO>> searchInvoices(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String ncf,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        InvoiceSearchCriteria criteria = new InvoiceSearchCriteria();
        criteria.setClientId(clientId);
        criteria.setClientName(clientName);
        criteria.setStatus(status != null ? Invoice.InvoiceStatus.valueOf(status.toUpperCase()) : null);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);
        criteria.setMinAmount(minAmount);
        criteria.setMaxAmount(maxAmount);
        criteria.setInvoiceNumber(invoiceNumber);
        criteria.setNcf(ncf);
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setSortBy(sortBy);
        criteria.setSortDirection(sortDirection);

        PagedResponseDTO<InvoiceResponseDTO> result = invoiceService.searchInvoices(criteria);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<InvoiceResponseDTO> recalculateInvoice(@PathVariable UUID id) {
        InvoiceResponseDTO invoice = invoiceService.recalculateInvoice(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceResponseDTO>> getOverdueInvoices() {
        List<InvoiceResponseDTO> invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Map<String, String>> generateInvoicePDF(@PathVariable UUID id) {
        // TODO: Implement PDF generation
        return ResponseEntity.ok(Map.of(
                "message", "PDF generation not implemented yet",
                "invoiceId", id.toString()
        ));
    }

    @PostMapping("/bulk-create")
    public ResponseEntity<Map<String, Object>> bulkCreateInvoices(
            @Valid @RequestBody List<CreateInvoiceRequestDTO> requests,
            @AuthenticationPrincipal UserPrincipal principal) {

        // TODO: Implement bulk invoice creation
        return ResponseEntity.ok(Map.of(
                "message", "Bulk creation not implemented yet",
                "count", requests.size()
        ));
    }

    @GetMapping("/{id}/payment-history")
    public ResponseEntity<Map<String, Object>> getInvoicePaymentHistory(@PathVariable UUID id) {
        // TODO: Implement payment history retrieval
        return ResponseEntity.ok(Map.of(
                "message", "Payment history not implemented yet",
                "invoiceId", id.toString()
        ));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<InvoiceResponseDTO> duplicateInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {

        // TODO: Implement invoice duplication
        throw new UnsupportedOperationException("Invoice duplication not implemented yet");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> updateInvoiceStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal) {

        String newStatus = request.get("status");
        // TODO: Implement manual status update with validation

        return ResponseEntity.ok(Map.of(
                "message", "Status update not implemented yet",
                "newStatus", newStatus
        ));
    }
}