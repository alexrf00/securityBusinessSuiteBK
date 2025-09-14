// path: data/src/main/java/com/securitybusinesssuite/data/repository/impl/ReceiptRepositoryImpl.java
package com.securitybusinesssuite.data.repository.impl;

import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Receipt;
import com.securitybusinesssuite.data.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReceiptRepositoryImpl implements ReceiptRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_RECEIPT = """
        INSERT INTO receipts (id, receipt_number, client_id, issue_date, total_amount, payment_method,
                             currency, check_number, bank_name, reference_number, status, notes,
                             created_at, updated_at, created_by, updated_by)
        VALUES (?, ?, ?, ?, ?, ?::payment_method_enum, ?, ?, ?, ?, ?::receipt_status_enum, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_RECEIPT = """
        UPDATE receipts SET receipt_number = ?, client_id = ?, issue_date = ?, total_amount = ?,
                           payment_method = ?::payment_method_enum, currency = ?, check_number = ?,
                           bank_name = ?, reference_number = ?, status = ?::receipt_status_enum,
                           notes = ?, updated_at = ?, updated_by = ?
        WHERE id = ?
        """;

    private static final String SELECT_BASE = """
        SELECT id, receipt_number, client_id, issue_date, total_amount, payment_method, currency,
               check_number, bank_name, reference_number, status, notes, created_at, updated_at,
               created_by, updated_by
        FROM receipts
        """;

    private static final String SELECT_BY_ID = SELECT_BASE + " WHERE id = ?";
    private static final String SELECT_BY_RECEIPT_NUMBER = SELECT_BASE + " WHERE receipt_number = ?";
    private static final String SELECT_BY_CLIENT_ID = SELECT_BASE + " WHERE client_id = ?";
    private static final String SELECT_BY_STATUS = SELECT_BASE + " WHERE status = ?::receipt_status_enum";

    private static final String EXISTS_BY_RECEIPT_NUMBER = "SELECT EXISTS(SELECT 1 FROM receipts WHERE receipt_number = ?)";
    private static final String SUM_BY_CLIENT_ID = "SELECT COALESCE(SUM(total_amount), 0) FROM receipts WHERE client_id = ? AND status = 'ACTIVE'";
    private static final String DELETE_BY_ID = "DELETE FROM receipts WHERE id = ?";

    private final ReceiptRowMapper receiptRowMapper = new ReceiptRowMapper();

    @Override
    public Receipt save(Receipt receipt) {
        receipt.setId(UUID.randomUUID());
        receipt.setCreatedAt(LocalDateTime.now());
        receipt.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(INSERT_RECEIPT,
                receipt.getId(),
                receipt.getReceiptNumber(),
                receipt.getClientId(),
                receipt.getIssueDate(),
                receipt.getTotalAmount(),
                receipt.getPaymentMethod() != null ? receipt.getPaymentMethod().name() : null,
                receipt.getCurrency(),
                receipt.getCheckNumber(),
                receipt.getBankName(),
                receipt.getReferenceNumber(),
                receipt.getStatus() != null ? receipt.getStatus().name() : null,
                receipt.getNotes(),
                Timestamp.valueOf(receipt.getCreatedAt()),
                Timestamp.valueOf(receipt.getUpdatedAt()),
                receipt.getCreatedBy(),
                receipt.getUpdatedBy()
        );

        return receipt;
    }

    @Override
    public Receipt update(Receipt receipt) {
        receipt.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(UPDATE_RECEIPT,
                receipt.getReceiptNumber(),
                receipt.getClientId(),
                receipt.getIssueDate(),
                receipt.getTotalAmount(),
                receipt.getPaymentMethod() != null ? receipt.getPaymentMethod().name() : null,
                receipt.getCurrency(),
                receipt.getCheckNumber(),
                receipt.getBankName(),
                receipt.getReferenceNumber(),
                receipt.getStatus() != null ? receipt.getStatus().name() : null,
                receipt.getNotes(),
                Timestamp.valueOf(receipt.getUpdatedAt()),
                receipt.getUpdatedBy(),
                receipt.getId()
        );

        return receipt;
    }

    @Override
    public Optional<Receipt> findById(UUID id) {
        return jdbcTemplate.query(SELECT_BY_ID, receiptRowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<Receipt> findByReceiptNumber(String receiptNumber) {
        return jdbcTemplate.query(SELECT_BY_RECEIPT_NUMBER, receiptRowMapper, receiptNumber).stream().findFirst();
    }

    @Override
    public List<Receipt> findByClientId(UUID clientId) {
        return jdbcTemplate.query(SELECT_BY_CLIENT_ID, receiptRowMapper, clientId);
    }

    @Override
    public List<Receipt> findByStatus(Receipt.ReceiptStatus status) {
        return jdbcTemplate.query(SELECT_BY_STATUS, receiptRowMapper, status.name());
    }

    @Override
    public Page<Receipt> findAll(Pageable pageable) {
        String sql = SELECT_BASE + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Receipt> receipts = jdbcTemplate.query(sql, receiptRowMapper,
                pageable.getPageSize(), pageable.getOffset());

        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM receipts", Long.class);
        return new PageImpl<>(receipts, pageable, total);
    }

    @Override
    public Page<Receipt> findByFilters(UUID clientId, Receipt.ReceiptStatus status,
                                       LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (clientId != null) {
            sql.append(" AND client_id = ?");
            params.add(clientId);
        }

        if (status != null) {
            sql.append(" AND status = ?::receipt_status_enum");
            params.add(status.name());
        }

        if (fromDate != null) {
            sql.append(" AND issue_date >= ?");
            params.add(fromDate);
        }

        if (toDate != null) {
            sql.append(" AND issue_date <= ?");
            params.add(toDate);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        List<Receipt> receipts = jdbcTemplate.query(sql.toString(), receiptRowMapper, params.toArray());

        // Count query for total
        String countSql = sql.toString().replace(SELECT_BASE, "SELECT COUNT(*)").split(" ORDER BY")[0];
        long total = jdbcTemplate.queryForObject(countSql, Long.class,
                params.subList(0, params.size() - 2).toArray());

        return new PageImpl<>(receipts, pageable, total);
    }

    @Override
    public boolean existsByReceiptNumber(String receiptNumber) {
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_BY_RECEIPT_NUMBER, Boolean.class, receiptNumber);
        return exists != null && exists;
    }

    @Override
    public BigDecimal getTotalByClientId(UUID clientId) {
        return jdbcTemplate.queryForObject(SUM_BY_CLIENT_ID, BigDecimal.class, clientId);
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update(DELETE_BY_ID, id);
    }

    private static class ReceiptRowMapper implements RowMapper<Receipt> {
        @Override
        public Receipt mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Receipt.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .receiptNumber(rs.getString("receipt_number"))
                    .clientId(UUID.fromString(rs.getString("client_id")))
                    .issueDate(rs.getDate("issue_date") != null ?
                            rs.getDate("issue_date").toLocalDate() : null)
                    .totalAmount(rs.getBigDecimal("total_amount"))
                    .paymentMethod(rs.getString("payment_method") != null ?
                            Client.PaymentMethod.valueOf(rs.getString("payment_method")) : null)
                    .currency(rs.getString("currency"))
                    .checkNumber(rs.getString("check_number"))
                    .bankName(rs.getString("bank_name"))
                    .referenceNumber(rs.getString("reference_number"))
                    .status(rs.getString("status") != null ?
                            Receipt.ReceiptStatus.valueOf(rs.getString("status")) : null)
                    .notes(rs.getString("notes"))
                    .createdAt(rs.getTimestamp("created_at") != null ?
                            rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .updatedAt(rs.getTimestamp("updated_at") != null ?
                            rs.getTimestamp("updated_at").toLocalDateTime() : null)
                    .createdBy(rs.getString("created_by") != null ?
                            UUID.fromString(rs.getString("created_by")) : null)
                    .updatedBy(rs.getString("updated_by") != null ?
                            UUID.fromString(rs.getString("updated_by")) : null)
                    .build();
        }
    }
}