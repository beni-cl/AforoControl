# AforoControl IoT y mockups al final

App Android (Kotlin + Jetpack Compose + Firebase) para monitorear y controlar el aforo de una sala técnica.
Evaluación 2 de TI3042 – Aplicaciones Móviles para IoT. En esta etapa el nodo IoT (Arduino UNO R4 WiFi) está **simulado** en Firestore; en la EV3 se reemplaza por el Arduino real.

## Funcionalidades

- Login con correo y contraseña (Firebase Authentication)
- Roles: **Administrador** (monitorea y controla) y **Guardia** (solo monitorea)
- Panel en tiempo real: aforo, semáforo (verde/amarillo/rojo), barrera y alerta de golpe
- Comandos del Admin: forzar/quitar cierre, resetear contador, cambiar aforo máximo, reconocer alerta
- Simulación de sensores: entrada, salida y golpe en la puerta
- Historial de eventos con fecha, hora y usuario
- Dos o más celulares sincronizados por WiFi a través de Cloud Firestore

## Cómo ponerla en marcha

### 1. Firebase (una sola vez)
1. Crear el proyecto en https://console.firebase.google.com
2. Agregar app Android con el paquete **`cl.inacap.aforocontrol`** y descargar `google-services.json`.
3. **Authentication** → Comenzar → activar *Correo electrónico/contraseña* → pestaña *Users* → crear:
   - `admin@aforo.cl`
   - `guardia@aforo.cl`
4. **Firestore Database** → Crear base de datos → ubicación `southamerica-east1` → modo de prueba.
5. En Firestore, crear la colección **`roles`** con dos documentos (el ID del documento es el correo, en minúsculas):

   | ID del documento | Campo | Tipo | Valor |
   |---|---|---|---|
   | `admin@aforo.cl` | `rol` | string | `admin` |
   | `guardia@aforo.cl` | `rol` | string | `guardia` |

6. Firestore → pestaña **Reglas** → pegar el contenido de `firestore.rules` → **Publicar**.

### 2. Android Studio
1. Copiar `google-services.json` dentro de la carpeta **`app/`** del proyecto.
2. Abrir la carpeta `AforoControl` con Android Studio (*File → Open*) y esperar el *Gradle sync*.
3. Conectar el celular (depuración USB activada) y presionar ▶ Run.

### 3. Probar con dos celulares
Instalar en dos celulares (o celular + emulador). Entrar como Admin en uno y como Guardia en el otro. Al tocar "Entrada" en cualquiera, el aforo cambia en ambos.

## Seguridad (ISO/IEC 27400)

- Contraseñas gestionadas y cifradas por Firebase Authentication; nunca se guardan en el celular.
- Toda la comunicación con Firebase va cifrada por HTTPS/TLS.
- Control de acceso por rol en la app **y** en las reglas de Firestore (el Guardia no puede enviar comandos aunque modifique la app).
- El historial no se puede editar ni borrar (trazabilidad).
- Mensaje de error genérico en el login; `allowBackup=false`; solo permisos de red.

Mockups

- <img width="428" height="975" alt="image" src="https://github.com/user-attachments/assets/c12e9636-7e22-47a2-891b-863388d9b1ee" />
<img width="428" height="975" alt="image" src="https://github.com/user-attachments/assets/e9c3ea95-3fa3-49fb-8c13-816782b84f68" />
<img width="428" height="975" alt="image" src="https://github.com/user-attachments/assets/38d6528c-5075-4ca5-aa72-7d254d111157" />
<img width="428" height="975" alt="image" src="https://github.com/user-attachments/assets/612278f9-c2cc-4dce-b28d-26cc87383309" />

