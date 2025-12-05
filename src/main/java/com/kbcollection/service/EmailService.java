package com.kbcollection.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @Inject
    Logger log;

    @ConfigProperty(name = "FRONTEND_URL", defaultValue = "http://localhost:5173")
    String frontendUrl;

    public void enviarVerificacion(String email, String token) {
        try {
            // Asegurar que no tenga slash al final para evitar //
            String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1)
                    : frontendUrl;
            String link = baseUrl + "/verificar?token=" + token;

            String html = "<h1>Bienvenido a KB Collection</h1>"
                    + "<p>Gracias por registrarte. Para activar tu cuenta, haz clic en el siguiente enlace:</p>"
                    + "<a href='" + link
                    + "' style='background:#004aad;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;'>ACTIVAR CUENTA</a>";

            mailer.send(Mail.withHtml(email, "Activa tu cuenta - KB Collection", html));
            log.info("📧 Correo de verificación enviado a: " + email);
        } catch (Exception e) {
            log.error("❌ Error enviando correo de verificación a " + email, e);
            // No lanzamos la excepción para no romper el flujo de registro, pero queda
            // registrado
        }
    }

    public void enviarRecuperacion(String email, String token) {
        try {
            String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1)
                    : frontendUrl;
            String link = baseUrl + "/restablecer?token=" + token;

            String html = "<h1>Recuperación de Contraseña</h1>"
                    + "<p>Has solicitado cambiar tu clave. Haz clic abajo para crear una nueva:</p>"
                    + "<a href='" + link
                    + "' style='background:#ef4444;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;'>RESTABLECER CONTRASEÑA</a>"
                    + "<p>Si no fuiste tú, ignora este mensaje.</p>";

            mailer.send(Mail.withHtml(email, "Recuperar Contraseña - KB Collection", html));
            log.info("📧 Correo de recuperación enviado a: " + email);
        } catch (Exception e) {
            log.error("❌ Error enviando correo de recuperación a " + email, e);
        }
    }

    public void enviarMensajeContacto(String nombre, String emailCliente, String asunto, String mensaje) {
        try {
            // Este es el correo a donde llegarán los mensajes (TU CORREO DE DUEÑO)
            String emailAdmin = "consultoriatecnologicaerazo@gmail.com";

            String html = "<h2>📩 Nuevo Mensaje de la Web</h2>"
                    + "<p><strong>Cliente:</strong> " + nombre + "</p>"
                    + "<p><strong>Correo del Cliente:</strong> " + emailCliente + "</p>"
                    + "<p><strong>Asunto:</strong> " + asunto + "</p>"
                    + "<hr/>"
                    + "<h3>Mensaje:</h3>"
                    + "<p>" + mensaje + "</p>";

            // Enviamos el correo A TI MISMO
            mailer.send(Mail.withHtml(emailAdmin, "Contacto Web: " + asunto, html));
            log.info("📧 Mensaje de contacto enviado al admin: " + emailAdmin);
        } catch (Exception e) {
            log.error("❌ Error enviando mensaje de contacto", e);
        }
    }
}