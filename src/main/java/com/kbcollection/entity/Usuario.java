package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario extends PanacheEntity {

    @Column(nullable = false)
    public String nombre;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String role; // "USER", "ADMIN", "SUPER_ADMIN"

    // --- SEGURIDAD ---
    public boolean verificado = false; // Email confirmado
    public String tokenVerificacion;
    public String tokenRecuperacion;
    public LocalDateTime tokenExpiracion;

    // --- NUEVO: BLOQUEO DE CUENTA ---
    public boolean activo = true; // Si es false, no puede entrar
}