package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named; // Necesario para @Named
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO; // Necesario para mapear juegos
import mp.tfg.mycheckpoint.dto.niveltier.NivelTierDTO; // Necesario para mapear niveles
import mp.tfg.mycheckpoint.dto.tierlist.TierListCreateDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierListDetalleDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierListDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierListUpdateDTO;
import mp.tfg.mycheckpoint.entity.TierList;
import mp.tfg.mycheckpoint.entity.NivelTier; // Necesario para la colección de niveles
import mp.tfg.mycheckpoint.entity.junction.TierListJuego; // Necesario para la colección de unión
import mp.tfg.mycheckpoint.mapper.JuegoMapper; // Necesario para mapear Juegos
import mp.tfg.mycheckpoint.mapper.NivelTierMapper; // Necesario para mapear NivelesTier
import mp.tfg.mycheckpoint.dto.tierlist.TierListDetalleDTO.NivelConJuegosDTO; // Importar la clase interna/anidada

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors; // Para usar Streams

@Mapper(componentModel = "spring",
        uses = { NivelTierMapper.class, JuegoMapper.class } // Indica que este mapper usará NivelTierMapper y JuegoMapper
)
public interface TierListMapper {

    // Mapea de Entidad TierList a TierListDTO
    // Mapeamos el ID del usuario propietario
    @Mapping(source = "usuario.id", target = "usuarioId")
    TierListDTO toDTO(TierList tierList);

    // Mapea de Entidad TierList a TierListDetalleDTO
    // MapStruct mapeará los campos básicos heredados de TierListDTO.
    // Para la colección de niveles con juegos anidados, necesitamos un método personalizado.
    @Mapping(target = "niveles", source = "nivelesTier", qualifiedByName = "mapNivelesConJuegos")
    TierListDetalleDTO toDetalleDTO(TierList tierList);

    // Método personalizado para mapear la colección de NivelTier a la estructura NivelConJuegosDTO.
    // Este método itera sobre los NivelTier de la TierList, mapea cada NivelTier a NivelTierDTO,
    // y para cada nivel, encuentra los TierListJuego asociados a ese nivel para obtener los Juegos.
    @Named("mapNivelesConJuegos")
    default List<NivelConJuegosDTO> mapNivelesConJuegos(List<NivelTier> nivelesTier) {
        if (nivelesTier == null) {
            return null;
        }
        // Asegurarse de que los niveles están ordenados (deberían estarlo por @OrderBy en la entidad)
        // Pero MapStruct trabaja con la colección tal como la recibe.
        // Si el orden no estuviera garantizado por la entidad, necesitarías ordenar aquí.

        return nivelesTier.stream()
                .map(nivelTier -> {
                    // Mapear el NivelTier a NivelTierDTO
                    NivelTierDTO nivelDto = toNivelTierDTO(nivelTier); // MapStruct genera la implementación de este sub-mapeo

                    // Encontrar los TierListJuego asociados a este nivel y mapear sus Juegos a JuegoSummaryDTO
                    Set<JuegoSummaryDTO> juegosDto = nivelTier.getTierListJuegos().stream()
                            .map(TierListJuego::getJuego) // Obtener el Juego de la entidad de unión
                            .map(juego -> toJuegoSummaryDTO(juego)) // Mapear el Juego a JuegoSummaryDTO (MapStruct genera)
                            .collect(Collectors.toSet());

                    // Crear la estructura NivelConJuegosDTO
                    return NivelConJuegosDTO.builder()
                            .nivel(nivelDto)
                            .juegos(juegosDto)
                            .build();
                })
                .collect(Collectors.toList()); // Recopilar en una lista para mantener el orden de los niveles
    }

    // MapStruct generará la implementación de estos métodos al estar en una interfaz con @Mapper y @Named/referenciados
    // y al estar los mappers correspondientes en 'uses'.
    NivelTierDTO toNivelTierDTO(NivelTier nivelTier); // Método usado internamente en mapNivelesConJuegos
    JuegoSummaryDTO toJuegoSummaryDTO(mp.tfg.mycheckpoint.entity.Juego juego); // Método usado internamente en mapNivelesConJuegos

    // Mapea de DTO de creación a Entidad TierList
    // Ignoramos campos autogenerados, relaciones y fechas.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true) // El usuario se asigna en el servicio/controller
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "nivelesTier", ignore = true) // Las colecciones se gestionan por separado (niveles por defecto o añadidos después)
    @Mapping(target = "tierListJuegos", ignore = true) // La colección se gestiona por separado
    TierList toEntity(TierListCreateDTO tierListCreateDTO);

    // Mapea de DTO de actualización a Entidad TierList existente
    // Ignoramos el ID, las fechas, usuario y colecciones.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true) // Gestionado por DB/Timestamp
    @Mapping(target = "fechaEliminacion", ignore = true)
    @Mapping(target = "nivelesTier", ignore = true)
    @Mapping(target = "tierListJuegos", ignore = true)
    void updateEntityFromDTO(TierListUpdateDTO tierListUpdateDTO, @MappingTarget TierList tierList);

    // No necesitamos métodos toEntity o updateEntityFromDTO para los DTOs relacionados con TierListJuego
    // ya que la lógica de añadir/actualizar juegos en una tier list se maneja en el servicio y repositorio
    // de TierListJuego.
}