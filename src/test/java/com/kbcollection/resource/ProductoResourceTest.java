package com.kbcollection.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Test de integración básico para ProductoResource
 * 
 * NOTA: Para tests más completos, deberías:
 * 1. Usar @TestProfile para tener una BD de pruebas
 * 2. Limpiar datos entre tests con @BeforeEach
 * 3. Mockear servicios externos (Cloudinary, Twilio, etc.)
 */
@QuarkusTest
public class ProductoResourceTest {

    @Test
    public void testListarProductosEndpoint() {
        given()
            .when().get("/api/productos?page=0&size=10")
            .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalPages", notNullValue())
            .body("currentPage", notNullValue());
    }

    @Test
    public void testObtenerProductoNoExistente() {
        given()
            .when().get("/api/productos/99999")
            .then()
            .statusCode(404);
    }

    @Test
    public void testCrearProductoSinAutenticacion() {
        String productJson = """
            {
                "nombre": "Producto Test",
                "precio": 10.0,
                "stock": 100
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(productJson)
            .when().post("/api/productos")
            .then()
            .statusCode(401); // Debe rechazar sin token JWT
    }
}
