import { Router } from 'express';
import { verificarToken } from '../middlewares/auth.middleware';
import { registrarDispositivo, listarDispositivos } from '../controllers/dispositivo.controller';

const router = Router();
router.use(verificarToken);

router.get('/', listarDispositivos);
router.post('/', registrarDispositivo);

export default router;
