package com.kbcollection.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.kbcollection.dto.RegisterDTO;
import com.kbcollection.entity.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class UsuarioService {

    @Inject
    TwilioService twilioService;

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
        u.telefono = dto.telefono; // Guardamos el teléfono
        
        // Generar código numérico de 6 dígitos
        String codigo = String.valueOf((int)(Math.random() * 900000) + 100000);
        
        u.tokenVerificacion = codigo;
        u.tokenExpiracion = LocalDateTime.now().plusMinutes(15);
        u.verificado = false;
        
        u.persist();

        // Enviar SMS si el usuario proporcionó teléfono
        if (u.telefono != null && !u.telefono.isBlank()) {
            Thread.ofVirtual().start(() -> {
                twilioService.enviarSmsVerificacion(u.telefono, codigo);
            });
        }

        return u;
    }

    @Transactional
    public boolean verificarCuenta(String token) {
        Usuario u = Usuario.find("tokenVerificacion", token).firstResult();
        
        if (u == null) return false;
        if (u.tokenExpiracion == null || LocalDateTime.now().isAfter(u.tokenExpiracion)) {
            return false;
        }

        u.verificado = true;
        u.tokenVerificacion = null;
        u.tokenExpiracion = null;
        u.persist();
        return true;
    }
}