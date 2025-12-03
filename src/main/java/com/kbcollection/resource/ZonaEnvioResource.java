package com.kbcollection.resource;

import com.kbcollection.entity.ZonaEnvio;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/zonas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ZonaEnvioResource {

    @GET
    @PermitAll // Público para que el Checkout lo lea
    public List<ZonaEnvio> listar() {
        return ZonaEnvio.listAll();
    }

    @POST
    @Transactional
    @RolesAllowed("ADMIN")
    public Response crear(ZonaEnvio zona) {
        if (zona.departamento == null || zona.tarifa < 0) return Response.status(400).build();
        zona.persist();
        return Response.status(201).entity(zona).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response actualizar(@PathParam("id") Long id, ZonaEnvio datos) {
        ZonaEnvio z = ZonaEnvio.findById(id);
        if (z == null) return Response.status(404).build();
        z.departamento = datos.departamento;
        z.tarifa = datos.tarifa;
        z.municipios = datos.municipios;
        return Response.ok(z).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response eliminar(@PathParam("id") Long id) {
        ZonaEnvio.deleteById(id);
        return Response.ok().build();
    }
}