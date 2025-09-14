// path: business/src/main/java/com/securitybusinesssuite/business/dto/searchDTO/ClientSearchCriteria.java
package com.securitybusinesssuite.business.dto.search;

import com.securitybusinesssuite.data.entity.Client;
import lombok.Data;

@Data
public class ClientSearchCriteria {
    private String businessName;
    private Client.ClientType clientType;
    private Client.ClientStatus status;
    private Client.BusinessSector businessSector;
    private String rnc;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}