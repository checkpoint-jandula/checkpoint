// src/App.jsx
import React from 'react';
import { BrowserRouter, Routes, Route, Link, useNavigate } from 'react-router-dom'; // Importa useNavigate
import HomePage from './pages/HomePage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore } from './store/authStore'; // Importa tu store de Zustand
import Button from '@mui/material/Button'; // Para el botón de logout

const queryClient = new QueryClient();

function AuthNavLinks() {
  const navigate = useNavigate();
  const { isAuthenticated, clearAuthData, user } = useAuthStore(); // Obtén el estado y la acción

  const handleLogout = () => {
    clearAuthData();
    navigate('/login'); // Redirige a login después de cerrar sesión
  };

  return (
    <nav style={{ padding: '1rem', backgroundColor: '#f0f0f0', marginBottom: '1rem', textAlign: 'center', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem' }}>
      <Link to="/" style={{ textDecoration: 'none' }}>Home</Link>
      {isAuthenticated ? (
        <>
          {user && ( // Muestra el nombre de usuario si está disponible
            <span style={{ fontStyle: 'italic' }}>Hola, {user.nombre_usuario}</span>
          )}
          <Link to="/profile" style={{ textDecoration: 'none' }}>Perfil</Link>
          <Button variant="outlined" size="small" onClick={handleLogout}>
            Cerrar Sesión
          </Button>
        </>
      ) : (
        <>
          <Link to="/login" style={{ textDecoration: 'none' }}>Login</Link>
          <Link to="/registro" style={{ textDecoration: 'none' }}>Registro</Link>
        </>
      )}
    </nav>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthNavLinks /> {/* Usa el nuevo componente para la barra de navegación */}
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/registro" element={<RegisterPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;