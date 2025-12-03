package com.kbcollection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // <--- IMPORTAR
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class PrecioMayoreo extends PanacheEntity {

    @ManyToOne
    @JsonIgnore // <--- ESTO ES CRÍTICO: Evita que se vuelva a pintar el producto
    public Producto producto;

    public int cantidadMin;

    public double precioUnitario;
}