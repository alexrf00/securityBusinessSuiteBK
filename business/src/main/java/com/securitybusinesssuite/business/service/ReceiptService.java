// path: business/src/main/java/com/securitybusinesssuite/business/service/ReceiptService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.receiptrequest.CreateReceiptRequestDTO;
import com.securitybusinesssuite.business.dto.receiptrequest.PaymentAllocationDTO;
import com.securitybusinesssuite.business.dto.receiptrequest.UpdateReceiptRequestDTO;
import com.securitybusinesssuite.business.dto.receiptresponse.ReceiptResponseDTO;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;
import com.securitybusinesssuite.business.dto.search.ReceiptSearchCriteria;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ReceiptService {
    ReceiptResponseDTO createReceipt(CreateReceiptRequestDTO request, UUID createdBy);
    ReceiptResponseDTO updateReceipt(UUID id, UpdateReceiptRequestDTO request, UUID updatedBy);
    ReceiptResponseDTO getReceipt(UUID id);
    PagedResponseDTO<ReceiptResponseDTO> searchReceipts(ReceiptSearchCriteria criteria);
    void voidReceipt(UUID id, String reason, UUID updatedBy);
    void allocatePayment(UUID receiptId, List<PaymentAllocationDTO> allocations, UUID createdBy);
    BigDecimal getAvailableAmount(UUID receiptId);
    List<ReceiptResponseDTO> getClientReceipts(UUID clientId);
}