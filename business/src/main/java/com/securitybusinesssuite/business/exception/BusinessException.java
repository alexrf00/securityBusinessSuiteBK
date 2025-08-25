// path: business/src/main/java/com/securitybusinesssuite/business/exception/BusinessException.java
package com.securitybusinesssuite.business.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}