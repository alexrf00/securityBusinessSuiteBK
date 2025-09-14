// path: data/src/main/java/com/securitybusinesssuite/data/entity/NCFSequence.java
package com.securitybusinesssuite.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NCFSequence {
    private UUID id;
    private Invoice.NCFType ncfType;
    private String prefix;
    private Long currentNumber;
    private Long maxNumber;
    private Integer year;
    private boolean isActive;
    private LocalDateTime createdAt;
}