// path: business/src/main/java/com/securitybusinesssuite/business/exception/InvoiceNotFoundException.java
package com.securitybusinesssuite.business.exception;

public class InvoiceNotFoundException extends BusinessException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}