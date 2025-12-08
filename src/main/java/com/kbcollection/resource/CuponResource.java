package com.kbcollection.resource;

import com.kbcollection.entity.Cupon;
import com.kbcollection.entity.Usuario;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

@Path("/api/cupones")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CuponResource {

    @GET
    @Path("/validar/{codigo}")
    @PermitAll
    public Response validar(@PathParam("codigo") String codigo) {
        // Busca cupón activo por código (sin importar empresa por ahora, o podrías filtrar)
        Cupon c = Cupon.find("codigo = ?1 and activo = true", codigo.toUpperCase()).firstResult();
        
        if (c == null) {
            return Response.status(404).entity(Map.of("error", "Cupón no válido")).build();
        }
        
        return Response.ok(Map.of(
            "codigo", c.codigo,
            "porcentaje", c.porcentaje,
            "empresaId", c.empresa != null ? c.empresa.id : "GLOBAL"
        )).build();
    }

    @GET
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public List<Cupon> listar(@QueryParam("empresaId") Long empresaId) {
        if (empresaId != null) {
            return Cupon.find("empresa.id", empresaId).list();
        }
        return Cupon.listAll();
    }

    @POST
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response crear(Cupon cupon, @Context SecurityContext sec) {
        if (cupon.codigo == null || cupon.porcentaje <= 0) return Response.status(400).build();
        
        String email = sec.getUserPrincipal().getName();
        Usuario admin = Usuario.find("email", email).firstResult();

        cupon.codigo = cupon.codigo.toUpperCase();
        cupon.activo = true;
        
        if (admin.empresa != null) {
            cupon.empresa = admin.empresa;
        } else {
            // Super Admin crea cupón GLOBAL (empresa = null) o asignado (si se implementa lógica)
            cupon.empresa = null; 
        }

        cupon.persist();
        return Response.ok(cupon).build();
    }

    @PUT
    @Path("/{id}/toggle")
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response alternarEstado(@PathParam("id") Long id) {
        Cupon c = Cupon.findById(id);
        if (c == null) return Response.status(404).build();
        c.activo = !c.activo;
        return Response.ok(c).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response eliminar(@PathParam("id") Long id) {
        Cupon.deleteById(id);
        return Response.ok().build();
    }
}