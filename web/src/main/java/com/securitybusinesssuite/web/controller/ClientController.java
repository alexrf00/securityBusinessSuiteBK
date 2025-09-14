// path: web/src/main/java/com/securitybusinesssuite/web/controller/ClientController.java
package com.securitybusinesssuite.web.controller;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.clientrequest.CreateClientRequestDTO;
import com.securitybusinesssuite.business.dto.clientrequest.UpdateClientRequestDTO;
import com.securitybusinesssuite.business.dto.invoiceresponse.InvoiceResponseDTO;
import com.securitybusinesssuite.business.dto.receiptresponse.ReceiptResponseDTO;
import com.securitybusinesssuite.business.dto.search.ClientSearchCriteria;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;
import com.securitybusinesssuite.business.service.ClientService;
import com.securitybusinesssuite.business.service.InvoiceService;
import com.securitybusinesssuite.business.service.ReceiptService;
import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.web.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final InvoiceService invoiceService;
    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(
            @Valid @RequestBody CreateClientRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ClientResponseDTO client = clientService.createClient(request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClient(@PathVariable UUID id) {
        ClientResponseDTO client = clientService.getClient(id);
        return ResponseEntity.ok(client);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ClientResponseDTO client = clientService.updateClient(id, request, UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(client);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(Map.of("message", "Client deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<PagedResponseDTO<ClientResponseDTO>> searchClients(
            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String clientType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String businessSector,
            @RequestParam(required = false) String rnc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        ClientSearchCriteria criteria = new ClientSearchCriteria();
        criteria.setBusinessName(businessName);
        criteria.setClientType(clientType != null ? Client.ClientType.valueOf(clientType.toUpperCase()) : null);
        criteria.setStatus(status != null ? Client.ClientStatus.valueOf(status.toUpperCase()) : null);
        criteria.setBusinessSector(businessSector != null ? Client.BusinessSector.valueOf(businessSector.toUpperCase()) : null);
        criteria.setRnc(rnc);
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setSortBy(sortBy);
        criteria.setSortDirection(sortDirection);

        PagedResponseDTO<ClientResponseDTO> result = clientService.searchClients(criteria);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getClientInvoices(@PathVariable UUID id) {
        List<InvoiceResponseDTO> invoices = invoiceService.getClientInvoices(id);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}/invoices/unpaid")
    public ResponseEntity<List<InvoiceResponseDTO>> getClientUnpaidInvoices(@PathVariable UUID id) {
        List<InvoiceResponseDTO> invoices = invoiceService.getUnpaidInvoices(id);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}/receipts")
    public ResponseEntity<List<ReceiptResponseDTO>> getClientReceipts(@PathVariable UUID id) {
        List<ReceiptResponseDTO> receipts = receiptService.getClientReceipts(id);
        return ResponseEntity.ok(receipts);
    }

    @PostMapping("/validate-rnc")
    public ResponseEntity<Map<String, Object>> validateRNC(@RequestBody Map<String, String> request) {
        String rnc = request.get("rnc");

        if (rnc == null || rnc.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("valid", true, "message", "RNC is optional"));
        }

        boolean exists = clientService.existsByRnc(rnc);
        if (exists) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "A client with this RNC already exists"
            ));
        }

        // Validate format
        if (!rnc.matches("\\d{3}-\\d{7}-\\d{1}")) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "RNC must be in format XXX-XXXXXXX-X"
            ));
        }

        return ResponseEntity.ok(Map.of("valid", true, "message", "RNC is valid"));
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, String>> exportClients(
            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String clientType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String businessSector) {

        // TODO: Implement export functionality (CSV, Excel)
        return ResponseEntity.ok(Map.of("message", "Export functionality not implemented yet"));
    }
}