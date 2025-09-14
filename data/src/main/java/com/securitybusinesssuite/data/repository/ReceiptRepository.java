// path: data/src/main/java/com/securitybusinesssuite/data/repository/ReceiptRepository.java
package com.securitybusinesssuite.data.repository;

import com.securitybusinesssuite.data.entity.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository {
    Receipt save(Receipt receipt);
    Receipt update(Receipt receipt);
    Optional<Receipt> findById(UUID id);
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
    List<Receipt> findByClientId(UUID clientId);
    List<Receipt> findByStatus(Receipt.ReceiptStatus status);
    Page<Receipt> findAll(Pageable pageable);
    Page<Receipt> findByFilters(UUID clientId, Receipt.ReceiptStatus status,
                                LocalDate fromDate, LocalDate toDate, Pageable pageable);
    boolean existsByReceiptNumber(String receiptNumber);
    BigDecimal getTotalByClientId(UUID clientId);
    void deleteById(UUID id);
}