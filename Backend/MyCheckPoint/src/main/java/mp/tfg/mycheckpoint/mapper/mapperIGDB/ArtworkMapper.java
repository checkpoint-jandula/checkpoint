package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.ArtworkDTO;
import mp.tfg.mycheckpoint.entity.Artwork;

@Mapper(componentModel = "spring")
public interface ArtworkMapper {

    /**
     * Convierte una entidad {@link Artwork} a su DTO {@link ArtworkDTO}.
     * @param artwork La entidad Artwork a convertir.
     * @return El DTO ArtworkDTO resultante.
     */
    ArtworkDTO toDTO(Artwork artwork);

    // No se necesita toEntity ni updateEntityFromDTO.
}