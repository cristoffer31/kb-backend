-- 1. Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    verificado BIT(1) DEFAULT 0,
    tokenVerificacion VARCHAR(255),
    tokenRecuperacion VARCHAR(255),
    tokenExpiracion DATETIME(6),
    activo BIT(1) DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY UK_email (email)
) ENGINE=InnoDB;

-- 2. Categorías
CREATE TABLE IF NOT EXISTS Category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255),
    descripcion VARCHAR(255),
    imagenUrl VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 3. Productos
CREATE TABLE IF NOT EXISTS Producto (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255),
    descripcion VARCHAR(255),
    precio DOUBLE,
    stock INT,
    codigoBarras VARCHAR(255),
    imagenUrl VARCHAR(255),
    precioOferta DOUBLE,
    enOferta BIT(1),
    talla VARCHAR(255),
    variante VARCHAR(255),
    codigoAgrupador VARCHAR(255),
    category_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT FK_Producto_Category FOREIGN KEY (category_id) REFERENCES Category (id)
) ENGINE=InnoDB;

-- 4. Precios de Mayoreo
CREATE TABLE IF NOT EXISTS PrecioMayoreo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    producto_id BIGINT,
    cantidadMin INT,
    precioUnitario DOUBLE,
    PRIMARY KEY (id),
    CONSTRAINT FK_PrecioMayoreo_Producto FOREIGN KEY (producto_id) REFERENCES Producto (id)
) ENGINE=InnoDB;

-- 5. Cupones
CREATE TABLE IF NOT EXISTS Cupon (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(255),
    porcentaje DOUBLE,
    activo BIT(1),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 6. Pedidos
CREATE TABLE IF NOT EXISTS Pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    subtotal DOUBLE,
    descuento DOUBLE,
    costoEnvio DOUBLE,
    total DOUBLE,
    metodoPago VARCHAR(50),
    status VARCHAR(20),
    direccion TEXT,
    departamento VARCHAR(255),
    coordenadas VARCHAR(255),
    tipoComprobante VARCHAR(255),
    documentoFiscal VARCHAR(255),
    nrc VARCHAR(255),
    razonSocial VARCHAR(255),
    giro VARCHAR(255),
    paypalOrderId VARCHAR(255),
    fecha DATETIME(6),
    usuario_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT FK_Pedido_Usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB;

-- 7. Items del Pedido
CREATE TABLE IF NOT EXISTS PedidoItem (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT,
    producto_id BIGINT,
    cantidad INT,
    precioUnitario DOUBLE,
    PRIMARY KEY (id),
    CONSTRAINT FK_PedidoItem_Pedido FOREIGN KEY (pedido_id) REFERENCES Pedido (id),
    CONSTRAINT FK_PedidoItem_Producto FOREIGN KEY (producto_id) REFERENCES Producto (id)
) ENGINE=InnoDB;

-- 8. Carousel (Imágenes Publicitarias)
CREATE TABLE IF NOT EXISTS CarouselImage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    imageUrl VARCHAR(255),
    titulo VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 9. Zonas de Envío
CREATE TABLE IF NOT EXISTS ZonaEnvio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    departamento VARCHAR(255),
    tarifa DOUBLE,
    municipios TEXT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;
