package com.kbcollection.resource;

import com.kbcollection.entity.CarouselImage;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/carousel")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarouselResource {

    @GET
    public List<CarouselImage> listar() {
        return CarouselImage.listAll();
    }

    @POST
    @Transactional
    @RolesAllowed("ADMIN")
    public Response crear(CarouselImage img) {
        if (img.imageUrl == null || img.imageUrl.isEmpty()) {
            return Response.status(400).build();
        }
        img.persist();
        return Response.ok(img).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response eliminar(@PathParam("id") Long id) {
        CarouselImage.deleteById(id);
        return Response.ok().build();
    }
}