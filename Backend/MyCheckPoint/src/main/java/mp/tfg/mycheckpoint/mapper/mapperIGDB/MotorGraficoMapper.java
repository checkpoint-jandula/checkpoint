package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import mp.tfg.mycheckpoint.dto.igdb.MotorGraficoDTO;
import mp.tfg.mycheckpoint.entity.MotorGrafico;

@Mapper(componentModel = "spring")
public interface MotorGraficoMapper {

    MotorGraficoDTO toDTO(MotorGrafico motorGrafico);
}