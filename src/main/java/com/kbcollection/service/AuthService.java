package com.kbcollection.service;

import com.kbcollection.dto.LoginDTO;
import com.kbcollection.entity.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import at.favre.lib.crypto.bcrypt.BCrypt;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "JWT_SECRET")
    String jwtSecret;

    public String login(LoginDTO dto) {
        Usuario u = Usuario.find("email", dto.email).firstResult();

        if (u == null) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        if (!u.activo) {
            throw new RuntimeException("⛔ Tu cuenta ha sido bloqueada.");
        }

        if (!u.verificado && !u.role.contains("ADMIN")) {
            throw new RuntimeException("Debes verificar tu correo electrónico.");
        }

        BCrypt.Result res = BCrypt.verifyer().verify(dto.password.toCharArray(), u.passwordHash);
        if (!res.verified) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        Set<String> roles = new HashSet<>();
        roles.add(u.role);

        if ("SUPER_ADMIN".equals(u.role)) {
            roles.add("ADMIN");
        }

        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        return Jwt
                .issuer("kbcollection")
                .upn(u.email)
                .groups(roles)
                .expiresIn(Duration.ofHours(4))
                .sign(key);
    }
}