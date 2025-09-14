// path: data/src/main/java/com/securitybusinesssuite/data/repository/impl/SequenceRepositoryImpl.java
package com.securitybusinesssuite.data.repository.impl;

import com.securitybusinesssuite.data.entity.*;
import com.securitybusinesssuite.data.repository.SequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SequenceRepositoryImpl implements SequenceRepository {

    private final JdbcTemplate jdbcTemplate;

    // NCF Sequence queries
    private static final String SELECT_NCF_SEQUENCE = """
        SELECT id, ncf_type, prefix, current_number, max_number, year, is_active, created_at
        FROM ncf_sequences WHERE ncf_type = ?::ncf_type_enum AND year = ? AND is_active = true
        """;

    private static final String UPDATE_NCF_SEQUENCE = """
        UPDATE ncf_sequences SET current_number = ? WHERE id = ?
        """;

    // Client Sequence queries
    private static final String SELECT_CLIENT_SEQUENCE = """
        SELECT id, current_number, prefix, year FROM client_sequences WHERE year = ?
        """;

    private static final String INSERT_CLIENT_SEQUENCE = """
        INSERT INTO client_sequences (id, current_number, prefix, year) VALUES (?, ?, ?, ?)
        """;

    private static final String UPDATE_CLIENT_SEQUENCE = """
        UPDATE client_sequences SET current_number = ? WHERE id = ?
        """;

    // Invoice Sequence queries  
    private static final String SELECT_INVOICE_SEQUENCE = """
        SELECT id, current_number, year FROM invoice_sequences WHERE year = ?
        """;

    private static final String INSERT_INVOICE_SEQUENCE = """
        INSERT INTO invoice_sequences (id, current_number, year) VALUES (?, ?, ?)
        """;

    private static final String UPDATE_INVOICE_SEQUENCE = """
        UPDATE invoice_sequences SET current_number = ? WHERE id = ?
        """;

    // Receipt Sequence queries
    private static final String SELECT_RECEIPT_SEQUENCE = """
        SELECT id, current_number, prefix, year FROM receipt_sequences WHERE year = ?
        """;

    private static final String INSERT_RECEIPT_SEQUENCE = """
        INSERT INTO receipt_sequences (id, current_number, prefix, year) VALUES (?, ?, ?, ?)
        """;

    private static final String UPDATE_RECEIPT_SEQUENCE = """
        UPDATE receipt_sequences SET current_number = ? WHERE id = ?
        """;

    @Override
    public Optional<NCFSequence> findNCFSequenceByTypeAndYear(Invoice.NCFType type, int year) {
        return jdbcTemplate.query(SELECT_NCF_SEQUENCE, new NCFSequenceRowMapper(), type.name(), year)
                .stream().findFirst();
    }

    @Override
    public NCFSequence updateNCFSequence(NCFSequence sequence) {
        jdbcTemplate.update(UPDATE_NCF_SEQUENCE, sequence.getCurrentNumber(), sequence.getId());
        return sequence;
    }

    @Override
    public NCFSequence saveNCFSequence(NCFSequence sequence) {
        // This would typically be used for initialization, but sequences are pre-populated
        throw new UnsupportedOperationException("NCF sequences are pre-populated in migration");
    }

    @Override
    public Optional<ClientSequence> findClientSequenceByYear(int year) {
        return jdbcTemplate.query(SELECT_CLIENT_SEQUENCE, new ClientSequenceRowMapper(), year)
                .stream().findFirst();
    }

    @Override
    public ClientSequence saveClientSequence(ClientSequence sequence) {
        sequence.setId(UUID.randomUUID());
        jdbcTemplate.update(INSERT_CLIENT_SEQUENCE,
                sequence.getId(),
                sequence.getCurrentNumber(),
                sequence.getPrefix(),
                sequence.getYear()
        );
        return sequence;
    }

    @Override
    public ClientSequence updateClientSequence(ClientSequence sequence) {
        jdbcTemplate.update(UPDATE_CLIENT_SEQUENCE, sequence.getCurrentNumber(), sequence.getId());
        return sequence;
    }

    @Override
    public Optional<InvoiceSequence> findInvoiceSequenceByYear(int year) {
        return jdbcTemplate.query(SELECT_INVOICE_SEQUENCE, new InvoiceSequenceRowMapper(), year)
                .stream().findFirst();
    }

    @Override
    public InvoiceSequence saveInvoiceSequence(InvoiceSequence sequence) {
        sequence.setId(UUID.randomUUID());
        jdbcTemplate.update(INSERT_INVOICE_SEQUENCE,
                sequence.getId(),
                sequence.getCurrentNumber(),
                sequence.getYear()
        );
        return sequence;
    }

    @Override
    public InvoiceSequence updateInvoiceSequence(InvoiceSequence sequence) {
        jdbcTemplate.update(UPDATE_INVOICE_SEQUENCE, sequence.getCurrentNumber(), sequence.getId());
        return sequence;
    }

    @Override
    public Optional<ReceiptSequence> findReceiptSequenceByYear(int year) {
        return jdbcTemplate.query(SELECT_RECEIPT_SEQUENCE, new ReceiptSequenceRowMapper(), year)
                .stream().findFirst();
    }

    @Override
    public ReceiptSequence saveReceiptSequence(ReceiptSequence sequence) {
        sequence.setId(UUID.randomUUID());
        jdbcTemplate.update(INSERT_RECEIPT_SEQUENCE,
                sequence.getId(),
                sequence.getCurrentNumber(),
                sequence.getPrefix(),
                sequence.getYear()
        );
        return sequence;
    }

    @Override
    public ReceiptSequence updateReceiptSequence(ReceiptSequence sequence) {
        jdbcTemplate.update(UPDATE_RECEIPT_SEQUENCE, sequence.getCurrentNumber(), sequence.getId());
        return sequence;
    }

    // Row Mappers
    private static class NCFSequenceRowMapper implements RowMapper<NCFSequence> {
        @Override
        public NCFSequence mapRow(ResultSet rs, int rowNum) throws SQLException {
            return NCFSequence.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .ncfType(Invoice.NCFType.valueOf(rs.getString("ncf_type")))
                    .prefix(rs.getString("prefix"))
                    .currentNumber(rs.getLong("current_number"))
                    .maxNumber(rs.getLong("max_number"))
                    .year(rs.getInt("year"))
                    .isActive(rs.getBoolean("is_active"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .build();
        }
    }

    private static class ClientSequenceRowMapper implements RowMapper<ClientSequence> {
        @Override
        public ClientSequence mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ClientSequence.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .currentNumber(rs.getInt("current_number"))
                    .prefix(rs.getString("prefix"))
                    .year(rs.getInt("year"))
                    .build();
        }
    }

    private static class InvoiceSequenceRowMapper implements RowMapper<InvoiceSequence> {
        @Override
        public InvoiceSequence mapRow(ResultSet rs, int rowNum) throws SQLException {
            return InvoiceSequence.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .currentNumber(rs.getInt("current_number"))
                    .year(rs.getInt("year"))
                    .build();
        }
    }

    private static class ReceiptSequenceRowMapper implements RowMapper<ReceiptSequence> {
        @Override
        public ReceiptSequence mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ReceiptSequence.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .currentNumber(rs.getInt("current_number"))
                    .prefix(rs.getString("prefix"))
                    .year(rs.getInt("year"))
                    .build();
        }
    }
}