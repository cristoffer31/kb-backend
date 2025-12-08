package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Category extends PanacheEntity {

    public String nombre;
    public String descripcion;
    public String imagenUrl;

    // --- NUEVO: RELACIÓN MULTI-EMPRESA ---
    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    public Empresa empresa;
}