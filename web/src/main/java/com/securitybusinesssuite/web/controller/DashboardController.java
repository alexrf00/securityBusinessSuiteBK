// path: web/src/main/java/com/securitybusinesssuite/web/controller/DashboardController.java
package com.securitybusinesssuite.web.controller;

import com.securitybusinesssuite.business.dto.dashboard.ChartDataDTO;
import com.securitybusinesssuite.business.dto.dashboard.DashboardStatsDTO;
import com.securitybusinesssuite.business.dto.dashboard.RecentActivityDTO;
import com.securitybusinesssuite.business.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivity() {
        List<RecentActivityDTO> activities = dashboardService.getRecentActivity();
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/charts/invoices-by-status")
    public ResponseEntity<List<ChartDataDTO>> getInvoicesByStatusChart() {
        List<ChartDataDTO> chartData = dashboardService.getInvoicesByStatusChart();
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/charts/revenue-by-month")
    public ResponseEntity<List<ChartDataDTO>> getRevenueByMonthChart() {
        List<ChartDataDTO> chartData = dashboardService.getRevenueByMonthChart();
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/charts/clients-by-type")
    public ResponseEntity<List<ChartDataDTO>> getClientsByTypeChart() {
        List<ChartDataDTO> chartData = dashboardService.getClientsByTypeChart();
        return ResponseEntity.ok(chartData);
    }
}