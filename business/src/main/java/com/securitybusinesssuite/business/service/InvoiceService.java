// path: business/src/main/java/com/securitybusinesssuite/business/service/InvoiceService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.business.dto.invoicerequest.CreateInvoiceRequestDTO;
import com.securitybusinesssuite.business.dto.invoicerequest.UpdateInvoiceRequestDTO;
import com.securitybusinesssuite.business.dto.invoiceresponse.InvoiceResponseDTO;
import com.securitybusinesssuite.business.dto.search.InvoiceSearchCriteria;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO request, UUID createdBy);
    InvoiceResponseDTO updateInvoice(UUID id, UpdateInvoiceRequestDTO request, UUID updatedBy);
    InvoiceResponseDTO getInvoice(UUID id);
    PagedResponseDTO<InvoiceResponseDTO> searchInvoices(InvoiceSearchCriteria criteria);
    void cancelInvoice(UUID id, String reason, UUID updatedBy);
    InvoiceResponseDTO recalculateInvoice(UUID id);
    List<InvoiceResponseDTO> getOverdueInvoices();
    List<InvoiceResponseDTO> getClientInvoices(UUID clientId);
    List<InvoiceResponseDTO> getUnpaidInvoices(UUID clientId);
    void updateInvoicePaymentStatus(UUID invoiceId);
}
