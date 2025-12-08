package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class CarouselImage extends PanacheEntity {
    public String imageUrl;
    public String titulo;

    // --- NUEVO ---
    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    public Empresa empresa;
}