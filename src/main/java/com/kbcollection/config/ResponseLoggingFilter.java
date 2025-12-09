package com.kbcollection.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;

/**
 * Filtro para registrar las respuestas de la API
 * 
 * Complementa al RequestLoggingFilter para tener visibilidad completa
 * del ciclo de vida de cada petición.
 */
@Provider
public class ResponseLoggingFilter implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(ResponseLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        
        String path = requestContext.getUriInfo().getPath();
        
        // Solo logueamos rutas de API
        if (path.startsWith("/api")) {
            int status = responseContext.getStatus();
            String method = requestContext.getMethod();
            
            String logLevel = status >= 500 ? "ERROR" : (status >= 400 ? "WARN" : "INFO");
            
            String message = String.format(
                "<<< RESPONSE: %s %s - Status: %d", 
                method, 
                path, 
                status
            );
            
            // Log según el nivel de severidad
            if ("ERROR".equals(logLevel)) {
                LOG.error(message);
            } else if ("WARN".equals(logLevel)) {
                LOG.warn(message);
            } else {
                LOG.info(message);
            }
        }
    }
}
