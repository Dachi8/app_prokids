import { Request, Response } from 'express';
import { pool } from '../config/database';

/** POST /api/dispositivos — registra o actualiza el token FCM del dispositivo. */
export const registrarDispositivo = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const { fcm_token, plataforma } = req.body || {};

  if (!fcm_token || !String(fcm_token).trim()) {
    return res.status(400).json({ status: 'error', mensaje: 'Falta el token del dispositivo' });
  }

  try {
    await pool.query(
      `INSERT INTO dispositivos_push (usuario_id, fcm_token, plataforma, fecha_registro)
       VALUES (?, ?, ?, ?)
       ON DUPLICATE KEY UPDATE usuario_id = VALUES(usuario_id), fecha_registro = VALUES(fecha_registro)`,
      [authId, String(fcm_token).trim(), plataforma || 'android', Date.now()]
    );
    res.status(201).json({ status: 'ok', mensaje: 'Dispositivo registrado' });
  } catch (error) {
    console.error('Error registrando dispositivo:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al registrar el dispositivo' });
  }
};

/** GET /api/dispositivos — lista los dispositivos del usuario autenticado. */
export const listarDispositivos = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  try {
    const [rows]: any = await pool.query(
      'SELECT id, usuario_id, fcm_token, plataforma, fecha_registro FROM dispositivos_push WHERE usuario_id = ? ORDER BY id DESC',
      [authId]
    );
    res.json({ status: 'ok', dispositivos: rows });
  } catch (error) {
    console.error('Error listando dispositivos:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar los dispositivos' });
  }
};
