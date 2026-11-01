import { pool } from '../config/database';
import { normalizarCodigo } from './perfil';

/** Busca un usuario por su código de vinculación (SERENA-XXXX). */
export const buscarPorCodigo = async (codigo: string): Promise<any | null> => {
  const [rows]: any = await pool.query(
    `SELECT id, nombre, correo, rol, codigo_vinculacion, puntos_balance,
            avatar_emoji, fecha_nacimiento, estado
     FROM usuarios WHERE codigo_vinculacion = ? LIMIT 1`,
    [normalizarCodigo(codigo)]
  );
  return rows.length > 0 ? rows[0] : null;
};

/** Busca un usuario por id. */
export const buscarPorId = async (id: number): Promise<any | null> => {
  const [rows]: any = await pool.query(
    `SELECT id, nombre, correo, rol, codigo_vinculacion, puntos_balance,
            avatar_emoji, fecha_nacimiento, estado
     FROM usuarios WHERE id = ? LIMIT 1`,
    [id]
  );
  return rows.length > 0 ? rows[0] : null;
};

/** Devuelve el rol almacenado en `usuarios.rol`. */
export const rolDe = async (usuarioId: number): Promise<string> => {
  const [rows]: any = await pool.query(
    'SELECT rol FROM usuarios WHERE id = ? LIMIT 1',
    [usuarioId]
  );
  return rows.length > 0 ? rows[0].rol : '';
};

/**
 * Un usuario puede ver los datos de un paciente si es él mismo o si es un
 * médico/familiar vinculado en `representantes_pacientes`.
 */
export const puedeAccederPaciente = async (
  authId: number,
  pacienteId: number
): Promise<boolean> => {
  if (Number(authId) === Number(pacienteId)) return true;
  const [rows]: any = await pool.query(
    `SELECT id FROM representantes_pacientes
     WHERE representante_id = ? AND paciente_id = ? LIMIT 1`,
    [authId, pacienteId]
  );
  return rows.length > 0;
};

/**
 * Resuelve el paciente sobre el que actúa una petición.
 * - Si llega `codigo`, se busca por código y se comprueba el acceso.
 * - Si no llega, se usa el propio usuario autenticado.
 */
export const resolverPaciente = async (
  authId: number,
  codigo?: string
): Promise<{ paciente: any | null; autorizado: boolean }> => {
  if (codigo && String(codigo).trim().length > 0) {
    const paciente = await buscarPorCodigo(codigo);
    if (!paciente) return { paciente: null, autorizado: false };
    const autorizado = await puedeAccederPaciente(authId, Number(paciente.id));
    return { paciente, autorizado };
  }
  const paciente = await buscarPorId(authId);
  return { paciente, autorizado: Boolean(paciente) };
};

/** Genera un código único SERENA-XXXX comprobando la base de datos. */
export const generarCodigoUnico = async (): Promise<string> => {
  const crypto = await import('crypto');
  for (let intento = 0; intento < 60; intento++) {
    const codigo = `SERENA-${crypto.randomInt(0, 10000).toString().padStart(4, '0')}`;
    const [rows]: any = await pool.query(
      'SELECT id FROM usuarios WHERE codigo_vinculacion = ? LIMIT 1',
      [codigo]
    );
    if (rows.length === 0) return codigo;
  }
  return `SERENA-${crypto.randomBytes(4).toString('hex').toUpperCase()}`;
};
