-- MySQL 8.4 (CHECK también exige MySQL >= 8.0.16). Ejecutar en una BD vacía.
CREATE TABLE usuarios (
 id_usuario INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 username VARCHAR(50) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 rol VARCHAR(20) NOT NULL,
 CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMINISTRADOR','CAJERO'))
) ENGINE=InnoDB;
CREATE TABLE categorias (
 id_categoria INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL UNIQUE,
 descripcion VARCHAR(255) NOT NULL DEFAULT ''
) ENGINE=InnoDB;
CREATE TABLE productos (
 id_producto INT AUTO_INCREMENT PRIMARY KEY,
 codigo VARCHAR(50) NOT NULL UNIQUE,
 nombre VARCHAR(100) NOT NULL,
 precio DECIMAL(12,2) NOT NULL,
 stock INT NOT NULL,
 stock_minimo INT NOT NULL DEFAULT 0,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 categoria_id INT NOT NULL,
 tipo VARCHAR(25) NOT NULL,
 fecha_vencimiento DATE NULL,
 CONSTRAINT ck_producto_precio CHECK (precio > 0),
 CONSTRAINT ck_producto_stock CHECK (stock >= 0 AND stock_minimo >= 0),
 CONSTRAINT ck_producto_tipo CHECK (
   (tipo = 'PERECEDERO' AND fecha_vencimiento IS NOT NULL) OR
   (tipo = 'NO_PERECEDERO' AND fecha_vencimiento IS NULL)),
 CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id_categoria) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE clientes (
 id_cliente INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 nit VARCHAR(20) NOT NULL UNIQUE,
 telefono VARCHAR(25) NOT NULL DEFAULT ''
) ENGINE=InnoDB;
CREATE TABLE ventas (
 id_venta INT AUTO_INCREMENT PRIMARY KEY,
 fecha DATETIME NOT NULL,
 usuario_id INT NOT NULL,
 cliente_id INT NOT NULL,
 metodo_pago VARCHAR(10) NOT NULL,
 total DECIMAL(12,2) NOT NULL,
 CONSTRAINT ck_venta_total CHECK (total > 0),
 CONSTRAINT ck_venta_pago CHECK (metodo_pago IN ('EFECTIVO','TARJETA')),
 CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id_cliente) ON DELETE RESTRICT ON UPDATE RESTRICT,
 INDEX ix_venta_fecha (fecha),
 INDEX ix_venta_usuario_fecha (usuario_id, fecha)
) ENGINE=InnoDB;
CREATE TABLE detalle_ventas (
 id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
 venta_id INT NOT NULL,
 producto_id INT NOT NULL,
 cantidad INT NOT NULL,
 precio_unitario DECIMAL(12,2) NOT NULL,
 subtotal DECIMAL(12,2) NOT NULL,
 CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
 CONSTRAINT ck_detalle_precio CHECK (precio_unitario > 0),
 CONSTRAINT ck_detalle_subtotal CHECK (subtotal = cantidad * precio_unitario),
 CONSTRAINT uq_detalle_producto UNIQUE (venta_id, producto_id),
 CONSTRAINT fk_detalle_venta FOREIGN KEY (venta_id) REFERENCES ventas(id_venta) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos(id_producto) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;
INSERT INTO clientes (nombre, nit, telefono) VALUES ('Consumidor Final', 'CF', '');
-- No se incluyen contraseñas compartidas: crear administrador con ConsolaApp --crear-admin.
