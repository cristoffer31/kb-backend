package com.kbcollection.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    public void enviarVerificacion(String email, String token) {
        String link = "http://localhost:5173/verificar?token=" + token;
        String html = "<h1>Bienvenido a KB Collection</h1>"
                + "<p>Gracias por registrarte. Para activar tu cuenta, haz clic en el siguiente enlace:</p>"
                + "<a href='" + link + "' style='background:#004aad;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;'>ACTIVAR CUENTA</a>";

        mailer.send(Mail.withHtml(email, "Activa tu cuenta - KB Collection", html));
    }

    public void enviarRecuperacion(String email, String token) {
        String link = "http://localhost:5173/restablecer?token=" + token;
        String html = "<h1>Recuperación de Contraseña</h1>"
                + "<p>Has solicitado cambiar tu clave. Haz clic abajo para crear una nueva:</p>"
                + "<a href='" + link + "' style='background:#ef4444;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;'>RESTABLECER CONTRASEÑA</a>"
                + "<p>Si no fuiste tú, ignora este mensaje.</p>";

        mailer.send(Mail.withHtml(email, "Recuperar Contraseña - KB Collection", html));
    }

    public void enviarMensajeContacto(String nombre, String emailCliente, String asunto, String mensaje) {
        // Este es el correo a donde llegarán los mensajes (TU CORREO DE DUEÑO)
        String emailAdmin = "consultoriatecnologicaerazo@gmail.com"; // <--- OJO: Confirma que este es tu correo

        String html = "<h2>📩 Nuevo Mensaje de la Web</h2>"
                + "<p><strong>Cliente:</strong> " + nombre + "</p>"
                + "<p><strong>Correo del Cliente:</strong> " + emailCliente + "</p>"
                + "<p><strong>Asunto:</strong> " + asunto + "</p>"
                + "<hr/>"
                + "<h3>Mensaje:</h3>"
                + "<p>" + mensaje + "</p>";

        // Enviamos el correo A TI MISMO
        mailer.send(Mail.withHtml(emailAdmin, "Contacto Web: " + asunto, html));
    }
}