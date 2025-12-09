package com.kbcollection.resource;

import com.kbcollection.dto.CategoryForm;
import com.kbcollection.entity.Category;
import com.kbcollection.entity.Empresa;
import com.kbcollection.entity.Usuario;
import jakarta.annotation.security.PermitAll; // <--- IMPORTAR
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/api/categorias")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @GET
    @PermitAll // <--- PÚBLICO
    public List<Category> listar(@QueryParam("empresaId") Long empresaId) {
        if (empresaId != null) {
            return Category.find("empresa.id", empresaId).list();
        }
        return Category.listAll(); 
    }

    // ... (Métodos crear, actualizar, eliminar quedan igual con @RolesAllowed) ...
    @GET
    @Path("/{id}")
    @PermitAll
    public Response obtener(@PathParam("id") Long id) {
        Category c = Category.findById(id);
        return c != null ? Response.ok(c).build() : Response.status(404).build();
    }

    @POST
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response crear(CategoryForm form, @Context SecurityContext sec) {
        String email = sec.getUserPrincipal().getName();
        Usuario admin = Usuario.find("email", email).firstResult();
        Category c = new Category();
        c.nombre = form.nombre;
        c.descripcion = form.descripcion;
        c.imagenUrl = form.imagenUrl;
        if (admin.empresa != null) {
            c.empresa = admin.empresa;
        } else {
            c.empresa = Empresa.findById(1L);
        }
        c.persist();
        return Response.status(201).entity(c).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response actualizar(@PathParam("id") Long id, CategoryForm form) {
        Category c = Category.findById(id);
        if (c == null) return Response.status(404).build();
        c.nombre = form.nombre;
        c.descripcion = form.descripcion;
        c.imagenUrl = form.imagenUrl;
        return Response.ok(c).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response eliminar(@PathParam("id") Long id) {
        Category.deleteById(id);
        return Response.ok().build();
    }
}