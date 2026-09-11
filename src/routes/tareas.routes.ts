import { Router } from 'express';
import { crearTarea, asignarTarea, listarMisTareas, completarEjecucion } from '../controllers/tarea.controller';
import { verificarToken } from '../middlewares/auth.middleware';
import { verificarRol } from '../middlewares/rol.middleware';

const router = Router();

router.use(verificarToken);

router.post('/', verificarRol(['Administrador']), crearTarea);
router.post('/asignar', verificarRol(['Administrador']), asignarTarea);
router.get('/mis-tareas', listarMisTareas);
router.patch('/completar', completarEjecucion);

export default router;