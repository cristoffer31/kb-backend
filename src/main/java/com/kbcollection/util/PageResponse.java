package com.kbcollection.util;

import java.util.List;

/**
 * Clase genérica para respuestas paginadas.
 * 
 * Uso:
 * 
 * PanacheQuery<Producto> query = Producto.find("categoria.id", catId);
 * query.page(Page.of(pageNum, pageSize));
 * 
 * PageResponse<Producto> response = PageResponse.of(
 *     query.list(),
 *     query.pageCount(),
 *     query.count(),
 *     pageNum,
 *     pageSize
 * );
 * 
 * return Response.ok(response).build();
 */
public class PageResponse<T> {
    
    public List<T> content;
    public int totalPages;
    public long totalElements;
    public int currentPage;
    public int size;
    public boolean first;
    public boolean last;
    public boolean empty;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int totalPages, long totalElements, int currentPage, int size) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.size = size;
        this.first = currentPage == 0;
        this.last = currentPage == totalPages - 1 || totalPages == 0;
        this.empty = content == null || content.isEmpty();
    }

    /**
     * Método estático para crear una respuesta paginada fácilmente.
     */
    public static <T> PageResponse<T> of(List<T> content, int totalPages, long totalElements, int currentPage, int size) {
        return new PageResponse<>(content, totalPages, totalElements, currentPage, size);
    }

    /**
     * Crear una página vacía
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0, 0, 0, 0);
    }
}
