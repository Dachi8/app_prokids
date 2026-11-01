-- ============================================================
--  Serena + ProKids · Esquema MySQL
--  Base de datos única para la app Android (server-first).
--
--  Uso:
--    mysql -u root -p < database/schema.sql
--
--  NOTA: este script recrea las tablas del proyecto para dejar la
--  base lista. Si ya tienes datos que quieres conservar, haz un
--  respaldo antes de ejecutarlo.
-- ============================================================

CREATE DATABASE IF NOT EXISTS bdd_proyecto_asistente
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bdd_proyecto_asistente;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS recompensas_canjeadas;
DROP TABLE IF EXISTS recompensas;
DROP TABLE IF EXISTS recordatorios;
DROP TABLE IF EXISTS tarea_ejecuciones;
DROP TABLE IF EXISTS tarea_asignaciones;
DROP TABLE IF EXISTS tareas;
DROP TABLE IF EXISTS rutinas;
DROP TABLE IF EXISTS notas;
DROP TABLE IF EXISTS registros_animo;
DROP TABLE IF EXISTS dispositivos_push;
DROP TABLE IF EXISTS representantes_pacientes;
DROP TABLE IF EXISTS sesiones;
DROP TABLE IF EXISTS usuarios_roles;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS roles;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- Roles (IAM / RBAC)
-- ------------------------------------------------------------
CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(40) NOT NULL UNIQUE,
  descripcion VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (nombre, descripcion) VALUES
  ('PACIENTE', 'Seguimiento personal, chat Serena, libreta y recompensas'),
  ('MEDICO', 'Supervisión clínica y asignación de tareas'),
  ('FAMILIAR', 'Apoyo cercano y tareas afectivas con recompensa'),
  ('ADMINISTRADOR', 'Gestión total de usuarios, roles y asignaciones');

