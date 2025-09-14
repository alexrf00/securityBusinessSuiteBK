// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/ClientServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.dto.*;
import com.securitybusinesssuite.business.dto.clientrequest.CreateClientRequestDTO;
import com.securitybusinesssuite.business.dto.clientrequest.UpdateClientRequestDTO;
import com.securitybusinesssuite.business.dto.search.ClientSearchCriteria;
import com.securitybusinesssuite.business.dto.search.PagedResponseDTO;
import com.securitybusinesssuite.business.exception.BusinessException;
import com.securitybusinesssuite.business.service.ClientService;
import com.securitybusinesssuite.business.service.ValidationService;
import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.ClientSequence;
import com.securitybusinesssuite.data.repository.ClientRepository;
import com.securitybusinesssuite.data.repository.SequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final SequenceRepository sequenceRepository;
    private final ValidationService validationService;

    @Override
    @Transactional
    public ClientResponseDTO createClient(CreateClientRequestDTO request, UUID createdBy) {
        // Validate request
        validateClientData(request);

        // Check if RNC already exists
        if (request.getRnc() != null && !request.getRnc().trim().isEmpty()) {
            if (clientRepository.existsByRnc(request.getRnc())) {
                throw new BusinessException("A client with this RNC already exists");
            }
        }

        // Generate client code
        String clientCode = generateClientCode();

        // Create client entity
        Client client = Client.builder()
                .clientCode(clientCode)
                .rnc(request.getRnc())
                .clientType(request.getClientType())
                .businessName(request.getBusinessName())
                .contactPerson(request.getContactPerson())
                .businessSector(request.getBusinessSector())
                .phone(request.getPhone())
                .email(request.getEmail())
                .streetName(request.getStreetName())
                .streetNumber(request.getStreetNumber())
                .sector(request.getSector())
                .provincia(request.getProvincia())
                .services(request.getServices())
                .hasContract(request.isHasContract())
                .contractStartDate(request.getContractStartDate())
                .contractEndDate(request.getContractEndDate())
                .autoRenewal(request.isAutoRenewal())
                .paymentMethod(request.getPaymentMethod())
                .hourlyRate(request.getHourlyRate())
                .notes(request.getNotes())
                .status(Client.ClientStatus.ACTIVE)
                .createdBy(createdBy)
                .build();

        // Set business rules based on client type
        setClientBusinessRules(client);

        // Validate client business rules
        validationService.validateClientRequirements(client);

        // Save client
        Client savedClient = clientRepository.save(client);

        log.info("Client created: {} - {}", savedClient.getClientCode(), savedClient.getBusinessName());
        return ClientResponseDTO.fromEntity(savedClient);
    }

    @Override
    @Transactional
    public ClientResponseDTO updateClient(UUID id, UpdateClientRequestDTO request, UUID updatedBy) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Client not found"));

        // Check if RNC is changing and new RNC already exists
        if (request.getRnc() != null && !request.getRnc().equals(existingClient.getRnc())) {
            if (clientRepository.existsByRnc(request.getRnc())) {
                throw new BusinessException("A client with this RNC already exists");
            }
        }

        // Update client fields
        existingClient.setRnc(request.getRnc());
        existingClient.setClientType(request.getClientType());
        existingClient.setBusinessName(request.getBusinessName());
        existingClient.setContactPerson(request.getContactPerson());
        existingClient.setBusinessSector(request.getBusinessSector());
        existingClient.setPhone(request.getPhone());
        existingClient.setEmail(request.getEmail());
        existingClient.setStreetName(request.getStreetName());
        existingClient.setStreetNumber(request.getStreetNumber());
        existingClient.setSector(request.getSector());
        existingClient.setProvincia(request.getProvincia());
        existingClient.setServices(request.getServices());
        existingClient.setHasContract(request.isHasContract());
        existingClient.setContractStartDate(request.getContractStartDate());
        existingClient.setContractEndDate(request.getContractEndDate());
        existingClient.setAutoRenewal(request.isAutoRenewal());
        existingClient.setPaymentMethod(request.getPaymentMethod());
        existingClient.setHourlyRate(request.getHourlyRate());
        existingClient.setStatus(request.getStatus());
        existingClient.setNotes(request.getNotes());
        existingClient.setUpdatedBy(updatedBy);

        // Update business rules based on client type
        setClientBusinessRules(existingClient);

        // Validate client business rules
        validationService.validateClientRequirements(existingClient);

        // Update client
        Client updatedClient = clientRepository.update(existingClient);

        log.info("Client updated: {} - {}", updatedClient.getClientCode(), updatedClient.getBusinessName());
        return ClientResponseDTO.fromEntity(updatedClient);
    }

    @Override
    public ClientResponseDTO getClient(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Client not found"));
        return ClientResponseDTO.fromEntity(client);
    }

    @Override
    public PagedResponseDTO<ClientResponseDTO> searchClients(ClientSearchCriteria criteria) {
        // Create sort
        Sort.Direction direction = "ASC".equalsIgnoreCase(criteria.getSortDirection()) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, criteria.getSortBy());

        // Create pageable
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        // Execute search
        Page<Client> clientPage = clientRepository.findByFilters(
                criteria.getBusinessName(),
                criteria.getClientType(),
                criteria.getStatus(),
                criteria.getBusinessSector(),
                pageable
        );

        // Convert to DTOs
        return PagedResponseDTO.<ClientResponseDTO>builder()
                .content(clientPage.getContent().stream()
                        .map(ClientResponseDTO::fromEntity)
                        .collect(Collectors.toList()))
                .page(clientPage.getNumber())
                .size(clientPage.getSize())
                .totalElements(clientPage.getTotalElements())
                .totalPages(clientPage.getTotalPages())
                .first(clientPage.isFirst())
                .last(clientPage.isLast())
                .hasNext(clientPage.hasNext())
                .hasPrevious(clientPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public void deleteClient(UUID id) {
        if (!clientRepository.findById(id).isPresent()) {
            throw new BusinessException("Client not found");
        }

        // TODO: Add business logic to check if client has active invoices/contracts
        // For now, we'll just delete
        clientRepository.deleteById(id);
        log.info("Client deleted: {}", id);
    }

    @Override
    public boolean existsByRnc(String rnc) {
        return clientRepository.existsByRnc(rnc);
    }

    @Override
    @Transactional
    public String generateClientCode() {
        int currentYear = LocalDate.now().getYear();

        ClientSequence sequence = sequenceRepository.findClientSequenceByYear(currentYear)
                .orElseGet(() -> {
                    // Create new sequence for current year
                    ClientSequence newSequence = ClientSequence.builder()
                            .currentNumber(0)
                            .prefix("I")
                            .year(currentYear)
                            .build();
                    return sequenceRepository.saveClientSequence(newSequence);
                });

        // Increment sequence
        sequence.setCurrentNumber(sequence.getCurrentNumber() + 1);
        sequenceRepository.updateClientSequence(sequence);

        // Format: I-0001, I-0002, etc.
        return String.format("%s-%04d", sequence.getPrefix(), sequence.getCurrentNumber());
    }

    @Override
    public void validateClientData(CreateClientRequestDTO request) {
        if (request.getBusinessName() == null || request.getBusinessName().trim().isEmpty()) {
            throw new BusinessException("Business name is required");
        }

        if (request.getClientType() == null) {
            throw new BusinessException("Client type is required");
        }

        // Validate RNC format
        validationService.validateRNCFormat(request.getRnc());

        // Validate contract dates
        if (request.isHasContract()) {
            if (request.getContractStartDate() == null) {
                throw new BusinessException("Contract start date is required when client has contract");
            }
        }
    }

    private void setClientBusinessRules(Client client) {
        // Set business rules based on client type
        switch (client.getClientType()) {
            case SRL:
                client.setRequiresNcf(true);
                client.setRequiresRnc(true);
                client.setAppliesItbis(true);
                break;

            case PERSONA_FISICA:
                // For physical persons with RNC
                client.setRequiresNcf(client.getRnc() != null && !client.getRnc().trim().isEmpty());
                client.setRequiresRnc(client.getRnc() != null && !client.getRnc().trim().isEmpty());
                client.setAppliesItbis(true);
                break;

            case CONSUMIDOR_FINAL:
                client.setRequiresNcf(false);
                client.setRequiresRnc(false);
                client.setAppliesItbis(true); // Still applies ITBIS but no NCF
                break;
        }
    }
}