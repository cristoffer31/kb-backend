package com.kbcollection.resource;

import com.kbcollection.dto.CheckoutRequest;
import com.kbcollection.entity.*;
import com.kbcollection.service.TwilioService;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/api/pedidos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CheckoutResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    TwilioService twilioService; // <--- Notificaciones

    @GET
    @Path("/mis-pedidos")
    @RolesAllowed({"USER", "ADMIN", "SUPER_ADMIN"}) 
    public Response misPedidos() {
        String email = identity.getPrincipal().getName();
        List<Pedido> pedidos = Pedido.find("usuario.email", Sort.descending("id"), email).list();
        return Response.ok(pedidos).build();
    }

    @POST
    @Transactional
    public Response crearPedido(CheckoutRequest req) {
        Usuario usuario = null;
        if (identity.getPrincipal() != null && identity.getPrincipal().getName() != null) {
            usuario = Usuario.find("email", identity.getPrincipal().getName()).firstResult();
        }

        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Usuario no identificado").build();
        }

        double subtotal = 0;

        // 1. Validaciones y Cálculo
        for (var item : req.items) {
            Producto p = Producto.findById(item.productoId);
            if (p == null) return Response.status(400).entity("Producto no encontrado").build();
            if (p.stock < item.cantidad) return Response.status(400).entity("Sin stock: " + p.nombre).build();

            double precioUnitario = p.precio;
            if (p.enOferta && p.precioOferta > 0) precioUnitario = p.precioOferta;

            PrecioMayoreo pm = PrecioMayoreo.find(
                    "producto.id = ?1 AND cantidadMin <= ?2 ORDER BY cantidadMin DESC",
                    p.id, item.cantidad
            ).firstResult();

            if (pm != null && pm.precioUnitario < precioUnitario) precioUnitario = pm.precioUnitario;
            
            subtotal += (precioUnitario * item.cantidad);
        }

        // 2. Cupones
        double descuento = 0;
        if (req.cupon != null && !req.cupon.isBlank()) {
            Cupon cupon = Cupon.find("codigo", req.cupon).firstResult();
            if (cupon != null && cupon.activo) {
                descuento = subtotal * (cupon.porcentaje / 100.0);
            }
        }

        double costoEnvio = req.costoEnvio > 0 ? req.costoEnvio : 0;
        double total = subtotal - descuento + costoEnvio;

        // 3. Guardar Pedido
        Pedido pedido = new Pedido();
        pedido.usuario = usuario;
        pedido.telefono = req.telefono; // <--- Importante para Twilio
        pedido.subtotal = subtotal;
        pedido.descuento = descuento;
        pedido.costoEnvio = costoEnvio;
        pedido.total = total;
        pedido.metodoPago = req.metodoPago != null ? req.metodoPago : "PAYPAL";
        pedido.status = "PENDIENTE"; 
        pedido.direccion = req.direccion;
        pedido.departamento = req.departamento;
        pedido.coordenadas = req.coordenadas;
        pedido.paypalOrderId = req.paypalOrderId;
        
        pedido.tipoComprobante = req.tipoComprobante != null ? req.tipoComprobante : "CONSUMIDOR_FINAL";
        pedido.documentoFiscal = req.documentoFiscal;
        pedido.nrc = req.nrc;
        pedido.razonSocial = req.razonSocial;
        pedido.giro = req.giro;
        
        pedido.persist();

        // 4. Guardar Items y Restar Stock
        for (var item : req.items) {
            Producto p = Producto.findById(item.productoId);
            PedidoItem it = new PedidoItem();
            it.pedido = pedido;
            it.producto = p;
            it.cantidad = item.cantidad;
            
            double precioFinal = p.precio;
            if (p.enOferta && p.precioOferta > 0) precioFinal = p.precioOferta;
            
            PrecioMayoreo pm = PrecioMayoreo.find("producto.id = ?1 AND cantidadMin <= ?2 ORDER BY cantidadMin DESC", p.id, item.cantidad).firstResult();
            if (pm != null && pm.precioUnitario < precioFinal) precioFinal = pm.precioUnitario;

            it.precioUnitario = precioFinal;
            it.persist();

            p.stock -= item.cantidad;
        }
        
        // 5. Notificación Twilio (Hilo Virtual)
        Thread.ofVirtual().start(() -> {
            twilioService.notificarPedidoAdmin(pedido);
        });

        return Response.ok(Map.of("mensaje", "Pedido creado", "id", pedido.id, "total", total)).build();
    }
    
    @GET
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response listarPedidos() {
        String email = identity.getPrincipal().getName();
        Usuario admin = Usuario.find("email", email).firstResult();

        List<Pedido> pedidos;

        if ("SUPER_ADMIN".equals(admin.role)) {
            // El Dueño ve TODO
            pedidos = Pedido.listAll(Sort.descending("id"));
        } else {
            // El Admin de Empresa solo ve pedidos que contengan SUS productos
            if (admin.empresa == null) return Response.ok(List.of()).build();
            
            pedidos = Pedido.find(
                "SELECT DISTINCT p FROM Pedido p JOIN p.items i WHERE i.producto.empresa.id = ?1 ORDER BY p.id DESC", 
                admin.empresa.id
            ).list();
        }

        return Response.ok(pedidos).build();
    }
    
    @GET
    @Path("/{id}")
    @RolesAllowed({"USER", "ADMIN", "SUPER_ADMIN"})
    public Response obtenerPedido(@PathParam("id") Long id) {
        Pedido p = Pedido.findById(id);
        return p != null ? Response.ok(p).build() : Response.status(404).build();
    }
    
    @PUT
    @Path("/{id}/status")
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response cambiarEstado(@PathParam("id") Long id, Map<String,String> body) {
        Pedido p = Pedido.findById(id);
        if(p == null) return Response.status(404).build();
        if(body.containsKey("status")) p.status = body.get("status");
        return Response.ok(Map.of("mensaje", "Estado actualizado")).build();
    }
}