import { z } from 'zod';

export const registroSchema = z.object({
  nombre: z.string().min(1, 'El nombre es obligatorio'),
  correo: z.string().email('Correo inválido'),
  password: z.string().optional(),
  fecha_nacimiento: z.string().optional().nullable(),
  rol: z.string().optional(),
  codigo_vinculacion: z.string().optional().nullable(),
  puntos_balance: z.number().optional(),
  avatar_emoji: z.string().optional(),
});

export const loginSchema = z.object({
  correo: z.string().email('Correo inválido'),
  password: z.string(),
});

export const perfilSchema = z.object({
  rol: z.string().optional(),
  codigo_vinculacion: z.string().optional().nullable(),
  puntos_balance: z.number().optional(),
  avatar_emoji: z.string().optional(),
});
