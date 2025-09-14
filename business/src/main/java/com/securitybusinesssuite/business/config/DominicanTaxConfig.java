// path: business/src/main/java/com/securitybusinesssuite/business/config/DominicanTaxConfig.java
package com.securitybusinesssuite.business.config;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DominicanTaxConfig {
    public static final BigDecimal ITBIS_RATE = new BigDecimal("0.18");
    public static final String DEFAULT_SERVICE_CODE = "SF-0001";
    public static final String DEFAULT_SERVICE_DESCRIPTION = "SERVICIO SEGURIDAD PRIVADA";
    public static final String DEFAULT_CURRENCY = "DOP";
    public static final String RNC_PATTERN = "\\d{3}-\\d{7}-\\d{1}";

    public BigDecimal getItbisRate() {
        return ITBIS_RATE;
    }

    public String getDefaultServiceCode() {
        return DEFAULT_SERVICE_CODE;
    }

    public String getDefaultServiceDescription() {
        return DEFAULT_SERVICE_DESCRIPTION;
    }
}