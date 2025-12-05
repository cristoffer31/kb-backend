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
        // GENERACIÓN MANUAL DE CLAVE (Estándar Base64)
        // Quarkus interpreta 'mp.jwt.verify.publickey' como Base64 en modo Raw.
        // Por tanto, debemos decodificar el String Base64 para obtener los bytes
        // reales.
        try {
            byte[] encodedKey = java.util.Base64.getDecoder().decode(jwtSecret);
            SecretKey key = new SecretKeySpec(encodedKey, "HmacSHA256");

            return Jwt
                    .issuer("kbcollection")
                    .upn(u.email)
                    .groups(roles)
                    .expiresIn(Duration.ofHours(4))
                    .sign(key);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "CRÍTICO: El JWT_SECRET en Railway NO es un Base64 válido. Debe ser generado correctamente.");
        }
    }
}