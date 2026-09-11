import { Router } from 'express';
import { registrarDispositivo } from '../controllers/dispositivo.controller';
import { verificarToken } from '../middlewares/auth.middleware';

const router = Router();
router.use(verificarToken);
router.post('/', registrarDispositivo);

export default router;