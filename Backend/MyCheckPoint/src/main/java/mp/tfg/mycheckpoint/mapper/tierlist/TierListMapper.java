package mp.tfg.mycheckpoint.mapper.tierlist;

import mp.tfg.mycheckpoint.dto.tierlist.TierListCreateRequestDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierListResponseDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierListUpdateRequestDTO;
import mp.tfg.mycheckpoint.dto.tierlist.TierSectionResponseDTO;
import mp.tfg.mycheckpoint.entity.TierList;
import mp.tfg.mycheckpoint.entity.TierSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {TierSectionMapper.class})
public abstract class TierListMapper {

    @Autowired
    protected TierSectionMapper tierSectionMapper;

    @Mapping(target = "publicId", source = "tierList.publicId")
    @Mapping(target = "name", source = "tierList.name")
    @Mapping(target = "description", source = "tierList.description")
    @Mapping(target = "type", source = "tierList.type")
    @Mapping(target = "sourceGameListPublicId", source = "tierList.sourceGameList.publicId")
    @Mapping(target = "ownerUsername", source = "tierList.owner.nombreUsuario")
    @Mapping(target = "isPublic", source = "public") // Propiedad 'public' para getter isPublic()
    @Mapping(target = "createdAt", source = "tierList.createdAt")
    @Mapping(target = "updatedAt", source = "tierList.updatedAt")
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "unclassifiedSection", ignore = true)
    protected abstract TierListResponseDTO tierListToBaseDto(TierList tierList);

    public TierListResponseDTO toTierListResponseDTOWithSections(TierList tierList) {
        if (tierList == null) {
            return null;
        }
        TierListResponseDTO responseDTO = tierListToBaseDto(tierList);
        mapSectionsLogic(tierList, responseDTO);
        return responseDTO;
    }

    protected void mapSectionsLogic(TierList tierList, TierListResponseDTO responseDTO) {
        System.out.println("TierListMapper - INICIO mapSectionsLogic para TierList ID: " + (tierList != null ? tierList.getPublicId() : "null"));
        if (responseDTO == null) {
            System.err.println("TierListMapper - mapSectionsLogic: responseDTO es null.");
            return;
        }
        if (tierList == null || tierList.getSections() == null) {
            System.out.println("TierListMapper - mapSectionsLogic: TierList o sus secciones son null.");
            responseDTO.setSections(new ArrayList<>());
            responseDTO.setUnclassifiedSection(null);
            return;
        }
        if (this.tierSectionMapper == null) {
            System.err.println("TierListMapper - mapSectionsLogic: tierSectionMapper es null. La inyección falló.");
            responseDTO.setSections(new ArrayList<>());
            responseDTO.setUnclassifiedSection(null);
            return;
        }

        List<TierSectionResponseDTO> userDefinedSections = tierList.getSections().stream()
                .filter(s -> !s.isDefaultUnclassified())
                .map(this.tierSectionMapper::toResponseDTO)
                .collect(Collectors.toList());
        responseDTO.setSections(userDefinedSections);
        System.out.println("TierListMapper - mapSectionsLogic: Mapeadas " + userDefinedSections.size() + " secciones definidas por el usuario.");

        TierSectionResponseDTO unclassified = tierList.getSections().stream()
                .filter(TierSection::isDefaultUnclassified)
                .findFirst()
                .map(this.tierSectionMapper::toResponseDTO)
                .orElse(null);
        responseDTO.setUnclassifiedSection(unclassified);
        if (unclassified != null) {
            System.out.println("TierListMapper - mapSectionsLogic: Mapeada sección sin clasificar: " + unclassified.getName());
        } else {
            System.out.println("TierListMapper - mapSectionsLogic: No se encontró sección sin clasificar para mapear.");
        }
        System.out.println("TierListMapper - FIN mapSectionsLogic para TierList ID: " + tierList.getPublicId());
    }

    public abstract TierList fromCreateRequestDTO(TierListCreateRequestDTO createRequestDTO);

    @Mapping(source = "isPublic", target = "public")
    public abstract void updateFromUpdateRequestDTO(TierListUpdateRequestDTO updateRequestDTO, @MappingTarget TierList tierList);
}