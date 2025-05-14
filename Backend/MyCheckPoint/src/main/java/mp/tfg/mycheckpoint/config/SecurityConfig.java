package mp.tfg.mycheckpoint.config;

// Quitar UserDetailsServiceImpl de aquí si ya no se usa directamente
// import mp.tfg.mycheckpoint.security.UserDetailsServiceImpl;
import mp.tfg.mycheckpoint.security.jwt.JwtAuthenticationEntryPoint; // Importar
import mp.tfg.mycheckpoint.security.jwt.JwtAuthenticationFilter;   // Importar

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Importar

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // Inyectar el EntryPoint y el Filtro
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
                // Configurar el manejo de excepciones de autenticación para usar nuestro EntryPoint
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/public/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        // Endpoints de Juegos
                        .requestMatchers(HttpMethod.GET, "/api/juegos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/juegos/igdb/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/juegos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/juegos/igdb/**").permitAll()


                        // Por ahora, el resto requerirá autenticación
                        .anyRequest().authenticated()
                );

        // Añadir nuestro filtro JWT personalizado ANTES del filtro UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}