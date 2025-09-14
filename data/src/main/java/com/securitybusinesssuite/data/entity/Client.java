// path: data/src/main/java/com/securitybusinesssuite/data/entity/Client.java
package com.securitybusinesssuite.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private UUID id;
    private String clientCode;
    private String rnc;
    private ClientType clientType;
    private String businessName;
    private String contactPerson;
    private BusinessSector businessSector;
    private String phone;
    private String email;
    private String streetName;
    private String streetNumber;
    private String sector;
    private String provincia;
    private List<SecurityService> services;
    private boolean hasContract;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private boolean autoRenewal;
    private PaymentMethod paymentMethod;
    private BigDecimal hourlyRate;
    private boolean requiresNcf;
    private boolean requiresRnc;
    private boolean appliesItbis;
    private ClientStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public enum ClientType {
        PERSONA_FISICA, SRL, CONSUMIDOR_FINAL
    }

    public enum BusinessSector {
        DEALER_AUTOS, RESIDENCIAL, COOPERATIVA, GOBIERNO, INDIVIDUAL
    }

    public enum SecurityService {
        PUESTO_FIJO, PATRULLAJE, ESCOLTA
    }

    public enum PaymentMethod {
        EFECTIVO, CHEQUE, TRANSFERENCIA_BANCARIA
    }

    public enum ClientStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}