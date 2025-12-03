package com.kbcollection.dto;

import java.util.List;

public class ProductoForm {
    public String nombre;
    public String descripcion;
    public Double precio;
    public Integer stock;
    public String codigoBarras;
    public String imagenUrl;
    public Long categoryId;
    
    public Double precioOferta;
    public Boolean enOferta;

    // --- VARIANTES ---
    public String talla;
    public String variante;
    public String codigoAgrupador;

    public List<ReglaPrecio> preciosMayoreo;

    public static class ReglaPrecio {
        public int cantidadMin;
        public double precioUnitario;
    }
}