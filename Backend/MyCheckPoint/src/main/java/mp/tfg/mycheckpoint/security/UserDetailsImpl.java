package mp.tfg.mycheckpoint.security;

import mp.tfg.mycheckpoint.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
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
    private final boolean emailVerified; // <-- NUEVO CAMPO
    private final Collection<? extends GrantedAuthority> authorities;
    private final OffsetDateTime fechaEliminacion;

    public UserDetailsImpl(Long id, UUID publicId, String usernameForSecurity, String email, String password,
                           boolean emailVerified, // <-- AÑADIR AL CONSTRUCTOR
                           OffsetDateTime fechaEliminacion,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.publicId = publicId;
        this.username = usernameForSecurity;
        this.email = email;
        this.password = password;
        this.emailVerified = emailVerified; // <-- ASIGNAR
        this.authorities = authorities;
        this.fechaEliminacion = fechaEliminacion;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserDetailsImpl(
                user.getId(),
                user.getPublicId(),
                user.getEmail(),
                user.getEmail(),
                user.getContraseña(),
                user.isEmailVerified(),
                user.getFechaEliminacion(),// <-- PASAR EL VALOR
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

    // Getter para emailVerified si lo necesitas fuera
    public boolean isEmailVerified() {
        return emailVerified;
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
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Aquí podrías añadir lógica si implementas bloqueo de cuentas
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // La cuenta está habilitada si el email está verificado.
        // Si fechaEliminacion tiene un valor futuro, el login debe ser posible para cancelarla.
        // Si fechaEliminacion ya pasó, la tarea programada debería haber eliminado al usuario.
        // Si, por alguna razón, la tarea no ha corrido y la fecha ya pasó, este método
        // correctamente devolvería false, impidiendo el login.
        if (!this.emailVerified) {
            return false; // Si el email no está verificado, la cuenta no está habilitada.
        }
        // Si el email está verificado, la cuenta está habilitada a menos que la fecha de eliminación haya pasado.
        return this.fechaEliminacion == null || this.fechaEliminacion.isAfter(OffsetDateTime.now());
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