-- ------------------------------------------------------------
-- Usuarios y sesiones
-- ------------------------------------------------------------
CREATE TABLE usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  correo VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  fecha_nacimiento DATE NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'activo',
  fecha_registro BIGINT NOT NULL,
  ultimo_acceso BIGINT NULL,
  rol VARCHAR(40) NOT NULL DEFAULT 'PACIENTE',
  codigo_vinculacion VARCHAR(40) NOT NULL UNIQUE,
  puntos_balance INT NOT NULL DEFAULT 0,
  avatar_emoji VARCHAR(16) NOT NULL DEFAULT '🌱',
  INDEX idx_usuarios_rol (rol),
  INDEX idx_usuarios_codigo (codigo_vinculacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuarios_roles (
  usuario_id INT NOT NULL,
  rol_id INT NOT NULL,
  PRIMARY KEY (usuario_id, rol_id),
  CONSTRAINT fk_usuarios_roles_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  CONSTRAINT fk_usuarios_roles_rol FOREIGN KEY (rol_id)
    REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sesiones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  token_hash CHAR(64) NOT NULL UNIQUE,
  fecha_expiracion DATETIME NOT NULL,
  fecha_creacion BIGINT NOT NULL,
  CONSTRAINT fk_sesiones_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  INDEX idx_sesiones_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Vínculo representante (médico / familiar) ↔ paciente
-- ------------------------------------------------------------
CREATE TABLE representantes_pacientes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  representante_id INT NOT NULL,
  paciente_id INT NOT NULL,
  parentesco VARCHAR(60) NULL DEFAULT 'Familiar',
  fecha_registro BIGINT NOT NULL,
  UNIQUE KEY uq_representante_paciente (representante_id, paciente_id),
  CONSTRAINT fk_rep_representante FOREIGN KEY (representante_id)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  CONSTRAINT fk_rep_paciente FOREIGN KEY (paciente_id)
    REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Tareas en 3 niveles: plantilla → asignación → ejecución
-- ------------------------------------------------------------
CREATE TABLE tareas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  creado_por INT NOT NULL,
  que_hacer VARCHAR(200) NOT NULL,
  descripcion TEXT NULL,
  frecuencia VARCHAR(30) NOT NULL DEFAULT 'diaria',
  hora_limite TIME NULL,
  categoria VARCHAR(60) NOT NULL DEFAULT 'Autocuidado',
  puntos_recompensa INT NOT NULL DEFAULT 50,
  icono_key VARCHAR(40) NOT NULL DEFAULT 'heart',
  fecha_registro BIGINT NOT NULL,
  CONSTRAINT fk_tareas_creador FOREIGN KEY (creado_por)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  INDEX idx_tareas_creador (creado_por)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tarea_asignaciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tarea_id INT NOT NULL,
  usuario_id INT NOT NULL,
  fecha_registro BIGINT NOT NULL,
  CONSTRAINT fk_asignacion_tarea FOREIGN KEY (tarea_id)
    REFERENCES tareas (id) ON DELETE CASCADE,
  CONSTRAINT fk_asignacion_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  INDEX idx_asignacion_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tarea_ejecuciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  asignacion_id INT NOT NULL,
  fecha DATE NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'pendiente',
  fecha_completado BIGINT NULL,
  UNIQUE KEY uq_ejecucion_dia (asignacion_id, fecha),
  CONSTRAINT fk_ejecucion_asignacion FOREIGN KEY (asignacion_id)
    REFERENCES tarea_asignaciones (id) ON DELETE CASCADE,
  INDEX idx_ejecucion_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE recordatorios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  ejecucion_id INT NOT NULL,
  hora_envio DATETIME NOT NULL,
  enviado TINYINT(1) NOT NULL DEFAULT 0,
  fecha_envio BIGINT NULL,
  CONSTRAINT fk_recordatorio_ejecucion FOREIGN KEY (ejecucion_id)
    REFERENCES tarea_ejecuciones (id) ON DELETE CASCADE,
  INDEX idx_recordatorios_pendientes (enviado, hora_envio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Bienestar: ánimo, libreta y rutinas
-- ------------------------------------------------------------
CREATE TABLE registros_animo (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  fecha_texto VARCHAR(10) NOT NULL,
  timestamp BIGINT NOT NULL,
  animo_score INT NOT NULL DEFAULT 3,
  animo_label VARCHAR(60) NOT NULL DEFAULT '',
  animo_emoji VARCHAR(16) NOT NULL DEFAULT '',
  nivel_energia INT NOT NULL DEFAULT 3,
  etiquetas_emocion VARCHAR(255) NOT NULL DEFAULT '',
  etiquetas_contexto VARCHAR(255) NOT NULL DEFAULT '',
  nota TEXT NULL,
  CONSTRAINT fk_animo_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  INDEX idx_animo_usuario_fecha (usuario_id, fecha_texto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  titulo VARCHAR(160) NOT NULL,
  descripcion TEXT NULL,
  emocion VARCHAR(60) NOT NULL DEFAULT 'Calma',
  emocion_emoji VARCHAR(16) NOT NULL DEFAULT '💭',
  intensidad INT NOT NULL DEFAULT 3,
  compartida_equipo TINYINT(1) NOT NULL DEFAULT 1,
  recordatorio BIGINT NULL,
  fecha_registro BIGINT NOT NULL,
  fecha_texto VARCHAR(10) NOT NULL,
  CONSTRAINT fk_notas_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  INDEX idx_notas_usuario_fecha (usuario_id, fecha_registro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rutinas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  title VARCHAR(160) NOT NULL,
  description TEXT NULL,
  category VARCHAR(40) NOT NULL DEFAULT 'WELLNESS',
  icon_key VARCHAR(40) NOT NULL DEFAULT 'heart',
  completed_dates TEXT NULL,
  streak INT NOT NULL DEFAULT 0,
  last_completed_date VARCHAR(10) NOT NULL DEFAULT '',
  target_days_per_week INT NOT NULL DEFAULT 7,
  CONSTRAINT fk_rutinas_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON DELETE CASCADE,
  INDEX idx_rutinas_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Recompensas (catálogo global + canjes por paciente)
-- ------------------------------------------------------------
CREATE TABLE recompensas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(160) NOT NULL,
  descripcion TEXT NULL,
  puntos_costo INT NOT NULL DEFAULT 50,
  icono_emoji VARCHAR(16) NOT NULL DEFAULT '🎁',
  categoria VARCHAR(60) NOT NULL DEFAULT 'General',
  tipo_juego VARCHAR(40) NOT NULL DEFAULT '',
  activa TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE recompensas_canjeadas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  recompensa_id INT NOT NULL,
  usuario_id INT NOT NULL,
  fecha_canje BIGINT NOT NULL,
  UNIQUE KEY uq_canje (recompensa_id, usuario_id),
  CONSTRAINT fk_canje_recompensa FOREIGN KEY (recompensa_id)
    REFERENCES recompensas (id) ON DELETE CASCADE,
  CONSTRAINT fk_canje_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO recompensas (titulo, descripcion, puntos_costo, icono_emoji, categoria, tipo_juego) VALUES
  ('Peluche Virtual Abrazo', 'Un abrazo suave y cálido para tu corazón.', 100, '🧸', 'Emocional', 'PLUSH'),
  ('Xilófono de Calma', 'Toca notas suaves y relájate con su sonido.', 120, '🎼', 'Sensorial', 'XYLOPHONE'),
  ('Burbujas Sensoriales', 'Explota burbujas de colores y respira profundo.', 80, '🫧', 'Sensorial', 'BUBBLES'),
  ('Puzzle de Emociones', 'Arma caritas y aprende a nombrar lo que sientes.', 150, '🧩', 'Juego', 'EMOTION_PUZZLE'),
  ('Memorama del Mundo Calma', 'Encuentra parejas y entrena tu atención.', 140, '🃏', 'Juego', 'MEMORY'),
  ('Lienzo Creativo', 'Dibuja libremente con colores y texturas.', 110, '🎨', 'Creatividad', 'DRAWING'),
  ('Celebración de Estrellas', 'Una lluvia de estrellas para celebrar tus logros.', 200, '🌟', 'Celebración', 'CELEBRATION'),
  ('Jardín de Gratitud', 'Riega tu jardín mientras agradeces lo bueno del día.', 90, '🌷', 'Emocional', 'PLUSH');

-- ------------------------------------------------------------
-- Dispositivos para notificaciones push
-- ------------------------------------------------------------
CREATE TABLE dispositivos_push (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  fcm_token VARCHAR(255) NOT NULL UNIQUE,
  plataforma VARCHAR(20) NOT NULL DEFAULT 'android',
  fecha_registro BIGINT NOT NULL,
  CONSTRAINT fk_dispositivo_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
