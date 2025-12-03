package com.kbcollection.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class CarouselImage extends PanacheEntity {
    public String imageUrl;
    public String titulo; // Opcional, por si quieres ponerle nombre
}