// path: data/src/main/java/com/securitybusinesssuite/data/repository/ClientRepository.java
package com.securitybusinesssuite.data.repository;

import com.securitybusinesssuite.data.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    Client save(Client client);
    Client update(Client client);
    Optional<Client> findById(UUID id);
    Optional<Client> findByClientCode(String clientCode);
    Optional<Client> findByRnc(String rnc);
    List<Client> findByBusinessNameContaining(String businessName);
    List<Client> findByStatus(Client.ClientStatus status);
    List<Client> findByClientType(Client.ClientType clientType);
    Page<Client> findAll(Pageable pageable);
    Page<Client> findByFilters(String businessName, Client.ClientType clientType,
                               Client.ClientStatus status, Client.BusinessSector sector, Pageable pageable);
    boolean existsByRnc(String rnc);
    boolean existsByClientCode(String clientCode);
    long countByStatus(Client.ClientStatus status);
    void deleteById(UUID id);
}