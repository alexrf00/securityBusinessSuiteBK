// path: data/src/main/java/com/securitybusinesssuite/data/repository/impl/ClientRepositoryImpl.java
package com.securitybusinesssuite.data.repository.impl;

import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ClientRepositoryImpl implements ClientRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_CLIENT = """
        INSERT INTO clients (id, client_code, rnc, client_type, business_name, contact_person,
                            business_sector, phone, email, street_name, street_number, sector, 
                            provincia, services, has_contract, contract_start_date, contract_end_date,
                            auto_renewal, payment_method, hourly_rate, requires_ncf, requires_rnc,
                            applies_itbis, status, notes, created_at, updated_at, created_by, updated_by)
        VALUES (?, ?, ?, ?::client_type_enum, ?, ?, ?::business_sector_enum, ?, ?, ?, ?, ?, ?, 
                ?::security_service_enum[], ?, ?, ?, ?, ?::payment_method_enum, ?, ?, ?, ?, 
                ?::client_status_enum, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_CLIENT = """
        UPDATE clients SET client_code = ?, rnc = ?, client_type = ?::client_type_enum, 
                          business_name = ?, contact_person = ?, business_sector = ?::business_sector_enum,
                          phone = ?, email = ?, street_name = ?, street_number = ?, sector = ?, 
                          provincia = ?, services = ?::security_service_enum[], has_contract = ?,
                          contract_start_date = ?, contract_end_date = ?, auto_renewal = ?,
                          payment_method = ?::payment_method_enum, hourly_rate = ?, requires_ncf = ?,
                          requires_rnc = ?, applies_itbis = ?, status = ?::client_status_enum,
                          notes = ?, updated_at = ?, updated_by = ?
        WHERE id = ?
        """;

    private static final String SELECT_BASE = """
        SELECT id, client_code, rnc, client_type, business_name, contact_person, business_sector,
               phone, email, street_name, street_number, sector, provincia, services, has_contract,
               contract_start_date, contract_end_date, auto_renewal, payment_method, hourly_rate,
               requires_ncf, requires_rnc, applies_itbis, status, notes, created_at, updated_at,
               created_by, updated_by
        FROM clients
        """;

    private static final String SELECT_BY_ID = SELECT_BASE + " WHERE id = ?";
    private static final String SELECT_BY_CLIENT_CODE = SELECT_BASE + " WHERE client_code = ?";
    private static final String SELECT_BY_RNC = SELECT_BASE + " WHERE rnc = ?";
    private static final String SELECT_BY_STATUS = SELECT_BASE + " WHERE status = ?::client_status_enum";
    private static final String SELECT_BY_TYPE = SELECT_BASE + " WHERE client_type = ?::client_type_enum";
    private static final String SELECT_BY_BUSINESS_NAME = SELECT_BASE + " WHERE LOWER(business_name) LIKE LOWER(?)";

    private static final String EXISTS_BY_RNC = "SELECT EXISTS(SELECT 1 FROM clients WHERE rnc = ?)";
    private static final String EXISTS_BY_CLIENT_CODE = "SELECT EXISTS(SELECT 1 FROM clients WHERE client_code = ?)";
    private static final String COUNT_BY_STATUS = "SELECT COUNT(*) FROM clients WHERE status = ?::client_status_enum";
    private static final String DELETE_BY_ID = "DELETE FROM clients WHERE id = ?";

    private final ClientRowMapper clientRowMapper = new ClientRowMapper();

    @Override
    public Client save(Client client) {
        client.setId(UUID.randomUUID());
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(INSERT_CLIENT,
                client.getId(),
                client.getClientCode(),
                client.getRnc(),
                client.getClientType() != null ? client.getClientType().name() : null,
                client.getBusinessName(),
                client.getContactPerson(),
                client.getBusinessSector() != null ? client.getBusinessSector().name() : null,
                client.getPhone(),
                client.getEmail(),
                client.getStreetName(),
                client.getStreetNumber(),
                client.getSector(),
                client.getProvincia(),
                client.getServices() != null ?
                        client.getServices().stream().map(Enum::name).toArray(String[]::new) : null,
                client.isHasContract(),
                client.getContractStartDate(),
                client.getContractEndDate(),
                client.isAutoRenewal(),
                client.getPaymentMethod() != null ? client.getPaymentMethod().name() : null,
                client.getHourlyRate(),
                client.isRequiresNcf(),
                client.isRequiresRnc(),
                client.isAppliesItbis(),
                client.getStatus() != null ? client.getStatus().name() : null,
                client.getNotes(),
                Timestamp.valueOf(client.getCreatedAt()),
                Timestamp.valueOf(client.getUpdatedAt()),
                client.getCreatedBy(),
                client.getUpdatedBy()
        );

        return client;
    }

    @Override
    public Client update(Client client) {
        client.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(UPDATE_CLIENT,
                client.getClientCode(),
                client.getRnc(),
                client.getClientType() != null ? client.getClientType().name() : null,
                client.getBusinessName(),
                client.getContactPerson(),
                client.getBusinessSector() != null ? client.getBusinessSector().name() : null,
                client.getPhone(),
                client.getEmail(),
                client.getStreetName(),
                client.getStreetNumber(),
                client.getSector(),
                client.getProvincia(),
                client.getServices() != null ?
                        client.getServices().stream().map(Enum::name).toArray(String[]::new) : null,
                client.isHasContract(),
                client.getContractStartDate(),
                client.getContractEndDate(),
                client.isAutoRenewal(),
                client.getPaymentMethod() != null ? client.getPaymentMethod().name() : null,
                client.getHourlyRate(),
                client.isRequiresNcf(),
                client.isRequiresRnc(),
                client.isAppliesItbis(),
                client.getStatus() != null ? client.getStatus().name() : null,
                client.getNotes(),
                Timestamp.valueOf(client.getUpdatedAt()),
                client.getUpdatedBy(),
                client.getId()
        );

        return client;
    }

    @Override
    public Optional<Client> findById(UUID id) {
        return jdbcTemplate.query(SELECT_BY_ID, clientRowMapper, id)
                .stream().findFirst();
    }

    @Override
    public Optional<Client> findByClientCode(String clientCode) {
        return jdbcTemplate.query(SELECT_BY_CLIENT_CODE, clientRowMapper, clientCode)
                .stream().findFirst();
    }

    @Override
    public Optional<Client> findByRnc(String rnc) {
        return jdbcTemplate.query(SELECT_BY_RNC, clientRowMapper, rnc)
                .stream().findFirst();
    }

    @Override
    public List<Client> findByBusinessNameContaining(String businessName) {
        return jdbcTemplate.query(SELECT_BY_BUSINESS_NAME, clientRowMapper, "%" + businessName + "%");
    }

    @Override
    public List<Client> findByStatus(Client.ClientStatus status) {
        return jdbcTemplate.query(SELECT_BY_STATUS, clientRowMapper, status.name());
    }

    @Override
    public List<Client> findByClientType(Client.ClientType clientType) {
        return jdbcTemplate.query(SELECT_BY_TYPE, clientRowMapper, clientType.name());
    }

    @Override
    public Page<Client> findAll(Pageable pageable) {
        String sql = SELECT_BASE + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Client> clients = jdbcTemplate.query(sql, clientRowMapper,
                pageable.getPageSize(), pageable.getOffset());

        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clients", Long.class);
        return new PageImpl<>(clients, pageable, total);
    }

    @Override
    public Page<Client> findByFilters(String businessName, Client.ClientType clientType,
                                      Client.ClientStatus status, Client.BusinessSector sector,
                                      Pageable pageable) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (businessName != null && !businessName.trim().isEmpty()) {
            sql.append(" AND LOWER(business_name) LIKE LOWER(?)");
            params.add("%" + businessName.trim() + "%");
        }

        if (clientType != null) {
            sql.append(" AND client_type = ?::client_type_enum");
            params.add(clientType.name());
        }

        if (status != null) {
            sql.append(" AND status = ?::client_status_enum");
            params.add(status.name());
        }

        if (sector != null) {
            sql.append(" AND business_sector = ?::business_sector_enum");
            params.add(sector.name());
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        List<Client> clients = jdbcTemplate.query(sql.toString(), clientRowMapper, params.toArray());

        // Count query for total
        String countSql = sql.toString().replace(SELECT_BASE, "SELECT COUNT(*)").split(" ORDER BY")[0];
        long total = jdbcTemplate.queryForObject(countSql, Long.class,
                params.subList(0, params.size() - 2).toArray());

        return new PageImpl<>(clients, pageable, total);
    }

    @Override
    public boolean existsByRnc(String rnc) {
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_BY_RNC, Boolean.class, rnc);
        return exists != null && exists;
    }

    @Override
    public boolean existsByClientCode(String clientCode) {
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_BY_CLIENT_CODE, Boolean.class, clientCode);
        return exists != null && exists;
    }

    @Override
    public long countByStatus(Client.ClientStatus status) {
        Long count = jdbcTemplate.queryForObject(COUNT_BY_STATUS, Long.class, status.name());
        return count != null ? count : 0;
    }

    @Override
    public void deleteById(UUID id) {
        jdbcTemplate.update(DELETE_BY_ID, id);
    }

    private static class ClientRowMapper implements RowMapper<Client> {
        @Override
        public Client mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Parse services array
            List<Client.SecurityService> services = new ArrayList<>();
            Array servicesArray = rs.getArray("services");
            if (servicesArray != null) {
                String[] serviceNames = (String[]) servicesArray.getArray();
                services = Arrays.stream(serviceNames)
                        .map(Client.SecurityService::valueOf)
                        .collect(Collectors.toList());
            }

            return Client.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .clientCode(rs.getString("client_code"))
                    .rnc(rs.getString("rnc"))
                    .clientType(rs.getString("client_type") != null ?
                            Client.ClientType.valueOf(rs.getString("client_type")) : null)
                    .businessName(rs.getString("business_name"))
                    .contactPerson(rs.getString("contact_person"))
                    .businessSector(rs.getString("business_sector") != null ?
                            Client.BusinessSector.valueOf(rs.getString("business_sector")) : null)
                    .phone(rs.getString("phone"))
                    .email(rs.getString("email"))
                    .streetName(rs.getString("street_name"))
                    .streetNumber(rs.getString("street_number"))
                    .sector(rs.getString("sector"))
                    .provincia(rs.getString("provincia"))
                    .services(services)
                    .hasContract(rs.getBoolean("has_contract"))
                    .contractStartDate(rs.getDate("contract_start_date") != null ?
                            rs.getDate("contract_start_date").toLocalDate() : null)
                    .contractEndDate(rs.getDate("contract_end_date") != null ?
                            rs.getDate("contract_end_date").toLocalDate() : null)
                    .autoRenewal(rs.getBoolean("auto_renewal"))
                    .paymentMethod(rs.getString("payment_method") != null ?
                            Client.PaymentMethod.valueOf(rs.getString("payment_method")) : null)
                    .hourlyRate(rs.getBigDecimal("hourly_rate"))
                    .requiresNcf(rs.getBoolean("requires_ncf"))
                    .requiresRnc(rs.getBoolean("requires_rnc"))
                    .appliesItbis(rs.getBoolean("applies_itbis"))
                    .status(rs.getString("status") != null ?
                            Client.ClientStatus.valueOf(rs.getString("status")) : null)
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