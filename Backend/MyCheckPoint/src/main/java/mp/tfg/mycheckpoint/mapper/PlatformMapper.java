package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.platform.PlatformDTO;
import mp.tfg.mycheckpoint.entity.Platform;
import org.mapstruct.Mapper;

import java.util.List; // Para mapear listas

@Mapper(componentModel = "spring")
public interface PlatformMapper {
    PlatformDTO toDto(Platform platform);
    Platform toEntity(PlatformDTO platformDTO); // Útil si recibes DTO para actualizar

    List<PlatformDTO> toDtoList(List<Platform> platforms); // Para devolver listas
}