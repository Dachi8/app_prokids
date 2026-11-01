import { Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import crypto from 'crypto';
import { pool } from '../config/database';
import { registroSchema, loginSchema, perfilSchema } from '../schemas/auth.schema';
import { ahoraMs, normalizarCodigo, perfilDto } from '../utils/perfil';
import { generarCodigoUnico } from '../utils/acceso';

const AVATARES_POR_ROL: Record<string, string> = {
  PACIENTE: '🌱',
  MEDICO: '🩺',
  FAMILIAR: '👨‍👩‍👧',
  ADMINISTRADOR: '🛡️',
};

/** Crea una sesión y devuelve el token en claro (solo se guarda su hash). */
const crearSesion = async (usuarioId: number): Promise<string> => {
  const token = crypto.randomBytes(32).toString('hex');
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
  const fechaExpiracion = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
  await pool.query(
    'INSERT INTO sesiones (usuario_id, token_hash, fecha_expiracion, fecha_creacion) VALUES (?, ?, ?, ?)',
    [usuarioId, tokenHash, fechaExpiracion, ahoraMs()]
  );
  return token;
};

const asignarRol = async (usuarioId: number, rol: string) => {
  const [rolRows]: any = await pool.query(
    'SELECT id FROM roles WHERE nombre = ? LIMIT 1',
    [rol]
  );
  if (rolRows.length > 0) {
    await pool.query(
      'INSERT IGNORE INTO usuarios_roles (usuario_id, rol_id) VALUES (?, ?)',
      [usuarioId, rolRows[0].id]
    );
  }
};

/**
 * Registro idempotente:
 * - Si el correo no existe, crea el usuario.
 * - Si ya existe, devuelve su perfil y una sesión nueva (si la contraseña
 *   coincide cuando se envía). Esto permite que la app funcione igual al
 *   reinstalarse o al recuperar una cuenta existente.
 */
export const registrar = async (req: Request, res: Response) => {
  const parseResult = registroSchema.safeParse(req.body);
  if (!parseResult.success) {
    return res.status(400).json({
      status: 'error',
      errores: parseResult.error.flatten().fieldErrors,
    });
  }

  const {
    nombre,
    correo,
    password,
    fecha_nacimiento,
    rol,
    codigo_vinculacion,
    puntos_balance,
    avatar_emoji,
  } = parseResult.data;

  try {
    const [existentes]: any = await pool.query(
      'SELECT * FROM usuarios WHERE correo = ? LIMIT 1',
      [correo.trim().toLowerCase()]
    );

    if (existentes.length > 0) {
      const usuario = existentes[0];
      if (password) {
        const valida = await bcrypt.compare(password, usuario.password_hash);
        if (!valida) {
          return res.status(401).json({
            status: 'error',
            mensaje: 'Ese correo ya está registrado. Escribe tu contraseña o usa otro correo.',
          });
        }
      }
      await pool.query('UPDATE usuarios SET ultimo_acceso = ? WHERE id = ?', [
        ahoraMs(),
        usuario.id,
      ]);
      const token = await crearSesion(Number(usuario.id));
      return res.json({
        status: 'ok',
        mensaje: 'Sesión iniciada con la cuenta existente',
        usuario_id: Number(usuario.id),
        token,
        codigo_vinculacion: usuario.codigo_vinculacion,
        rol: usuario.rol,
      });
    }

    const rolEfectivo = (rol || 'PACIENTE').toUpperCase();
    const claveEfectiva = password || crypto.randomBytes(24).toString('hex');
    const passwordHash = await bcrypt.hash(claveEfectiva, 10);

    let codigo = codigo_vinculacion ? normalizarCodigo(codigo_vinculacion) : null;
    if (codigo) {
      const [conCodigo]: any = await pool.query(
        'SELECT id FROM usuarios WHERE codigo_vinculacion = ? LIMIT 1',
        [codigo]
      );
      if (conCodigo.length > 0) codigo = await generarCodigoUnico();
    } else {
      codigo = await generarCodigoUnico();
    }

    const [resultado]: any = await pool.query(
      `INSERT INTO usuarios
         (nombre, correo, password_hash, fecha_nacimiento, estado, fecha_registro,
          rol, codigo_vinculacion, puntos_balance, avatar_emoji)
       VALUES (?, ?, ?, ?, 'activo', ?, ?, ?, ?, ?)`,
      [
        nombre.trim(),
        correo.trim().toLowerCase(),
        passwordHash,
        fecha_nacimiento || null,
        ahoraMs(),
        rolEfectivo,
        codigo,
        puntos_balance ?? 0,
        avatar_emoji || AVATARES_POR_ROL[rolEfectivo] || '🌱',
      ]
    );

    const usuarioId = Number(resultado.insertId);
    await asignarRol(usuarioId, rolEfectivo);
    const token = await crearSesion(usuarioId);

    res.status(201).json({
      status: 'ok',
      mensaje: 'Usuario registrado correctamente',
      usuario_id: usuarioId,
      token,
      codigo_vinculacion: codigo,
      rol: rolEfectivo,
    });
  } catch (error) {
    console.error('Error en registro:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al registrar usuario' });
  }
};

