package mp.tfg.mycheckpoint.mapper;

// Importa las anotaciones de la librería de mapeo (MapStruct)
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import mp.tfg.mycheckpoint.dto.amistad.AmistadCreateDTO;
import mp.tfg.mycheckpoint.dto.amistad.AmistadDTO;
import mp.tfg.mycheckpoint.entity.junction.Amistad; // Importar la entidad de unión Amistad
import mp.tfg.mycheckpoint.entity.embedded.AmistadId; // Importar la clase del ID embebido

@Mapper(componentModel = "spring") // Configuración para MapStruct con Spring
public interface AmistadMapper {

    AmistadMapper INSTANCE = Mappers.getMapper(AmistadMapper.class); // Para usar sin inyección de Spring

    // Mapea de Entidad a DTO
    // Necesita mapear las partes del EmbeddedId al DTO plano
    @Mapping(source = "id.usuarioId", target = "usuarioId")
    @Mapping(source = "id.amigoId", target = "amigoId")
    AmistadDTO toDTO(Amistad amistad);

    // Mapea de DTO de creación a Entidad
    // Al mapear a la entidad de unión, necesitas construir el EmbeddedId.
    // MapStruct puede hacer esto con un método "factory" o usando anotaciones Mapping.
    // Ejemplo con @Mapping:
    @Mapping(target = "id", source = "dto") // Mapea el DTO completo a la propiedad 'id' de la entidad
    @Mapping(target = "usuario", ignore = true) // Las entidades relacionadas se establecen en el servicio
    @Mapping(target = "amigo", ignore = true) // Las entidades relacionadas se establecen en el servicio
    @Mapping(target = "fechaCreacion", ignore = true) // Gestionado por DB/Timestamp
    @Mapping(target = "fechaModificacion", ignore = true) // Gestionado por DB/Timestamp
    @Mapping(target = "fechaEliminacion", ignore = true) // Gestionado por soft delete
    Amistad toEntity(AmistadCreateDTO amistadCreateDTO);

    // Método para construir el ID embebido a partir del DTO (MapStruct puede llamarlo implícitamente)
    @Mapping(source = "usuarioId", target = "usuarioId")
    @Mapping(source = "amigoId", target = "amigoId")
    AmistadId toAmistadId(AmistadCreateDTO dto);


    // Si necesitaras un DTO de detalle con info de los usuarios, podrías añadirlo aquí
    // AmistadDetalleDTO toDetalleDTO(Amistad amistad);
}