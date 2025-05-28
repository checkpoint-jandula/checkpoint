// src/pages/RegisterPage.js
import React from 'react';
import { useFormik } from 'formik';
import { useNavigate } from 'react-router-dom';
import { registerSchema } from '../schemas/registerSchema';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import { Link as RouterLink } from 'react-router-dom'; // Para el enlace a login

import { useRegisterUser } from '../hooks/useRegisterUser'; // ¡Importa el hook!

const RegisterPage = () => {
  const navigate = useNavigate();
  const registerMutation = useRegisterUser(); // ¡Usa el hook!

  const formik = useFormik({
    initialValues: {
      nombre_usuario: '',
      email: '',
      contraseña: '',
    },
    validationSchema: registerSchema,
    onSubmit: (values, { setSubmitting }) => { // Formik te da setSubmitting
      registerMutation.mutate(values, {
        onSuccess: (data) => {
          // 'data' es UserDTO
          console.log('Registro exitoso:', data);
          // No es necesario llamar a setSubmitting(false) aquí si la mutación
          // ya te da 'isLoading' para deshabilitar el botón.
          // El Alert de isSuccess se mostrará automáticamente.
          // Podrías querer redirigir después de un tiempo o añadir un botón para ir a login.
          // Ejemplo: setTimeout(() => navigate('/login'), 3000);
        },
        onError: (error) => {
          console.error('Error en onSubmit RegisterPage:', error);
          // El Alert de isError se mostrará automáticamente.
          // No es necesario llamar a setSubmitting(false) aquí.
          // Si quieres mostrar errores específicos de campos devueltos por el backend
          // (ej. si el email ya existe y tu API devuelve un ValidationErrorResponse con detalles),
          // necesitarías una lógica más avanzada aquí para usar formik.setErrors().
          // Por ahora, el Alert general es suficiente.
        }
      });
    },
  });

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Typography component="h1" variant="h5">
          Crear Cuenta
        </Typography>

        {/* Mensaje de éxito global después del envío */}
        {registerMutation.isSuccess && (
           <Alert severity="success" sx={{ mt: 2, width: '100%' }}>
              ¡Registro exitoso! Revisa tu email para confirmar tu cuenta (si aplica en tu backend).
              <Button component={RouterLink} to="/login" sx={{ ml: 2 }} variant="outlined" size="small">
                Ir a Login
              </Button>
           </Alert>
        )}

        {/* Mensaje de error global después del envío */}
        {registerMutation.isError && (
          <Alert severity="error" sx={{ mt: 2, mb: formik.isSubmitting ? 0 : 2, width: '100%' }}>
            {registerMutation.error?.response?.data?.message ||
             (registerMutation.error?.response?.data?.errors && registerMutation.error.response.data.errors.join('. ')) || // Tu YAML usa un array de strings para errors en ValidationErrorResponse
             (registerMutation.error?.response?.data?.error) || // Para ErrorResponse genérico
             'Error al registrar. Inténtalo de nuevo.'}
          </Alert>
        )}

        {/* Ocultar formulario si el registro fue exitoso para no reenviar */}
        {!registerMutation.isSuccess && (
          <Box component="form" onSubmit={formik.handleSubmit} sx={{ mt: 1, width: '100%' }}>
            <TextField
              fullWidth
              id="nombre_usuario"
              name="nombre_usuario"
              label="Nombre de Usuario"
              value={formik.values.nombre_usuario}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.nombre_usuario && Boolean(formik.errors.nombre_usuario)}
              helperText={formik.touched.nombre_usuario && formik.errors.nombre_usuario}
              margin="normal"
              disabled={registerMutation.isLoading || registerMutation.isSuccess}
            />
            <TextField
              fullWidth
              id="email"
              name="email"
              label="Correo Electrónico"
              type="email"
              value={formik.values.email}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.email && Boolean(formik.errors.email)}
              helperText={formik.touched.email && formik.errors.email}
              margin="normal"
              disabled={registerMutation.isLoading || registerMutation.isSuccess}
            />
            <TextField
              fullWidth
              id="contraseña"
              name="contraseña"
              label="Contraseña"
              type="password"
              value={formik.values.contraseña}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
              error={formik.touched.contraseña && Boolean(formik.errors.contraseña)}
              helperText={formik.touched.contraseña && formik.errors.contraseña}
              margin="normal"
              disabled={registerMutation.isLoading || registerMutation.isSuccess}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={registerMutation.isLoading || registerMutation.isSuccess}
            >
              {registerMutation.isLoading ? <CircularProgress size={24} /> : 'Crear Cuenta'}
            </Button>
            <Box textAlign="center">
                <Typography variant="body2">
                    ¿Ya tienes una cuenta?{' '}
                    <RouterLink to="/login" style={{ textDecoration: 'none' }}>
                        Inicia sesión
                    </RouterLink>
                </Typography>
            </Box>
          </Box>
        )}
      </Box>
    </Container>
  );
};

export default RegisterPage;