export const login = async (req: Request, res: Response) => {
  const parseResult = loginSchema.safeParse(req.body);
  if (!parseResult.success) {
    return res.status(400).json({
      status: 'error',
      errores: parseResult.error.flatten().fieldErrors,
    });
  }

  const { correo, password } = parseResult.data;

  try {
    const [rows]: any = await pool.query(
      'SELECT * FROM usuarios WHERE correo = ? LIMIT 1',
      [correo.trim().toLowerCase()]
    );

    if (rows.length === 0) {
      return res.status(401).json({ status: 'error', mensaje: 'Credenciales inválidas' });
    }

    const usuario = rows[0];
    if (usuario.estado !== 'activo') {
      return res.status(403).json({ status: 'error', mensaje: 'Usuario inactivo o bloqueado' });
    }

    const passwordValida = await bcrypt.compare(password, usuario.password_hash);
    if (!passwordValida) {
      return res.status(401).json({ status: 'error', mensaje: 'Credenciales inválidas' });
    }

    const token = await crearSesion(Number(usuario.id));
    await pool.query('UPDATE usuarios SET ultimo_acceso = ? WHERE id = ?', [
      ahoraMs(),
      usuario.id,
    ]);

    res.json({
      status: 'ok',
      token,
      usuario: perfilDto(usuario),
    });
  } catch (error) {
    console.error('Error en login:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al iniciar sesión' });
  }
};

/** Devuelve el perfil del usuario autenticado. */
export const obtenerPerfil = async (req: Request, res: Response) => {
  const usuarioId = (req as any).usuarioId;
  try {
    const [rows]: any = await pool.query(
      'SELECT * FROM usuarios WHERE id = ? LIMIT 1',
      [usuarioId]
    );
    if (rows.length === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Usuario no encontrado' });
    }
    res.json({ status: 'ok', usuario: perfilDto(rows[0]) });
  } catch (error) {
    console.error('Error obteniendo perfil:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al obtener el perfil' });
  }
};

/** Sincroniza rol, código, saldo y avatar del perfil con el servidor. */
export const sincronizarPerfil = async (req: Request, res: Response) => {
  const usuarioId = (req as any).usuarioId;
  const parseResult = perfilSchema.safeParse(req.body || {});
  if (!parseResult.success) {
    return res.status(400).json({
      status: 'error',
      errores: parseResult.error.flatten().fieldErrors,
    });
  }

  const { rol, codigo_vinculacion, puntos_balance, avatar_emoji } = parseResult.data;

  try {
    const [rows]: any = await pool.query(
      'SELECT * FROM usuarios WHERE id = ? LIMIT 1',
      [usuarioId]
    );
    if (rows.length === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Usuario no encontrado' });
    }

    const campos: string[] = [];
    const valores: any[] = [];

    if (rol) {
      const rolLimpio = rol.toUpperCase();
      campos.push('rol = ?');
      valores.push(rolLimpio);
      await asignarRol(Number(usuarioId), rolLimpio);
    }
    if (typeof puntos_balance === 'number') {
      campos.push('puntos_balance = ?');
      valores.push(puntos_balance);
    }
    if (avatar_emoji) {
      campos.push('avatar_emoji = ?');
      valores.push(avatar_emoji);
    }

    if (codigo_vinculacion) {
      const codigo = normalizarCodigo(codigo_vinculacion);
      const [otros]: any = await pool.query(
        'SELECT id FROM usuarios WHERE codigo_vinculacion = ? AND id <> ? LIMIT 1',
        [codigo, usuarioId]
      );
      if (otros.length === 0) {
        campos.push('codigo_vinculacion = ?');
        valores.push(codigo);
      }
    }

    if (campos.length > 0) {
      valores.push(usuarioId);
      await pool.query(`UPDATE usuarios SET ${campos.join(', ')} WHERE id = ?`, valores);
    }

    const [perfil]: any = await pool.query(
      'SELECT * FROM usuarios WHERE id = ? LIMIT 1',
      [usuarioId]
    );

    res.json({ status: 'ok', usuario: perfilDto(perfil[0]) });
  } catch (error) {
    console.error('Error sincronizando perfil:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al sincronizar el perfil' });
  }
};

/** Genera un nuevo código de vinculación para el perfil autenticado. */
export const regenerarCodigo = async (req: Request, res: Response) => {
  const usuarioId = (req as any).usuarioId;
  try {
    const codigo = await generarCodigoUnico();
    await pool.query('UPDATE usuarios SET codigo_vinculacion = ? WHERE id = ?', [
      codigo,
      usuarioId,
    ]);
    res.json({
      status: 'ok',
      codigo_vinculacion: codigo,
      mensaje: 'Código de vinculación actualizado',
    });
  } catch (error) {
    console.error('Error regenerando código:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al generar el código' });
  }
};

/** Elimina la cuenta y todos sus datos (las FK están en ON DELETE CASCADE). */
export const eliminarCuenta = async (req: Request, res: Response) => {
  const usuarioId = (req as any).usuarioId;
  try {
    await pool.query('DELETE FROM usuarios WHERE id = ?', [usuarioId]);
    res.json({ status: 'ok', mensaje: 'Cuenta desactivada y datos eliminados' });
  } catch (error) {
    console.error('Error eliminando cuenta:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al eliminar la cuenta' });
  }
};
