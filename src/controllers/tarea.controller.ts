import { Request, Response } from 'express';
import { pool } from '../config/database';
import { crearTareaSchema, asignarTareaSchema, completarEjecucionSchema } from '../schemas/tarea.schema';

export const crearTarea = async (req: Request, res: Response) => {
  const parseResult = crearTareaSchema.safeParse(req.body);

  if (!parseResult.success) {
    return res.status(400).json({ status: 'error', errores: parseResult.error.flatten().fieldErrors });
  }

  const { que_hacer, descripcion, frecuencia, hora_limite } = parseResult.data;
  const creadoPor = (req as any).usuarioId;

  try {
    const [resultado]: any = await pool.query(
      'INSERT INTO tareas (creado_por, que_hacer, descripcion, frecuencia, hora_limite) VALUES (?, ?, ?, ?, ?)',
      [creadoPor, que_hacer, descripcion || null, frecuencia, hora_limite || null]
    );

    res.status(201).json({ status: 'ok', tarea_id: resultado.insertId });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'Error al crear la tarea' });
  }
};

export const asignarTarea = async (req: Request, res: Response) => {
  const parseResult = asignarTareaSchema.safeParse(req.body);

  if (!parseResult.success) {
    return res.status(400).json({ status: 'error', errores: parseResult.error.flatten().fieldErrors });
  }

  const { tarea_id, usuario_id } = parseResult.data;

  try {
    const [tareaRows]: any = await pool.query(
      'SELECT hora_limite FROM tareas WHERE id = ?',
      [tarea_id]
    );

    if (tareaRows.length === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Tarea no encontrada' });
    }

    const [asignacionResultado]: any = await pool.query(
      'INSERT INTO tarea_asignaciones (tarea_id, usuario_id) VALUES (?, ?)',
      [tarea_id, usuario_id]
    );

    const asignacionId = asignacionResultado.insertId;
    const hoy = new Date().toISOString().split('T')[0];

    const [ejecucionResultado]: any = await pool.query(
      'INSERT INTO tarea_ejecuciones (asignacion_id, fecha) VALUES (?, ?)',
      [asignacionId, hoy]
    );

    const horaLimite = tareaRows[0].hora_limite;
    if (horaLimite) {
      const horaEnvio = `${hoy} ${horaLimite}`;
      await pool.query(
        'INSERT INTO recordatorios (ejecucion_id, hora_envio) VALUES (?, ?)',
        [ejecucionResultado.insertId, horaEnvio]
      );
    }

    res.status(201).json({ status: 'ok', mensaje: 'Tarea asignada correctamente', asignacion_id: asignacionId });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'Error al asignar la tarea' });
  }
};

export const listarMisTareas = async (req: Request, res: Response) => {
  const usuarioId = (req as any).usuarioId;
  const hoy = new Date().toISOString().split('T')[0];

  try {
    const [pacientesVinculados]: any = await pool.query(
      'SELECT paciente_id FROM representantes_pacientes WHERE representante_id = ?',
      [usuarioId]
    );

    const idsAConsultar = [usuarioId, ...pacientesVinculados.map((p: any) => p.paciente_id)];
    const placeholders = idsAConsultar.map(() => '?').join(',');

    const [ejecuciones]: any = await pool.query(
      `SELECT te.id AS ejecucion_id, te.fecha, te.estado, t.que_hacer, t.descripcion, ta.usuario_id
       FROM tarea_ejecuciones te
       JOIN tarea_asignaciones ta ON ta.id = te.asignacion_id
       JOIN tareas t ON t.id = ta.tarea_id
       WHERE ta.usuario_id IN (${placeholders}) AND te.fecha = ?`,
      [...idsAConsultar, hoy]
    );

    res.json({ status: 'ok', tareas: ejecuciones });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar tareas' });
  }
};

export const completarEjecucion = async (req: Request, res: Response) => {
  const parseResult = completarEjecucionSchema.safeParse(req.body);

  if (!parseResult.success) {
    return res.status(400).json({ status: 'error', errores: parseResult.error.flatten().fieldErrors });
  }

  const { ejecucion_id } = parseResult.data;
  const usuarioId = (req as any).usuarioId;

  try {
    const [resultado]: any = await pool.query(
      `UPDATE tarea_ejecuciones te
       JOIN tarea_asignaciones ta ON ta.id = te.asignacion_id
       SET te.estado = 'completada', te.fecha_completado = NOW()
       WHERE te.id = ? AND ta.usuario_id = ? AND te.estado != 'completada'`,
      [ejecucion_id, usuarioId]
    );

    if (resultado.affectedRows === 0) {
      return res.status(404).json({
        status: 'error',
        mensaje: 'Ejecución no encontrada, ya completada, o no te pertenece',
      });
    }

    res.json({ status: 'ok', mensaje: 'Tarea marcada como completada' });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'Error al completar la tarea' });
  }
};