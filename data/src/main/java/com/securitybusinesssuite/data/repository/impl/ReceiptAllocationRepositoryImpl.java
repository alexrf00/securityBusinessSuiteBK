// path: data/src/main/java/com/securitybusinesssuite/data/repository/impl/ReceiptAllocationRepositoryImpl.java
package com.securitybusinesssuite.data.repository.impl;

import com.securitybusinesssuite.data.entity.ReceiptAllocation;
import com.securitybusinesssuite.data.repository.ReceiptAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReceiptAllocationRepositoryImpl implements ReceiptAllocationRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_ALLOCATION = """
        INSERT INTO receipt_allocations (id, receipt_id, invoice_id, allocated_amount, created_at, created_by)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String SELECT_BASE = """
        SELECT id, receipt_id, invoice_id, allocated_amount, created_at, created_by
        FROM receipt_allocations
        """;

    private static final String SELECT_BY_ID = SELECT_BASE + " WHERE id = ?";
    private static final String SELECT_BY_RECEIPT_ID = SELECT_BASE + " WHERE receipt_id = ?";
    private static final String SELECT_BY_INVOICE_ID = SELECT_BASE + " WHERE invoice_id = ?";

    private static final String SUM_BY_RECEIPT_ID = "SELECT COALESCE(SUM(allocated_amount), 0) FROM receipt_allocations WHERE receipt_id = ?";
    private static final String SUM_BY_INVOICE_ID = "SELECT COALESCE(SUM(allocated_amount), 0) FROM receipt_allocations WHERE invoice_id = ?";

    private static final String DELETE_BY_ID = "DELETE FROM receipt_allocations WHERE id = ?";
    private static final String DELETE_BY_RECEIPT_ID = "DELETE FROM receipt_allocations WHERE receipt_id = ?";

    private final ReceiptAllocationRowMapper allocationRowMapper = new ReceiptAllocationRowMapper();

    @Override
    public ReceiptAllocation save(ReceiptAllocation allocation) {
        allocation.setId(UUID.randomUUID());
        allocation.setCreatedAt(LocalDateTime.now());

        jdbcTemplate.update(INSERT_ALLOCATION,
                allocation.getId(),
                allocation.getReceiptId(),
                allocation.getInvoiceId(),
                allocation.getAllocatedAmount(),
                Timestamp.valueOf(allocation.getCreatedAt()),
                allocation.getCreatedBy()
        );

        return allocation;
    }

    @Override
    public Optional<ReceiptAllocation> findById(UUID id) {
        return jdbcTemplate.query(SELECT_BY_ID, allocationRowMapper, id).stream().findFirst();
    }

    @Override
    public List<ReceiptAllocation> findByReceiptId(UUID receiptId) {
        return jdbcTemplate.query(SELECT_BY_RECEIPT_ID, allocationRowMapper, receiptId);
    }

    @Override
    public List<ReceiptAllocation> findByInvoiceId(UUID invoiceId) {
        return jdbcTemplate.query(SELECT_BY_INVOICE_ID, allocationRowMapper, invoiceId);
    }

    @Override
    public BigDecimal getTotalAllocatedByReceiptId(UUID receiptId) {
        return jdbcTemplate.queryForObject(SUM_BY_RECEIPT_ID, BigDecimal.class, receiptId);
    }

    @Override
    public BigDecimal getTotalAllocatedByInvoiceId(UUID invoiceId) {
        return jdbcTemplate.queryForObject(SUM_BY_INVOICE_ID, BigDecimal.class, invoiceId);
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update(DELETE_BY_ID, id);
    }

    @Override
    public void deleteByReceiptId(UUID receiptId) {
        jdbcTemplate.update(DELETE_BY_RECEIPT_ID, receiptId);
    }

    private static class ReceiptAllocationRowMapper implements RowMapper<ReceiptAllocation> {
        @Override
        public ReceiptAllocation mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ReceiptAllocation.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .receiptId(UUID.fromString(rs.getString("receipt_id")))
                    .invoiceId(UUID.fromString(rs.getString("invoice_id")))
                    .allocatedAmount(rs.getBigDecimal("allocated_amount"))
                    .createdAt(rs.getTimestamp("created_at") != null ?
                            rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .createdBy(rs.getString("created_by") != null ?
                            UUID.fromString(rs.getString("created_by")) : null)
                    .build();
        }
    }
}