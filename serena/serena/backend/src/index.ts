import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { pool } from './config/database';

import authRoutes from './routes/auth.routes';
import tareasRoutes from './routes/tareas.routes';
import animoRoutes from './routes/animo.routes';
import notasRoutes from './routes/notas.routes';
import recompensasRoutes from './routes/recompensas.routes';
import rutinaRoutes from './routes/rutina.routes';
import dispositivoRoutes from './routes/dispositivo.routes';
import vinculacionRoutes from './routes/vinculacion.routes';
import syncRoutes from './routes/sync.routes';
import { iniciarJobRecordatorios } from './jobs/recordatorios.jobs';

dotenv.config();

const app = express();

app.use(cors());
app.use(express.json({ limit: '2mb' }));

// Página principal informativa para abrir en el navegador.
app.get('/', async (_req, res) => {
  let dbStatus = 'Desconectada';
  try {
    await pool.query('SELECT 1');
    dbStatus = 'Conectada correctamente con MySQL';
  } catch (error: any) {
    dbStatus = `Error al conectar: ${error.message}`;
  }

  res.send(`
    <!DOCTYPE html>
    <html lang="es">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>Backend ProKids & Serena</title>
      <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #F8FAFC; color: #1E293B; margin: 0; padding: 40px; display: flex; justify-content: center; }
        .card { background: white; max-width: 680px; width: 100%; border-radius: 20px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.08); padding: 32px; border: 1px solid #E2E8F0; }
        .badge { display: inline-block; padding: 6px 14px; border-radius: 999px; font-weight: 600; font-size: 13px; background: #DCFCE7; color: #15803D; margin-bottom: 16px; }
        h1 { margin: 0 0 10px 0; color: #0F172A; font-size: 26px; }
        p { color: #64748B; font-size: 15px; line-height: 1.6; }
        .info-box { background: #F1F5F9; border-radius: 12px; padding: 16px; margin: 20px 0; font-family: monospace; font-size: 14px; }
        ul { list-style: none; padding: 0; margin: 16px 0; }
        li { padding: 10px 14px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 8px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center; }
        code { color: #0F172A; }
        a { color: #3B82F6; text-decoration: none; font-weight: 600; }
        a:hover { text-decoration: underline; }
      </style>
    </head>
    <body>
      <div class="card">
        <span class="badge">Servidor activo</span>
        <h1>Backend ProKids & Serena</h1>
        <p>Servicio backend listo para la aplicación Serena y la gestión de pacientes, tareas, ánimo, libreta y recompensas.</p>

        <div class="info-box">
          <strong>Base de datos MySQL:</strong> ${dbStatus}<br>
          <strong>Puerto:</strong> ${process.env.PORT || 4000}<br>
          <strong>Base:</strong> ${process.env.DB_NAME || 'bdd_proyecto_asistente'}
        </div>

        <h3>Endpoints de prueba:</h3>
        <ul>
          <li><span>Estado del servidor</span> <a href="/api/health" target="_blank">/api/health</a></li>
          <li><span>Verificación MySQL</span> <a href="/api/db-test" target="_blank">/api/db-test</a></li>
          <li><span>Autenticación</span> <code>/api/auth</code></li>
          <li><span>Tareas</span> <code>/api/tareas-prokids</code></li>
          <li><span>Sincronización</span> <code>/api/sync</code></li>
        </ul>
      </div>
    </body>
    </html>
  `);
});

// Health check.
app.get('/api/health', (_req, res) => {
  res.json({ status: 'ok', mensaje: 'Servidor ProKids funcionando correctamente' });
});

// Prueba de conexión a MySQL.
app.get('/api/db-test', async (_req, res) => {
  try {
    const [rows] = await pool.query('SELECT 1 + 1 AS resultado, DATABASE() AS base_datos');
    res.json({ status: 'ok', mensaje: 'Conexión a MySQL exitosa', data: rows });
  } catch (error: any) {
    console.error('Error en /api/db-test:', error);
    res.status(500).json({
      status: 'error',
      mensaje: 'No se pudo conectar a la base de datos MySQL',
      detalle: error.message,
    });
  }
});

// Rutas de la API.
app.use('/api/auth', authRoutes);
app.use('/api/tareas-prokids', tareasRoutes);
app.use('/api/animo', animoRoutes);
app.use('/api/notas', notasRoutes);
app.use('/api/recompensas', recompensasRoutes);
app.use('/api/rutinas', rutinaRoutes);
app.use('/api/dispositivos', dispositivoRoutes);
app.use('/api/vinculaciones', vinculacionRoutes);
app.use('/api/sync', syncRoutes);

// Manejo de rutas no encontradas.
app.use((_req, res) => {
  res.status(404).json({ status: 'error', mensaje: 'Ruta no encontrada' });
});

const PORT = Number(process.env.PORT) || 4000;

app.listen(PORT, () => {
  console.log('=========================================');
  console.log('Servidor ProKids corriendo en:');
  console.log(`-> Local:   http://localhost:${PORT}`);
  console.log(`-> Salud:   http://localhost:${PORT}/api/health`);
  console.log(`-> DB Test: http://localhost:${PORT}/api/db-test`);
  console.log('=========================================');
  iniciarJobRecordatorios();
});
