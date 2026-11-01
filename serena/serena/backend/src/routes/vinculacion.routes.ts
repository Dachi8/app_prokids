import { Router } from 'express';
import { verificarToken } from '../middlewares/auth.middleware';
import {
  resolverCodigo,
  crearVinculo,
  misPacientes,
  desvincular,
  obtenerPacientePorId,
} from '../controllers/vinculacion.controller';

const router = Router();
router.use(verificarToken);

router.get('/paciente/:codigo', resolverCodigo);
router.get('/paciente-id/:id', obtenerPacientePorId);
router.get('/mis-pacientes', misPacientes);
router.post('/', crearVinculo);
router.delete('/:pacienteId', desvincular);

export default router;
