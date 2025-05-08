package mp.tfg.mycheckpoint.mapper;
import mp.tfg.mycheckpoint.dto.igdb.GameModeDTO;
import mp.tfg.mycheckpoint.entity.GameMode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface GameModeMapper {
    @Mapping(source = "idigdb", target = "id") GameModeDTO toDto(GameMode entity);
    Set<GameModeDTO> toDtoSet(Set<GameMode> entities);
}