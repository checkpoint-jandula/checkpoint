package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.duracion.DuracionJuegoDTO;
import mp.tfg.mycheckpoint.entity.DuracionJuego;

// Anotación de MapStruct indicando que es un mapper
// componentModel = "spring" hace que MapStruct genere una implementación que se puede inyectar con Spring
@Mapper(componentModel = "spring")
public interface DuracionJuegoMapper {

    // Mapea de Entidad DuracionJuego a DuracionJuegoDTO
    // MapStruct puede manejar la mayoría de los mapeos directos por nombre.
    // Si hay alguna diferencia de nombre (ej: idDuracion en entidad vs id en DTO,
    // aunque aquí son iguales idDuracion) o tipo (Float vs Double),
    // podrías necesitar @Mapping.

    @Mapping(source = "idDuracion", target = "idDuracion") // Mapeo explícito por claridad (opcional si los nombres coinciden)
    @Mapping(source = "juego.id", target = "juegoId") // Mapea el ID del juego asociado a juegoId en el DTO
    @Mapping(source = "mediaHistoria", target = "mediaHistoria") // Mapeo de Float a Double si la entidad usa Float
    @Mapping(source = "mediaHistoriaSecundarias", target = "mediaHistoriaSecundarias")
    @Mapping(source = "mediaCompletista", target = "mediaCompletista")
    @Mapping(source = "numeroUsuarios", target = "numeroUsuarios")
    @Mapping(source = "fechaModificacion", target = "fechaModificacion")
    DuracionJuegoDTO toDTO(DuracionJuego duracionJuego);

    // No necesitamos métodos toEntity o updateEntityFromDTO ya que estos datos se calculan/gestionan internamente
}