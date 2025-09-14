// path: data/src/main/java/com/securitybusinesssuite/data/repository/impl/InvoiceRepositoryImpl.java
package com.securitybusinesssuite.data.repository.impl;

import com.securitybusinesssuite.data.entity.Invoice;
import com.securitybusinesssuite.data.repository.InvoiceRepository;
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
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_INVOICE = """
        INSERT INTO invoices (id, invoice_number, ncf, ncf_type, client_id, issue_date, due_date,
                             invoice_type, subtotal, itbis_amount, discount_amount, total_amount,
                             net_amount, paid_amount, balance_due, status, notes, dgii_track_id,
                             dgii_status, ecf_payload, qr_hash, created_at, updated_at, created_by, updated_by)
        VALUES (?, ?, ?, ?::ncf_type_enum, ?, ?, ?, ?::invoice_type_enum, ?, ?, ?, ?, ?, ?, ?, 
                ?::invoice_status_enum, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_INVOICE = """
        UPDATE invoices SET invoice_number = ?, ncf = ?, ncf_type = ?::ncf_type_enum, client_id = ?,
                           issue_date = ?, due_date = ?, invoice_type = ?::invoice_type_enum,
                           subtotal = ?, itbis_amount = ?, discount_amount = ?, total_amount = ?,
                           net_amount = ?, paid_amount = ?, balance_due = ?, status = ?::invoice_status_enum,
                           notes = ?, dgii_track_id = ?, dgii_status = ?, ecf_payload = ?, qr_hash = ?,
                           updated_at = ?, updated_by = ?
        WHERE id = ?
        """;

    private static final String SELECT_BASE = """
        SELECT id, invoice_number, ncf, ncf_type, client_id, issue_date, due_date, invoice_type,
               subtotal, itbis_amount, discount_amount, total_amount, net_amount, paid_amount,
               balance_due, status, notes, dgii_track_id, dgii_status, ecf_payload, qr_hash,
               created_at, updated_at, created_by, updated_by
        FROM invoices
        """;

    private static final String SELECT_BY_ID = SELECT_BASE + " WHERE id = ?";
    private static final String SELECT_BY_INVOICE_NUMBER = SELECT_BASE + " WHERE invoice_number = ?";
    private static final String SELECT_BY_NCF = SELECT_BASE + " WHERE ncf = ?";
    private static final String SELECT_BY_CLIENT_ID = SELECT_BASE + " WHERE client_id = ?";
    private static final String SELECT_BY_STATUS = SELECT_BASE + " WHERE status = ?::invoice_status_enum";
    private static final String SELECT_OVERDUE = SELECT_BASE + " WHERE status = 'PENDING' AND due_date < CURRENT_DATE";
    private static final String SELECT_BY_STATUS_AND_DUE_DATE = SELECT_BASE + " WHERE status = ?::invoice_status_enum AND due_date < ?";

    private static final String EXISTS_BY_INVOICE_NUMBER = "SELECT EXISTS(SELECT 1 FROM invoices WHERE invoice_number = ?)";
    private static final String EXISTS_BY_NCF = "SELECT EXISTS(SELECT 1 FROM invoices WHERE ncf = ?)";
    private static final String SUM_BY_CLIENT_AND_STATUS = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices WHERE client_id = ? AND status = ?::invoice_status_enum";
    private static final String COUNT_BY_STATUS = "SELECT COUNT(*) FROM invoices WHERE status = ?::invoice_status_enum";
    private static final String DELETE_BY_ID = "DELETE FROM invoices WHERE id = ?";

    private final InvoiceRowMapper invoiceRowMapper = new InvoiceRowMapper();

    @Override
    public Invoice save(Invoice invoice) {
        invoice.setId(UUID.randomUUID());
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(INSERT_INVOICE,
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getNcf(),
                invoice.getNcfType() != null ? invoice.getNcfType().name() : null,
                invoice.getClientId(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getInvoiceType() != null ? invoice.getInvoiceType().name() : null,
                invoice.getSubtotal(),
                invoice.getItbisAmount(),
                invoice.getDiscountAmount(),
                invoice.getTotalAmount(),
                invoice.getNetAmount(),
                invoice.getPaidAmount(),
                invoice.getBalanceDue(),
                invoice.getStatus() != null ? invoice.getStatus().name() : null,
                invoice.getNotes(),
                invoice.getDgiiTrackId(),
                invoice.getDgiiStatus(),
                invoice.getEcfPayload(),
                invoice.getQrHash(),
                Timestamp.valueOf(invoice.getCreatedAt()),
                Timestamp.valueOf(invoice.getUpdatedAt()),
                invoice.getCreatedBy(),
                invoice.getUpdatedBy()
        );

        return invoice;
    }

    @Override
    public Invoice update(Invoice invoice) {
        invoice.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(UPDATE_INVOICE,
                invoice.getInvoiceNumber(),
                invoice.getNcf(),
                invoice.getNcfType() != null ? invoice.getNcfType().name() : null,
                invoice.getClientId(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getInvoiceType() != null ? invoice.getInvoiceType().name() : null,
                invoice.getSubtotal(),
                invoice.getItbisAmount(),
                invoice.getDiscountAmount(),
                invoice.getTotalAmount(),
                invoice.getNetAmount(),
                invoice.getPaidAmount(),
                invoice.getBalanceDue(),
                invoice.getStatus() != null ? invoice.getStatus().name() : null,
                invoice.getNotes(),
                invoice.getDgiiTrackId(),
                invoice.getDgiiStatus(),
                invoice.getEcfPayload(),
                invoice.getQrHash(),
                Timestamp.valueOf(invoice.getUpdatedAt()),
                invoice.getUpdatedBy(),
                invoice.getId()
        );

        return invoice;
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return jdbcTemplate.query(SELECT_BY_ID, invoiceRowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return jdbcTemplate.query(SELECT_BY_INVOICE_NUMBER, invoiceRowMapper, invoiceNumber).stream().findFirst();
    }

    @Override
    public Optional<Invoice> findByNcf(String ncf) {
        return jdbcTemplate.query(SELECT_BY_NCF, invoiceRowMapper, ncf).stream().findFirst();
    }

    @Override
    public List<Invoice> findByClientId(UUID clientId) {
        return jdbcTemplate.query(SELECT_BY_CLIENT_ID, invoiceRowMapper, clientId);
    }

    @Override
    public List<Invoice> findByStatus(Invoice.InvoiceStatus status) {
        return jdbcTemplate.query(SELECT_BY_STATUS, invoiceRowMapper, status.name());
    }

    @Override
    public List<Invoice> findByStatusAndDueDateBefore(Invoice.InvoiceStatus status, LocalDate date) {
        return jdbcTemplate.query(SELECT_BY_STATUS_AND_DUE_DATE, invoiceRowMapper, status.name(), date);
    }

    @Override
    public List<Invoice> findOverdueInvoices() {
        return jdbcTemplate.query(SELECT_OVERDUE, invoiceRowMapper);
    }

    @Override
    public Page<Invoice> findAll(Pageable pageable) {
        String sql = SELECT_BASE + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Invoice> invoices = jdbcTemplate.query(sql, invoiceRowMapper,
                pageable.getPageSize(), pageable.getOffset());

        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM invoices", Long.class);
        return new PageImpl<>(invoices, pageable, total);
    }

    @Override
    public Page<Invoice> findByFilters(UUID clientId, Invoice.InvoiceStatus status,
                                       LocalDate fromDate, LocalDate toDate,
                                       BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (clientId != null) {
            sql.append(" AND client_id = ?");
            params.add(clientId);
        }

        if (status != null) {
            sql.append(" AND status = ?::invoice_status_enum");
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

        if (minAmount != null) {
            sql.append(" AND total_amount >= ?");
            params.add(minAmount);
        }

        if (maxAmount != null) {
            sql.append(" AND total_amount <= ?");
            params.add(maxAmount);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        List<Invoice> invoices = jdbcTemplate.query(sql.toString(), invoiceRowMapper, params.toArray());

        // Count query for total
        String countSql = sql.toString().replace(SELECT_BASE, "SELECT COUNT(*)").split(" ORDER BY")[0];
        long total = jdbcTemplate.queryForObject(countSql, Long.class,
                params.subList(0, params.size() - 2).toArray());

        return new PageImpl<>(invoices, pageable, total);
    }

    @Override
    public boolean existsByInvoiceNumber(String invoiceNumber) {
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_BY_INVOICE_NUMBER, Boolean.class, invoiceNumber);
        return exists != null && exists;
    }

    @Override
    public boolean existsByNcf(String ncf) {
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_BY_NCF, Boolean.class, ncf);
        return exists != null && exists;
    }

    @Override
    public BigDecimal getTotalByClientAndStatus(UUID clientId, Invoice.InvoiceStatus status) {
        return jdbcTemplate.queryForObject(SUM_BY_CLIENT_AND_STATUS, BigDecimal.class, clientId, status.name());
    }

    @Override
    public long countByStatus(Invoice.InvoiceStatus status) {
        Long count = jdbcTemplate.queryForObject(COUNT_BY_STATUS, Long.class, status.name());
        return count != null ? count : 0;
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update(DELETE_BY_ID, id);
    }

    private static class InvoiceRowMapper implements RowMapper<Invoice> {
        @Override
        public Invoice mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Invoice.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .invoiceNumber(rs.getString("invoice_number"))
                    .ncf(rs.getString("ncf"))
                    .ncfType(rs.getString("ncf_type") != null ?
                            Invoice.NCFType.valueOf(rs.getString("ncf_type")) : null)
                    .clientId(UUID.fromString(rs.getString("client_id")))
                    .issueDate(rs.getDate("issue_date") != null ?
                            rs.getDate("issue_date").toLocalDate() : null)
                    .dueDate(rs.getDate("due_date") != null ?
                            rs.getDate("due_date").toLocalDate() : null)
                    .invoiceType(rs.getString("invoice_type") != null ?
                            Invoice.InvoiceType.valueOf(rs.getString("invoice_type")) : null)
                    .subtotal(rs.getBigDecimal("subtotal"))
                    .itbisAmount(rs.getBigDecimal("itbis_amount"))
                    .discountAmount(rs.getBigDecimal("discount_amount"))
                    .totalAmount(rs.getBigDecimal("total_amount"))
                    .netAmount(rs.getBigDecimal("net_amount"))
                    .paidAmount(rs.getBigDecimal("paid_amount"))
                    .balanceDue(rs.getBigDecimal("balance_due"))
                    .status(rs.getString("status") != null ?
                            Invoice.InvoiceStatus.valueOf(rs.getString("status")) : null)
                    .notes(rs.getString("notes"))
                    .dgiiTrackId(rs.getString("dgii_track_id"))
                    .dgiiStatus(rs.getString("dgii_status"))
                    .ecfPayload(rs.getString("ecf_payload"))
                    .qrHash(rs.getString("qr_hash"))
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