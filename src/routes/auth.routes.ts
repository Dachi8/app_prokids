import { Router } from 'express';
import { registrar, login } from '../controllers/auth.controller';
import { verificarToken } from '../middlewares/auth.middleware';

const router = Router();

router.post('/registro', registrar);
router.post('/login', login);

router.get('/perfil', verificarToken, (req: any, res) => {
  res.json({ status: 'ok', usuario_id: req.usuarioId });
});

export default router;