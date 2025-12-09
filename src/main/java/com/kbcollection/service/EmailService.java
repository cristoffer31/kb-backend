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

    @ConfigProperty(name = "twilio.admin-phone") // Usamos esto para saber a quién notificar admin
    String adminEmailDestino; 

    // 1. Verificación de Cuenta
  public void enviarVerificacion(String email, String token) {
        try {
            // Limpiamos la URL por si tiene slash al final
            String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
            
            // Construimos el enlace al Frontend
            String link = baseUrl + "/verificar?token=" + token;

            // Diseño HTML del correo
            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;">
                    <h2 style="color: #004aad; text-align: center;">Bienvenido a KB Collection</h2>
                    <p style="font-size: 16px; color: #333;">Gracias por registrarte en nuestra plataforma corporativa.</p>
                    <p style="font-size: 16px; color: #333;">Para activar tu cuenta y comenzar a comprar, por favor confirma tu correo electrónico haciendo clic en el botón de abajo:</p>
                    <br/>
                    <div style="text-align: center;">
                        <a href="%s" style="background-color: #004aad; color: white; padding: 15px 30px; text-decoration: none; border-radius: 50px; font-weight: bold; font-size: 16px; display: inline-block;">VERIFICAR MI CUENTA</a>
                    </div>
                    <br/>
                    <p style="font-size: 12px; color: #777; text-align: center;">Si no creaste esta cuenta, puedes ignorar este mensaje.</p>
                </div>
            """.formatted(link);

            // Enviar el correo
            mailer.send(Mail.withHtml(email, "Verifica tu cuenta - KB Collection", html));
            
            log.info("📧 Correo de verificación enviado a: " + email);

        } catch (Exception e) {
            log.error("❌ Error enviando correo a " + email, e);
        }
    }


    // 2. Recuperación de Contraseña
    public void enviarRecuperacion(String email, String token) {
        try {
            String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
            String link = baseUrl + "/restablecer?token=" + token;

            String html = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                    + "<h1 style='color: #dc2626;'>Recuperación de Contraseña</h1>"
                    + "<p>Has solicitado cambiar tu clave. Haz clic abajo para crear una nueva:</p>"
                    + "<br/>"
                    + "<a href='" + link + "' style='background-color: #dc2626; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;'>RESTABLECER CONTRASEÑA</a>"
                    + "<br/><br/>"
                    + "<p style='font-size: 12px; color: #777;'>El enlace expira en 15 minutos.</p>"
                    + "</div>";

            mailer.send(Mail.withHtml(email, "Recuperar Contraseña - KB Collection", html));
            log.info("📧 Correo de recuperación enviado a: " + email);
        } catch (Exception e) {
            log.error("❌ Error enviando correo de recuperación a " + email, e);
        }
    }

    // 3. Formulario de Contacto
    public void enviarMensajeContacto(String nombre, String emailCliente, String asunto, String mensaje) {
        try {
            // Enviamos el correo al dueño (Admin)
            // Puedes cambiar esto por un correo fijo si prefieres
            String destino = "consultoriatecnologicaerazo@gmail.com"; 

            String html = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 8px;'>"
                    + "<h2 style='color: #004aad;'>📩 Nuevo Mensaje Web</h2>"
                    + "<p><strong>Cliente:</strong> " + nombre + "</p>"
                    + "<p><strong>Correo:</strong> <a href='mailto:" + emailCliente + "'>" + emailCliente + "</a></p>"
                    + "<p><strong>Asunto:</strong> " + asunto + "</p>"
                    + "<hr style='border: 0; border-top: 1px solid #eee;'/>"
                    + "<h3>Mensaje:</h3>"
                    + "<p style='background: #f9f9f9; padding: 15px; border-radius: 5px;'>" + mensaje + "</p>"
                    + "</div>";

            mailer.send(Mail.withHtml(destino, "Contacto Web: " + asunto, html));
            log.info("📧 Mensaje de contacto reenviado al admin: " + destino);
        } catch (Exception e) {
            log.error("❌ Error enviando mensaje de contacto", e);
        }
    }
}