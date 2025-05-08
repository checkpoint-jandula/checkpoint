package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.dto.igdb.CompanyDTO;
import mp.tfg.mycheckpoint.entity.Company;
import org.mapstruct.Mapper;
// No se necesita @Mapping si los nombres de campos coinciden (id -> id, idigdb -> idigdb, etc.)
// Si decides que el "id" del DTO sea el id_igdb, necesitarías:
// import org.mapstruct.Mapping;

import java.util.Set;


@Mapper(componentModel = "spring")
public interface CompanyMapper {

    // Si el ID del DTO es el ID interno de la entidad Company:
    CompanyDTO toDto(Company company);

    // Si decidieras que el ID del DTO fuera el id_igdb de la entidad Company:
    // @Mapping(source = "idigdb", target = "id") // Mapea idigdb de la entidad al id del DTO
    // @Mapping(target = "idigdb", source = "idigdb") // Mapea idigdb a idigdb (si lo mantienes en DTO)
    // CompanyDTO toDtoUsingIgdbId(Company company);

    Set<CompanyDTO> toDtoSet(Set<Company> companies);

    // Mapeo inverso si es necesario (ej. para crear/actualizar compañías)
    // Company toEntity(CompanyDTO companyDTO);
}