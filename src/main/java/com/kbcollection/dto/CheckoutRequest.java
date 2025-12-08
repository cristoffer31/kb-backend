package com.kbcollection.dto;

import java.util.List;

public class CheckoutRequest {

    public static class Item {
        public Long productoId;
        public int cantidad;
    }

    public List<Item> items;
    public String cupon;
    public String metodoPago;
    
    public String direccion;
    public String departamento;
    public String coordenadas;
    public double costoEnvio;
    public String paypalOrderId;
    
    // --- NUEVO CAMPO ---
    public String telefono;
    // -------------------

    public String tipoComprobante;
    public String documentoFiscal; 
    public String nrc;
    public String razonSocial;
    public String giro;
}