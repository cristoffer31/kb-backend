package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase; // <--- CAMBIO
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario extends PanacheEntityBase { // <--- CAMBIO

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- OBLIGATORIO PARA MYSQL
    public Long id;

    @Column(nullable = false)
    public String nombre;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String role; 

    // --- NUEVOS CAMPOS ---
    public String telefono; 

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    public Empresa empresa; 

    public boolean verificado = false;
    public String tokenVerificacion;
    public String tokenRecuperacion;
    public LocalDateTime tokenExpiracion;
    public boolean activo = true;
}