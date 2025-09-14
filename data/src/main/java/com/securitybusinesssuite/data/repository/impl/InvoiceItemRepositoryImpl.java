// path: data/src/main/java/com/securitybusinesssuite/data/repository/impl/InvoiceItemRepositoryImpl.java
package com.securitybusinesssuite.data.repository.impl;

import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.InvoiceItem;
import com.securitybusinesssuite.data.repository.InvoiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InvoiceItemRepositoryImpl implements InvoiceItemRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_ITEM = """
        INSERT INTO invoice_items (id, invoice_id, service_code, description, service_type,
                                  quantity, unit_price, itbis_rate, itbis_amount, discount_percentage,
                                  discount_amount, line_subtotal, line_total, created_at, created_by)
        VALUES (?, ?, ?, ?, ?::security_service_enum, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_ITEM = """
        UPDATE invoice_items SET service_code = ?, description = ?, service_type = ?::security_service_enum,
                                quantity = ?, unit_price = ?, itbis_rate = ?, itbis_amount = ?,
                                discount_percentage = ?, discount_amount = ?, line_subtotal = ?, line_total = ?
        WHERE id = ?
        """;

    private static final String SELECT_BASE = """
        SELECT id, invoice_id, service_code, description, service_type, quantity, unit_price,
               itbis_rate, itbis_amount, discount_percentage, discount_amount, line_subtotal,
               line_total, created_at, created_by
        FROM invoice_items
        """;

    private static final String SELECT_BY_ID = SELECT_BASE + " WHERE id = ?";
    private static final String SELECT_BY_INVOICE_ID = SELECT_BASE + " WHERE invoice_id = ? ORDER BY created_at";
    private static final String DELETE_BY_ID = "DELETE FROM invoice_items WHERE id = ?";
    private static final String DELETE_BY_INVOICE_ID = "DELETE FROM invoice_items WHERE invoice_id = ?";

    private final InvoiceItemRowMapper itemRowMapper = new InvoiceItemRowMapper();

    @Override
    public InvoiceItem save(InvoiceItem item) {
        item.setId(UUID.randomUUID());
        item.setCreatedAt(LocalDateTime.now());

        jdbcTemplate.update(INSERT_ITEM,
                item.getId(),
                item.getInvoiceId(),
                item.getServiceCode(),
                item.getDescription(),
                item.getServiceType() != null ? item.getServiceType().name() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getItbisRate(),
                item.getItbisAmount(),
                item.getDiscountPercentage(),
                item.getDiscountAmount(),
                item.getLineSubtotal(),
                item.getLineTotal(),
                Timestamp.valueOf(item.getCreatedAt()),
                item.getCreatedBy()
        );

        return item;
    }

    @Override
    public InvoiceItem update(InvoiceItem item) {
        jdbcTemplate.update(UPDATE_ITEM,
                item.getServiceCode(),
                item.getDescription(),
                item.getServiceType() != null ? item.getServiceType().name() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getItbisRate(),
                item.getItbisAmount(),
                item.getDiscountPercentage(),
                item.getDiscountAmount(),
                item.getLineSubtotal(),
                item.getLineTotal(),
                item.getId()
        );

        return item;
    }

    @Override
    public Optional<InvoiceItem> findById(UUID id) {
        return jdbcTemplate.query(SELECT_BY_ID, itemRowMapper, id).stream().findFirst();
    }

    @Override
    public List<InvoiceItem> findByInvoiceId(UUID invoiceId) {
        return jdbcTemplate.query(SELECT_BY_INVOICE_ID, itemRowMapper, invoiceId);
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update(DELETE_BY_ID, id);
    }

    @Override
    public void deleteByInvoiceId(UUID invoiceId) {
        jdbcTemplate.update(DELETE_BY_INVOICE_ID, invoiceId);
    }

    private static class InvoiceItemRowMapper implements RowMapper<InvoiceItem> {
        @Override
        public InvoiceItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return InvoiceItem.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .invoiceId(UUID.fromString(rs.getString("invoice_id")))
                    .serviceCode(rs.getString("service_code"))
                    .description(rs.getString("description"))
                    .serviceType(rs.getString("service_type") != null ?
                            Client.SecurityService.valueOf(rs.getString("service_type")) : null)
                    .quantity(rs.getBigDecimal("quantity"))
                    .unitPrice(rs.getBigDecimal("unit_price"))
                    .itbisRate(rs.getBigDecimal("itbis_rate"))
                    .itbisAmount(rs.getBigDecimal("itbis_amount"))
                    .discountPercentage(rs.getBigDecimal("discount_percentage"))
                    .discountAmount(rs.getBigDecimal("discount_amount"))
                    .lineSubtotal(rs.getBigDecimal("line_subtotal"))
                    .lineTotal(rs.getBigDecimal("line_total"))
                    .createdAt(rs.getTimestamp("created_at") != null ?
                            rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .createdBy(rs.getString("created_by") != null ?
                            UUID.fromString(rs.getString("created_by")) : null)
                    .build();
        }
    }
}