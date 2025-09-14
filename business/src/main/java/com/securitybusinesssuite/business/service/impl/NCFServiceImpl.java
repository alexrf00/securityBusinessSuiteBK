// path: business/src/main/java/com/securitybusinesssuite/business/service/impl/NCFServiceImpl.java
package com.securitybusinesssuite.business.service.impl;

import com.securitybusinesssuite.business.exception.BusinessException;
import com.securitybusinesssuite.business.service.NCFService;
import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Invoice;
import com.securitybusinesssuite.data.entity.NCFSequence;
import com.securitybusinesssuite.data.repository.SequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NCFServiceImpl implements NCFService {

    private final SequenceRepository sequenceRepository;

    @Override
    @Transactional
    public String generateNCF(Client client) {
        if (!isNCFRequired(client)) {
            return null;
        }

        Invoice.NCFType ncfType = determineNCFType(client);
        validateNCFAvailability(ncfType);

        int currentYear = LocalDate.now().getYear();
        NCFSequence sequence = sequenceRepository.findNCFSequenceByTypeAndYear(ncfType, currentYear)
                .orElseThrow(() -> new BusinessException("NCF sequence not found for type " + ncfType + " and year " + currentYear));

        // Increment sequence
        sequence.setCurrentNumber(sequence.getCurrentNumber() + 1);

        // Check if we've exceeded the maximum
        if (sequence.getCurrentNumber() > sequence.getMaxNumber()) {
            throw new BusinessException("NCF sequence exhausted for type " + ncfType + ". Maximum number reached: " + sequence.getMaxNumber());
        }

        // Save updated sequence
        sequenceRepository.updateNCFSequence(sequence);

        // Format NCF: B0100000001
        String ncf = String.format("%s%08d", sequence.getPrefix(), sequence.getCurrentNumber());

        log.info("Generated NCF: {} for client: {} ({})", ncf, client.getBusinessName(), client.getClientCode());
        return ncf;
    }

    @Override
    public Invoice.NCFType determineNCFType(Client client) {
        if (client.getClientType() == Client.ClientType.CONSUMIDOR_FINAL) {
            return null; // No NCF required for final consumers
        }

        // For Dominican Republic business logic:
        // B01 - Factura de Crédito Fiscal (most common for business clients)
        // B02 - Factura de Consumo (for specific scenarios)

        if (client.getClientType() == Client.ClientType.SRL ||
                client.getClientType() == Client.ClientType.PERSONA_FISICA) {
            return Invoice.NCFType.B01; // Default to B01 for business clients
        }

        return Invoice.NCFType.B01; // Default fallback
    }

    @Override
    public boolean isNCFRequired(Client client) {
        // NCF is required for all clients except CONSUMIDOR_FINAL
        return client.getClientType() != Client.ClientType.CONSUMIDOR_FINAL && client.isRequiresNcf();
    }

    @Override
    public void validateNCFAvailability(Invoice.NCFType type) {
        int currentYear = LocalDate.now().getYear();
        NCFSequence sequence = sequenceRepository.findNCFSequenceByTypeAndYear(type, currentYear)
                .orElseThrow(() -> new BusinessException("NCF sequence not initialized for type " + type + " and year " + currentYear));

        if (!sequence.isActive()) {
            throw new BusinessException("NCF sequence is not active for type " + type);
        }

        if (sequence.getCurrentNumber() >= sequence.getMaxNumber()) {
            throw new BusinessException("NCF sequence exhausted for type " + type + ". Please contact DGII for new sequence.");
        }

        // Warn when approaching limit (90% used)
        long remaining = sequence.getMaxNumber() - sequence.getCurrentNumber();
        long total = sequence.getMaxNumber();
        double percentageUsed = ((double) sequence.getCurrentNumber() / total) * 100;

        if (percentageUsed > 90) {
            log.warn("NCF sequence {} is {}% used. Only {} numbers remaining.",
                    type, String.format("%.1f", percentageUsed), remaining);
        }
    }
}