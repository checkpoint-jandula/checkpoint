// src/pages/LoginPage.jsx
import React, { useEffect, useState } from 'react';
import { useFormik } from 'formik'; // <--- ¡ASEGÚRATE DE QUE ESTA LÍNEA ESTÉ PRESENTE!
import { useLocation, useNavigate, Link as RouterLink } from 'react-router-dom';
import { loginSchema } from '../schemas/loginSchema';
import { useLoginUser } from '../hooks/useLoginUser';
// ...el resto de tus importaciones de MUI (TextField, Button, etc.)
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const loginMutation = useLoginUser();
  const [feedbackMessage, setFeedbackMessage] = useState(null);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('verification_success') === 'true') {
      setFeedbackMessage({
        type: 'success',
        text: '¡Correo verificado exitosamente! Ahora puedes iniciar sesión.',
      });
      navigate('/login', { replace: true });
    } else if (params.get('verification_error')) {
      const errorCode = params.get('verification_error');
      let errorMessage = 'Error en la verificación del correo.';
      if (errorCode === 'invalid_token') {
        errorMessage = 'El enlace de verificación es inválido o ha expirado.';
      } else if (errorCode === 'token_not_found') {
        errorMessage = 'El token de verificación no fue encontrado.';
      } else if (errorCode === 'server_error') {
        errorMessage = 'Ocurrió un error en el servidor al verificar el correo. Inténtalo más tarde.';
      }
      setFeedbackMessage({ type: 'error', text: errorMessage });
      navigate('/login', { replace: true });
    }
  }, [location, navigate]);

const formik = useFormik({
    initialValues: {
      identificador: '',
      contraseña: '',
    },
    validationSchema: loginSchema,
    onSubmit: (values) => {
      setFeedbackMessage(null);
      loginMutation.mutate(values, {
        onSuccess: (data) => {
          console.log('Login exitoso, datos recibidos en componente:', data);
          // El estado de Zustand ya se actualizó en el onSuccess del hook useLoginUser
          navigate('/'); // <--- CAMBIO AQUÍ: Redirige a la HomePage
        },
        onError: (error) => {
          console.error('Error en onSubmit LoginPage:', error.response?.data || error.message);
        }
      });
    },
  });

  // ... el resto de tu JSX para el return ...
  return (
    <Container component="main" maxWidth="xs">
      <Box sx={{ marginTop: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Typography component="h1" variant="h5">
          Iniciar Sesión
        </Typography>

        {feedbackMessage && (
          <Alert severity={feedbackMessage.type} sx={{ mt: 2, width: '100%' }}>
            {feedbackMessage.text}
          </Alert>
        )}

        {loginMutation.isError && !feedbackMessage && (
          <Alert severity="error" sx={{ mt: 2, width: '100%' }}>
            {loginMutation.error?.response?.data?.message ||
             loginMutation.error?.response?.data?.error ||
             (loginMutation.error?.response?.data?.errors && loginMutation.error.response.data.errors.join('. ')) ||
             loginMutation.error?.message ||
             'Error al iniciar sesión. Verifica tus credenciales.'}
          </Alert>
        )}

        <Box component="form" onSubmit={formik.handleSubmit} sx={{ mt: 1, width: '100%' }}>
          <TextField
            fullWidth
            id="identificador"
            name="identificador"
            label="Identificador (Email o Usuario)"
            value={formik.values.identificador}
            onChange={formik.handleChange}
            onBlur={formik.handleBlur}
            error={formik.touched.identificador && Boolean(formik.errors.identificador)}
            helperText={formik.touched.identificador && formik.errors.identificador}
            margin="normal"
            disabled={loginMutation.isLoading}
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
            disabled={loginMutation.isLoading}
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
            disabled={loginMutation.isLoading}
          >
            {loginMutation.isLoading ? <CircularProgress size={24} /> : 'Iniciar Sesión'}
          </Button>
          <Box textAlign="center">
              <Typography variant="body2">
                  ¿No tienes una cuenta?{' '}
                  <RouterLink to="/registro" style={{ textDecoration: 'none' }}>
                      Regístrate
                  </RouterLink>
              </Typography>
          </Box>
        </Box>
      </Box>
    </Container>
  );
};

export default LoginPage;