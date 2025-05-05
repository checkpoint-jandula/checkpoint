package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.KeywordDTO;
import mp.tfg.mycheckpoint.entity.Keyword;

@Mapper(componentModel = "spring")
public interface KeywordMapper {

    /**
     * Convierte una entidad {@link Keyword} a su DTO {@link KeywordDTO}.
     * @param keyword La entidad Keyword a convertir.
     * @return El DTO KeywordDTO resultante.
     */
    KeywordDTO toDTO(Keyword keyword);

    // No se necesita toEntity ni updateEntityFromDTO ya que estos datos se gestionan al importar.
}