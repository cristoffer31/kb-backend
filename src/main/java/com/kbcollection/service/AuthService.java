package com.kbcollection.service;

import com.kbcollection.dto.LoginDTO;
import com.kbcollection.entity.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import javax.crypto.SecretKey; // <--- NUEVO IMPORT
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

        // 1. Bloqueo Anti-Fraude (Si está inactivo o no verificado)
        if (!u.activo) {
            throw new RuntimeException("⛔ Tu cuenta ha sido bloqueada.");
        }

        // Excepción: Los ADMIN y SUPER_ADMIN pueden entrar sin verificar correo si
        // quieres
        if (!u.verificado && !u.role.contains("ADMIN")) {
            throw new RuntimeException("Debes verificar tu correo electrónico.");
        }

        BCrypt.Result res = BCrypt.verifyer().verify(dto.password.toCharArray(), u.passwordHash);
        if (!res.verified) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        // --- CORRECCIÓN CLAVE: GESTIÓN DE ROLES ---
        Set<String> roles = new HashSet<>();
        roles.add(u.role); // Agregamos su rol real (ej: SUPER_ADMIN)

        // Si es SUPER_ADMIN, le "regalamos" el rol ADMIN para que pueda entrar a todo
        if ("SUPER_ADMIN".equals(u.role)) {
            roles.add("ADMIN");
        }
        // ------------------------------------------

        // GENERACIÓN MANUAL DE CLAVE (Robustez total)
        // Solución al error SRJWT05012: La clave era muy corta (216 bits).
        // Hasheamos el secreto con SHA-256 para obtener SIEMPRE 32 bytes (256 bits).
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            SecretKey key = new SecretKeySpec(hash, "HmacSHA256");

            return Jwt
                    .issuer("kbcollection")
                    .upn(u.email)
                    .groups(roles) // Enviamos TODOS los roles
                    .expiresIn(Duration.ofHours(4))
                    .sign(key); // <--- FIRMAMOS CON LA CLAVE MANUALMENTE DE 256 BITS
        } catch (Exception e) {
            throw new RuntimeException("Error generando clave JWT: " + e.getMessage());
        }
    }
}