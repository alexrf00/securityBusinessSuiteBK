// path: data/src/main/java/com/securitybusinesssuite/data/repository/InvoiceItemRepository.java
package com.securitybusinesssuite.data.repository;

import com.securitybusinesssuite.data.entity.InvoiceItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceItemRepository {
    InvoiceItem save(InvoiceItem item);
    InvoiceItem update(InvoiceItem item);
    Optional<InvoiceItem> findById(UUID id);
    List<InvoiceItem> findByInvoiceId(UUID invoiceId);
    void deleteById(UUID id);
    void deleteByInvoiceId(UUID invoiceId);
}