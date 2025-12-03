package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class ZonaEnvio extends PanacheEntity {
    
    public String departamento;
    public double tarifa;
    
    @Column(columnDefinition = "TEXT")
    public String municipios; 
}