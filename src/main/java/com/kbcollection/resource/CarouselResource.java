package com.kbcollection.resource;

import com.kbcollection.entity.CarouselImage;
import com.kbcollection.entity.Empresa;
import com.kbcollection.entity.Usuario;
import jakarta.annotation.security.PermitAll; // <--- IMPORTAR
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/api/carousel")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarouselResource {

    @GET
    @PermitAll // <--- PÚBLICO: Importante para que cargue la Home sin login
    public List<CarouselImage> listar(@QueryParam("empresaId") Long empresaId) {
        if (empresaId != null) {
            return CarouselImage.find("empresa.id", empresaId).list();
        }
        return CarouselImage.listAll(); 
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
        if (admin.empresa != null) {
            img.empresa = admin.empresa;
        } else {
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