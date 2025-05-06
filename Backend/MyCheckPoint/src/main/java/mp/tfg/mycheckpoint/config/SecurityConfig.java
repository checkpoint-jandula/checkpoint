package mp.tfg.mycheckpoint.config;

// Asegúrate de que las clases importadas coincidan con los nuevos nombres en español
import mp.tfg.mycheckpoint.security.jwt.FiltroAutenticacionJwt;
import mp.tfg.mycheckpoint.security.service.ServicioDetallesUsuarioImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity // Habilita la seguridad web de Spring
public class ConfiguracionSeguridad { // Nombre en español

    // Inyecta los componentes que necesitaremos (con nombres en español)
    @Autowired
    private ServicioDetallesUsuarioImpl servicioDetallesUsuario; // Nombre en español

    @Autowired
    private FiltroAutenticacionJwt filtroAutenticacionJwt; // Nombre en español

    // Define el Bean para codificar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Define el Bean del AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Configura la cadena de filtros de seguridad HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs stateless (JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No crear sesiones HTTP
                .authorizeHttpRequests(authz -> authz
                        // Permitir acceso público a endpoints de autenticación/registro
                        .requestMatchers("/api/v1/auth/**").permitAll() // Ajusta el path según tu ControladorAuth
                        .requestMatchers("/api/v1/usuarios/registrar").permitAll() // Permitir registro (si usas este endpoint específico)
                        // Permitir acceso público a OpenAPI/Swagger UI (si lo usas)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Requerir autenticación para cualquier otra solicitud
                        .anyRequest().authenticated()
                )
                // Añadir nuestro filtro JWT (con nombre en español) antes del filtro estándar
                .addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}