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

        // GENERACIÓN MANUAL DE CLAVE (Estándar)
        // Usamos la clave directa tal cual viene de la configuración (igual que el
        // validador).
        // IMPORTANTE: El JWT_SECRET en Railway DEBE tener 32+ caracteres (256 bits)
        // para HS256.
        // GENERACIÓN MANUAL DE CLAVE (Robustez alineada)
        // Quarkus (smallrye.jwt.verify.key) lee el secreto como Raw String por defecto.
        // Para que coincida la firma, usamos el secreto tal cual (sin decodificar
        // Base64).
        // Esto funciona perfecto aunque la cadena sea Base64 (será una clave de ~44
        // bytes).
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        return Jwt
                .issuer("kbcollection")
                .upn(u.email)
                .groups(roles)
                .expiresIn(Duration.ofHours(4))
                .sign(key);
    }
}