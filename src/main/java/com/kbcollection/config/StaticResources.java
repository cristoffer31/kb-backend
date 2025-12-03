package com.kbcollection.config;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class StaticResources {

    void installRoute(@Observes StartupEvent startupEvent, Router router) {
        // Esto permite ver las imágenes guardadas en la carpeta "uploads" del proyecto
        // Accesible en: http://localhost:8080/uploads/nombre-imagen.jpg
        router.route("/uploads/*")
              .handler(StaticHandler.create(FileSystemAccess.RELATIVE, "uploads/"));
    }
}