// path: web/src/main/java/com/securitybusinesssuite/web/SecurityBusinessSuiteApplication.java
package com.securitybusinesssuite.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.securitybusinesssuite")
public class SecurityBusinessSuiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityBusinessSuiteApplication.class, args);
    }
}