package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.WebDTO;
import mp.tfg.mycheckpoint.entity.Web;

@Mapper(componentModel = "spring")
public interface WebMapper {

    /**
     * Convierte una entidad {@link Web} a su DTO {@link WebDTO}.
     * @param web La entidad Web a convertir.
     * @return El DTO WebDTO resultante.
     */
    WebDTO toDTO(Web web);

    // No se necesita toEntity ni updateEntityFromDTO.
}