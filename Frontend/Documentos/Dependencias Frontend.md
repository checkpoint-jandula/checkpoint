# Guía de Dependencias de Frontend para MyCheckPoint (React)

Este documento describe las dependencias clave que utilizarás en el frontend de tu proyecto MyCheckPoint, explicando su propósito, funcionalidad y aplicación específica en tu desarrollo.

## 1. `react-router-dom`

* **¿Para qué sirve?** Es la biblioteca estándar para la navegación en aplicaciones web de React (Single Page Applications o SPA). Permite cambiar el contenido de la página sin recargarla completamente en el navegador, ofreciendo una experiencia de usuario fluida.
* **¿Qué hace?** Proporciona componentes (`BrowserRouter`, `Routes`, `Route`) para definir las URLs de tu aplicación y asociarlas a componentes React específicos. También incluye componentes de navegación (`Link`, `NavLink`).
* **¿Cómo lo usaré en mi proyecto?**
    * Definir rutas para la autenticación: `login`, `registro`, `recuperación de contraseña`, y `confirmación de cuenta` (coincidiendo con tus endpoints de `/api/v1/auth/**`).
    * Implementar rutas para la gestión del usuario: `perfil del usuario` (`/profile` o `/users/me`), su `biblioteca de juegos` (`/my-library`), y la `gestión de listas de juegos` (`/my-gamelists`).
    * Crear rutas para la interacción con juegos: `búsqueda de juegos` (`/games/search`), `detalles de un juego específico` (`/games/:igdbId/details`), y la `búsqueda de usuarios` (`/users/search`).
    * Configurar rutas para la gestión de Tier Lists (`/tierlists/public`, `/tierlists/:publicId`) y la funcionalidad de `amistades`.

## 2. `axios`

* **¿Para qué sirve?** Es un cliente HTTP basado en promesas que facilita la realización de peticiones a APIs REST desde el navegador.
* **¿Qué hace?** Permite realizar peticiones `GET`, `POST`, `PUT`, `DELETE` (y otras) de forma sencilla, maneja la transformación JSON automáticamente y ofrece características como interceptores y cancelación de peticiones.
* **¿Cómo lo usaré en mi proyecto?**
    * **Autenticación:** Enviar credenciales a `/api/v1/auth/login` y recibir el token JWT.
    * **Peticiones autenticadas:** Configurar un interceptor para adjuntar automáticamente el token JWT (obtenido de Zustand) en el encabezado `Authorization` de todas las peticiones a tus endpoints protegidos (ej. `/api/v1/users/me/**`, `/api/v1/friends/**`, `/api/v1/tierlists/**`).
    * **CRUD de datos:** Realizar todas las operaciones de Crear, Leer, Actualizar y Eliminar para tu biblioteca de juegos, listas de juegos, Tier Lists, gestión de amistades y perfiles de usuario.
    * Manejo centralizado de errores provenientes de tu API (ej. `ErrorResponse`, `ValidationErrorResponse`).

## 3. Material-UI (MUI)

* **¿Para qué sirve?** Es una librería de componentes de interfaz de usuario (UI) que implementa las directrices de diseño de Material Design de Google. Facilita la creación de interfaces de usuario atractivas y responsivas rápidamente.
* **¿Qué hace?** Ofrece una amplia colección de componentes React pre-construidos y estilizados (botones, entradas de texto, tarjetas, barras de navegación, tablas, modales, etc.), permitiéndote concentrarte en la lógica de la aplicación en lugar de en el diseño y CSS.
* **¿Cómo lo usaré en mi proyecto?**
    * **Layout y navegación:** Construir la estructura de la aplicación (`AppBar`, `Drawer`) y organizar el contenido (`Box`, `Container`, `Grid`).
    * **Formularios:** Implementar los formularios de `login`, `registro`, `cambio de contraseña`, `gestión de datos de juegos` y `creación/edición de listas/Tier Lists` utilizando componentes como `TextField`, `Button`, `Select` de MUI.
    * **Visualización de datos:** Mostrar información de `juegos`, `biblioteca`, `listas`, `Tier Lists` y `perfiles` con `Card`, `List`, `Table`, `Typography`, `Avatar`.
    * **Interacción:** Manejar diálogos modales (`Dialog`), indicadores de carga (`CircularProgress`) y notificaciones (`Snackbar`).

## 4. Zustand

* **¿Para qué sirve?** Es una librería de gestión de estado global ligera y sencilla para React. Permite compartir el estado entre componentes de manera eficiente sin la complejidad de otras soluciones.
* **¿Qué hace?** Permite crear "stores" pequeños y dedicados para diferentes partes del estado de tu aplicación. Los componentes pueden "suscribirse" a partes específicas de un store y re-renderizarse solo cuando esas partes cambian.
* **¿Cómo lo usaré en mi proyecto?**
    * **Gestión de autenticación (JWT):** Crear un `authStore` para almacenar el token JWT (`token_acceso`), la `información del usuario logueado` (`UserDTO`), y el `estado de autenticación` (`isAuthenticated`).
    * **Datos del usuario logueado:** Almacenar la `UserDTO` del usuario autenticado para que sus datos (nombre de usuario, foto de perfil) sean accesibles globalmente.
    * **Estado global de UI:** Gestionar preferencias de interfaz, como el tema (claro/oscuro).

## 5. `formik`

