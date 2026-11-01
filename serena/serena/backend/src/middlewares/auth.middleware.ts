import { Request, Response, NextFunction } from 'express';
import crypto from 'crypto';
import { pool } from '../config/database';
import { parseToken } from '../utils/token';

/**
 * Verifica el token Bearer contra la tabla `sesiones`.
 * El token se guarda hasheado con SHA-256; nunca en claro.
 */
export const verificarToken = async (req: Request, res: Response, next: NextFunction) => {
  const token = parseToken(req.headers.authorization);

  if (!token) {
    return res.status(401).json({ status: 'error', mensaje: 'Token no proporcionado' });
  }

  const tokenHash = crypto.createHash('sha256').update(token).digest('hex');

  try {
    const [rows]: any = await pool.query(
      `SELECT usuario_id, fecha_expiracion
       FROM sesiones WHERE token_hash = ? LIMIT 1`,
      [tokenHash]
    );

    if (rows.length === 0) {
      return res.status(401).json({ status: 'error', mensaje: 'Token inválido' });
    }

    const sesion = rows[0];

    if (new Date(sesion.fecha_expiracion) < new Date()) {
      return res.status(401).json({ status: 'error', mensaje: 'Sesión expirada' });
    }

    (req as any).usuarioId = Number(sesion.usuario_id);
    (req as any).token = token;
    next();
  } catch (error) {
    console.error('Error verificando token:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al verificar la sesión' });
  }
};
