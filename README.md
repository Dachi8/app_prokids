# App de ProKids


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
