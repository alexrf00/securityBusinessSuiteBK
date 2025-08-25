// path: business/src/main/java/com/securitybusinesssuite/business/service/UserService.java
package com.securitybusinesssuite.business.service;

import com.securitybusinesssuite.business.dto.UserDto;
import com.securitybusinesssuite.data.entity.User;

import java.util.UUID;

public interface UserService {
    UserDto getUserById(UUID id);
    User findByEmail(String email);
    User findById(UUID id);
}