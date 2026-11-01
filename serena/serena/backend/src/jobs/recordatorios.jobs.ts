import cron from 'node-cron';
import { pool } from '../config/database';

/**
 * Marca como enviados los recordatorios cuya hora ya pasó.
 * Si más adelante se configuran credenciales de Firebase, aquí es donde se
 * enviaría la notificación push al dispositivo del paciente.
 */
const procesarRecordatorios = async () => {
  try {
    const [rows]: any = await pool.query(
      `SELECT r.id, r.ejecucion_id, r.hora_envio,
              ta.usuario_id, t.que_hacer
       FROM recordatorios r
       JOIN tarea_ejecuciones te ON te.id = r.ejecucion_id
       JOIN tarea_asignaciones ta ON ta.id = te.asignacion_id
       JOIN tareas t ON t.id = ta.tarea_id
       WHERE r.enviado = 0 AND r.hora_envio <= NOW()
       ORDER BY r.hora_envio ASC
       LIMIT 200`
    );

    if (rows.length === 0) return;

    for (const recordatorio of rows) {
      await pool.query(
        'UPDATE recordatorios SET enviado = 1, fecha_envio = ? WHERE id = ?',
        [Date.now(), recordatorio.id]
      );
      console.log(
        `[recordatorios] Recordatorio ${recordatorio.id} procesado para el usuario ${recordatorio.usuario_id}: ${recordatorio.que_hacer}`
      );
    }
  } catch (error) {
    console.error('[recordatorios] Error procesando recordatorios:', error);
  }
};

/** Inicia el job que revisa recordatorios cada minuto. */
export const iniciarJobRecordatorios = () => {
  cron.schedule('* * * * *', () => {
    void procesarRecordatorios();
  });
  console.log('Job de recordatorios iniciado (revisión cada minuto)');
};
