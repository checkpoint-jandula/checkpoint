// src/schemas/loginSchema.js
import * as yup from 'yup';

export const loginSchema = yup.object({
  identificador: yup.string()
    .required('El identificador (email o nombre de usuario) es obligatorio.'),
  contraseña: yup.string()
    .required('La contraseña es obligatoria.'),
});