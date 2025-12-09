package com.kbcollection.resource;

import com.kbcollection.entity.CarouselImage;
import com.kbcollection.entity.Empresa;
import com.kbcollection.entity.Usuario;
import jakarta.annotation.security.PermitAll; // <--- IMPORTANTE
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/api/carousel")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarouselResource {

    @GET
    @PermitAll // <--- ¡ESTO FALTABA! Hace que el carrusel sea público
    public List<CarouselImage> listar(@QueryParam("empresaId") Long empresaId) {
        if (empresaId != null) {
            return CarouselImage.find("empresa.id", empresaId).list();
        }
        // Si no se especifica empresa, devolver lista vacía o default
        return List.of(); 
    }

    @POST
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response crear(CarouselImage img, @Context SecurityContext sec) {
        if (img.imageUrl == null || img.imageUrl.isEmpty()) {
            return Response.status(400).build();
        }

        String email = sec.getUserPrincipal().getName();
        Usuario admin = Usuario.find("email", email).firstResult();

        // Asignar empresa automáticamente
        if (admin.empresa != null) {
            img.empresa = admin.empresa;
        } else {
            // Si es Super Admin, por defecto a la empresa 1 (KB) si no especifica otra lógica
            img.empresa = Empresa.findById(1L);
        }

        img.persist();
        return Response.ok(img).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response eliminar(@PathParam("id") Long id) {
        CarouselImage.deleteById(id);
        return Response.ok().build();
    }
}