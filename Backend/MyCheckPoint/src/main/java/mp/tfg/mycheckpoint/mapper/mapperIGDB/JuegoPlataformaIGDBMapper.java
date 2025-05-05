package mp.tfg.mycheckpoint.mapper.mapperIGDB;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import mp.tfg.mycheckpoint.dto.igdb.JuegoPlataformaIGDBDTO;
import mp.tfg.mycheckpoint.entity.junction.JuegoPlataformaIGDB;
import mp.tfg.mycheckpoint.mapper.mapperIGDB.PlataformaIgdbMapper; // Necesita el mapper de PlataformaIGDB

@Mapper(componentModel = "spring", uses = { PlataformaIgdbMapper.class }) // Indica que usa PlataformaIgdbMapper
public interface JuegoPlataformaIGDBMapper {

    // Mapea de Entidad de unión a DTO
    // MapStruct mapeará automáticamente el campo 'plataformaIgdb' de la entidad a 'plataformaIgdb' en el DTO
    // utilizando el PlataformaIgdbMapper configurado en 'uses'.
    // Mapeará 'fechaLanzamiento' directamente por nombre.
    JuegoPlataformaIGDBDTO toDTO(JuegoPlataformaIGDB juegoPlataformaIGDB);

    // No necesitamos métodos toEntity o updateEntityFromDTO para estas entidades de unión de datos IGDB
    // ya que su creación/actualización se gestionaría al importar datos de IGDB o al manipular el Juego.
}