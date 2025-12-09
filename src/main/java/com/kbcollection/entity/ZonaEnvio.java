package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class ZonaEnvio extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    public String departamento;
    public double tarifa;
    
    @Column(columnDefinition = "TEXT")
    public String municipios; 
}