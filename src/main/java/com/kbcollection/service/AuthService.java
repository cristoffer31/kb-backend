package com.kbcollection.service;

import com.kbcollection.dto.LoginDTO;
import com.kbcollection.entity.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.HashSet; // <--- IMPORTANTE
import java.util.Set;

import at.favre.lib.crypto.bcrypt.BCrypt;

@ApplicationScoped
public class AuthService {

    public String login(LoginDTO dto) {
        System.out.println("🔍 INTENTO LOGIN: " + dto.email);
        Usuario u = Usuario.find("email", dto.email).firstResult();

        if (u == null) {
            System.out.println("❌ LOGIN FALLO: Usuario null");
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }
        System.out.println("✅ Usuario encontrado: " + u.email + " | Activo: " + u.activo + " | Verificado: "
                + u.verificado + " | Rol: " + u.role);

        // 1. Bloqueo Anti-Fraude (Si está inactivo o no verificado)
        if (!u.activo) {
            System.out.println("❌ LOGIN FALLO: Usuario inactivo");
            throw new RuntimeException("⛔ Tu cuenta ha sido bloqueada.");
        }

        // Excepción: Los ADMIN y SUPER_ADMIN pueden entrar sin verificar correo si
        // quieres
        if (!u.verificado && !u.role.contains("ADMIN")) {
            System.out.println("❌ LOGIN FALLO: No verificado");
            throw new RuntimeException("Debes verificar tu correo electrónico.");
        }

        BCrypt.Result res = BCrypt.verifyer().verify(dto.password.toCharArray(), u.passwordHash);
        if (!res.verified) {
            System.out.println("❌ LOGIN FALLO: Password incorrecto. hashDB=" + u.passwordHash);
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }
        System.out.println("✅ Password correcto. Generando Token...");

        // --- CORRECCIÓN CLAVE: GESTIÓN DE ROLES ---
        Set<String> roles = new HashSet<>();
        roles.add(u.role); // Agregamos su rol real (ej: SUPER_ADMIN)

        // Si es SUPER_ADMIN, le "regalamos" el rol ADMIN para que pueda entrar a todo
        if ("SUPER_ADMIN".equals(u.role)) {
            roles.add("ADMIN");
        }
        // ------------------------------------------

        // GENERACIÓN MANUAL DE CLAVE (Evita error SRJWT05028)
        // Forzamos que el String sea tratado como una clave HMAC pura (HmacSHA256)
        // nos aseguramos que tenga longitud segura si es posible, pero confiamos en la
        // inyección.
        Key key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        return Jwt
                .issuer("kbcollection")
                .upn(u.email)
                .groups(roles) // Enviamos TODOS los roles
                .expiresIn(Duration.ofHours(4))
                .sign(key); // <--- FIRMAMOS CON LA CLAVE MANUALMENTE
    }
}