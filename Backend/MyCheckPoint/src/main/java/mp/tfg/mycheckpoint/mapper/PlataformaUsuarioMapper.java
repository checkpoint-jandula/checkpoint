package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
// import org.mapstruct.MappingTarget;
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.junction.PlataformaUsuarioDTO; // El DTO para la entidad de unión
import mp.tfg.mycheckpoint.entity.junction.PlataformaUsuario; // La entidad de unión
import mp.tfg.mycheckpoint.entity.embedded.PlataformaUsuarioId; // El ID embebido

// Importar mappers si anidas DTOs de entidades relacionadas en PlataformaUsuarioDTO
// import mp.tfg.mycheckpoint.mapper.UsuarioMapper;
// import mp.tfg.mycheckpoint.mapper.PlataformaMapper;

// Si anidas DTOs de relaciones en PlataformaUsuarioDTO, añade los mappers correspondientes a 'uses'.
// @Mapper(componentModel = "spring", uses = { UsuarioMapper.class, PlataformaMapper.class })
@Mapper(componentModel = "spring") // Si PlataformaUsuarioDTO solo tiene IDs y fechas
public interface PlataformaUsuarioMapper {

    // Mapea de Entidad PlataformaUsuario a PlataformaUsuarioDTO
    // Mapeamos las partes del EmbeddedId al DTO plano
    @Mapping(source = "id.usuarioId", target = "usuarioId")
    @Mapping(source = "id.plataformaId", target = "plataformaId")
    // Si anidas DTOs de relaciones en PlataformaUsuarioDTO:
    // @Mapping(source = "usuario", target = "usuario") // Mapea entidad Usuario a UsuarioSummaryDTO (si UsuarioMapper está en uses)
    // @Mapping(source = "plataforma", target = "plataforma") // Mapea entidad Plataforma a PlataformaDTO (si PlataformaMapper está en uses)
    PlataformaUsuarioDTO toDTO(PlataformaUsuario plataformaUsuario);

    // No necesitamos métodos toEntity o updateEntityFromDTO ya que la creación/actualización
    // de la relación se maneja buscando entidades y creando/modificando la entidad de unión
    // directamente en el servicio.

    // Si necesitaras un método para mapear IDs a la clase EmbeddedId, similar al de Amistad:
    // @Mapping(source = "usuarioId", target = "usuarioId")
    // @Mapping(source = "plataformaId", target = "plataformaId")
    // PlataformaUsuarioId toEmbeddedId(Long usuarioId, Long plataformaId); // Método helper para construir el ID
}