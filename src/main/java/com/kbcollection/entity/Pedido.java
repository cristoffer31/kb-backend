package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Pedido extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public double subtotal;
    public double descuento;
    public double costoEnvio;
    public double total;
    
    @Column(length = 50)
    public String metodoPago;
    @Column(length = 20)
    public String status;
    
    @Column(columnDefinition = "TEXT")
    public String direccion;
    public String departamento;
    public String coordenadas;
    public String telefono; 

    public String tipoComprobante;
    public String documentoFiscal; 
    public String nrc;             
    public String razonSocial;    
    public String giro;            

    public String paypalOrderId;
    public LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("pedidos")
    public Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    public List<PedidoItem> items;
}