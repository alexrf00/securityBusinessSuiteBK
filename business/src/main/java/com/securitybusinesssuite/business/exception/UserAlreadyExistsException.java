// path: business/src/main/java/com/securitybusinesssuite/business/exception/UserAlreadyExistsException.java
package com.securitybusinesssuite.business.exception;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}