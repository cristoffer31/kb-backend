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

        if (u == null || !u.activo) {
            throw new RuntimeException("Credenciales inválidas o cuenta bloqueada");
        }

        if (!BCrypt.verifyer().verify(dto.password.toCharArray(), u.passwordHash).verified) {
            throw new RuntimeException("Credenciales inválidas");
        }

        Set<String> roles = new HashSet<>();
        roles.add(u.role);

        // --- LÓGICA EMPRESA ---
        String empresaId = "GLOBAL";
        if (u.empresa != null) {
            empresaId = u.empresa.id.toString();
        } else if ("SUPER_ADMIN".equals(u.role)) {
            empresaId = "ALL"; 
            roles.add("ADMIN");
        }

        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        return Jwt.issuer("kbcollection")
                .upn(u.email)
                .groups(roles)
                .claim("id", u.id)
                .claim("empresaId", empresaId)
                .expiresIn(Duration.ofHours(8))
                .sign(key);
    }
}