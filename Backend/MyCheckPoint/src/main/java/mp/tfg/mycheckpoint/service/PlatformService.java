package mp.tfg.mycheckpoint.service;
import mp.tfg.mycheckpoint.dto.platform.PlatformDTO;
import java.util.List;
import java.util.Optional;

public interface PlatformService {
    List<PlatformDTO> getAllPlatforms();
    Optional<PlatformDTO> getPlatformById(Long id);
    // Métodos para crear/actualizar si son necesarios para admin
}