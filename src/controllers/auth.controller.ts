import { Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import { pool } from '../config/database';
import { registroSchema, loginSchema } from '../schemas/auth.schema';

export const registrar = async (req: Request, res: Response) => {
  const parseResult = registroSchema.safeParse(req.body);

  if (!parseResult.success) {
    return res.status(400).json({
      status: 'error',
      errores: parseResult.error.flatten().fieldErrors,
    });
  }

  const { nombre, correo, password, fecha_nacimiento } = parseResult.data;

  try {
    const [existentes]: any = await pool.query(
      'SELECT id FROM usuarios WHERE correo = ?',
      [correo]
    );

    if (existentes.length > 0) {
      return res.status(409).json({ status: 'error', mensaje: 'Ese correo ya está registrado' });
    }

    const passwordHash = await bcrypt.hash(password, 10);

    const [resultado]: any = await pool.query(
      'INSERT INTO usuarios (nombre, correo, password_hash, fecha_nacimiento) VALUES (?, ?, ?, ?)',
      [nombre, correo, passwordHash, fecha_nacimiento || null]
    );

    res.status(201).json({
      status: 'ok',
      mensaje: 'Usuario registrado correctamente',
      usuario_id: resultado.insertId,
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'Error al registrar usuario' });
  }
};

import crypto from 'crypto';

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
      'SELECT id, nombre, password_hash, estado FROM usuarios WHERE correo = ?',
      [correo]
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

    const token = crypto.randomBytes(32).toString('hex');
    const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
    const fechaExpiracion = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000); // 7 días

    await pool.query(
      'INSERT INTO sesiones (usuario_id, token_hash, fecha_expiracion) VALUES (?, ?, ?)',
      [usuario.id, tokenHash, fechaExpiracion]
    );

    await pool.query('UPDATE usuarios SET ultimo_acceso = NOW() WHERE id = ?', [usuario.id]);

    res.json({
      status: 'ok',
      token,
      usuario: { id: usuario.id, nombre: usuario.nombre },
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'Error al iniciar sesión' });
  }
};