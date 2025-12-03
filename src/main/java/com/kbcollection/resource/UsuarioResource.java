package com.kbcollection.resource;

import com.kbcollection.entity.Usuario;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/api/usuarios")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    // Solo el SUPER_ADMIN puede ver la lista completa
    @GET
    @RolesAllowed("SUPER_ADMIN") 
    public List<Usuario> listar() {
        return Usuario.listAll(io.quarkus.panache.common.Sort.descending("id"));
    }

    // BLOQUEAR / DESBLOQUEAR USUARIO
    @PUT
    @Path("/{id}/bloqueo")
    @RolesAllowed("SUPER_ADMIN")
    @Transactional
    public Response alternarBloqueo(@PathParam("id") Long id) {
        Usuario u = Usuario.findById(id);
        if (u == null) return Response.status(404).build();

        // SEGURIDAD: No te puedes bloquear a ti mismo ni a otro Super Admin
        if (u.role.equals("SUPER_ADMIN")) {
            return Response.status(403).entity(Map.of("error", "No puedes bloquear a un Super Admin")).build();
        }

        u.activo = !u.activo; // Cambiar estado
        return Response.ok(u).build();
    }

    // CAMBIAR ROL (Ascender a Admin / Degradar a User)
    @PUT
    @Path("/{id}/rol")
    @RolesAllowed("SUPER_ADMIN")
    @Transactional
    public Response cambiarRol(@PathParam("id") Long id, Map<String, String> body) {
        Usuario u = Usuario.findById(id);
        if (u == null) return Response.status(404).build();

        String nuevoRol = body.get("role"); // Esperamos "ADMIN" o "USER"

        if (u.role.equals("SUPER_ADMIN")) {
            return Response.status(403).entity(Map.of("error", "No se puede degradar al Super Admin principal")).build();
        }

        if (nuevoRol != null && (nuevoRol.equals("ADMIN") || nuevoRol.equals("USER"))) {
            u.role = nuevoRol;
            return Response.ok(u).build();
        }

        return Response.status(400).build();
    }
}