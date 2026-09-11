import { initializeApp, cert } from 'firebase-admin/app';
import { getMessaging } from 'firebase-admin/messaging';
import { readFileSync } from 'fs';
import path from 'path';

const credencialesPath = path.join(__dirname, 'firebase-credentials.json');
const credenciales = JSON.parse(readFileSync(credencialesPath, 'utf-8'));

const app = initializeApp({
  credential: cert(credenciales),
});

export const mensajeria = getMessaging(app);