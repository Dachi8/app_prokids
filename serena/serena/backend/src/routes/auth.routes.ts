import { Router } from 'express';
import {
  registrar,
  login,
  obtenerPerfil,
  sincronizarPerfil,
  regenerarCodigo,
  eliminarCuenta,
} from '../controllers/auth.controller';
import { verificarToken } from '../middlewares/auth.middleware';

const router = Router();

router.post('/registro', registrar);
router.post('/login', login);

router.get('/perfil', verificarToken, obtenerPerfil);
router.post('/sincronizar-perfil', verificarToken, sincronizarPerfil);
router.post('/regenerar-codigo', verificarToken, regenerarCodigo);
router.delete('/cuenta', verificarToken, eliminarCuenta);

export default router;
