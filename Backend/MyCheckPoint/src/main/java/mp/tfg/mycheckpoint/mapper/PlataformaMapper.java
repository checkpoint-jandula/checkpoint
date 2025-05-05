package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.plataforma.PlataformaCreateDTO;
import mp.tfg.mycheckpoint.dto.plataforma.PlataformaDTO;
import mp.tfg.mycheckpoint.dto.plataforma.PlataformaUpdateDTO;
import mp.tfg.mycheckpoint.entity.Plataforma; // Nuestra entidad Plataforma

@Mapper(componentModel = "spring")
public interface PlataformaMapper {

    // Mapea de Entidad Plataforma a PlataformaDTO
    PlataformaDTO toDTO(Plataforma plataforma);

    // Mapea de DTO de creación a Entidad Plataforma
    // Ignoramos campos autogenerados y colecciones.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "plataformasUsuario", ignore = true) // Colecciones se gestionan por separado
    @Mapping(target = "juegosUsuario", ignore = true) // Colecciones se gestionan por separado
    Plataforma toEntity(PlataformaCreateDTO plataformaCreateDTO);

    // Mapea de DTO de actualización a Entidad Plataforma existente
    // Ignoramos el ID, las fechas y colecciones. MapStruct actualizará los campos correspondientes.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true) // Gestionado por DB/Timestamp
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "plataformasUsuario", ignore = true)
    @Mapping(target = "juegosUsuario", ignore = true)
    void updateEntityFromDTO(PlataformaUpdateDTO plataformaUpdateDTO, @MappingTarget Plataforma plataforma);
}