import { Request, Response } from 'express';
import { pool } from '../config/database';
import { ahoraMs, aBooleano, fechaHoy } from '../utils/perfil';
import {
  buscarPorCodigo,
  buscarPorId,
  puedeAccederPaciente,
  rolDe,
} from '../utils/acceso';
import { codigoDeRequest } from '../utils/paciente';

/** Crea la ejecución de hoy para todas las asignaciones del paciente. */
const asegurarEjecucionesDeHoy = async (pacienteId: number) => {
  await pool.query(
    `INSERT IGNORE INTO tarea_ejecuciones (asignacion_id, fecha, estado)
     SELECT ta.id, CURDATE(), 'pendiente'
     FROM tarea_asignaciones ta
     WHERE ta.usuario_id = ?`,
    [pacienteId]
  );
};

const SELECT_TAREAS = `
  SELECT ta.id                       AS id,
         t.que_hacer                 AS title,
         COALESCE(t.descripcion, '') AS description,
         COALESCE(creador.rol, 'PACIENTE')   AS assignedByRole,
         COALESCE(creador.nombre, 'Serena')  AS assignedByName,
         t.puntos_recompensa         AS pointsReward,
         t.categoria                 AS category,
         CASE WHEN te.estado = 'completada' THEN 1 ELSE 0 END AS isCompleted,
         te.fecha_completado         AS completedAt,
         t.fecha_registro            AS createdAt,
         ta.usuario_id               AS pacienteId,
         t.id                        AS tareaId
  FROM tarea_asignaciones ta
  JOIN tareas t ON t.id = ta.tarea_id
  LEFT JOIN usuarios creador ON creador.id = t.creado_por
  LEFT JOIN tarea_ejecuciones te
         ON te.asignacion_id = ta.id AND te.fecha = CURDATE()
  WHERE ta.usuario_id = ?
  ORDER BY ta.id DESC
`;

const listarTareasDe = async (pacienteId: number) => {
  await asegurarEjecucionesDeHoy(pacienteId);
  const [rows]: any = await pool.query(SELECT_TAREAS, [pacienteId]);
  return rows.map((r: any) => ({
    ...r,
    id: Number(r.id),
    pointsReward: Number(r.pointsReward ?? 0),
    isCompleted: aBooleano(r.isCompleted),
    completedAt: r.completedAt == null ? null : Number(r.completedAt),
    createdAt: Number(r.createdAt ?? 0),
    pacienteId: Number(r.pacienteId),
    tareaId: Number(r.tareaId),
  }));
};

/** GET /api/tareas-prokids/mis-tareas — tareas del usuario autenticado. */
export const listarMisTareas = async (req: Request, res: Response) => {
  const usuarioId = (req as any).usuarioId;
  try {
    const tareas = await listarTareasDe(Number(usuarioId));
    res.json({ status: 'ok', tareas });
  } catch (error) {
    console.error('Error listando tareas:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar tareas' });
  }
};

/** GET /api/tareas-prokids/paciente/:codigo — tareas de un paciente. */
export const listarTareasPaciente = async (req: Request, res: Response) => {
  const usuarioId = (req as any).usuarioId;
  const codigo = codigoDeRequest(req);
  if (!codigo) {
    return res.status(400).json({ status: 'error', mensaje: 'Falta el código del paciente' });
  }
  try {
    const paciente = await buscarPorCodigo(codigo);
    if (!paciente) {
      return res.status(404).json({ status: 'error', mensaje: 'Paciente no encontrado' });
    }
    const autorizado = await puedeAccederPaciente(Number(usuarioId), Number(paciente.id));
    if (!autorizado) {
      return res.status(403).json({ status: 'error', mensaje: 'No autorizado para ver este paciente' });
    }
    const tareas = await listarTareasDe(Number(paciente.id));
    res.json({ status: 'ok', tareas, paciente: { id: Number(paciente.id) } });
  } catch (error) {
    console.error('Error listando tareas del paciente:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar tareas' });
  }
};

