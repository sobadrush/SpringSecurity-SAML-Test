package com.example.saml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot SAML2 認證應用程式
 * 
 * 此應用程式示範如何使用 Spring Security SAML2 進行單一登入 (SSO)
 * 使用 CAS Server 作為 Identity Provider (IDP)
 */
@SpringBootApplication
public class SamlApplication {

    public static void main(String[] args) {
        SpringApplication.run(SamlApplication.class, args);
    }

}
