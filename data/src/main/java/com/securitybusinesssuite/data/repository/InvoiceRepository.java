// path: data/src/main/java/com/securitybusinesssuite/data/repository/InvoiceRepository.java
package com.securitybusinesssuite.data.repository;

import com.securitybusinesssuite.data.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Invoice update(Invoice invoice);
    Optional<Invoice> findById(UUID id);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByNcf(String ncf);
    List<Invoice> findByClientId(UUID clientId);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    List<Invoice> findByStatusAndDueDateBefore(Invoice.InvoiceStatus status, LocalDate date);
    List<Invoice> findOverdueInvoices();
    Page<Invoice> findAll(Pageable pageable);
    Page<Invoice> findByFilters(UUID clientId, Invoice.InvoiceStatus status,
                                LocalDate fromDate, LocalDate toDate,
                                BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    boolean existsByInvoiceNumber(String invoiceNumber);
    boolean existsByNcf(String ncf);
    BigDecimal getTotalByClientAndStatus(UUID clientId, Invoice.InvoiceStatus status);
    long countByStatus(Invoice.InvoiceStatus status);
    void deleteById(UUID id);
}