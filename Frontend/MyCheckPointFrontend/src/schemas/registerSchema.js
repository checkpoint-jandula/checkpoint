// src/schemas/registerSchema.js
import * as yup from 'yup';

export const registerSchema = yup.object({
  nombre_usuario: yup.string()
    .min(3, 'El nombre de usuario debe tener al menos 3 caracteres.')
    .max(100, 'El nombre de usuario no puede exceder los 100 caracteres.')
    .required('El nombre de usuario es obligatorio.'),
  email: yup.string()
    .email('Debe ser un formato de email válido.')
    .max(255, 'El email no puede exceder los 255 caracteres.')
    .required('El email es obligatorio.'),
  contraseña: yup.string()
    .min(8, 'La contraseña debe tener al menos 8 caracteres.')
    .max(100, 'La contraseña no puede exceder los 100 caracteres.')
    // Podrías añadir más validaciones aquí si quieres, por ejemplo, usando .matches() para una regex
    .required('La contraseña es obligatoria.'),
  // Si tuvieras un campo "confirmar contraseña", lo añadirías aquí con una validación .oneOf([yup.ref('contraseña'), null], 'Las contraseñas deben coincidir')
});