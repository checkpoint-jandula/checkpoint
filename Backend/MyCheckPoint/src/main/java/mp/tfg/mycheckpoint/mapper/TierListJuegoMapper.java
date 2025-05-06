package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
// import org.mapstruct.MappingTarget;
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.junction.TierListJuegoDTO; // El DTO para la entidad de unión misma
import mp.tfg.mycheckpoint.dto.junction.NivelTierJuegoDTO; // El DTO que anida nivel y juego (si lo usas)

import mp.tfg.mycheckpoint.entity.junction.TierListJuego; // La entidad de unión


@Mapper(componentModel = "spring",
        uses = { JuegoMapper.class, NivelTierMapper.class } // Indica qué mappers usar
)
public interface TierListJuegoMapper {

    // Mapea de Entidad de unión a DTO básico de unión
    @Mapping(source = "id.tierListId", target = "tierlistId")
    @Mapping(source = "id.juegoId", target = "juegoId")
    @Mapping(source = "nivelTier.id", target = "nivelTierId") // Mapea el ID del nivel asociado
    TierListJuegoDTO toDTO(TierListJuego tierListJuego);

    // Si usas NivelTierJuegoDTO para representar la unión con detalles anidados:
    @Mapping(source = "nivelTier", target = "nivelTier") // Mapea la entidad NivelTier a su DTO
    @Mapping(source = "juego", target = "juego") // Mapea la entidad Juego a su DTO Summary
    NivelTierJuegoDTO toNivelTierJuegoDTO(TierListJuego tierListJuego);

    // No necesitamos métodos toEntity o updateEntityFromDTO para AddJuegoToTierListDTO o UpdateJuegoInTierListDTO.
    // Esos DTOs se usan en la capa de servicio para obtener los IDs y la info necesaria
    // para crear o actualizar una entidad TierListJuego *existente* obtenida del repositorio.

    // Si tuvieras un endpoint para crear TierListJuego directamente con un body:
    // @Mapping(target = "id", ignore = true) // El ID compuesto se construye en el servicio
    // @Mapping(target = "tierList", ignore = true) // Entidades relacionadas se buscan y asignan en el servicio
    // @Mapping(target = "juego", ignore = true)
    // @Mapping(target = "nivelTier", ignore = true)
    // @Mapping(target = "fechaCreacion", ignore = true)
    // @Mapping(target = "fechaModificacion", ignore = true)
    // @Mapping(target = "fechaEliminacion", ignore = true)
    // TierListJuego toEntity(AddJuegoToTierListDTO dto); // Podría usarse para crear si el DTO tuviera los 3 IDs

    // Si tuvieras un endpoint para actualizar TierListJuego directamente con un body:
    // @Mapping(target = "id", ignore = true)
    // @Mapping(target = "tierList", ignore = true)
    // @Mapping(target = "juego", ignore = true)
    // @Mapping(target = "nivelTier", ignore = true) // El nuevo nivel se busca y asigna en el servicio
    // @Mapping(target = "fechaCreacion", ignore = true)
    // @Mapping(target = "fechaModificacion", ignore = true) // Gestionado por DB/Timestamp
    // @Mapping(target = "fechaEliminacion", ignore = true)
    // void updateEntityFromDTO(UpdateJuegoInTierListDTO dto, @MappingTarget TierListJuego tierListJuego); // Podría usarse para actualizar el nivel
}