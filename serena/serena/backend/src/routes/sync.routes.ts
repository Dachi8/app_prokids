import { Router } from 'express';
import { verificarToken } from '../middlewares/auth.middleware';
import {
  listarTareasPaciente,
  crearTareaSync,
  completarTareaAsignacion,
  eliminarTareaAsignacion,
} from '../controllers/tarea.controller';
import { listarAnimos, crearAnimo } from '../controllers/animo.controller';
import { listarNotas, crearNota, borrarNota } from '../controllers/notas.controller';

const router = Router();
router.use(verificarToken);

// Tareas
router.get('/tareas/:codigoPaciente', listarTareasPaciente);
router.post('/tareas', crearTareaSync);
router.patch('/tareas/:tareaId/completar', completarTareaAsignacion);
router.delete('/tareas/:tareaId', eliminarTareaAsignacion);

// Ánimo
router.get('/animo/:codigoPaciente', listarAnimos);
router.post('/animo', crearAnimo);

// Notas
router.get('/notas/:codigoPaciente', listarNotas);
router.post('/notas', crearNota);
router.delete('/notas/:notaId', borrarNota);

export default router;
