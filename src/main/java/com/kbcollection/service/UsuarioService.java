package com.kbcollection.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.kbcollection.dto.RegisterDTO;
import com.kbcollection.entity.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import java.time.LocalDateTime; // <--- IMPORTAR

@ApplicationScoped
public class UsuarioService {

    @Transactional
    public Usuario registrar(RegisterDTO dto) {
        if (Usuario.find("email", dto.email).firstResult() != null) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Usuario u = new Usuario();
        u.nombre = dto.nombre;
        u.email = dto.email;
        u.passwordHash = BCrypt.withDefaults().hashToString(12, dto.password.toCharArray());
        u.role = "USER";
        
        u.verificado = false;
        u.tokenVerificacion = UUID.randomUUID().toString();
        
        // --- SEGURIDAD: 5 MINUTOS DE VALIDEZ ---
        u.tokenExpiracion = LocalDateTime.now().plusMinutes(5); 
        // ---------------------------------------
        
        u.persist();
        return u;
    }

    @Transactional
    public boolean verificarCuenta(String token) {
        Usuario u = Usuario.find("tokenVerificacion", token).firstResult();
        
        // 1. Si no existe el token
        if (u == null) return false;

        // 2. Si la fecha actual es DESPUÉS de la fecha de expiración
        if (u.tokenExpiracion == null || LocalDateTime.now().isAfter(u.tokenExpiracion)) {
            return false; // El token venció
        }

        // Si pasa las validaciones, activamos
        u.verificado = true;
        u.tokenVerificacion = null;
        u.tokenExpiracion = null; // Limpiamos
        u.persist();
        return true;
    }
}