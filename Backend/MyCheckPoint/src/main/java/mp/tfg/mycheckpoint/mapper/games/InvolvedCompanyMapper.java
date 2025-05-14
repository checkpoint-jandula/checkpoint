package mp.tfg.mycheckpoint.mapper.games;


import mp.tfg.mycheckpoint.dto.games.InvolvedCompanyDto;
import mp.tfg.mycheckpoint.entity.games.GameCompanyInvolvement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class}) // Usa CompanyMapper para el campo 'company'
public interface InvolvedCompanyMapper {
    InvolvedCompanyMapper INSTANCE = Mappers.getMapper(InvolvedCompanyMapper.class);

    // MapStruct usará CompanyMapper para company -> companyDto
    @Mapping(source = "involvementIgdbId", target = "involvementIgdbId") // MapStruct maneja si los nombres son iguales
    InvolvedCompanyDto toDto(GameCompanyInvolvement involvement);

    // MapStruct usará CompanyMapper para companyDto -> company
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "game", ignore = true) // El juego se establecerá en el servicio
    @Mapping(source = "involvementIgdbId", target = "involvementIgdbId")
    GameCompanyInvolvement toEntity(InvolvedCompanyDto involvementDto);
}
