import { z } from 'zod';

export const crearTareaSchema = z.object({
  que_hacer: z.string().min(3),
  descripcion: z.string().optional(),
  frecuencia: z.enum(['diaria', 'semanal', 'unica']),
  hora_limite: z.string().optional(),
});

export const asignarTareaSchema = z.object({
  tarea_id: z.number(),
  usuario_id: z.number(),
});

export const completarEjecucionSchema = z.object({
  ejecucion_id: z.number(),
});