package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Cupon extends PanacheEntity {
    public String codigo;
    public double porcentaje;
    public boolean activo;

    // --- NUEVO (Opcional: NULL si es global) ---
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    public Empresa empresa; 
}