import { Router } from 'express';
import { verificarToken } from '../middlewares/auth.middleware';
import { listarAnimos, crearAnimo, borrarAnimo } from '../controllers/animo.controller';

const router = Router();
router.use(verificarToken);

router.get('/', listarAnimos);
router.post('/', crearAnimo);
router.delete('/:id', borrarAnimo);

export default router;
