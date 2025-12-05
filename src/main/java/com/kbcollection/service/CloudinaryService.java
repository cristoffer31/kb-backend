package com.kbcollection.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@ApplicationScoped
public class CloudinaryService {

    // Leemos las claves del application.properties
    @ConfigProperty(name = "cloudinary.cloud-name")
    String cloudName;

    @ConfigProperty(name = "cloudinary.api-key")
    String apiKey;

    @ConfigProperty(name = "cloudinary.api-secret")
    String apiSecret;

    public String subirImagen(File archivo) {
        try {
            // Conexión con Cloudinary
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true));

            // Subir el archivo
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(archivo, ObjectUtils.emptyMap());

            // Retornar la URL segura (https)
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Error al subir imagen a Cloudinary", e);
        }
    }
}