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

    @POST
    @Path("/login")
    public Response login(LoginDTO dto) {
        try {
            String token = authService.login(dto);
            Usuario u = Usuario.find("email", dto.email).firstResult();

            // Devolvemos token y usuario
            return Response.ok(Map.of(
                    "token", token,
                    "usuario", u != null ? u : "Usuario no encontrado" // Safety check although login succeeds
            )).build();
        } catch (RuntimeException e) {
            System.out.println("🔥 ERROR CRÍTICO EN LOGIN: " + e.getMessage());
            e.printStackTrace(); // <--- IMPORTANTE PARA VER POR QUÉ FALLA JWT
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
            // Intentar enviar correo, pero no fallar el registro si el correo falla (ya que
            // el usuario ya se creó)
            try {
                emailService.enviarVerificacion(u.email, u.tokenVerificacion);
            } catch (Exception e) {
                System.out.println("⚠️ Error enviando correo de verificación: " + e.getMessage());
            }

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

    // MAPEO API CORRECTO PARA FORGOT PASSWORD
    @POST
    @Path("/forgot-password")
    // NO @Transactional para evitar timeout por correo lento
    public Response forgotPassword(Map<String, String> body) {
        System.out.println("🔍 Intentando forgot-password...");
        try {
            String email = body.get("email");

            // 1. Operación de BD rápida (Crear token)
            String token = generarTokenRecuperacion(email);

            if (token != null) {
                // 2. Operación lenta (Enviar correo) - FUERA de la transacción
                System.out.println("💾 Token guardado. Intentando enviar correo...");
                emailService.enviarRecuperacion(email, token);
                System.out.println("🚀 Correo enviado.");
            } else {
                System.out.println("⚠️ Usuario no encontrado o error generando token.");
            }

            return Response.ok(Map.of("mensaje", "Si existe, se enviaron instrucciones.")).build();
        } catch (Exception e) {
            System.out.println("🔥 ERROR en forgot-password:");
            e.printStackTrace();
            return Response.status(500).entity(Map.of("error", "Error interno")).build();
        }
    }

    // Método auxiliar TRANSACCIONAL (sólo toca DB)
    @Transactional
    public String generarTokenRecuperacion(String email) {
        if (email == null || email.isBlank())
            return null;
        Usuario u = Usuario.find("email", email).firstResult();
        if (u != null) {
            u.tokenRecuperacion = UUID.randomUUID().toString();
            u.persist();
            return u.tokenRecuperacion;
        }
        return null; // Usuario no encontrado
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

    // --- ENDPOINT DE EMERGENCIA (Bypass Email) ---
    @GET
    @Path("/manual-verify/{email}")
    @Transactional
    public Response manualVerify(@PathParam("email") String email) {
        Usuario u = Usuario.find("email", email).firstResult();
        if (u == null)
            return Response.status(404).entity("Usuario no encontrado").build();

        u.verificado = true;
        u.persist();

        return Response.ok("✅ Usuario " + email + " verificado manualmente. Ya puedes hacer Login.").build();
    }

    // --- ENDPOINT MASTER FIX (Password + Activo + Verificado) ---
    @GET
    @Path("/manual-reset/{email}/{newPassword}")
    @Transactional
    public Response manualReset(@PathParam("email") String email, @PathParam("newPassword") String newPassword) {
        Usuario u = Usuario.find("email", email).firstResult();
        if (u == null)
            return Response.status(404).entity("Usuario no encontrado").build();

        u.passwordHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
        u.verificado = true;
        u.activo = true;
        u.persist();

        return Response.ok("✅ MASTER FIX APLICADO: Password cambiada, usuario activo y verificado.").build();
    }
}