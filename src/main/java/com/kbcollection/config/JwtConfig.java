package com.kbcollection.config;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.inject.Alternative;
import jakarta.annotation.Priority;

import java.util.Set;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;

@Dependent
public class JwtConfig {

    @ConfigProperty(name = "JWT_SECRET")
    String jwtSecret;

    @Produces
    @Dependent
    @Alternative
    @Priority(1)
    JWTAuthContextInfo getJWTAuthContextInfo() {
        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo();

        // Forzamos el uso del secreto inyectado para validación
        contextInfo.setSecretKeyContent(jwtSecret);
        contextInfo.setIssuedBy("kbcollection");

        // CRÍTICO: Si no definimos esto, espera RS256 por defecto y rechaza nuestro
        // token HS256
        contextInfo.setSignatureAlgorithm(Set.of(SignatureAlgorithm.HS256));

        return contextInfo;
    }
}
