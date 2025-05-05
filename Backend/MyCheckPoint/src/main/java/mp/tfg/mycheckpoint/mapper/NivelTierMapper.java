package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.niveltier.NivelTierCreateDTO;
import mp.tfg.mycheckpoint.dto.niveltier.NivelTierDTO;
import mp.tfg.mycheckpoint.dto.niveltier.NivelTierUpdateDTO;
import mp.tfg.mycheckpoint.entity.NivelTier;

@Mapper(componentModel = "spring")
public interface NivelTierMapper {

    // Mapea de Entidad NivelTier a NivelTierDTO
    // Mapeamos el ID de la TierList asociada
    @Mapping(source = "tierList.id", target = "tierlistId")
    NivelTierDTO toDTO(NivelTier nivelTier);

    // Mapea de DTO de creación a Entidad NivelTier
    // Ignoramos campos autogenerados y la relación a TierList (se asigna en el servicio/controller)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tierList", ignore = true)
    @Mapping(target = "tierListJuegos", ignore = true) // La colección se gestiona por separado
    NivelTier toEntity(NivelTierCreateDTO nivelTierCreateDTO);

    // Mapea de DTO de actualización a Entidad NivelTier existente
    // Ignoramos el ID del nivel, el de la tier list y la colección de juegos
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tierList", ignore = true)
    @Mapping(target = "tierListJuegos", ignore = true)
    void updateEntityFromDTO(NivelTierUpdateDTO nivelTierUpdateDTO, @MappingTarget NivelTier nivelTier);

    // Si necesitas mapear la lista de niveles DENTRO de un DTO de TierListDetalle,
    // MapStruct usará este mapper automáticamente si está en el 'uses' del TierListMapper.
}