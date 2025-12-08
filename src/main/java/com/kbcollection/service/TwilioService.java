package com.kbcollection.service;

import com.kbcollection.entity.Pedido;
import com.kbcollection.entity.PedidoItem;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TwilioService {

    private static final Logger LOG = Logger.getLogger(TwilioService.class);

    @ConfigProperty(name = "twilio.account-sid")
    String accountSid;

    @ConfigProperty(name = "twilio.auth-token")
    String authToken;

    @ConfigProperty(name = "twilio.phone-number")
    String fromSmsNumber; 

    @ConfigProperty(name = "twilio.whatsapp-number")
    String fromWhatsappNumber; 

    @ConfigProperty(name = "twilio.admin-phone")
    String adminPhone;

    @PostConstruct
    void init() {
        // Evita errores si las variables no están configuradas en local
        if (accountSid != null && !accountSid.isBlank() && !accountSid.contains("TU_")) {
            try {
                Twilio.init(accountSid, authToken);
                LOG.info(" Twilio Service Iniciado");
            } catch (Exception e) {
                LOG.error(" Error iniciando Twilio: " + e.getMessage());
            }
        }
    }

    // 1. WHATSAPP: Notificación de Pedido al Admin
    public void notificarPedidoAdmin(Pedido p) {
        try {
            StringBuilder msg = new StringBuilder();
            msg.append(" *NUEVO PEDIDO #").append(p.id).append("* \n");
            msg.append(" Cliente: ").append(p.usuario.nombre).append("\n");
            msg.append(" Tel: ").append(p.telefono).append("\n\n");
            
            msg.append("*Detalle de Compra:* \n");
            for (PedidoItem item : p.items) {
                // Muestra la empresa de cada producto
                String empresa = (item.producto.empresa != null) ? item.producto.empresa.nombre : "Global";
                msg.append("- ").append(item.cantidad).append("x ").append(item.producto.nombre)
                   .append(" (").append(empresa).append(")\n");
            }
            
            msg.append("\n*TOTAL: $").append(String.format("%.2f", p.total)).append("*");

            Message.creator(
                new PhoneNumber("whatsapp:" + adminPhone),
                new PhoneNumber(fromWhatsappNumber),
                msg.toString()
            ).create();
            
            LOG.info("WhatsApp enviado al Admin por pedido #" + p.id);
        } catch (Exception e) {
            LOG.error("Error enviando WhatsApp: " + e.getMessage());
        }
    }

    // 2. SMS: Código de Verificación
    public void enviarSmsVerificacion(String telefonoDestino, String codigo) {
        try {
            String cuerpo = "KB Corporación: Tu código de verificación es " + codigo;
            
            Message.creator(
                new PhoneNumber(telefonoDestino),
                new PhoneNumber(fromSmsNumber),
                cuerpo
            ).create();
            
            LOG.info("SMS enviado a " + telefonoDestino);
        } catch (Exception e) {
            LOG.error("Error enviando SMS: " + e.getMessage());
        }
    }
}