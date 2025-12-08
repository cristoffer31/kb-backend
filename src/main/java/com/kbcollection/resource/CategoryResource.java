package com.kbcollection.resource;

import com.kbcollection.dto.CategoryForm;
import com.kbcollection.entity.Category;
import com.kbcollection.entity.Empresa;
import com.kbcollection.entity.Usuario;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/api/categorias")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @GET
    public List<Category> listar(@QueryParam("empresaId") Long empresaId) {
        if (empresaId != null) {
            return Category.find("empresa.id", empresaId).list();
        }
        // Si no se filtra, devuelve vacío o todo (según prefieras)
        // Para seguridad, mejor devolver vacío si no hay empresa
        return List.of(); 
    }

    @GET
    @Path("/{id}")
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

        // Asignar Empresa
        if (admin.empresa != null) {
            c.empresa = admin.empresa;
        } else {
            c.empresa = Empresa.findById(1L); // Fallback a KB
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
        // La empresa NO se cambia al editar
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