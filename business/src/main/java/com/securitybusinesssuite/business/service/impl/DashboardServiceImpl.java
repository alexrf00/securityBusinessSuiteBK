// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/DashboardServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.dto.dashboard.ChartDataDTO;
import com.securitybusinesssuite.business.dto.dashboard.DashboardStatsDTO;
import com.securitybusinesssuite.business.dto.dashboard.RecentActivityDTO;
import com.securitybusinesssuite.business.service.DashboardService;
import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Invoice;
import com.securitybusinesssuite.data.repository.ClientRepository;
import com.securitybusinesssuite.data.repository.InvoiceRepository;
import com.securitybusinesssuite.data.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReceiptRepository receiptRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        // Get basic counts
        long totalClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE) +
                clientRepository.countByStatus(Client.ClientStatus.INACTIVE) +
                clientRepository.countByStatus(Client.ClientStatus.SUSPENDED);

        long activeClients = clientRepository.countByStatus(Client.ClientStatus.ACTIVE);
        long pendingInvoices = invoiceRepository.countByStatus(Invoice.InvoiceStatus.PENDING);
        long overdueInvoices = invoiceRepository.countByStatus(Invoice.InvoiceStatus.OVERDUE);

        // Get financial totals
        BigDecimal totalPendingAmount = getTotalAmountByStatus(Invoice.InvoiceStatus.PENDING);
        BigDecimal totalOverdueAmount = getTotalAmountByStatus(Invoice.InvoiceStatus.OVERDUE);
        BigDecimal monthlyRevenue = getMonthlyRevenue();
        BigDecimal yearToDateRevenue = getYearToDateRevenue();

        return DashboardStatsDTO.builder()
                .totalClients(totalClients)
                .activeClients(activeClients)
                .pendingInvoices(pendingInvoices)
                .overdueInvoices(overdueInvoices)
                .totalPendingAmount(totalPendingAmount)
                .totalOverdueAmount(totalOverdueAmount)
                .monthlyRevenue(monthlyRevenue)
                .yearToDateRevenue(yearToDateRevenue)
                .build();
    }

    @Override
    public List<RecentActivityDTO> getRecentActivity() {
        String sql = """
            (SELECT 'CLIENT_CREATED' as activity_type, c.business_name as description, 
                    c.business_name as client_name, NULL::decimal as amount, c.created_at as timestamp,
                    c.id::text as entity_id
             FROM clients c 
             WHERE c.created_at >= NOW() - INTERVAL '30 days'
             ORDER BY c.created_at DESC 
             LIMIT 5)
            UNION ALL
            (SELECT 'INVOICE_CREATED' as activity_type, 
                    'Invoice ' || i.invoice_number || ' created' as description,
                    c.business_name as client_name, i.total_amount as amount, i.created_at as timestamp,
                    i.id::text as entity_id
             FROM invoices i 
             JOIN clients c ON c.id = i.client_id
             WHERE i.created_at >= NOW() - INTERVAL '30 days'
             ORDER BY i.created_at DESC 
             LIMIT 5)
            UNION ALL
            (SELECT 'PAYMENT_RECEIVED' as activity_type,
                    'Payment ' || r.receipt_number || ' received' as description,
                    c.business_name as client_name, r.total_amount as amount, r.created_at as timestamp,
                    r.id::text as entity_id
             FROM receipts r 
             JOIN clients c ON c.id = r.client_id
             WHERE r.created_at >= NOW() - INTERVAL '30 days'
             ORDER BY r.created_at DESC 
             LIMIT 5)
            ORDER BY timestamp DESC 
            LIMIT 15
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                RecentActivityDTO.builder()
                        .id(UUID.fromString(rs.getString("entity_id")))
                        .type(RecentActivityDTO.ActivityType.valueOf(rs.getString("activity_type")))
                        .description(rs.getString("description"))
                        .clientName(rs.getString("client_name"))
                        .amount(rs.getBigDecimal("amount"))
                        .timestamp(rs.getTimestamp("timestamp").toLocalDateTime())
                        .build()
        );
    }

    @Override
    public List<ChartDataDTO> getInvoicesByStatusChart() {
        String sql = """
            SELECT status, COUNT(*) as count, SUM(total_amount) as total_amount
            FROM invoices 
            WHERE created_at >= DATE_TRUNC('year', CURRENT_DATE)
            GROUP BY status
            """;

        Map<String, String> statusColors = Map.of(
                "PENDING", "#fbbf24",
                "PAID", "#10b981",
                "OVERDUE", "#ef4444",
                "CANCELLED", "#6b7280"
        );

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                ChartDataDTO.builder()
                        .label(rs.getString("status"))
                        .value(rs.getBigDecimal("total_amount"))
                        .color(statusColors.getOrDefault(rs.getString("status"), "#6b7280"))
                        .build()
        );
    }

    @Override
    public List<ChartDataDTO> getRevenueByMonthChart() {
        String sql = """
            SELECT DATE_TRUNC('month', issue_date) as month,
                   SUM(total_amount) as revenue
            FROM invoices 
            WHERE issue_date >= DATE_TRUNC('year', CURRENT_DATE)
              AND status IN ('PAID', 'PENDING', 'OVERDUE')
            GROUP BY DATE_TRUNC('month', issue_date)
            ORDER BY month
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                ChartDataDTO.builder()
                        .label(rs.getDate("month").toLocalDate().getMonth().toString())
                        .value(rs.getBigDecimal("revenue"))
                        .color("#3b82f6")
                        .build()
        );
    }

    @Override
    public List<ChartDataDTO> getClientsByTypeChart() {
        String sql = """
            SELECT client_type, COUNT(*) as count
            FROM clients 
            WHERE status = 'ACTIVE'
            GROUP BY client_type
            """;

        Map<String, String> typeColors = Map.of(
                "SRL", "#8b5cf6",
                "PERSONA_FISICA", "#06b6d4",
                "CONSUMIDOR_FINAL", "#84cc16"
        );

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                ChartDataDTO.builder()
                        .label(rs.getString("client_type"))
                        .value(new BigDecimal(rs.getInt("count")))
                        .color(typeColors.getOrDefault(rs.getString("client_type"), "#6b7280"))
                        .build()
        );
    }

    private BigDecimal getTotalAmountByStatus(Invoice.InvoiceStatus status) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices WHERE status = ?::invoice_status_enum";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, status.name());
    }

    private BigDecimal getMonthlyRevenue() {
        String sql = """
            SELECT COALESCE(SUM(total_amount), 0) 
            FROM invoices 
            WHERE issue_date >= DATE_TRUNC('month', CURRENT_DATE)
              AND issue_date < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
              AND status IN ('PAID', 'PENDING', 'OVERDUE')
            """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    private BigDecimal getYearToDateRevenue() {
        String sql = """
            SELECT COALESCE(SUM(total_amount), 0) 
            FROM invoices 
            WHERE issue_date >= DATE_TRUNC('year', CURRENT_DATE)
              AND status IN ('PAID', 'PENDING', 'OVERDUE')
            """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }
}