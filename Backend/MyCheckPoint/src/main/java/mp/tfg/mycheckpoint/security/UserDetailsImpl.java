package mp.tfg.mycheckpoint.security;

import mp.tfg.mycheckpoint.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections; // Para una lista simple de autoridades
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final UUID publicId;
    private final String username; // Usaremos email o nombre_usuario como username para Spring Security
    private final String email;
    private final String password; // Contraseña HASHEADA
    private final Collection<? extends GrantedAuthority> authorities;
    // Puedes añadir más campos de User si los necesitas en el objeto Principal

    public UserDetailsImpl(Long id, UUID publicId, String usernameForSecurity, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.publicId = publicId;
        this.username = usernameForSecurity; // Este será el identificador principal para Spring Security
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        // Por ahora, todos los usuarios tienen un rol simple "ROLE_USER".
        // Más adelante podrías obtener roles desde la entidad User si los implementas.
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // Decidimos usar el email como el "username" principal para UserDetails,
        // pero también podríamos usar user.getNombreUsuario(). Es importante ser consistente.
        return new UserDetailsImpl(
                user.getId(),
                user.getPublicId(),
                user.getEmail(), // Usamos email como el 'username' para UserDetails
                user.getEmail(),
                user.getContraseña(), // La contraseña ya debe estar hasheada aquí
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // Este es el "username" que Spring Security usa para identificar al usuario
        // Puede ser el email, nombre_usuario, etc. Lo establecimos como email en el constructor/build.
        return username;
    }

    // Los siguientes métodos definen el estado de la cuenta.
    // Por ahora, los dejamos como true. Podrías mapearlos a campos en tu entidad User
    // si necesitas manejar cuentas bloqueadas, expiradas, etc.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Podrías tener un campo 'is_locked' en User
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // Podrías tener un campo 'is_enabled' o usar 'fecha_eliminacion' en User
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}