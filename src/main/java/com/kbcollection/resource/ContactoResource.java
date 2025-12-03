package com.kbcollection.resource;

import com.kbcollection.dto.ContactoDTO;
import com.kbcollection.service.EmailService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/contacto")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ContactoResource {

    @Inject
    EmailService emailService;

    @POST
    @RolesAllowed({"USER", "ADMIN"})
    public Response enviarMensaje(ContactoDTO form) {
        try {
            emailService.enviarMensajeContacto(form.nombre, form.email, form.asunto, form.mensaje);
            return Response.ok(Map.of("mensaje", "Correo enviado exitosamente")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(Map.of("error", "Error al enviar correo")).build();
        }
    }
}