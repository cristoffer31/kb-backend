package com.kbcollection.resource;

import com.kbcollection.dto.ProductoForm;
import com.kbcollection.entity.Category;
import com.kbcollection.entity.Producto;
import com.kbcollection.entity.PrecioMayoreo;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Path("/api/productos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductoResource {

    @GET
    public Response listar(@QueryParam("page") @DefaultValue("0") int page, 
                           @QueryParam("size") @DefaultValue("12") int size) {
        PanacheQuery<Producto> query = Producto.findAll();
        query.page(io.quarkus.panache.common.Page.of(page, size));
        return Response.ok(Map.of(
            "content", query.list(),
            "totalPages", query.pageCount(),
            "totalElements", query.count(),
            "currentPage", page
        )).build();
    }

    // --- VARIANTES ---
    @GET
    @Path("/variantes/{codigo}")
    public List<Producto> obtenerVariantes(@PathParam("codigo") String codigo) {
        if (codigo == null || codigo.isBlank()) return List.of();
        return Producto.find("codigoAgrupador", codigo).list();
    }

    @GET
    @Path("/ofertas")
    public List<Producto> obtenerOfertas() {
        return Producto.find("enOferta = true").list();
    }

    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") Long id) {
        Producto p = Producto.findById(id);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(p).build();
    }

    @POST
    @Transactional
    @RolesAllowed("ADMIN")
    public Response crear(ProductoForm form) {
        Producto p = new Producto();
        mapFormToEntity(form, p);
        p.persist();
        return Response.status(Response.Status.CREATED).entity(p).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response actualizar(@PathParam("id") Long id, ProductoForm form) {
        Producto p = Producto.findById(id);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).build();
        mapFormToEntity(form, p);
        return Response.ok(p).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response eliminar(@PathParam("id") Long id) {
        boolean deleted = Producto.deleteById(id);
        if (!deleted) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok().build();
    }

    @GET
    @Path("/buscar")
    public Response buscar(
            @QueryParam("nombre") String nombre,
            @QueryParam("categoriaId") Long categoriaId
    ) {
        String query = "1=1";
        Map<String,Object> params = new HashMap<>();
        if (nombre != null && !nombre.isBlank()) {
            query += " AND LOWER(nombre) LIKE :nombre";
            params.put("nombre", "%" + nombre.toLowerCase() + "%");
        }
        if (categoriaId != null) {
            query += " AND category.id = :categoriaId";
            params.put("categoriaId", categoriaId);
        }
        List<Producto> resultados = Producto.list(query, params);
        return Response.ok(resultados).build();
    }

    private void mapFormToEntity(ProductoForm form, Producto p) {
        p.nombre = form.nombre;
        p.descripcion = form.descripcion;
        p.precio = form.precio;
        p.stock = form.stock;
        p.codigoBarras = form.codigoBarras;
        p.imagenUrl = form.imagenUrl;
        p.precioOferta = form.precioOferta != null ? form.precioOferta : 0.0;
        p.enOferta = form.enOferta != null ? form.enOferta : false;

        // --- MAPEO VARIANTES ---
        p.talla = form.talla;
        p.variante = form.variante;
        p.codigoAgrupador = form.codigoAgrupador;

        if (form.categoryId != null) {
            p.category = Category.findById(form.categoryId);
        } else {
            p.category = null;
        }

        // Precios Mayoreo
        if (p.preciosMayoreo == null) p.preciosMayoreo = new java.util.ArrayList<>();
        else p.preciosMayoreo.clear();
        
        if (form.preciosMayoreo != null) {
            for (ProductoForm.ReglaPrecio regla : form.preciosMayoreo) {
                PrecioMayoreo pm = new PrecioMayoreo();
                pm.cantidadMin = regla.cantidadMin;
                pm.precioUnitario = regla.precioUnitario;
                pm.producto = p;
                p.preciosMayoreo.add(pm);
            }
        }
    }
}