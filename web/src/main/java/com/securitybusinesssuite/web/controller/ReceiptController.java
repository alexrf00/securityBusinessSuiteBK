// path: web/src/main/java/com/securitybusinesssuite/web/controller/ReceiptController.java
package com.securitybusinesssuite.web.controller;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.receiptrequest.CreateReceiptRequestDTO;
import com.securitybusinesssuite.business.dto.receiptrequest.PaymentAllocationDTO;
import com.securitybusinesssuite.business.dto.receiptrequest.UpdateReceiptRequestDTO;
import com.securitybusinesssuite.business.dto.receiptresponse.ReceiptAllocationResponseDTO;
import com.securitybusinesssuite.business.dto.receiptresponse.ReceiptResponseDTO;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;
import com.securitybusinesssuite.business.dto.search.ReceiptSearchCriteria;
import com.securitybusinesssuite.business.service.ReceiptService;
import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Receipt;
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
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<ReceiptResponseDTO> createReceipt(
            @Valid @RequestBody CreateReceiptRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ReceiptResponseDTO receipt = receiptService.createReceipt(request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptResponseDTO> getReceipt(@PathVariable UUID id) {
        ReceiptResponseDTO receipt = receiptService.getReceipt(id);
        return ResponseEntity.ok(receipt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiptResponseDTO> updateReceipt(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReceiptRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ReceiptResponseDTO receipt = receiptService.updateReceipt(id, request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(receipt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> voidReceipt(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal principal) {

        String reason = request != null ? request.get("reason") : "Voided by user";
        receiptService.voidReceipt(id, reason, UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(Map.of("message", "Receipt voided successfully"));
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<ReceiptResponseDTO>> searchReceipts(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String receiptNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        ReceiptSearchCriteria criteria = new ReceiptSearchCriteria();
        criteria.setClientId(clientId);
        criteria.setClientName(clientName);
        criteria.setStatus(status != null ? Receipt.ReceiptStatus.valueOf(status.toUpperCase()) : null);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);
        criteria.setPaymentMethod(paymentMethod != null ? Client.PaymentMethod.valueOf(paymentMethod.toUpperCase()) : null);
        criteria.setReceiptNumber(receiptNumber);
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setSortBy(sortBy);
        criteria.setSortDirection(sortDirection);

        PagedResponseDTO<ReceiptResponseDTO> result = receiptService.searchReceipts(criteria);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/allocate")
    public ResponseEntity<Map<String, String>> allocatePayment(
            @PathVariable UUID id,
            @Valid @RequestBody List<PaymentAllocationDTO> allocations,
            @AuthenticationPrincipal UserPrincipal principal) {

        receiptService.allocatePayment(id, allocations, UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(Map.of("message", "Payment allocated successfully"));
    }

    @GetMapping("/{id}/available-amount")
    public ResponseEntity<Map<String, BigDecimal>> getAvailableAmount(@PathVariable UUID id) {
        BigDecimal availableAmount = receiptService.getAvailableAmount(id);
        return ResponseEntity.ok(Map.of("availableAmount", availableAmount));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Map<String, String>> generateReceiptPDF(@PathVariable UUID id) {
        // TODO: Implement PDF generation
        return ResponseEntity.ok(Map.of(
                "message", "PDF generation not implemented yet",
                "receiptId", id.toString()
        ));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ReceiptResponseDTO> duplicateReceipt(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {

        // TODO: Implement receipt duplication
        throw new UnsupportedOperationException("Receipt duplication not implemented yet");
    }

    @GetMapping("/{id}/allocations")
    public ResponseEntity<List<ReceiptAllocationResponseDTO>> getReceiptAllocations(@PathVariable UUID id) {
        ReceiptResponseDTO receipt = receiptService.getReceipt(id);
        return ResponseEntity.ok(receipt.getAllocations());
    }

    @PostMapping("/bulk-void")
    public ResponseEntity<Map<String, Object>> bulkVoidReceipts(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal principal) {

        // TODO: Implement bulk void functionality
        return ResponseEntity.ok(Map.of(
                "message", "Bulk void not implemented yet"
        ));
    }
}