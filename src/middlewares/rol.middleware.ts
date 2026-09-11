import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/database';

export const verificarRol = (rolesPermitidos: string[]) => {
  return async (req: Request, res: Response, next: NextFunction) => {
    const usuarioId = (req as any).usuarioId;

    try {
      const [rows]: any = await pool.query(
        `SELECT r.nombre FROM roles r
         JOIN usuarios_roles ur ON ur.rol_id = r.id
         WHERE ur.usuario_id = ?`,
        [usuarioId]
      );

      const rolesDelUsuario = rows.map((r: any) => r.nombre);
      const tienePermiso = rolesDelUsuario.some((rol: string) => rolesPermitidos.includes(rol));

      if (!tienePermiso) {
        return res.status(403).json({ status: 'error', mensaje: 'No tienes permiso para esta acción' });
      }

      next();
    } catch (error) {
      console.error(error);
      res.status(500).json({ status: 'error', mensaje: 'Error al verificar permisos' });
    }
  };
};