/** Crea una plantilla de tarea y la asigna a un paciente. */
const crearYAsignar = async (
  authId: number,
  pacienteId: number,
  datos: {
    que_hacer: string;
    descripcion?: string | null;
    frecuencia?: string | null;
    hora_limite?: string | null;
    categoria?: string | null;
    puntos_recompensa?: number | null;
    icono_key?: string | null;
  }
) => {
  const [result]: any = await pool.query(
    `INSERT INTO tareas
       (creado_por, que_hacer, descripcion, frecuencia, hora_limite, categoria,
        puntos_recompensa, icono_key, fecha_registro)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    [
      authId,
      datos.que_hacer,
      datos.descripcion || null,
      datos.frecuencia || 'diaria',
      datos.hora_limite || null,
      datos.categoria || 'Autocuidado',
      datos.puntos_recompensa ?? 50,
      datos.icono_key || 'heart',
      ahoraMs(),
    ]
  );
  const tareaId = Number(result.insertId);

  const [asignacion]: any = await pool.query(
    'INSERT INTO tarea_asignaciones (tarea_id, usuario_id, fecha_registro) VALUES (?, ?, ?)',
    [tareaId, pacienteId, ahoraMs()]
  );
  const asignacionId = Number(asignacion.insertId);

  await pool.query(
    "INSERT IGNORE INTO tarea_ejecuciones (asignacion_id, fecha, estado) VALUES (?, CURDATE(), 'pendiente')",
    [asignacionId]
  );

  return { tareaId, asignacionId };
};

/**
 * POST /api/sync/tareas — crea una tarea y la asigna.
 * Body: { titulo, descripcion?, categoria?, puntos_recompensa?, paciente_codigo?,
 *         frecuencia?, hora_limite? }
 */
export const crearTareaSync = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const {
    titulo,
    que_hacer,
    descripcion,
    categoria,
    puntos_recompensa,
    paciente_codigo,
    frecuencia,
    hora_limite,
    icono_key,
  } = req.body || {};

  const nombreTarea = (titulo || que_hacer || '').trim();
  if (!nombreTarea) {
    return res.status(400).json({ status: 'error', mensaje: 'El título de la tarea es obligatorio' });
  }

  try {
    let paciente: any = null;
    if (paciente_codigo && String(paciente_codigo).trim()) {
      paciente = await buscarPorCodigo(paciente_codigo);
      if (!paciente) {
        return res.status(404).json({ status: 'error', mensaje: 'Paciente no encontrado' });
      }
      const autorizado = await puedeAccederPaciente(authId, Number(paciente.id));
      if (!autorizado) {
        return res.status(403).json({ status: 'error', mensaje: 'No autorizado para asignar a este paciente' });
      }
    } else {
      paciente = await buscarPorId(authId);
    }

    const { tareaId, asignacionId } = await crearYAsignar(authId, Number(paciente.id), {
      que_hacer: nombreTarea,
      descripcion,
      frecuencia,
      hora_limite,
      categoria,
      puntos_recompensa,
      icono_key,
    });

    res.status(201).json({
      status: 'ok',
      tarea_id: tareaId,
      asignacion_id: asignacionId,
      mensaje: 'Tarea creada y asignada',
    });
  } catch (error) {
    console.error('Error creando tarea:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al crear la tarea' });
  }
};

/** POST /api/tareas-prokids/plantilla — crea solo la plantilla. */
export const crearTareaPlantilla = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const {
    que_hacer,
    descripcion,
    frecuencia,
    hora_limite,
    categoria,
    puntos_recompensa,
    icono_key,
  } = req.body || {};

  if (!que_hacer || !String(que_hacer).trim()) {
    return res.status(400).json({ status: 'error', mensaje: 'El campo que_hacer es obligatorio' });
  }

  try {
    const [result]: any = await pool.query(
      `INSERT INTO tareas
         (creado_por, que_hacer, descripcion, frecuencia, hora_limite, categoria,
          puntos_recompensa, icono_key, fecha_registro)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        authId,
        String(que_hacer).trim(),
        descripcion || null,
        frecuencia || 'diaria',
        hora_limite || null,
        categoria || 'Autocuidado',
        puntos_recompensa ?? 50,
        icono_key || 'heart',
        ahoraMs(),
      ]
    );
    res.status(201).json({ status: 'ok', tarea_id: Number(result.insertId) });
  } catch (error) {
    console.error('Error creando plantilla:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al crear la tarea' });
  }
};

