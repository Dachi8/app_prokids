import { Request, Response, NextFunction } from 'express';
import crypto from 'crypto';
import { pool } from '../config/database';

export const verificarToken = async (req: Request, res: Response, next: NextFunction) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ status: 'error', mensaje: 'Token no proporcionado' });
  }

  const token = authHeader.split(' ')[1];
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex');

  try {
    const [rows]: any = await pool.query(
      'SELECT usuario_id, fecha_expiracion FROM sesiones WHERE token_hash = ?',
      [tokenHash]
    );

    if (rows.length === 0) {
      return res.status(401).json({ status: 'error', mensaje: 'Token inválido' });
    }

    const sesion = rows[0];

    if (new Date(sesion.fecha_expiracion) < new Date()) {
      return res.status(401).json({ status: 'error', mensaje: 'Sesión expirada' });
    }

    (req as any).usuarioId = sesion.usuario_id;
    next();
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'Error al verificar sesión' });
  }
};