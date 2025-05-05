package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import mp.tfg.mycheckpoint.dto.igdb.JuegoIdiomaSoporteDTO;
import mp.tfg.mycheckpoint.entity.junction.JuegoIdiomaSoporte;
import mp.tfg.mycheckpoint.mapper.mapperIGDB.IdiomaMapper; // Necesita el mapper de Idioma

@Mapper(componentModel = "spring", uses = { IdiomaMapper.class }) // Indica que usa IdiomaMapper
public interface JuegoIdiomaSoporteMapper {

    // Mapea de Entidad de unión a DTO
    JuegoIdiomaSoporteDTO toDTO(JuegoIdiomaSoporte juegoIdiomaSoporte);

    // No necesitamos métodos toEntity/updateEntityFromDTO
}