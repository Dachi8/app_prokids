  import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';

dotenv.config();
const app = express();

app.use(cors());
app.use(express.json());

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', mensaje: 'Servidor ProKids funcionando' });
});

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => {
  console.log(`Servidor corriendo en http://localhost:${PORT}`);
});

import { pool } from './config/database';

app.get('/api/db-test', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT 1 + 1 AS resultado');
    res.json({ status: 'ok', db: rows });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', mensaje: 'No se pudo conectar a la base de datos' });
  }
});

import authRoutes from './routes/auth.routes';

app.use('/api/auth', authRoutes);

import tareasRoutes from './routes/tareas.routes';
app.use('/api/tareas', tareasRoutes);

import dispositivoRoutes from './routes/dispositivo.routes';
app.use('/api/dispositivos', dispositivoRoutes);

import { iniciarJobRecordatorios } from './jobs/recordatorios.jobs';

// ... después de app.listen(...)
iniciarJobRecordatorios();