package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // Necesario si hay diferencias de nombre, como idigdbPlatformId
import mp.tfg.mycheckpoint.dto.igdb.PlataformaIgdbDTO;
import mp.tfg.mycheckpoint.entity.PlataformaIGDB;

@Mapper(componentModel = "spring")
public interface PlataformaIgdbMapper {

    /**
     * Convierte una entidad {@link PlataformaIGDB} a su DTO {@link PlataformaIgdbDTO}.
     * @param plataformaIgdb La entidad PlataformaIGDB a convertir.
     * @return El DTO PlataformaIgdbDTO resultante.
     */
    @Mapping(source = "idigdbPlatformId", target = "idigdbPlatformId") // Mapeo explícito por claridad
    PlataformaIgdbDTO toDTO(PlataformaIGDB plataformaIgdb);

    // No se necesita toEntity ni updateEntityFromDTO.
}