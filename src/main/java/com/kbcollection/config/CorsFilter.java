package com.kbcollection.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        // 1. Obtener el origen que intenta conectarse
        String origin = requestContext.getHeaderString("Origin");

        // 2. Definir orígenes permitidos (Tu frontend de Vercel y Localhost para
        // pruebas)
        String allowedOrigin = "https://kb-frontend-eta.vercel.app";
        if (origin != null && origin.equals("http://localhost:5173")) {
            allowedOrigin = origin; // Permitir localhost si estamos desarrollando
        }

        // 3. Forzar los encabezados de aceptación
        responseContext.getHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}