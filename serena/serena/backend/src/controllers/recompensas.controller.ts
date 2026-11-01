import { Request, Response } from 'express';
import { pool } from '../config/database';
import { mapaRecompensa } from '../utils/perfil';
import { resolverPacienteRequest } from '../utils/paciente';

/** GET /api/recompensas?codigo=SERENA-XXXX — catálogo con estado de canje. */
export const listarRecompensas = async (req: Request, res: Response) => {
  try {
    const resolucion = await resolverPacienteRequest(req);
    if (!resolucion.ok) {
      return res.status(resolucion.code).json({ status: 'error', mensaje: resolucion.mensaje });
    }

    const pacienteId = Number(resolucion.paciente.id);

    const [rows]: any = await pool.query(
      `SELECT r.*,
              CASE WHEN EXISTS (
                SELECT 1 FROM recompensas_canjeadas rc
                WHERE rc.recompensa_id = r.id AND rc.usuario_id = ?
              ) THEN 1 ELSE 0 END AS canjeada,
              (SELECT MAX(rc2.fecha_canje) FROM recompensas_canjeadas rc2
               WHERE rc2.recompensa_id = r.id AND rc2.usuario_id = ?) AS fecha_canje
       FROM recompensas r
       WHERE r.activa = 1
       ORDER BY r.puntos_costo ASC, r.id ASC`,
      [pacienteId, pacienteId]
    );

    res.json({ status: 'ok', recompensas: rows.map(mapaRecompensa) });
  } catch (error) {
    console.error('Error listando recompensas:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar las recompensas' });
  }
};

/**
 * POST /api/recompensas/canjear — descuenta estrellas y registra el canje.
 * Body: { recompensa_id, paciente_codigo? }
 */
export const canjearRecompensa = async (req: Request, res: Response) => {
  const resolucion = await resolverPacienteRequest(req);
  if (!resolucion.ok) {
    return res.status(resolucion.code).json({ status: 'error', mensaje: resolucion.mensaje });
  }

  const pacienteId = Number(resolucion.paciente.id);
  const recompensaId = Number(req.body?.recompensa_id);

  if (!recompensaId) {
    return res.status(400).json({ status: 'error', mensaje: 'Falta recompensa_id' });
  }

  const conexion = await pool.getConnection();
  try {
    await conexion.beginTransaction();

    const [recompensas]: any = await conexion.query(
      'SELECT * FROM recompensas WHERE id = ? AND activa = 1 LIMIT 1',
      [recompensaId]
    );
    if (recompensas.length === 0) {
      await conexion.rollback();
      return res.status(404).json({ status: 'error', mensaje: 'Recompensa no encontrada' });
    }

    const recompensa = recompensas[0];

    const [usuarios]: any = await conexion.query(
      'SELECT puntos_balance FROM usuarios WHERE id = ? FOR UPDATE',
      [pacienteId]
    );
    if (usuarios.length === 0) {
      await conexion.rollback();
      return res.status(404).json({ status: 'error', mensaje: 'Paciente no encontrado' });
    }

    const saldo = Number(usuarios[0].puntos_balance ?? 0);

    const [canjes]: any = await conexion.query(
      'SELECT id FROM recompensas_canjeadas WHERE recompensa_id = ? AND usuario_id = ? LIMIT 1',
      [recompensaId, pacienteId]
    );

    if (canjes.length > 0) {
      await conexion.commit();
      return res.json({
        status: 'ok',
        mensaje: 'Esta recompensa ya estaba canjeada',
        puntos_balance: saldo,
      });
    }

    const costo = Number(recompensa.puntos_costo ?? 0);
    if (saldo < costo) {
      await conexion.rollback();
      return res.status(400).json({
        status: 'error',
        mensaje: 'Estrellas insuficientes para canjear esta recompensa',
        puntos_balance: saldo,
      });
    }

    await conexion.query(
      'UPDATE usuarios SET puntos_balance = puntos_balance - ? WHERE id = ?',
      [costo, pacienteId]
    );
    await conexion.query(
      'INSERT INTO recompensas_canjeadas (recompensa_id, usuario_id, fecha_canje) VALUES (?, ?, ?)',
      [recompensaId, pacienteId, Date.now()]
    );

    await conexion.commit();

    res.json({
      status: 'ok',
      mensaje: 'Recompensa canjeada',
      puntos_balance: saldo - costo,
      recompensa_id: recompensaId,
    });
  } catch (error) {
    await conexion.rollback();
    console.error('Error canjeando recompensa:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al canjear la recompensa' });
  } finally {
    conexion.release();
  }
};
