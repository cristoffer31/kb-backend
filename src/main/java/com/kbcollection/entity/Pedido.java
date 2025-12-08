package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Pedido extends PanacheEntity {

    public double subtotal;
    public double descuento;
    public double costoEnvio;
    public double total;

     public String telefono;

     
    @Column(length = 50)
    public String metodoPago;

    @Column(length = 20)
    public String status;

    @Column(columnDefinition = "TEXT")
    public String direccion;

    public String departamento;
    public String coordenadas;

    // --- DATOS FISCALES COMPLETOS ---
    public String tipoComprobante; // "CONSUMIDOR_FINAL" o "CREDITO_FISCAL"
    
    // Campos para Crédito Fiscal
    public String documentoFiscal; // NIT
    public String nrc;             // Número de Registro
    public String razonSocial;     // Nombre de la empresa
    public String giro;            // Actividad económica
    // -------------------------------

    public String paypalOrderId;
    public LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("pedidos")
    public Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    public List<PedidoItem> items;
    
}