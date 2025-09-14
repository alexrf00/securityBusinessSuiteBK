// path: business/src/main/java/com/securitybusinesssuite/business/dto/ClientResponseDTO.java
package com.securitybusinesssuite.business.dto;

import com.securitybusinesssuite.data.entity.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {
    private UUID id;
    private String clientCode;
    private String rnc;
    private Client.ClientType clientType;
    private String businessName;
    private String contactPerson;
    private Client.BusinessSector businessSector;
    private String phone;
    private String email;
    private String streetName;
    private String streetNumber;
    private String sector;
    private String provincia;
    private List<Client.SecurityService> services;
    private boolean hasContract;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private boolean autoRenewal;
    private Client.PaymentMethod paymentMethod;
    private BigDecimal hourlyRate;
    private boolean requiresNcf;
    private boolean requiresRnc;
    private boolean appliesItbis;
    private Client.ClientStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClientResponseDTO fromEntity(Client client) {
        return ClientResponseDTO.builder()
                .id(client.getId())
                .clientCode(client.getClientCode())
                .rnc(client.getRnc())
                .clientType(client.getClientType())
                .businessName(client.getBusinessName())
                .contactPerson(client.getContactPerson())
                .businessSector(client.getBusinessSector())
                .phone(client.getPhone())
                .email(client.getEmail())
                .streetName(client.getStreetName())
                .streetNumber(client.getStreetNumber())
                .sector(client.getSector())
                .provincia(client.getProvincia())
                .services(client.getServices())
                .hasContract(client.isHasContract())
                .contractStartDate(client.getContractStartDate())
                .contractEndDate(client.getContractEndDate())
                .autoRenewal(client.isAutoRenewal())
                .paymentMethod(client.getPaymentMethod())
                .hourlyRate(client.getHourlyRate())
                .requiresNcf(client.isRequiresNcf())
                .requiresRnc(client.isRequiresRnc())
                .appliesItbis(client.isAppliesItbis())
                .status(client.getStatus())
                .notes(client.getNotes())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
