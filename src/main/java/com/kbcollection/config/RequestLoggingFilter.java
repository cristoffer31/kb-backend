package com.kbcollection.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;

@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();
        String authHeader = requestContext.getHeaderString("Authorization");

        // Solo logueamos rutas de API para no saturar con health checks, etc.
        if (path.startsWith("/api")) {
            LOG.info(">>> REQUEST: " + method + " " + path);
            if (authHeader != null) {
                // Imprimimos solo el inicio del token para seguridad
                String safeToken = authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader;
                LOG.info(">>> HEADER AUTH: " + safeToken);
            } else {
                LOG.info(">>> HEADER AUTH: NULL (No token received)");
            }
        }
    }
}