/** POST /api/tareas-prokids/asignar — asigna una plantilla existente. */
export const asignarTareaUsuario = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const { tarea_id, usuario_id } = req.body || {};

  if (!tarea_id || !usuario_id) {
    return res.status(400).json({ status: 'error', mensaje: 'Faltan tarea_id o usuario_id' });
  }

  try {
    const paciente = await buscarPorId(Number(usuario_id));
    if (!paciente) {
      return res.status(404).json({ status: 'error', mensaje: 'Paciente no encontrado' });
    }
    const autorizado = await puedeAccederPaciente(authId, Number(paciente.id));
    if (!autorizado) {
      return res.status(403).json({ status: 'error', mensaje: 'No autorizado' });
    }

    const [result]: any = await pool.query(
      'INSERT INTO tarea_asignaciones (tarea_id, usuario_id, fecha_registro) VALUES (?, ?, ?)',
      [Number(tarea_id), Number(usuario_id), ahoraMs()]
    );
    const asignacionId = Number(result.insertId);
    await pool.query(
      "INSERT IGNORE INTO tarea_ejecuciones (asignacion_id, fecha, estado) VALUES (?, CURDATE(), 'pendiente')",
      [asignacionId]
    );

    res.status(201).json({ status: 'ok', asignacion_id: asignacionId });
  } catch (error) {
    console.error('Error asignando tarea:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al asignar la tarea' });
  }
};

/** Marca como completada la ejecución de hoy y suma los puntos una sola vez. */
const completarAsignacion = async (authId: number, asignacionId: number) => {
  const [rows]: any = await pool.query(
    `SELECT ta.id, ta.usuario_id, t.puntos_recompensa
     FROM tarea_asignaciones ta
     JOIN tareas t ON t.id = ta.tarea_id
     WHERE ta.id = ? LIMIT 1`,
    [asignacionId]
  );
  if (rows.length === 0) return { ok: false, code: 404, mensaje: 'Tarea no encontrada' };

  const fila = rows[0];
  const pacienteId = Number(fila.usuario_id);
  const autorizado = await puedeAccederPaciente(authId, pacienteId);
  if (!autorizado) return { ok: false, code: 403, mensaje: 'No autorizado' };

  await pool.query(
    "INSERT IGNORE INTO tarea_ejecuciones (asignacion_id, fecha, estado) VALUES (?, CURDATE(), 'pendiente')",
    [asignacionId]
  );

  const [ejecuciones]: any = await pool.query(
    'SELECT id, estado FROM tarea_ejecuciones WHERE asignacion_id = ? AND fecha = CURDATE() LIMIT 1',
    [asignacionId]
  );
  if (ejecuciones.length === 0) {
    return { ok: false, code: 500, mensaje: 'No se pudo crear la ejecución de hoy' };
  }

  const ejecucion = ejecuciones[0];
  if (ejecucion.estado !== 'completada') {
    await pool.query(
      "UPDATE tarea_ejecuciones SET estado = 'completada', fecha_completado = ? WHERE id = ?",
      [ahoraMs(), ejecucion.id]
    );
    await pool.query(
      'UPDATE usuarios SET puntos_balance = puntos_balance + ? WHERE id = ?',
      [Number(fila.puntos_recompensa ?? 50), pacienteId]
    );
  }

  return { ok: true, code: 200, mensaje: 'Tarea completada', pacienteId };
};

/** PATCH /api/sync/tareas/:tareaId/completar (tareaId = asignación). */
export const completarTareaAsignacion = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const asignacionId = Number(req.params.tareaId);
  try {
    const resultado = await completarAsignacion(authId, asignacionId);
    if (!resultado.ok) {
      return res.status(resultado.code).json({ status: 'error', mensaje: resultado.mensaje });
    }
    res.json({ status: 'ok', mensaje: resultado.mensaje });
  } catch (error) {
    console.error('Error completando tarea:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al completar la tarea' });
  }
};

/** PATCH /api/tareas-prokids/completar — body { ejecucion_id }. */
export const completarEjecucion = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const { ejecucion_id } = req.body || {};
  try {
    const [rows]: any = await pool.query(
      `SELECT te.id, te.asignacion_id
       FROM tarea_ejecuciones te WHERE te.id = ? LIMIT 1`,
      [Number(ejecucion_id)]
    );
    if (rows.length === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Ejecución no encontrada' });
    }
    const resultado = await completarAsignacion(authId, Number(rows[0].asignacion_id));
    if (!resultado.ok) {
      return res.status(resultado.code).json({ status: 'error', mensaje: resultado.mensaje });
    }
    res.json({ status: 'ok', mensaje: 'Tarea completada' });
  } catch (error) {
    console.error('Error completando ejecución:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al completar la tarea' });
  }
};

