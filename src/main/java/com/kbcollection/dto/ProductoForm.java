package com.kbcollection.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO para crear/actualizar productos con validaciones robustas.
 */
public class ProductoForm {
    
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    public String nombre;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    public String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "999999.99", message = "El precio es demasiado alto")
    public Double precio;
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    public Integer stock;
    
    @Size(max = 100, message = "El código de barras es demasiado largo")
    public String codigoBarras;
    
    @Size(max = 500, message = "La URL de imagen es demasiado larga")
    public String imagenUrl;
    
    public Long categoryId;
    
    @DecimalMin(value = "0.0", message = "El precio de oferta no puede ser negativo")
    public Double precioOferta;
    
    public Boolean enOferta;

    // --- VARIANTES ---
    @Size(max = 50, message = "La talla es demasiado larga")
    public String talla;
    
    @Size(max = 100, message = "La variante es demasiado larga")
    public String variante;
    
    @Size(max = 100, message = "El código agrupador es demasiado largo")
    public String codigoAgrupador;

    public List<ReglaPrecio> preciosMayoreo;

    public static class ReglaPrecio {
        @Min(value = 1, message = "La cantidad mínima debe ser al menos 1")
        public int cantidadMin;
        
        @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
        public double precioUnitario;
    }
}