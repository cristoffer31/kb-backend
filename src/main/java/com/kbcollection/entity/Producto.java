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

    public String talla;
    public String variante;
    public String codigoAgrupador;

    @ManyToOne
    public Category category;
    
    // --- RELACIÓN MULTI-EMPRESA ---
    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    public Empresa empresa;
    // -----------------------------
    
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<PrecioMayoreo> preciosMayoreo = new ArrayList<>();
}