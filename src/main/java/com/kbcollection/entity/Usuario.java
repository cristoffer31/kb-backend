package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
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

    // --- RELACIÓN MULTI-EMPRESA ---
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    public Empresa empresa; // NULL = Super Admin Global o Cliente
    // -----------------------------

    public boolean verificado = false;
    public String tokenVerificacion;
    public String tokenRecuperacion;
    public LocalDateTime tokenExpiracion;
    public boolean activo = true;
    
    public String telefono;
}