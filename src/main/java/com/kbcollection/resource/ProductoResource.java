package com.kbcollection.resource;

import com.kbcollection.dto.ProductoForm;
import com.kbcollection.entity.*;
import jakarta.annotation.security.PermitAll; // <--- IMPORTANTE
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api/productos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductoResource {

    @GET
    @PermitAll // <--- AGREGADO: Permite ver productos sin login o con token
    public Response listar(@QueryParam("page") @DefaultValue("0") int page, 
                           @QueryParam("size") @DefaultValue("12") int size,
                           @QueryParam("empresaId") Long empresaId) {
        
        String query = "1=1";
        Map<String, Object> params = new HashMap<>();

        if (empresaId != null) {
            query += " AND empresa.id = :empresaId";
            params.put("empresaId", empresaId);
        }

        PanacheQuery<Producto> pq = Producto.find(query, params);
        pq.page(io.quarkus.panache.common.Page.of(page, size));
        
        return Response.ok(Map.of(
            "content", pq.list(),
            "totalPages", pq.pageCount(),
            "totalElements", pq.count(),
            "currentPage", page
        )).build();
    }

    // --- OTROS MÉTODOS PÚBLICOS ---
    
    @GET
    @Path("/variantes/{codigo}")
    @PermitAll
    public List<Producto> obtenerVariantes(@PathParam("codigo") String codigo) {
        if (codigo == null || codigo.isBlank()) return List.of();
        return Producto.find("codigoAgrupador", codigo).list();
    }

    @GET
    @Path("/ofertas")
    @PermitAll
    public List<Producto> obtenerOfertas(@QueryParam("empresaId") Long empresaId) {
        if (empresaId != null) {
            return Producto.find("enOferta = true AND empresa.id = ?1", empresaId).list();
        }
        return Producto.find("enOferta = true").list();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Response obtener(@PathParam("id") Long id) {
        Producto p = Producto.findById(id);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(p).build();
    }

    @GET
    @Path("/buscar")
    @PermitAll
    public Response buscar(@QueryParam("nombre") String nombre,
                           @QueryParam("categoriaId") Long categoriaId,
                           @QueryParam("empresaId") Long empresaId) {
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
        if (empresaId != null) {
            query += " AND empresa.id = :empresaId";
            params.put("empresaId", empresaId);
        }
        
        return Response.ok(Producto.list(query, params)).build();
    }

    // --- MÉTODOS DE ADMIN (ESTOS SÍ LLEVAN @RolesAllowed) ---
    
    @POST
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response crear(ProductoForm form, @Context SecurityContext sec) {
        String email = sec.getUserPrincipal().getName();
        Usuario admin = Usuario.find("email", email).firstResult();
        Producto p = new Producto();
        
        if (admin.empresa != null) p.empresa = admin.empresa;
        else p.empresa = Empresa.findById(1L);

        mapFormToEntity(form, p);
        p.persist();
        return Response.status(Response.Status.CREATED).entity(p).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response actualizar(@PathParam("id") Long id, ProductoForm form) {
        Producto p = Producto.findById(id);
        if (p == null) return Response.status(Response.Status.NOT_FOUND).build();
        mapFormToEntity(form, p);
        return Response.ok(p).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed({"ADMIN", "SUPER_ADMIN"})
    public Response eliminar(@PathParam("id") Long id) {
        boolean deleted = Producto.deleteById(id);
        if (!deleted) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok().build();
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
        p.talla = form.talla;
        p.variante = form.variante;
        p.codigoAgrupador = form.codigoAgrupador;
        if (form.categoryId != null) p.category = Category.findById(form.categoryId);
        else p.category = null;
        
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