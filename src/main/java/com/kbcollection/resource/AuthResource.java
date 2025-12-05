package com.kbcollection.resource;

import com.kbcollection.dto.LoginDTO;
import com.kbcollection.dto.RegisterDTO;
import com.kbcollection.entity.Usuario;
import com.kbcollection.service.AuthService;
import com.kbcollection.service.UsuarioService;
import com.kbcollection.service.EmailService;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.validation.Valid;
import at.favre.lib.crypto.bcrypt.BCrypt;
import java.util.Map;
import java.util.UUID;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    UsuarioService usuarioService;

    @Inject
    EmailService emailService;

    @Inject
    JWTParser parser;

    // ---------------------------------------------------------
    // ¡OJO! AQUÍ BORRAMOS LOS MÉTODOS @OPTIONS QUE CAUSABAN EL ERROR
    // ---------------------------------------------------------

    @POST
    @Path("/login")
    public Response login(LoginDTO dto) {
        try {
            String token = authService.login(dto);
            Usuario u = Usuario.find("email", dto.email).firstResult();

            // Devolvemos token y usuario
            return Response.ok(Map.of(
                    "token", token,
                    "usuario", u)).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterDTO dto) {
        try {
            Usuario u = usuarioService.registrar(dto);
            emailService.enviarVerificacion(u.email, u.tokenVerificacion);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("mensaje", "Registro exitoso. Revisa tu correo para activar la cuenta."))
                    .build();
        } catch (RuntimeException e) {
            return Response.status(400).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/me")
    @RolesAllowed({ "ADMIN", "USER" })
    public Response me(@HeaderParam("Authorization") String authHeader) {
        try {
            String token = authHeader.substring("Bearer ".length());
            String email = parser.parse(token).getName();
            Usuario u = Usuario.find("email", email).firstResult();

            return u != null ? Response.ok(u).build() : Response.status(401).build();
        } catch (Exception e) {
            return Response.status(401).build();
        }
    }

    @POST
    @Path("/verificar")
    @Transactional
    public Response verificarToken(Map<String, String> body) {
        if (usuarioService.verificarCuenta(body.get("token"))) {
            return Response.ok(Map.of("mensaje", "Cuenta verificada correctamente")).build();
        }
        return Response.status(400).entity(Map.of("error", "Token inválido o expirado")).build();
    }

    // RECUPERACIÓN DE CONTRASEÑA
    // RECUPERACIÓN DE CONTRASEÑA
    @POST
    @Path("/forgot-password")
    @Transactional
    public Response forgotPassword(Map<String, String> body) {
        System.out.println("🔍 Intentando forgot-password...");
        try {
            String email = body.get("email");
            System.out.println("📧 Email recibido: " + email);

            if (email == null || email.isBlank()) {
                System.out.println("❌ El email es nulo o vacío");
                return Response.status(400).entity(Map.of("error", "Email requerido")).build();
            }

            Usuario u = Usuario.find("email", email).firstResult();
            if (u != null) {
                System.out.println("✅ Usuario encontrado: " + u.id);
                u.tokenRecuperacion = UUID.randomUUID().toString();
                u.persist(); // Guardar token
                System.out.println("💾 Token guardado en DB. Enviando correo...");

                emailService.enviarRecuperacion(u.email, u.tokenRecuperacion);
                System.out.println("🚀 Correo enviado (o intento realizado).");
            } else {
                System.out.println("⚠️ Usuario no encontrado para ese email.");
            }
            return Response.ok(Map.of("mensaje", "Si existe, se enviaron instrucciones.")).build();
        } catch (Exception e) {
            System.out.println("🔥 ERROR CRÍTICO EN FORGOT-PASSWORD:");
            e.printStackTrace(); // Esto saldrá en los logs de Railway
            return Response.status(500).entity(Map.of("error", "Error interno: " + e.getMessage())).build();
        }
    }

    @POST
    @Path("/reset-password")
    @Transactional
    public Response resetPassword(Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("password");
        Usuario u = Usuario.find("tokenRecuperacion", token).firstResult();

        if (u == null)
            return Response.status(400).entity(Map.of("error", "Token inválido")).build();

        u.passwordHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
        u.tokenRecuperacion = null;
        u.persist();
        return Response.ok(Map.of("mensaje", "Contraseña actualizada")).build();
    }

    // ACTUALIZAR PERFIL
    @PUT
    @Path("/me")
    @RolesAllowed({ "ADMIN", "USER" })
    @Transactional
    public Response updateProfile(@HeaderParam("Authorization") String authHeader, Map<String, String> body) {
        try {
            String token = authHeader.substring("Bearer ".length());
            String emailToken = parser.parse(token).getName();
            Usuario u = Usuario.find("email", emailToken).firstResult();

            if (u == null)
                return Response.status(401).build();

            if (body.containsKey("nombre") && !body.get("nombre").isBlank()) {
                u.nombre = body.get("nombre");
            }

            if (body.containsKey("password") && !body.get("password").isBlank()) {
                String currentPassword = body.get("currentPassword");
                if (currentPassword == null || currentPassword.isBlank()) {
                    return Response.status(400).entity(Map.of("error", "Debes ingresar tu contraseña actual")).build();
                }
                if (!BCrypt.verifyer().verify(currentPassword.toCharArray(), u.passwordHash).verified) {
                    return Response.status(400).entity(Map.of("error", "La contraseña actual es incorrecta")).build();
                }

                String newPass = body.get("password");
                if (newPass.length() < 6) {
                    return Response.status(400).entity(Map.of("error", "Mínimo 6 caracteres")).build();
                }
                u.passwordHash = BCrypt.withDefaults().hashToString(12, newPass.toCharArray());
            }

            u.persist();
            u.passwordHash = null;
            return Response.ok(u).build();

        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", "Error al actualizar perfil")).build();
        }
    }
}