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
            LOG.info(">>> DUMPING HEADERS:");
            requestContext.getHeaders().forEach((key, values) -> {
                String safeValue = key.equalsIgnoreCase("Authorization")
                        ? (values.isEmpty() ? "EMPTY"
                                : values.get(0).substring(0, Math.min(values.get(0).length(), 15)) + "...")
                        : values.toString();
                LOG.info("   " + key + ": " + safeValue);
            });

            if (authHeader == null) {
                LOG.info(">>> WARNING: Authorization header is logically NULL via getHeaderString");
            }
        }
    }
}
