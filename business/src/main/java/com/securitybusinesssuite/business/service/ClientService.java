// path: business/src/main/java/com/securitybusinesssuite/business/service/ClientService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.clientrequest.CreateClientRequestDTO;
import com.securitybusinesssuite.business.dto.clientrequest.UpdateClientRequestDTO;
import com.securitybusinesssuite.business.dto.search.ClientSearchCriteria;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ClientService {
    ClientResponseDTO createClient(CreateClientRequestDTO request, UUID createdBy);
    ClientResponseDTO updateClient(UUID id, UpdateClientRequestDTO request, UUID updatedBy);
    ClientResponseDTO getClient(UUID id);
    PagedResponseDTO<ClientResponseDTO> searchClients(ClientSearchCriteria criteria);
    void deleteClient(UUID id);
    boolean existsByRnc(String rnc);
    String generateClientCode();
    void validateClientData(CreateClientRequestDTO request);
}