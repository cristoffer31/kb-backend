package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class Category extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nombre;
    public String descripcion;
    public String imagenUrl;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    public Empresa empresa;
}