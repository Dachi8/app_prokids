import cron from 'node-cron';
import { pool } from '../config/database';
import { mensajeria } from '../config/firebase';

export const iniciarJobRecordatorios = () => {
  cron.schedule('* * * * *', async () => {
    try {
      const [recordatoriosPendientes]: any = await pool.query(
        `SELECT r.id AS recordatorio_id, t.que_hacer, dp.fcm_token
         FROM recordatorios r
         JOIN tarea_ejecuciones te ON te.id = r.ejecucion_id
         JOIN tarea_asignaciones ta ON ta.id = te.asignacion_id
         JOIN tareas t ON t.id = ta.tarea_id
         JOIN dispositivos_push dp ON dp.usuario_id = ta.usuario_id
         WHERE r.enviado = FALSE AND r.hora_envio <= NOW()`
      );

      for (const recordatorio of recordatoriosPendientes) {
        try {
          await mensajeria.send({
            token: recordatorio.fcm_token,
            notification: {
              title: 'Recordatorio de tarea',
              body: `Es hora de: ${recordatorio.que_hacer}`,
            },
          });

          await pool.query(
            'UPDATE recordatorios SET enviado = TRUE, fecha_envio = NOW() WHERE id = ?',
            [recordatorio.recordatorio_id]
          );
        } catch (errorEnvio) {
          console.error(`Error enviando recordatorio ${recordatorio.recordatorio_id}:`, errorEnvio);
        }
      }
    } catch (error) {
      console.error('Error en el job de recordatorios:', error);
    }
  });

  console.log('Job de recordatorios iniciado (revisa cada minuto)');
};