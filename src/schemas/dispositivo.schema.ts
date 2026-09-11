import { z } from 'zod';

export const registrarDispositivoSchema = z.object({
  fcm_token: z.string().min(10),
  plataforma: z.enum(['android', 'ios']),
});