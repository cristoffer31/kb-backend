package com.kbcollection.config;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.inject.Alternative;
import jakarta.annotation.Priority;

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
        // Esto asegura que FIRMA y VALIDACIÓN usen exactamente la misma cadena
        contextInfo.setSecretKeyContent(jwtSecret);
        contextInfo.setIssuedBy("kbcollection");
        return contextInfo;
    }
}
