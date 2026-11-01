import { Request, Response } from 'express';
import { pool } from '../config/database';
import { fechaHoy, mapaAnimo } from '../utils/perfil';
import { resolverPacienteRequest } from '../utils/paciente';

const limitar = (valor: number, min: number, max: number) =>
  Math.min(max, Math.max(min, Number.isFinite(valor) ? valor : min));

/** GET /api/animo?codigo=SERENA-XXXX — historial de ánimo del paciente. */
export const listarAnimos = async (req: Request, res: Response) => {
  try {
    const resolucion = await resolverPacienteRequest(req);
    if (!resolucion.ok) {
      return res.status(resolucion.code).json({ status: 'error', mensaje: resolucion.mensaje });
    }

    const [rows]: any = await pool.query(
      `SELECT * FROM registros_animo
       WHERE usuario_id = ?
       ORDER BY timestamp DESC, id DESC`,
      [resolucion.paciente.id]
    );

    res.json({ status: 'ok', registros: rows.map(mapaAnimo) });
  } catch (error) {
    console.error('Error listando ánimos:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar los registros de ánimo' });
  }
};

/** POST /api/animo — guarda un registro de ánimo. */
export const crearAnimo = async (req: Request, res: Response) => {
  try {
    const resolucion = await resolverPacienteRequest(req);
    if (!resolucion.ok) {
      return res.status(resolucion.code).json({ status: 'error', mensaje: resolucion.mensaje });
    }

    const {
      fecha_texto,
      animo_score,
      animo_label,
      animo_emoji,
      nivel_energia,
      etiquetas_emocion,
      etiquetas_contexto,
      nota,
    } = req.body || {};

    const score = limitar(Number(animo_score ?? 3), 1, 5);
    const energia = limitar(Number(nivel_energia ?? 3), 1, 5);

    const [resultado]: any = await pool.query(
      `INSERT INTO registros_animo
         (usuario_id, fecha_texto, timestamp, animo_score, animo_label,
          animo_emoji, nivel_energia, etiquetas_emocion, etiquetas_contexto, nota)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        resolucion.paciente.id,
        fecha_texto || fechaHoy(),
        Date.now(),
        score,
        animo_label || '',
        animo_emoji || '',
        energia,
        etiquetas_emocion || '',
        etiquetas_contexto || '',
        nota || '',
      ]
    );

    const [rows]: any = await pool.query('SELECT * FROM registros_animo WHERE id = ? LIMIT 1', [
      resultado.insertId,
    ]);

    res.status(201).json({
      status: 'ok',
      id: Number(resultado.insertId),
      registro: rows.length > 0 ? mapaAnimo(rows[0]) : null,
    });
  } catch (error) {
    console.error('Error creando ánimo:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al guardar el estado de ánimo' });
  }
};

/** DELETE /api/animo/:id — elimina un registro propio. */
export const borrarAnimo = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const id = Number(req.params.id);
  try {
    const [resultado]: any = await pool.query(
      'DELETE FROM registros_animo WHERE id = ? AND usuario_id = ?',
      [id, authId]
    );
    if (resultado.affectedRows === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Registro no encontrado' });
    }
    res.json({ status: 'ok', mensaje: 'Registro de ánimo eliminado' });
  } catch (error) {
    console.error('Error eliminando ánimo:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al eliminar el registro' });
  }
};
