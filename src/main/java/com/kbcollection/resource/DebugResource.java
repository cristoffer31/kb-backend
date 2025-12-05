package com.kbcollection.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/api/debug-header")
public class DebugResource {

    @GET
    @PermitAll
    public Response debugHeader(@Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");
        System.out.println("DEBUG HEADER: " + authHeader);
        return Response.ok(Map.of("received_header", authHeader != null ? authHeader : "NULL")).build();
    }
}
