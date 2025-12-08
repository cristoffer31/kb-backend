package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Empresa extends PanacheEntity {
    public String nombre;
    public String slug;        // "kb", "kpbm", "sabesa"
    public String logoUrl;
    public String colorPrimario;
    
    // Método helper para encontrar por ID de forma segura
    public static Empresa findById(Long id) {
        return find("id", id).firstResult();
    }
}