import { Request, Response } from 'express';
import { pool } from '../config/database';
import { perfilDto } from '../utils/perfil';
import { buscarPorCodigo, buscarPorId } from '../utils/acceso';

const ROLES_QUE_VINCULAN = ['MEDICO', 'FAMILIAR', 'ADMINISTRADOR'];

/** GET /api/vinculaciones/paciente/:codigo — resuelve un código SERENA-XXXX. */
export const resolverCodigo = async (req: Request, res: Response) => {
  const { codigo } = req.params;
  try {
    const paciente = await buscarPorCodigo(codigo);
    if (!paciente) {
      return res.status(404).json({ status: 'error', mensaje: 'Código no encontrado' });
    }
    res.json({ status: 'ok', paciente: perfilDto(paciente) });
  } catch (error) {
    console.error('Error resolviendo código:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al resolver el código' });
  }
};

/** POST /api/vinculaciones — vincula al perfil autenticado con un paciente. */
export const crearVinculo = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const { codigo_paciente, parentesco } = req.body || {};

  if (!codigo_paciente || !String(codigo_paciente).trim()) {
    return res.status(400).json({ status: 'error', mensaje: 'Falta el código del paciente' });
  }

  try {
    const [yo]: any = await pool.query('SELECT rol FROM usuarios WHERE id = ? LIMIT 1', [authId]);
    if (yo.length === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Usuario no encontrado' });
    }
    if (!ROLES_QUE_VINCULAN.includes(yo[0].rol)) {
      return res.status(403).json({
        status: 'error',
        mensaje: 'Solo médicos, familiares o administradores pueden vincular pacientes',
      });
    }

    const paciente = await buscarPorCodigo(codigo_paciente);
    if (!paciente) {
      return res.status(404).json({ status: 'error', mensaje: 'Paciente no encontrado' });
    }

    if (Number(paciente.id) === authId) {
      return res.status(400).json({ status: 'error', mensaje: 'No puedes vincularte contigo mismo' });
    }

    await pool.query(
      `INSERT INTO representantes_pacientes
         (representante_id, paciente_id, parentesco, fecha_registro)
       VALUES (?, ?, ?, ?)
       ON DUPLICATE KEY UPDATE parentesco = VALUES(parentesco)`,
      [authId, Number(paciente.id), parentesco || 'Familiar', Date.now()]
    );

    res.status(201).json({
      status: 'ok',
      mensaje: 'Paciente vinculado correctamente',
      paciente: perfilDto(paciente),
    });
  } catch (error) {
    console.error('Error creando vínculo:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al vincular el paciente' });
  }
};

/** GET /api/vinculaciones/mis-pacientes — pacientes vinculados al perfil. */
export const misPacientes = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  try {
    const [rows]: any = await pool.query(
      `SELECT u.*, rp.parentesco
       FROM representantes_pacientes rp
       JOIN usuarios u ON u.id = rp.paciente_id
       WHERE rp.representante_id = ?
       ORDER BY u.nombre ASC`,
      [authId]
    );

    res.json({
      status: 'ok',
      pacientes: rows.map((fila: any) => ({
        ...perfilDto(fila),
        parentesco: fila.parentesco ?? 'Familiar',
      })),
    });
  } catch (error) {
    console.error('Error listando pacientes:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar los pacientes vinculados' });
  }
};

/** DELETE /api/vinculaciones/:pacienteId — elimina el vínculo, no al paciente. */
export const desvincular = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const pacienteId = Number(req.params.pacienteId);
  try {
    const [resultado]: any = await pool.query(
      'DELETE FROM representantes_pacientes WHERE representante_id = ? AND paciente_id = ?',
      [authId, pacienteId]
    );
    if (resultado.affectedRows === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Vínculo no encontrado' });
    }
    res.json({ status: 'ok', mensaje: 'Paciente desvinculado' });
  } catch (error) {
    console.error('Error desvinculando paciente:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al desvincular el paciente' });
  }
};

/** GET /api/vinculaciones/paciente-id/:id — perfil de un paciente por id. */
export const obtenerPacientePorId = async (req: Request, res: Response) => {
  const pacienteId = Number(req.params.id);
  try {
    const paciente = await buscarPorId(pacienteId);
    if (!paciente) {
      return res.status(404).json({ status: 'error', mensaje: 'Paciente no encontrado' });
    }
    res.json({ status: 'ok', paciente: perfilDto(paciente) });
  } catch (error) {
    console.error('Error obteniendo paciente:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al obtener el paciente' });
  }
};
