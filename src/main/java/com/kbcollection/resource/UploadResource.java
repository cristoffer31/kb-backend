package com.kbcollection.resource;

import com.kbcollection.service.CloudinaryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.util.Map;

@Path("/api/upload")
public class UploadResource {

    @Inject
    CloudinaryService cloudinaryService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@RestForm("file") FileUpload file) {
        
        if (file == null || file.fileName() == null) {
            return Response.status(400).entity(Map.of("error", "No se envió ningún archivo")).build();
        }

        try {
            // 1. Convertir el upload a un archivo físico temporal
            File archivoTemp = file.uploadedFile().toFile();

            // 2. Subirlo a Cloudinary usando nuestro servicio
            String urlCloudinary = cloudinaryService.subirImagen(archivoTemp);

            // 3. Devolver la URL pública (https://res.cloudinary.com/...)
            return Response.ok(Map.of("url", urlCloudinary)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(Map.of("error", "Fallo al subir imagen: " + e.getMessage())).build();
        }
    }
}