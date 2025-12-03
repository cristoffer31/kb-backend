package com.kbcollection.resource;

import com.kbcollection.entity.Cupon;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/api/cupones")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CuponResource {

    // --- NUEVO ENDPOINT PÚBLICO: VALIDAR CUPÓN ---
    @GET
    @Path("/validar/{codigo}")
    @PermitAll // <--- Cualquiera puede consultar si un cupón es válido
    public Response validar(@PathParam("codigo") String codigo) {
        Cupon c = Cupon.find("codigo", codigo.toUpperCase()).firstResult();
        
        if (c == null || !c.activo) {
            return Response.status(404).entity(Map.of("error", "Cupón no válido o expirado")).build();
        }
        
        return Response.ok(Map.of(
            "codigo", c.codigo,
            "porcentaje", c.porcentaje
        )).build();
    }
    // ---------------------------------------------

    @GET
    @RolesAllowed("ADMIN") // <--- Protegido
    public List<Cupon> listar() {
        return Cupon.listAll();
    }

    @POST
    @Transactional
    @RolesAllowed("ADMIN") // <--- Protegido
    public Response crear(Cupon cupon) {
        if (cupon.codigo == null || cupon.porcentaje <= 0) {
            return Response.status(400).build();
        }
        cupon.codigo = cupon.codigo.toUpperCase();
        cupon.activo = true;
        cupon.persist();
        return Response.ok(cupon).build();
    }

    @PUT
    @Path("/{id}/toggle")
    @Transactional
    @RolesAllowed("ADMIN") // <--- Protegido
    public Response alternarEstado(@PathParam("id") Long id) {
        Cupon c = Cupon.findById(id);
        if (c == null) return Response.status(404).build();
        c.activo = !c.activo;
        return Response.ok(c).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN") // <--- Protegido
    public Response eliminar(@PathParam("id") Long id) {
        Cupon.deleteById(id);
        return Response.ok().build();
    }
}