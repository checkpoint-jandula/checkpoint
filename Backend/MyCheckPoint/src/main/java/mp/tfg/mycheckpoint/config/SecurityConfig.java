package mp.tfg.mycheckpoint.config;


import mp.tfg.mycheckpoint.security.jwt.JwtAuthenticationEntryPoint;
import mp.tfg.mycheckpoint.security.jwt.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos existentes
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/public/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/auth/confirm-account").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/juegos/igdb/filtrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/games/{igdbId}/details").permitAll() // Vista detallada de juego
                        .requestMatchers(HttpMethod.GET, "/api/juegos/**").permitAll() // Búsqueda y listado de juegos
                        .requestMatchers(HttpMethod.POST, "/api/juegos/igdb/sincronizar/**").authenticated() // Sincronizar requiere auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/gamelists/public").permitAll() // Ver todas las GameLists públicas
                        .requestMatchers(HttpMethod.GET, "/api/v1/gamelists/{listPublicId}/public").permitAll() // Ver una GameList pública
                        .requestMatchers("/profile-pictures/**").permitAll() // Servir imágenes de perfil

                        // Endpoints de Biblioteca de Juegos del Usuario (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/me/library/games/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me/library/games/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me/library/games/**").authenticated()

                        // Endpoints de Listas de Juegos del Usuario (requieren autenticación)
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/me/gamelists/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me/gamelists/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/me/gamelists/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me/gamelists/**").authenticated()

                        // Endpoints de Amistad (requieren autenticación)
                        .requestMatchers("/api/v1/friends/**").authenticated()

                        // Endpoint de búsqueda de usuarios (requiere autenticación)
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/search").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/me/profile-picture").authenticated()

                        // --- NUEVOS ENDPOINTS PARA TIER LISTS ---
                        // Crear TierList de perfil
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/me/tierlists").authenticated()
                        // Obtener TierLists de perfil del usuario actual
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me/tierlists").authenticated()
                        // Obtener o crear TierList para una GameList (puede ser accedido por el dueño o si la GameList/TierList es pública)
                        .requestMatchers(HttpMethod.GET, "/api/v1/gamelists/{gameListPublicId}/tierlist").permitAll() // Permitir, el servicio valida
                        // Listar todas las TierLists públicas
                        .requestMatchers(HttpMethod.GET, "/api/v1/tierlists/public").permitAll()
                        // Obtener una TierList específica (puede ser accedido si es pública o por el dueño)
                        .requestMatchers(HttpMethod.GET, "/api/v1/tierlists/{tierListPublicId}").permitAll() // Permitir, el servicio valida
                        // Modificar TierList (metadatos, secciones, items) - Requieren autenticación y ser el dueño
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tierlists/{tierListPublicId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tierlists/{tierListPublicId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tierlists/{tierListPublicId}/sections").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tierlists/{tierListPublicId}/sections/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tierlists/{tierListPublicId}/sections/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tierlists/{tierListPublicId}/items/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tierlists/{tierListPublicId}/items/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tierlists/{tierListPublicId}/items/**").authenticated()

                        // Por ahora, el resto requerirá autenticación
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}