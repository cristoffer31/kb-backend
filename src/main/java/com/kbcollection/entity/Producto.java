package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Producto extends PanacheEntity {

    public String nombre;
    public String descripcion;
    public double precio;
    public int stock;
    public String codigoBarras;
    public String imagenUrl;

    public double precioOferta;
    public boolean enOferta;

    // --- VARIANTES (SÍ) ---
    public String talla;            // Ej: "P", "M"
    public String variante;         // Ej: "Lavanda"
    public String codigoAgrupador;  // Ej: "HUGGIES-001"
    // ----------------------

    // (Sin Fardos)

    @ManyToOne
    public Category category;
    
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<PrecioMayoreo> preciosMayoreo = new ArrayList<>();
}