// path: business/src/main/java/com/securitybusinesssuite/business/service/NCFService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.data.entity.Client;
import com.securitybusinesssuite.data.entity.Invoice;

public interface NCFService {
    String generateNCF(Client client);
    Invoice.NCFType determineNCFType(Client client);
    boolean isNCFRequired(Client client);
    void validateNCFAvailability(Invoice.NCFType type);
}