package com.kbcollection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Clase base para entidades que requieren auditoría.
 * 
 * Proporciona automáticamente:
 * - createdAt: fecha/hora de creación
 * - updatedAt: fecha/hora de última actualización
 * - createdBy: usuario que creó el registro
 * - updatedBy: usuario que actualizó el registro
 * 
 * Para usarla, simplemente haz que tu entidad extienda de esta clase:
 * 
 * @Entity
 * public class Producto extends AuditableEntity {
 *     // ... tus campos
 * }
 * 
 * NOTA: Para que createdBy/updatedBy se llenen automáticamente,
 * necesitas implementar un EntityListener que obtenga el usuario actual
 * del SecurityContext.
 */
@MappedSuperclass
public abstract class AuditableEntity {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    public String createdBy;

    @Column(name = "updated_by", length = 100)
    public String updatedBy;

    /**
     * Puedes descomentar esto si quieres llenar createdBy/updatedBy manualmente
     * O mejor, crea un @EntityListeners que lo haga automáticamente
     */
    /*
    @PrePersist
    protected void onCreate() {
        // SecurityContext ctx = CDI.current().select(SecurityIdentity.class).get();
        // if (ctx.getPrincipal() != null) {
        //     this.createdBy = ctx.getPrincipal().getName();
        // }
    }

    @PreUpdate
    protected void onUpdate() {
        // SecurityContext ctx = CDI.current().select(SecurityIdentity.class).get();
        // if (ctx.getPrincipal() != null) {
        //     this.updatedBy = ctx.getPrincipal().getName();
        // }
    }
    */
}
