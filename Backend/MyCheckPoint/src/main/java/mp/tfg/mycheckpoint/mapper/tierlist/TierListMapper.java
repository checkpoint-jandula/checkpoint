package mp.tfg.mycheckpoint.mapper.tierlist;

import mp.tfg.mycheckpoint.dto.tierlist.TierListCreateRequestDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierListResponseDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierListUpdateRequestDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierSectionResponseDTO;
import mp.tfg.mycheckpoint.entity.TierList;
import mp.tfg.mycheckpoint.entity.TierSection;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {TierSectionMapper.class}) // IMPORTANTE: uses = {TierSectionMapper.class}
public interface TierListMapper {

    @Mapping(target = "publicId", source = "tierList.publicId")
    @Mapping(target = "name", source = "tierList.name")
    @Mapping(target = "description", source = "tierList.description")
    @Mapping(target = "type", source = "tierList.type")
    @Mapping(target = "sourceGameListPublicId", source = "tierList.sourceGameList.publicId")
    @Mapping(target = "ownerUsername", source = "tierList.owner.nombreUsuario")
    @Mapping(target = "isPublic", source = "tierList.public") // Lombok genera isPublic()
    @Mapping(target = "createdAt", source = "tierList.createdAt")
    @Mapping(target = "updatedAt", source = "tierList.updatedAt")
    @Mapping(target = "sections", ignore = true) // Se mapearán manualmente en @AfterMapping
    @Mapping(target = "unclassifiedSection", ignore = true) // Se mapeará manualmente en @AfterMapping
    TierListResponseDTO toResponseDTO(TierList tierList); // Solo un argumento aquí

    @AfterMapping
    default void mapSectionsToResponseDTO(TierList tierList, @MappingTarget TierListResponseDTO responseDTO, @Context TierSectionMapper tierSectionMapper) {
        if (tierList.getSections() != null && tierSectionMapper != null) {
            List<TierSectionResponseDTO> userDefinedSections = tierList.getSections().stream()
                    .filter(s -> !s.isDefaultUnclassified())
                    .map(tierSectionMapper::toResponseDTO) // MapStruct usará el mapper de TierSection aquí
                    .collect(Collectors.toList());
            responseDTO.setSections(userDefinedSections); // Usar setter

            TierSectionResponseDTO unclassified = tierList.getSections().stream()
                    .filter(TierSection::isDefaultUnclassified)
                    .findFirst()
                    .map(tierSectionMapper::toResponseDTO) // MapStruct usará el mapper de TierSection aquí
                    .orElse(null);
            responseDTO.setUnclassifiedSection(unclassified); // Usar setter
        }
    }

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "sourceGameList", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TierList fromCreateRequestDTO(TierListCreateRequestDTO createRequestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "sourceGameList", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromUpdateRequestDTO(TierListUpdateRequestDTO updateRequestDTO, @MappingTarget TierList tierList);
}