package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class Cupon extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String codigo;
    public double porcentaje;
    public boolean activo;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    public Empresa empresa;
}