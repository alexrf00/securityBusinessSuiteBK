// path: business/src/main/java/com/securitybusinesssuite/business/service/DashboardService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.dashboard.ChartDataDTO;
import com.securitybusinesssuite.business.dto.dashboard.DashboardStatsDTO;
import com.securitybusinesssuite.business.dto.dashboard.RecentActivityDTO;

import java.util.List;

public interface DashboardService {
    DashboardStatsDTO getDashboardStats();
    List<RecentActivityDTO> getRecentActivity();
    List<ChartDataDTO> getInvoicesByStatusChart();
    List<ChartDataDTO> getRevenueByMonthChart();
    List<ChartDataDTO> getClientsByTypeChart();
}