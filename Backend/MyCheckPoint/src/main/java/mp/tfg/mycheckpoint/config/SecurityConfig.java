package mp.tfg.mycheckpoint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Importar HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Para csrf más moderno
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Aunque aún no lo usemos activamente para login en Módulo 1,
        // es bueno tenerlo definido para cuando UserServiceImpl lo necesite para hashear.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF para APIs REST stateless (forma moderna)
                .csrf(AbstractHttpConfigurer::disable)
                // Configurar la gestión de sesiones como STATELESS (no se crean sesiones HTTP)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configurar la autorización de peticiones HTTP
                .authorizeHttpRequests(authz -> authz
                        // Permitir explícitamente el POST a /api/v1/usuarios para el registro
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").permitAll()
                        // Permitir GET a /api/v1/usuarios/** para obtener usuarios (si quieres que sea público al inicio)
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**").permitAll()
                        // Permitir acceso a la documentación de OpenAPI/Swagger
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated()
                );
        // En módulos posteriores, aquí se añadirá el filtro JWT:
        // .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}