package mp.tfg.mycheckpoint.mapper.games;



import mp.tfg.mycheckpoint.dto.games.FranchiseDto;
import mp.tfg.mycheckpoint.entity.games.Franchise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FranchiseMapper {

    FranchiseMapper INSTANCE = Mappers.getMapper(FranchiseMapper.class);

    FranchiseDto toDto(Franchise franchise);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "games", ignore = true) // Ignorar la colección inversa
    @Mapping(source = "name", target = "name") // Mapeo explícito para 'name'
    Franchise toEntity(FranchiseDto franchiseDto);
}
