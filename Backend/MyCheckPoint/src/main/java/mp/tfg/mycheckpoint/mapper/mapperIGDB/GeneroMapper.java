package mp.tfg.mycheckpoint.mapper.mapperIGDB; // Subpaquete para mappers de IGDB

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.GeneroDTO;
import mp.tfg.mycheckpoint.entity.Genero;

@Mapper(componentModel = "spring")
public interface GeneroMapper {

    GeneroDTO toDTO(Genero genero);
    // Si necesitaras mapear de DTO a Entidad (ej. para guardar nuevos géneros de IGDB)
    // Genero toEntity(GeneroDTO generoDTO);
}