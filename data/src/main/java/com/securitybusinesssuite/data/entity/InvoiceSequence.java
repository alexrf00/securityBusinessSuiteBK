// path: data/src/main/java/com/securitybusinesssuite/data/entity/InvoiceSequence.java
package com.securitybusinesssuite.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSequence {
    private UUID id;
    private Integer currentNumber;
    private Integer year;
}