* **¿Para qué sirve?** Simplifica la construcción de formularios en React, encargándose de la gestión del estado del formulario, la validación y los errores.
* **¿Qué hace?** Proporciona un `hook` (`useFormik`) que ofrece acceso a un objeto con propiedades y métodos (`values`, `errors`, `handleChange`, `handleSubmit`) para conectar los campos del formulario a su lógica.
* **¿Cómo lo usaré en mi proyecto?**
    * Crear los formularios de `login`, `registro`, `cambio de contraseña` y `restablecimiento de contraseña`.
    * Construir formularios para `añadir o actualizar juegos en la biblioteca del usuario`.
    * Desarrollar los formularios para `crear y editar listas de juegos`.
    * Implementar formularios para la `creación y actualización de Tier Lists y sus secciones`.
    * Simplificar la lógica para mostrar mensajes de error de validación provenientes de tu backend (`ValidationErrorResponse`).

## 6. `yup`

* **¿Para qué sirve?** Es una librería de validación de esquemas JavaScript. Permite definir de forma declarativa las reglas de validación para objetos (como los datos de un formulario). Se integra de forma excelente con `formik`.
* **¿Qué hace?** Permite crear esquemas que describen la forma y las reglas de validación de un objeto. Por ejemplo, puedes definir que un campo `email` debe ser un email válido y no estar vacío, o que una `contraseña` debe tener una longitud mínima y máxima.
* **¿Cómo lo usaré en mi proyecto?**
    * Definir esquemas de validación para los formularios de `login`, `registro`, `cambio de contraseña` y `restablecimiento de contraseña`, asegurando que los datos cumplan con los requisitos de tu backend antes de enviarlos.
    * Validar los datos para `añadir/actualizar juegos en la biblioteca` (ej. puntuación entre 0 y 10).
    * Validar los `nombres y descripciones de listas de juegos y Tier Lists`.
    * Mostrar mensajes de error de validación en tiempo real al usuario mientras rellena el formulario.

## 7. `react-query` (o TanStack Query)

* **¿Para qué sirve?** Es una librería de "data-fetching" (obtención de datos) y gestión de estado asíncrono para React. Simplifica la interacción con APIs y maneja aspectos complejos como el almacenamiento en caché, la sincronización, la revalidación y la gestión de errores de forma declarada.
* **¿Qué hace?** Proporciona hooks como `useQuery` para consultas (`GET`) y `useMutation` para mutaciones (`POST`, `PUT`, `DELETE`). Se encarga de manejar los estados de carga, éxito y error de tus peticiones; almacenar en caché las respuestas de la API; revalidar automáticamente los datos en segundo plano; y ofrecer "optimistic updates".
* **¿Cómo lo usaré en mi proyecto?**
    * **Obtención de datos:** Utilizar `useQuery` para obtener la `biblioteca de juegos del usuario`, `listas de juegos`, `detalles de juegos`, `Tier Lists`, `listas de amigos y solicitudes`, y `resultados de búsqueda de IGDB/usuarios`.
    * **Modificación de datos:** Emplear `useMutation` para `añadir/actualizar juegos en la biblioteca`, `crear/actualizar/eliminar listas de juegos`, `enviar/aceptar/rechazar solicitudes de amistad`, `gestionar Tier Lists y sus elementos`, y `actualizar el perfil del usuario/cambiar contraseña`.
    * **Optimización:** Beneficiarme de la caché para una aplicación más rápida y una menor carga en el backend.
    * **Manejo de errores:** Mostrar mensajes de error relevantes a los usuarios de forma consistente (ej. si una petición falla).

## 8. `mui/icons-material`

* **¿Para qué sirve?** Es la biblioteca oficial de iconos de Material Design para ser utilizada con Material-UI (MUI) y React. Proporciona una amplia gama de iconos listos para usar que ayudan a mejorar la usabilidad y el atractivo visual de la interfaz de usuario.
* **¿Qué hace?** Ofrece cada icono de Material Design como un componente React individual (SVG envuelto). Esto permite importar solo los iconos que necesitas, optimizando el rendimiento. Los iconos son personalizables en términos de tamaño, color y otras propiedades CSS a través de `props`.
* **¿Cómo lo usaré en mi proyecto?**
  * **Mejorar la interfaz de usuario:** Integrar iconos en componentes de MUI como `Button` (ej. icono de guardar, enviar), `IconButton`, `List` (ej. iconos para acciones en elementos de lista), `TextField` (ej. iconos para mostrar/ocultar contraseña, limpiar campo), y `AppBar` (ej. icono de menú, perfil).
  * **Indicadores visuales:** Utilizar iconos para representar acciones comunes en la gestión de `juegos` (ej. añadir a biblioteca, marcar como jugado), `listas de juegos` (ej. editar lista, eliminar lista), `Tier Lists` (ej. mover elemento, añadir sección), y `amistades` (ej. enviar solicitud, aceptar/rechazar).
  * **Navegación y feedback:** Emplear iconos en la `navegación` (ej. iconos para diferentes secciones en un `Drawer` o `BottomNavigation`) y para dar `feedback visual` al usuario (ej. iconos de éxito, error o advertencia en `Snackbar` o `Dialog`).
  * **Personalización del perfil:** Utilizar iconos para opciones dentro del `perfil del usuario` o para distinguir tipos de notificaciones.