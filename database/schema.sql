-- =====================================================
-- BASE DE DATOS CORREGIDA - PROYECTO ASISTENTE VIRTUAL
-- Incluye:
-- IAM (Identity and Access Management)
-- RBAC (Role Based Access Control)
-- Arquitectura Cliente-Servidor
-- Auditoría y Seguridad
-- Compatible con MySQL/MariaDB
-- =====================================================

DROP DATABASE IF EXISTS bdd_proyecto_asistente;
CREATE DATABASE bdd_proyecto_asistente CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bdd_proyecto_asistente;

CREATE TABLE usuarios(
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE,
    estado ENUM('activo','inactivo','bloqueado') DEFAULT 'activo',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME NULL
);

CREATE TABLE roles(
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE representantes_pacientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    representante_id INT NOT NULL,
    paciente_id INT NOT NULL,
    parentesco VARCHAR(50),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(representante_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY(paciente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE KEY unico_vinculo (representante_id, paciente_id)
);

CREATE TABLE permisos(
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE usuarios_roles(
    usuario_id INT,
    rol_id INT,
    PRIMARY KEY(usuario_id, rol_id),
    FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY(rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE roles_permisos(
    rol_id INT,
    permiso_id INT,
    PRIMARY KEY(rol_id, permiso_id),
    FOREIGN KEY(rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY(permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
);

CREATE TABLE modulos(
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE multimedia(
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_path VARCHAR(255) NOT NULL,
    file_extension VARCHAR(10),
    file_size_kb INT,
    fecha_subida TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notas(
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recordatorio DATETIME,
    FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
-- 1. La PLANTILLA: qué es la tarea y cada cuánto se repite
CREATE TABLE tareas(
    id INT AUTO_INCREMENT PRIMARY KEY,
    creado_por INT NOT NULL,
    que_hacer VARCHAR(150) NOT NULL,
    descripcion TEXT,
    frecuencia ENUM('diaria','semanal','unica') DEFAULT 'diaria',
    hora_limite TIME,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(creado_por) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- 2. La ASIGNACIÓN: a qué paciente le corresponde esta plantilla
CREATE TABLE tarea_asignaciones(
    id INT AUTO_INCREMENT PRIMARY KEY,
    tarea_id INT NOT NULL,
    usuario_id INT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(tarea_id) REFERENCES tareas(id) ON DELETE CASCADE,
    FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- 3. La EJECUCIÓN: una fila por cada día que se debía cumplir
CREATE TABLE tarea_ejecuciones(
    id INT AUTO_INCREMENT PRIMARY KEY,
    asignacion_id INT NOT NULL,
    fecha DATE NOT NULL,
    estado ENUM('pendiente','en_progreso','completada','vencida') DEFAULT 'pendiente',
    fecha_completado DATETIME NULL,
    FOREIGN KEY(asignacion_id) REFERENCES tarea_asignaciones(id) ON DELETE CASCADE,
    UNIQUE KEY unico_dia_por_asignacion (asignacion_id, fecha)
);

CREATE TABLE recordatorios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ejecucion_id INT NOT NULL,
    hora_envio DATETIME NOT NULL,
    enviado BOOLEAN DEFAULT FALSE,
    fecha_envio DATETIME NULL,
    FOREIGN KEY(ejecucion_id) REFERENCES tarea_ejecuciones(id) ON DELETE CASCADE,
    INDEX idx_pendientes (enviado, hora_envio)
);

CREATE TABLE archivos(
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    recordatorio DATETIME,
    FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE nota_multimedia(
    nota_id INT,
    multimedia_id INT,
    PRIMARY KEY(nota_id,multimedia_id),
    FOREIGN KEY(nota_id) REFERENCES notas(id) ON DELETE CASCADE,
    FOREIGN KEY(multimedia_id) REFERENCES multimedia(id) ON DELETE CASCADE
);

CREATE TABLE tarea_multimedia(
    tarea_id INT,
    multimedia_id INT,
    PRIMARY KEY(tarea_id,multimedia_id),
    FOREIGN KEY(tarea_id) REFERENCES tareas(id) ON DELETE CASCADE,
    FOREIGN KEY(multimedia_id) REFERENCES multimedia(id) ON DELETE CASCADE
);

CREATE TABLE archivo_multimedia(
    archivo_id INT,
    multimedia_id INT,
    PRIMARY KEY(archivo_id,multimedia_id),
    FOREIGN KEY(archivo_id) REFERENCES archivos(id) ON DELETE CASCADE,
    FOREIGN KEY(multimedia_id) REFERENCES multimedia(id) ON DELETE CASCADE
);

CREATE TABLE sesiones(
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    fecha_inicio DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATETIME NOT NULL,
    FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE logs_auditoria(
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NULL,
    accion VARCHAR(255) NOT NULL,
    ip VARCHAR(50),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

INSERT INTO roles(nombre, descripcion) VALUES
('Administrador','Acceso total al sistema'),
('Usuario','Acceso operativo'),
('Invitado','Acceso de solo lectura');

INSERT INTO permisos(nombre) VALUES
('crear_usuario'),
('editar_usuario'),
('eliminar_usuario'),
('ver_usuario'),
('crear_tarea'),
('editar_tarea'),
('eliminar_tarea'),
('ver_tarea'),
('crear_nota'),
('editar_nota'),
('eliminar_nota'),
('ver_nota'),
('subir_archivo'),
('eliminar_archivo'),
('ver_archivo');

INSERT INTO modulos(nombre, descripcion) VALUES
('Notas','Gestion de notas'),
('Tareas','Gestion de tareas'),
('Archivos','Gestion documental'),
('Multimedia','Gestion multimedia'),
('Configuracion','Configuracion del sistema');
