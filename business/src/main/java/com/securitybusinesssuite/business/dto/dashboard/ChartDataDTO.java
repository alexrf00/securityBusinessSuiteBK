// path: business/src/main/java/com/securitybusinesssuite/business/dto/ChartDataDTO.java
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
public class ChartDataDTO {
    private String label;
    private BigDecimal value;
    private String color;
}