package mp.tfg.mycheckpoint.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
// import org.mapstruct.factory.Mappers; // No es necesario con componentModel = "spring"

import mp.tfg.mycheckpoint.dto.ranking.RankingDetalleDTO;
import mp.tfg.mycheckpoint.dto.ranking.RankingDTO;
import mp.tfg.mycheckpoint.entity.Ranking;
import mp.tfg.mycheckpoint.mapper.JuegoMapper; // Necesario para mapear Juego a JuegoSummaryDTO

@Mapper(componentModel = "spring",
        uses = { JuegoMapper.class } // Indica que este mapper usará JuegoMapper
)
public interface RankingMapper {

    // Mapea de Entidad Ranking a RankingDTO
    // Mapeamos el ID del juego desde la relación:
    // Si la entidad Ranking usa idRanking como PK, debemos mapear el id del juego asociado
    // Si la entidad usa juego_id como PK (lo cual no es el caso según tu entidad Ranking.java)
    // entonces el mapeo sería diferente. Tu entidad Ranking tiene idRanking como PK
    // y juego como una relación OneToOne con unique=true.
    @Mapping(source = "juego.id", target = "juegoId") // Mapea el ID del juego asociado al campo juegoId del DTO
    @Mapping(source = "idRanking", target = "idRanking") // Mapea la PK interna si quieres exponerla (opcional según OpenAPI)
    RankingDTO toDTO(Ranking ranking);

    // Mapea de Entidad Ranking a RankingDetalleDTO
    // MapStruct mapeará los campos básicos heredados de RankingDTO.
    // Para el campo 'juego', MapStruct mapeará la entidad Juego asociada
    // a JuegoSummaryDTO usando el JuegoMapper configurado en 'uses'.
    @Mapping(source = "juego", target = "juego") // Mapea la entidad Juego a su DTO resumen
    // Si decides exponer idRanking en RankingDTO y RankingDetalleDTO, añade el mapeo aquí también:
    // @Mapping(source = "idRanking", target = "idRanking")
    RankingDetalleDTO toDetalleDTO(Ranking ranking);

    // No necesitamos métodos toEntity o updateEntityFromDTO ya que estos datos se calculan/gestionan internamente.
}