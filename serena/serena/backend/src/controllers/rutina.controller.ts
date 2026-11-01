import { Request, Response } from 'express';
import { pool } from '../config/database';
import { fechaHoy, mapaRutina } from '../utils/perfil';
import { puedeAccederPaciente } from '../utils/acceso';
import { resolverPacienteRequest } from '../utils/paciente';

const RUTINAS_POR_DEFECTO = [
  {
    title: 'Respirar 3 minutos',
    description: 'Inhala 4 segundos, sostén 4 y exhala 6. Repite el ciclo.',
    category: 'MORNING',
    icon_key: 'wind',
  },
  {
    title: 'Caminar al aire libre',
    description: 'Un paseo corto para despejar la mente y mover el cuerpo.',
    category: 'WELLNESS',
    icon_key: 'walk',
  },
  {
    title: 'Gratitud nocturna',
    description: 'Anota tres cosas buenas que ocurrieron durante el día.',
    category: 'NIGHT',
    icon_key: 'moon',
  },
];

/** Crea las rutinas iniciales la primera vez que un paciente abre la pantalla. */
const asegurarRutinasPorDefecto = async (pacienteId: number) => {
  const [rows]: any = await pool.query(
    'SELECT COUNT(*) AS total FROM rutinas WHERE usuario_id = ?',
    [pacienteId]
  );
  if (Number(rows[0]?.total ?? 0) > 0) return;

  for (const rutina of RUTINAS_POR_DEFECTO) {
    await pool.query(
      `INSERT INTO rutinas
         (usuario_id, title, description, category, icon_key, completed_dates,
          streak, last_completed_date, target_days_per_week)
       VALUES (?, ?, ?, ?, ?, '', 0, '', 7)`,
      [pacienteId, rutina.title, rutina.description, rutina.category, rutina.icon_key]
    );
  }
};

const calcularRacha = (fechas: string[]): number => {
  const set = new Set(fechas);
  let racha = 0;
  const cursor = new Date();
  // Si hoy aún no se ha completado, la racha se cuenta desde ayer.
  if (!set.has(fechaHoy())) {
    cursor.setDate(cursor.getDate() - 1);
  }
  for (let i = 0; i < 3650; i++) {
    const y = cursor.getFullYear();
    const m = String(cursor.getMonth() + 1).padStart(2, '0');
    const d = String(cursor.getDate()).padStart(2, '0');
    const clave = `${y}-${m}-${d}`;
    if (!set.has(clave)) break;
    racha += 1;
    cursor.setDate(cursor.getDate() - 1);
  }
  return racha;
};

/** GET /api/rutinas?codigo=SERENA-XXXX */
export const listarRutinas = async (req: Request, res: Response) => {
  try {
    const resolucion = await resolverPacienteRequest(req);
    if (!resolucion.ok) {
      return res.status(resolucion.code).json({ status: 'error', mensaje: resolucion.mensaje });
    }

    const pacienteId = Number(resolucion.paciente.id);
    await asegurarRutinasPorDefecto(pacienteId);

    const [rows]: any = await pool.query(
      'SELECT * FROM rutinas WHERE usuario_id = ? ORDER BY id ASC',
      [pacienteId]
    );

    res.json({ status: 'ok', rutinas: rows.map(mapaRutina) });
  } catch (error) {
    console.error('Error listando rutinas:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al listar las rutinas' });
  }
};

/** POST /api/rutinas — crea una rutina personalizada. */
export const crearRutina = async (req: Request, res: Response) => {
  try {
    const resolucion = await resolverPacienteRequest(req);
    if (!resolucion.ok) {
      return res.status(resolucion.code).json({ status: 'error', mensaje: resolucion.mensaje });
    }

    const { title, description, category, icon_key, target_days_per_week } = req.body || {};
    if (!title || !String(title).trim()) {
      return res.status(400).json({ status: 'error', mensaje: 'El título es obligatorio' });
    }

    const [resultado]: any = await pool.query(
      `INSERT INTO rutinas
         (usuario_id, title, description, category, icon_key, completed_dates,
          streak, last_completed_date, target_days_per_week)
       VALUES (?, ?, ?, ?, ?, '', 0, '', ?)`,
      [
        resolucion.paciente.id,
        String(title).trim(),
        description || '',
        category || 'WELLNESS',
        icon_key || 'heart',
        Number(target_days_per_week ?? 7),
      ]
    );

    const [rows]: any = await pool.query('SELECT * FROM rutinas WHERE id = ? LIMIT 1', [
      resultado.insertId,
    ]);

    res.status(201).json({
      status: 'ok',
      id: Number(resultado.insertId),
      rutina: rows.length > 0 ? mapaRutina(rows[0]) : null,
    });
  } catch (error) {
    console.error('Error creando rutina:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al crear la rutina' });
  }
};

/** PATCH /api/rutinas/:id/toggle — marca o desmarca el día actual. */
export const toggleRutina = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const id = Number(req.params.id);
  const fecha = (req.body?.fecha as string) || fechaHoy();

  try {
    const [rows]: any = await pool.query('SELECT * FROM rutinas WHERE id = ? LIMIT 1', [id]);
    if (rows.length === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Rutina no encontrada' });
    }

    const rutina = rows[0];
    if (!(await puedeAccederPaciente(authId, Number(rutina.usuario_id)))) {
      return res.status(403).json({ status: 'error', mensaje: 'No autorizado' });
    }

    const fechas = String(rutina.completed_dates || '')
      .split(',')
      .map((f) => f.trim())
      .filter(Boolean);

    const indice = fechas.indexOf(fecha);
    if (indice >= 0) {
      fechas.splice(indice, 1);
    } else {
      fechas.push(fecha);
    }

    fechas.sort();
    const racha = calcularRacha(fechas);
    const ultima = fechas.length > 0 ? fechas[fechas.length - 1] : '';

    await pool.query(
      `UPDATE rutinas
       SET completed_dates = ?, streak = ?, last_completed_date = ?
       WHERE id = ?`,
      [fechas.join(','), racha, ultima, id]
    );

    const [actualizadas]: any = await pool.query('SELECT * FROM rutinas WHERE id = ? LIMIT 1', [id]);
    res.json({ status: 'ok', rutina: mapaRutina(actualizadas[0]) });
  } catch (error) {
    console.error('Error actualizando rutina:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al actualizar la rutina' });
  }
};

/** DELETE /api/rutinas/:id */
export const borrarRutina = async (req: Request, res: Response) => {
  const authId = Number((req as any).usuarioId);
  const id = Number(req.params.id);
  try {
    const [rows]: any = await pool.query('SELECT usuario_id FROM rutinas WHERE id = ? LIMIT 1', [id]);
    if (rows.length === 0) {
      return res.status(404).json({ status: 'error', mensaje: 'Rutina no encontrada' });
    }
    if (!(await puedeAccederPaciente(authId, Number(rows[0].usuario_id)))) {
      return res.status(403).json({ status: 'error', mensaje: 'No autorizado' });
    }
    await pool.query('DELETE FROM rutinas WHERE id = ?', [id]);
    res.json({ status: 'ok', mensaje: 'Rutina eliminada' });
  } catch (error) {
    console.error('Error eliminando rutina:', error);
    res.status(500).json({ status: 'error', mensaje: 'Error al eliminar la rutina' });
  }
};
