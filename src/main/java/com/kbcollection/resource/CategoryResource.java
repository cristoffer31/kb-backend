package com.kbcollection.resource;

import com.kbcollection.dto.CategoryForm;
import com.kbcollection.entity.Category;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/categorias")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @GET
    public List<Category> listar() {
        return Category.listAll();
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") Long id) {
        Category c = Category.findById(id);
        if (c == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(c).build();
    }

    @POST
    @Transactional
    @RolesAllowed("ADMIN")
    public Response crear(CategoryForm form) {
        Category c = new Category();
        c.nombre = form.nombre;
        c.descripcion = form.descripcion;
        c.imagenUrl = form.imagenUrl; // <--- GUARDAR IMAGEN
        c.persist();
        return Response.status(Response.Status.CREATED).entity(c).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response actualizar(@PathParam("id") Long id, CategoryForm form) {
        Category c = Category.findById(id);
        if (c == null) return Response.status(Response.Status.NOT_FOUND).build();

        c.nombre = form.nombre;
        c.descripcion = form.descripcion;
        c.imagenUrl = form.imagenUrl; // <--- ACTUALIZAR IMAGEN
        return Response.ok(c).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response eliminar(@PathParam("id") Long id) {
        boolean deleted = Category.deleteById(id);
        if (!deleted) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok().build();
    }
}