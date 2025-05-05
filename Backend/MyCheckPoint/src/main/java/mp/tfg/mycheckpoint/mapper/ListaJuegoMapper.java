package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
// import org.mapstruct.MappingTarget;
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.junction.ListaJuegoDTO; // El DTO para la entidad de unión
import mp.tfg.mycheckpoint.entity.junction.ListaJuego; // La entidad de unión
import mp.tfg.mycheckpoint.entity.embedded.ListaJuegoId; // El ID embebido

// Importar mappers si anidas DTOs de entidades relacionadas en ListaJuegoDTO
// import mp.tfg.mycheckpoint.mapper.ListaMapper;
// import mp.tfg.mycheckpoint.mapper.JuegoMapper;

// Si anidas DTOs de relaciones en ListaJuegoDTO, añade los mappers correspondientes a 'uses'.
// @Mapper(componentModel = "spring", uses = { ListaMapper.class, JuegoMapper.class })
@Mapper(componentModel = "spring") // Si ListaJuegoDTO solo tiene IDs y fechas
public interface ListaJuegoMapper {

    // Mapea de Entidad ListaJuego a ListaJuegoDTO
    // Mapeamos las partes del EmbeddedId al DTO plano
    @Mapping(source = "id.listaId", target = "listaId")
    @Mapping(source = "id.juegoId", target = "juegoId")
    // Si anidas DTOs de relaciones en ListaJuegoDTO:
    // @Mapping(source = "lista", target = "lista") // Mapea entidad Lista a ListaDTO (si ListaMapper está en uses)
    // @Mapping(source = "juego", target = "juego") // Mapea entidad Juego a JuegoSummaryDTO (si JuegoMapper está en uses)
    ListaJuegoDTO toDTO(ListaJuego listaJuego);

    // No necesitamos métodos toEntity o updateEntityFromDTO si la creación/actualización
    // de la relación se maneja buscando entidades y creando/modificando la entidad de unión
    // directamente en el servicio.

    // Si necesitaras mapear de DTO de entrada (como AddJuegoToListaDTO) a la entidad de unión,
    // tendrías que crear la lógica en el servicio para buscar la lista y el juego por ID
    // y luego construir la entidad ListaJuego con el EmbeddedId. MapStruct puede ayudar
    // a mapear campos simples, pero no a buscar entidades relacionadas.

    // Ejemplo de cómo MapStruct podría mapear un DTO de entrada a una parte de la entidad (el EmbeddedId)
    // Esto es útil si tu DTO de entrada se parece al EmbeddedId.
    @Mapping(source = "juegoId", target = "juegoId") // Nombre del campo en AddJuegoToListaDTO es 'juegoId' o 'juego_id'?
    @Mapping(source = "listaId", target = "listaId") // Necesitarías añadir listaId a AddJuegoToListaDTO si mapeas así
    ListaJuegoId toEmbeddedId(mp.tfg.mycheckpoint.dto.lista.AddJuegoToListaDTO dto); // Necesitarías AddJuegoToListaDTO aquí
}