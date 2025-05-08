package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.igdb.GenreDTO;
import mp.tfg.mycheckpoint.entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set; // Para mapear Sets

@Mapper(componentModel = "spring")
public interface GenreMapper {

    @Mapping(source = "idigdb", target = "id") // Mapear id_igdb a id del DTO
    GenreDTO toDto(Genre genre);

    List<GenreDTO> toDtoList(List<Genre> genres);
    Set<GenreDTO> toDtoSet(Set<Genre> genres); // Para mapear las colecciones Set

    // No necesitamos toEntity normalmente si solo leemos catálogos
}