/** DELETE /api/sync/tareas/:tareaId (tareaId = asignación). */
export const eliminarTareaAsignacion = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const asignacionId = Number(req.params.tareaId);

  try {
    const [rows]: any = await pool.query(
      `SELECT ta.id, ta.usuario_id, t.creado_por, COALESCE(creador.rol, 'PACIENTE') AS creador_rol
       FROM tarea_asignaciones ta
       JOIN tareas t ON t.id = ta.tarea_id
       LEFT JOIN usuarios creador ON creador.id = t.creado_por
       WHERE ta.id = ? LIMIT 1`,
      [asignacionId]
    );
    if (rows.length === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Tarea no encontrada' });
    }

    const fila = rows[0];
    const autorizado = await puedeAccederPaciente(authId, Number(fila.usuario_id));
    if (!autorizado) {
      return res.status(403).json({ status: 'error', mensaje: 'No autorizado' });
    }

    const rolLlamante = await rolDe(authId);
    const esCreador = Number(fila.creado_por) === authId;
    const puedeBorrar =
      rolLlamante === 'ADMINISTRADOR' ||
      (esCreador && rolLlamante === fila.creador_rol);

    if (!puedeBorrar) {
      return res.status(403).json({
        status: 'error',
        mensaje: 'No tienes permiso para eliminar esta tarea',
      });
    }

    await pool.query('DELETE FROM tarea_ejecuciones WHERE asignacion_id = ?', [asignacionId]);
    await pool.query('DELETE FROM tarea_asignaciones WHERE id = ?', [asignacionId]);
    res.json({ status: 'ok', mensaje: 'Tarea eliminada' });
  } catch (error) {
    console.error('Error eliminando tarea:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al eliminar la tarea' });
  }
};

/** GET /api/tareas-prokids/detalle/:codigo — detalle del día para el tablero. */
export const listarDetalleHoy = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const codigo = codigoDeRequest(req);
  if (!codigo) {
    return res.status(400).json({ status: 'error', mensaje: 'Falta el código del paciente' });
  }
  try {
    const paciente = await buscarPorCodigo(codigo);
    if (!paciente) {
      return res.status(404).json({ status: 'error', mensaje: 'Paciente no encontrado' });
    }
    if (!(await puedeAccederPaciente(authId, Number(paciente.id)))) {
      return res.status(403).json({ status: 'error', mensaje: 'No autorizado' });
    }
    await asegurarEjecucionesDeHoy(Number(paciente.id));
    const [rows]: any = await pool.query(
      `SELECT te.id AS ejecucionId, ta.id AS asignacionId, t.id AS tareaId,
              ta.usuario_id AS usuarioId, t.que_hacer AS queHacer,
              t.descripcion AS descripcion, t.categoria AS categoria,
              t.frecuencia AS frecuencia, t.hora_limite AS horaLimite,
              t.puntos_recompensa AS puntosRecompensa, t.icono_key AS iconoKey,
              DATE_FORMAT(te.fecha, '%Y-%m-%d') AS fecha, te.estado AS estado,
              te.fecha_completado AS fechaCompletado,
              COALESCE(creador.nombre, 'Serena') AS creadoPorNombre,
              COALESCE(creador.rol, 'PACIENTE') AS creadoPorRol
       FROM tarea_ejecuciones te
       JOIN tarea_asignaciones ta ON ta.id = te.asignacion_id
       JOIN tareas t ON t.id = ta.tarea_id
       LEFT JOIN usuarios creador ON creador.id = t.creado_por
       WHERE ta.usuario_id = ? AND te.fecha = CURDATE()
       ORDER BY te.id DESC`,
      [Number(paciente.id)]
    );
    res.json({ status: 'ok', detalle: rows, fecha: fechaHoy() });
  } catch (error) {
    console.error('Error listando detalle:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar el detalle' });
  }
};
