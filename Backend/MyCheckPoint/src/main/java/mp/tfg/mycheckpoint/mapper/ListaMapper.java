package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.lista.AddJuegoToListaDTO;
import mp.tfg.mycheckpoint.dto.lista.ListaCreateDTO;
import mp.tfg.mycheckpoint.dto.lista.ListaDetalleDTO;
import mp.tfg.mycheckpoint.dto.lista.ListaDTO;
import mp.tfg.mycheckpoint.dto.lista.ListaUpdateDTO;
import mp.tfg.mycheckpoint.entity.Lista;
import mp.tfg.mycheckpoint.entity.junction.ListaJuego; // Necesario para mapear la colección de unión
import mp.tfg.mycheckpoint.mapper.JuegoMapper; // Necesario para mapear Juegos a JuegoSummaryDTOs

import java.util.Set;

@Mapper(componentModel = "spring",
        uses = { JuegoMapper.class } // Indica que este mapper usará JuegoMapper
)
public interface ListaMapper {

    // Mapea de Entidad Lista a ListaDTO
    // MapStruct mapeará directamente los campos con el mismo nombre.
    // Para el usuarioId, mapeamos desde la relación:
    @Mapping(source = "usuario.id", target = "usuarioId")
    // Si incluyes numJuegos en ListaDTO, necesitarías calcularlo en el servicio y establecerlo manualmente,
    // o usar una expresion @Mapping si la lógica de conteo fuera simple (raro en mappers).
    ListaDTO toDTO(Lista lista);

    // Mapea de Entidad Lista a ListaDetalleDTO
    // MapStruct mapeará los campos básicos heredados del DTO padre.
    // Para la colección de juegos, necesitas mapear Set<ListaJuego> a Set<JuegoSummaryDTO>.
    // Esto requiere un método de mapeo personalizado o una expresión en MapStruct.
    // Por defecto, MapStruct intentaría mapear ListaJuego a algo llamado 'juegos' si existiera un mapper.
    // Aquí necesitamos extraer el 'juego' de cada ListaJuego y mapearlo a JuegoSummaryDTO.
    @Mapping(target = "juegos", expression = "java(mapListaJuegosToJuegoSummaryDTOs(lista.getListaJuegos()))")
    ListaDetalleDTO toDetalleDTO(Lista lista);

    // Método personalizado para mapear la colección de unión a la colección de DTOs de juego.
    // MapStruct generará la implementación de este método si se llama desde un @Mapping.
    // Debes declarar el método aquí y MapStruct lo usará, asumiendo que JuegoMapper está en 'uses'.
    Set<JuegoSummaryDTO> mapListaJuegosToJuegoSummaryDTOs(Set<ListaJuego> listaJuegos);


    // Mapea de DTO de creación a Entidad Lista
    // Ignoramos campos autogenerados, fechas y relaciones.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true) // El usuario se asigna en el servicio/controller
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "listaJuegos", ignore = true) // La colección se gestiona por separado
    Lista toEntity(ListaCreateDTO listaCreateDTO);

    // Mapea de DTO de actualización a Entidad Lista existente
    // Ignoramos campos que no se actualizan por este DTO.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true) // Gestionado por DB/Timestamp
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "listaJuegos", ignore = true)
    void updateEntityFromDTO(ListaUpdateDTO listaUpdateDTO, @MappingTarget Lista lista);

    // Mapea de DTO para añadir juego a una lista. Este DTO no se mapea directamente a una entidad Lista,
    // pero el mapper puede ser útil para otras transformaciones si fuera necesario.
    // AddJuegoToListaDTO no mapea a una Entidad Lista directamente.
    // Es más probable que uses este DTO en tu servicio para obtener el juegoId y la listaId
    // y luego crear una entidad ListaJuego. No hay un mapeo directo DTO->Entidad aquí.
}