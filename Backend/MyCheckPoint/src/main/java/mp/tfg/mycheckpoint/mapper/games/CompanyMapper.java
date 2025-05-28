package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.CompanyInfoDto;
import mp.tfg.mycheckpoint.entity.games.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    CompanyInfoDto toDto(Company company);

    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "involvements", ignore = true) // Ignorar la colección inversa
    Company toEntity(CompanyInfoDto companyInfoDto);
}
