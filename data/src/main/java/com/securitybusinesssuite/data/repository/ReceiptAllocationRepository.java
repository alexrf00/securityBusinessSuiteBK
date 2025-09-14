// path: data/src/main/java/com/securitybusinesssuite/data/repository/ReceiptAllocationRepository.java
package com.securitybusinesssuite.data.repository;

import com.securitybusinesssuite.data.entity.ReceiptAllocation;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptAllocationRepository {
    ReceiptAllocation save(ReceiptAllocation allocation);
    Optional<ReceiptAllocation> findById(UUID id);
    List<ReceiptAllocation> findByReceiptId(UUID receiptId);
    List<ReceiptAllocation> findByInvoiceId(UUID invoiceId);
    BigDecimal getTotalAllocatedByReceiptId(UUID receiptId);
    BigDecimal getTotalAllocatedByInvoiceId(UUID invoiceId);
    void deleteById(UUID id);
    void deleteByReceiptId(UUID receiptId);
}
