package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Cupon extends PanacheEntity {

    public String codigo;

    public double porcentaje;

    public boolean activo;
}
