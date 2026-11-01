/** Utilidades compartidas para fechas, códigos y respuestas de perfil. */

export const ahoraMs = (): number => Date.now();

/** Fecha local en formato YYYY-MM-DD (sin desfase UTC). */
export const fechaHoy = (): string => {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

export const normalizarCodigo = (codigo: string): string =>
  String(codigo || '').trim().toUpperCase().replace(/\s+/g, '');

/** Convierte una fila de `usuarios` al perfil público que consume la app. */
export const perfilDto = (u: any) => ({
  id: Number(u.id),
  nombre: u.nombre,
  correo: u.correo,
  rol: u.rol,
  codigo_vinculacion: u.codigo_vinculacion,
  puntos_balance: Number(u.puntos_balance ?? 0),
  avatar_emoji: u.avatar_emoji,
  fecha_nacimiento: u.fecha_nacimiento ?? null,
  estado: u.estado ?? 'activo',
});

/** Normaliza un valor TINYINT(1) de MySQL a booleano JSON. */
export const aBooleano = (valor: any): boolean =>
  valor === true || valor === 1 || valor === '1';

export const mapaAnimo = (r: any) => ({
  id: Number(r.id),
  usuario_id: Number(r.usuario_id),
  fecha_texto: r.fecha_texto,
  timestamp: Number(r.timestamp ?? 0),
  animo_score: Number(r.animo_score ?? 3),
  animo_label: r.animo_label ?? '',
  animo_emoji: r.animo_emoji ?? '',
  nivel_energia: Number(r.nivel_energia ?? 3),
  etiquetas_emocion: r.etiquetas_emocion ?? '',
  etiquetas_contexto: r.etiquetas_contexto ?? '',
  nota: r.nota ?? '',
});

export const mapaNota = (r: any) => ({
  id: Number(r.id),
  usuario_id: Number(r.usuario_id),
  titulo: r.titulo,
  descripcion: r.descripcion ?? '',
  emocion: r.emocion ?? 'Calma',
  emocion_emoji: r.emocion_emoji ?? '💭',
  intensidad: Number(r.intensidad ?? 3),
  compartida_equipo: aBooleano(r.compartida_equipo),
  recordatorio: r.recordatorio == null ? null : Number(r.recordatorio),
  fecha_registro: Number(r.fecha_registro ?? 0),
  fecha_texto: r.fecha_texto ?? '',
});

export const mapaRutina = (r: any) => ({
  id: Number(r.id),
  usuario_id: Number(r.usuario_id),
  title: r.title,
  description: r.description ?? '',
  category: r.category,
  iconKey: r.icon_key ?? 'heart',
  completedDates: r.completed_dates ?? '',
  streak: Number(r.streak ?? 0),
  lastCompletedDate: r.last_completed_date ?? '',
  targetDaysPerWeek: Number(r.target_days_per_week ?? 7),
});

export const mapaRecompensa = (r: any) => ({
  id: Number(r.id),
  titulo: r.titulo,
  descripcion: r.descripcion ?? '',
  puntos_costo: Number(r.puntos_costo ?? 0),
  icono_emoji: r.icono_emoji ?? '🎁',
  categoria: r.categoria ?? 'General',
  tipo_juego: r.tipo_juego ?? '',
  canjeada: aBooleano(r.canjeada),
  fecha_canje: r.fecha_canje == null ? null : Number(r.fecha_canje),
});
