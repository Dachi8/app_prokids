import { Router } from 'express';
import { verificarToken } from '../middlewares/auth.middleware';
import {
  listarNotas,
  listarNotasCompartidas,
  crearNota,
  borrarNota,
} from '../controllers/notas.controller';

const router = Router();
router.use(verificarToken);

router.get('/compartidas', listarNotasCompartidas);
router.get('/', listarNotas);
router.post('/', crearNota);
router.delete('/:id', borrarNota);

export default router;
