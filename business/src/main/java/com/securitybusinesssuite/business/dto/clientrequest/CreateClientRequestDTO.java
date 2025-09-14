// path: business/src/main/java/com/securitybusinesssuite/business/dto/CreateClientRequestDTO.java
package com.securitybusinesssuite.business.dto.clientrequest;

import com.securitybusinesssuite.data.entity.Client;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateClientRequestDTO {
    @NotBlank(message = "Business name is required")
    @Size(max = 255, message = "Business name cannot exceed 255 characters")
    private String businessName;

    @NotNull(message = "Client type is required")
    private Client.ClientType clientType;

    @Pattern(regexp = "\\d{3}-\\d{7}-\\d{1}", message = "RNC must be in format XXX-XXXXXXX-X")
    private String rnc;

    @Size(max = 100, message = "Contact person cannot exceed 100 characters")
    private String contactPerson;

    private Client.BusinessSector businessSector;

    @Pattern(regexp = "^[+]?[\\d\\s\\-()]+$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String streetName;
    private String streetNumber;
    private String sector;
    private String provincia;

    private List<Client.SecurityService> services;

    private boolean hasContract;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private boolean autoRenewal = true;

    private Client.PaymentMethod paymentMethod;

    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be positive")
    private BigDecimal hourlyRate;

    private String notes;
}
