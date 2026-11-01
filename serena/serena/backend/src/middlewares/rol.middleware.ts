import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/database';

/**
 * Middleware opcional para restringir endpoints por rol.
 * Debe usarse después de `verificarToken`.
 */
export const verificarRol = (rolesPermitidos: string[]) => {
  return async (req: Request, res: Response, next: NextFunction) => {
    const usuarioId = (req as any).usuarioId;
    if (!usuarioId) {
      return res.status(401).json({ status: 'error', mensaje: 'No autenticado' });
    }
    try {
      const [rows]: any = await pool.query(
        'SELECT rol FROM usuarios WHERE id = ? LIMIT 1',
        [usuarioId]
      );
      if (rows.length === 0) {
        return res.status(403).json({ status: 'error', mensaje: 'Sin permisos' });
      }
      const rolUsuario = rows[0].rol;
      if (!rolesPermitidos.includes(rolUsuario)) {
        return res.status(403).json({
          status: 'error',
          mensaje: `El rol ${rolUsuario} no tiene permiso para esta acción`,
        });
      }
      next();
    } catch (error) {
      console.error('Error verificando rol:', error);
      res.status(500).json({ status: 'error', mensaje: 'Error al verificar el rol' });
    }
  };
};
