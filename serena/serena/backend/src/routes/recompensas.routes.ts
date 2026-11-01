import { Router } from 'express';
import { verificarToken } from '../middlewares/auth.middleware';
import { listarRecompensas, canjearRecompensa } from '../controllers/recompensas.controller';

const router = Router();
router.use(verificarToken);

router.get('/', listarRecompensas);
router.post('/canjear', canjearRecompensa);

export default router;
