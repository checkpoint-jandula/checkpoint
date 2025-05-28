// src/pages/HomePage.jsx
import React from 'react';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import { Link as RouterLink } from 'react-router-dom'; // Para enlaces internos

const HomePage = () => {
  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom sx={{ mt: 4 }}>
        Bienvenido a MyCheckPoint
      </Typography>
      <Typography variant="body1" paragraph>
        Esta es la página de inicio de tu aplicación.
      </Typography>
      <Typography variant="body1">
        Puedes navegar a la sección de{' '}
        <RouterLink to="/registro">Registro</RouterLink> o{' '}
        <RouterLink to="/login">Login</RouterLink>.
      </Typography>
      {/* Aquí podrías añadir más contenido o enlaces a otras secciones cuando las desarrolles */}
    </Container>
  );
};

export default HomePage;