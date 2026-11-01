import { Request, Response } from 'express';
import { pool } from '../config/database';
import { fechaHoy, mapaNota } from '../utils/perfil';
import { resolverPacienteRequest } from '../utils/paciente';

/** GET /api/notas?codigo=SERENA-XXXX — libreta del paciente. */
export const listarNotas = async (req: Request, res: Response) => {
  try {
    const resolucion = await resolverPacienteRequest(req);
    if (!resolucion.ok) {
      return res.status(resolucion.code).json({ status: 'error', mensaje: resolucion.mensaje });
    }

    const [rows]: any = await pool.query(
      `SELECT * FROM notas
       WHERE usuario_id = ?
       ORDER BY fecha_registro DESC, id DESC`,
      [resolucion.paciente.id]
    );

    res.json({ status: 'ok', notas: rows.map(mapaNota) });
  } catch (error) {
    console.error('Error listando notas:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar las notas' });
  }
};

/**
 * GET /api/notas/compartidas — notas que los pacientes vinculados
 * decidieron compartir con su equipo de cuidado.
 */
export const listarNotasCompartidas = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  try {
    const [rows]: any = await pool.query(
      `SELECT n.*
       FROM notas n
       JOIN representantes_pacientes rp ON rp.paciente_id = n.usuario_id
       WHERE rp.representante_id = ? AND n.compartida_equipo = 1
       ORDER BY n.fecha_registro DESC, n.id DESC`,
      [authId]
    );
    res.json({ status: 'ok', notas: rows.map(mapaNota) });
  } catch (error) {
    console.error('Error listando notas compartidas:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar las notas compartidas' });
  }
};

/** POST /api/notas — crea una entrada de la libreta. */
export const crearNota = async (req: Request, res: Response) => {
  try {
    const resolucion = await resolverPacienteRequest(req);
    if (!resolucion.ok) {
      return res.status(resolucion.code).json({ status: 'error', mensaje: resolucion.mensaje });
    }

    const {
      titulo,
      descripcion,
      emocion,
      emocion_emoji,
      intensidad,
      compartida_equipo,
      recordatorio,
      fecha_texto,
    } = req.body || {};

    if (!titulo || !String(titulo).trim()) {
      return res.status(400).json({ status: 'error', mensaje: 'El título es obligatorio' });
    }

    const intensidadNum = Math.min(5, Math.max(1, Number(intensidad ?? 3)));

    const [resultado]: any = await pool.query(
      `INSERT INTO notas
         (usuario_id, titulo, descripcion, emocion, emocion_emoji, intensidad,
          compartida_equipo, recordatorio, fecha_registro, fecha_texto)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        resolucion.paciente.id,
        String(titulo).trim(),
        descripcion || '',
        emocion || 'Calma',
        emocion_emoji || '💭',
        intensidadNum,
        compartida_equipo === false ? 0 : 1,
        recordatorio == null ? null : Number(recordatorio),
        Date.now(),
        fecha_texto || fechaHoy(),
      ]
    );

    const [rows]: any = await pool.query('SELECT * FROM notas WHERE id = ? LIMIT 1', [
      resultado.insertId,
    ]);

    res.status(201).json({
      status: 'ok',
      id: Number(resultado.insertId),
      nota: rows.length > 0 ? mapaNota(rows[0]) : null,
    });
  } catch (error) {
    console.error('Error creando nota:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al guardar la nota' });
  }
};

/** DELETE /api/notas/:id — elimina una nota propia. */
export const borrarNota = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const id = Number(req.params.id || req.params.notaId);
  try {
    const [resultado]: any = await pool.query(
      'DELETE FROM notas WHERE id = ? AND usuario_id = ?',
      [id, authId]
    );
    if (resultado.affectedRows === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Nota no encontrada' });
    }
    res.json({ status: 'ok', mensaje: 'Nota eliminada' });
  } catch (error) {
    console.error('Error eliminando nota:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al eliminar la nota' });
  }
};
