package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class Empresa extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nombre;
    public String slug;
    public String logoUrl;
    public String colorPrimario;
    
    public static Empresa findById(Long id) {
        return find("id", id).firstResult();
    }
}