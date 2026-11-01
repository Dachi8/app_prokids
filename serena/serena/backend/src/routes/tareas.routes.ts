import { Router } from 'express';
import { verificarToken } from '../middlewares/auth.middleware';
import {
  listarMisTareas,
  listarTareasPaciente,
  crearTareaPlantilla,
  asignarTareaUsuario,
  completarEjecucion,
  listarDetalleHoy,
} from '../controllers/tarea.controller';

const router = Router();
router.use(verificarToken);

router.get('/mis-tareas', listarMisTareas);
router.get('/paciente/:codigo', listarTareasPaciente);
router.get('/detalle/:codigo', listarDetalleHoy);
router.post('/plantilla', crearTareaPlantilla);
router.post('/asignar', asignarTareaUsuario);
router.patch('/completar', completarEjecucion);

export default router;
