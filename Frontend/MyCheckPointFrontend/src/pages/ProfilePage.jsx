// src/pages/ProfilePage.jsx
import React from 'react';
import { useAuthStore } from '../store/authStore'; // Para obtener datos del usuario
import { useNavigate } from 'react-router-dom';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';

// Placeholder para un futuro hook que cargue datos del perfil si no están en Zustand
// import { useUserProfile } from '../hooks/useUserProfile'; // Si decides cargar datos aquí también

const ProfilePage = () => {
  const navigate = useNavigate();
  const { user, clearAuthData, isAuthenticated } = useAuthStore();

  // const { data: profileData, isLoading, isError } = useUserProfile(); // Si cargas con react-query aquí

  React.useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login'); // Si no está autenticado, redirigir a login
    }
  }, [isAuthenticated, navigate]);

  const handleLogout = () => {
    clearAuthData();
    navigate('/login'); // Redirigir a login después de cerrar sesión
  };

  if (!user && isAuthenticated) {
    // Esto puede pasar si el token está, pero los datos del user no se cargaron post-login
    // O si el user se suscribe antes de que setAuthData complete todo en el hook useLoginUser
    return (
        <Container>
            <Typography sx={{mt: 4}}>Cargando datos del perfil...</Typography>
        </Container>
    );
  }

  if (!isAuthenticated) { // Redirige si no está autenticado (el useEffect también lo hace)
    return null; 
  }


  return (
    <Container sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Perfil de Usuario
      </Typography>
      {user ? (
        <Box>
          <Typography variant="h6">Nombre de Usuario: {user.nombre_usuario}</Typography>
          <Typography variant="body1">Email: {user.email}</Typography>
          <Typography variant="body1">Tema Preferido: {user.tema}</Typography>
          <Typography variant="body1">Visibilidad: {user.visibilidad_perfil}</Typography>
          {/* Muestra más datos del UserDTO si quieres */}
          <Button variant="contained" color="secondary" onClick={handleLogout} sx={{ mt: 3 }}>
            Cerrar Sesión
          </Button>
        </Box>
      ) : (
        <Typography>No se pudieron cargar los datos del usuario.</Typography>
      )}
    </Container>
  );
};

export default ProfilePage;