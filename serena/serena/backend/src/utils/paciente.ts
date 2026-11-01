import { Request } from 'express';
import { buscarPorCodigo, buscarPorId, puedeAccederPaciente } from './acceso';

/** Extrae el código de paciente de params, query o body. */
export const codigoDeRequest = (req: Request): string | null => {
  const raw =
    (req.params && (req.params.codigoPaciente || req.params.codigo)) ||
    (req.query && (req.query.codigo as string)) ||
    (req.body && req.body.paciente_codigo);
  const codigo = raw ? String(raw).trim() : '';
  return codigo.length > 0 ? codigo : null;
};

export type ResultadoPaciente = {
  ok: boolean;
  paciente?: any;
  code: number;
  mensaje: string;
};

/**
 * Resuelve sobre qué paciente actúa una petición.
 * - Con `codigo`: busca al paciente y comprueba que el usuario tenga acceso.
 * - Sin `codigo`: usa el propio usuario autenticado.
 */
export const resolverPacienteRequest = async (req: Request): Promise<ResultadoPaciente> => {
  const authId = Number((req as any).usuarioId);
  const codigo = codigoDeRequest(req);

  if (codigo) {
    const paciente = await buscarPorCodigo(codigo);
    if (!paciente) {
      return { ok: false, code: 404, mensaje: 'Paciente no encontrado' };
    }
    const autorizado = await puedeAccederPaciente(authId, Number(paciente.id));
    if (!autorizado) {
      return { ok: false, code: 403, mensaje: 'No autorizado para acceder a este paciente' };
    }
    return { ok: true, paciente, code: 200, mensaje: '' };
  }

  const paciente = await buscarPorId(authId);
  if (!paciente) {
    return { ok: false, code: 404, mensaje: 'Usuario no encontrado' };
  }
  return { ok: true, paciente, code: 200, mensaje: '' };
};
