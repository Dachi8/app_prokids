/**
 * Extrae el token de la cabecera `Authorization`.
 *
 * Acepta tanto el formato estándar `Bearer <token>` como el token en claro,
 * porque la app Android envía el valor tal cual lo devolvió el login.
 */
export const parseToken = (authHeader: string | string[] | undefined): string | null => {
  if (!authHeader) return null;
  const raw = Array.isArray(authHeader) ? authHeader[0] : authHeader;
  if (!raw) return null;
  const parts = raw.trim().split(/\s+/);
  if (parts.length === 1) return parts[0] || null;
  const [type, token] = parts;
  return type.toLowerCase() === 'bearer' && token ? token : null;
};
