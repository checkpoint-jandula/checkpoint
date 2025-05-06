package mp.tfg.mycheckpoint.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mp.tfg.mycheckpoint.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Interfaz de Spring

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
// import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails { // Nombre estándar en inglés
    private static final long serialVersionUID = 1L;

    private Long id;
    private UUID publicId;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(Long id, UUID publicId, String username, String email, String password,
                            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.publicId = publicId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Usuario usuario) {
        List<GrantedAuthority> authorities = Collections.emptyList();
        /* Ejemplo con roles:
        List<GrantedAuthority> authorities = usuario.getRoles().stream()
              .map(rol -> new SimpleGrantedAuthority(rol.getNombre().name()))
              .collect(Collectors.toList());
        */

        return new UserDetailsImpl(
                usuario.getId(),
                usuario.getPublicId(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.getContrasena(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return username; }
    public Long getId() { return id; }
    public UUID getPublicId() { return publicId; }
    public String getEmail() { return email; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; /* TODO: Adaptar con soft delete */ }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}