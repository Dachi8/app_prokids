# ProKids - Backend

Sistema para la gestión de tareas y asistencia organizacional para personas neurodivergentes.

## Requisitos
- Node.js 20+
- MySQL 8.0+

## Instalación

1. Clona el repositorio
2. Instala dependencias:
   \`\`\`
   npm install
   \`\`\`
3. Copia `.env.example` a `.env` y completa tus credenciales de MySQL:
   \`\`\`
   copy .env.example .env
   \`\`\`
4. Crea la base de datos ejecutando el script:
   \`\`\`
   mysql -u root -p < database/schema.sql
   \`\`\`
5. Corre el servidor en modo desarrollo:
   \`\`\`
   npm run dev
   \`\`\`
6. Verifica que funcione entrando a `http://localhost:4000/api/health`

## Endpoints disponibles

### Autenticación
- `POST /api/auth/registro` — Body: `{ nombre, correo, password, fecha_nacimiento? }`
- `POST /api/auth/login` — Body: `{ correo, password }` — Devuelve `{ token, usuario }`
- `GET /api/auth/perfil` — Requiere header `Authorization: Bearer <token>`

### Tareas
- `POST /api/tareas` — Requiere header `Authorization: Bearer <token>` + rol Administrador — Body: `{ que_hacer, descripcion?, frecuencia, hora_limite? }`
- `POST /api/tareas/asignar` — Requiere header `Authorization: Bearer <token>` + rol Administrador — Body: `{ tarea_id, usuario_id }`
- `GET /api/tareas/mis-tareas` — Requiere header `Authorization: Bearer <token>` — Devuelve `{ tareas: [...] }`
- `PATCH /api/tareas/completar` — Requiere header `Authorization: Bearer <token>` — Body: `{ ejecucion_id }`

### Dispositivos push
- `POST /api/dispositivos` — Requiere header `Authorization: Bearer <token>` — Body: `{ fcm_token, plataforma }`

### Utilidad / diagnóstico
- `GET /api/health` — Verifica que el servidor esté corriendo
- `GET /api/db-test` — Verifica la conexión con la base de datos


