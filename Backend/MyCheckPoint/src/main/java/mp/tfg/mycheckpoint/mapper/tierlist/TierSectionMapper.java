package mp.tfg.mycheckpoint.mapper.tierlist;

import mp.tfg.mycheckpoint.dto.tierlist.TierSectionRequestDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierSectionResponseDTO;
import mp.tfg.mycheckpoint.entity.TierSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TierListItemMapper.class})
public interface TierSectionMapper {

    @Mapping(target = "internalId", source = "internalId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "order", source = "sectionOrder")
    // Para toResponseDTO: leer de la entidad TierSection (getter isDefaultUnclassified())
    // MapStruct infiere la propiedad fuente como "defaultUnclassified"
    @Mapping(target = "isDefaultUnclassified", source = "defaultUnclassified")
    @Mapping(target = "items", source = "items")
    TierSectionResponseDTO toResponseDTO(TierSection tierSection);

    List<TierSectionResponseDTO> toResponseDTOList(List<TierSection> tierSections);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "tierList", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "sectionOrder", ignore = true)
    // Para fromRequestDTO: crear TierSection usando el Builder.
    // El método del builder es isDefaultUnclassified(boolean val), por lo que el target es "isDefaultUnclassified".
    @Mapping(target = "isDefaultUnclassified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TierSection fromRequestDTO(TierSectionRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "tierList", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "sectionOrder", ignore = true)
    // Para updateFromRequestDTO: actualizar TierSection usando setters (setDefaultUnclassified(boolean val)).
    // MapStruct infiere la propiedad target como "defaultUnclassified".
    @Mapping(target = "defaultUnclassified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequestDTO(TierSectionRequestDTO requestDTO, @MappingTarget TierSection tierSection);
}