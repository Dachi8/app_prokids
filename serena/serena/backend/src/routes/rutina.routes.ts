import { Router } from 'express';
import { verificarToken } from '../middlewares/auth.middleware';
import {
  listarRutinas,
  crearRutina,
  toggleRutina,
  borrarRutina,
} from '../controllers/rutina.controller';

const router = Router();
router.use(verificarToken);

router.get('/', listarRutinas);
router.post('/', crearRutina);
router.patch('/:id/toggle', toggleRutina);
router.delete('/:id', borrarRutina);

export default router;
