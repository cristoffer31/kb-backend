package com.kbcollection.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class TokenService {

    public String generarToken(Long id, String email, String role) {
        return Jwt.issuer("kb-collection")
                .upn(email)
                .groups(Set.of(role))
                .claim("id", id)
                .sign();
    }
}
