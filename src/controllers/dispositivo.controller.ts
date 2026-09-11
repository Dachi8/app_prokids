import { Request, Response } from 'express';
import { pool } from '../config/database';
import { registrarDispositivoSchema } from '../schemas/dispositivo.schema';

export const registrarDispositivo = async (req: Request, res: Response) => {
  const parseResult = registrarDispositivoSchema.safeParse(req.body);

  if (!parseResult.success) {
    return res.status(400).json({ status: 'error', errores: parseResult.error.flatten().fieldErrors });
  }

  const { fcm_token, plataforma } = parseResult.data;
  const usuarioId = (req as any).usuarioId;

  try {
    await pool.query(
      `INSERT INTO dispositivos_push (usuario_id, fcm_token, plataforma) 
       VALUES (?, ?, ?)
       ON DUPLICATE KEY UPDATE usuario_id = VALUES(usuario_id)`,
      [usuarioId, fcm_token, plataforma]
    );

    res.status(201).json({ status: 'ok', mensaje: 'Dispositivo registrado' });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'Error al registrar dispositivo' });
  }
};