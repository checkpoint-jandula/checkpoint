package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.CompaniaDTO;
import mp.tfg.mycheckpoint.entity.Compania;

@Mapper(componentModel = "spring")
public interface CompaniaMapper {
    CompaniaDTO toDTO(Compania compania);
}