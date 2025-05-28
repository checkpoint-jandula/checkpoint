package mp.tfg.mycheckpoint.security;

import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true) // Es una operación de lectura
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Intentamos buscar por email primero, luego por nombre de usuario
        // Esto es porque nuestro LoginRequestDTO tomará un "identificador"
        User user = userRepository.findByEmail(usernameOrEmail)
                .orElseGet(() -> userRepository.findByNombreUsuario(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con identificador: " + usernameOrEmail)));

        return UserDetailsImpl.build(user);
    }
}