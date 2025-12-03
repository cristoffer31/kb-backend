package com.kbcollection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // <--- 1. IMPORTAR
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class PedidoItem extends PanacheEntity {

    @ManyToOne
    @JsonIgnore // <--- 2. AGREGAR ESTA LÍNEA (Rompe el bucle)
    public Pedido pedido;

    @ManyToOne
    public Producto producto;

    public int cantidad;
    public double precioUnitario;
}