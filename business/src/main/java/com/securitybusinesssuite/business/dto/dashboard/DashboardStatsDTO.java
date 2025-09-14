// path: business/src/main/java/com/securitybusinesssuite/business/dto/DashboardDTO.java
package com.securitybusinesssuite.business.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalClients;
    private long activeClients;
    private long pendingInvoices;
    private long overdueInvoices;
    private BigDecimal totalPendingAmount;
    private BigDecimal totalOverdueAmount;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearToDateRevenue;
}