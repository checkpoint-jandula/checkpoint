package mp.tfg.mycheckpoint.mapper;

import mp.tfg.mycheckpoint.entity.enums.GameTypeEnum;
import mp.tfg.mycheckpoint.entity.junction.JuegoRelacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
// import org.mapstruct.MappingTarget;

import mp.tfg.mycheckpoint.dto.juego.JuegoDTO;
import mp.tfg.mycheckpoint.dto.juego.JuegoSummaryDTO;
import mp.tfg.mycheckpoint.entity.Juego;
// Importar los mappers de IGDB y uniones que usará este mapper
import mp.tfg.mycheckpoint.mapper.mapperIGDB.*; // Importar todos los mappers del subpaquete igdb
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring",
        uses = { // Indica a MapStruct qué otros mappers debe usar
                GeneroMapper.class,
                MotorGraficoMapper.class,
                CompaniaMapper.class,
                KeywordMapper.class,
                ModoJuegoMapper.class,
                PerspectivaJugadorMapper.class,
                PlataformaIgdbMapper.class,
                TemaMapper.class,
                FranquiciaMapper.class,
                ArtworkMapper.class,
                VideoMapper.class,
                WebMapper.class,
                IdiomaMapper.class,
                JuegoPlataformaIGDBMapper.class,
                JuegoIdiomaSoporteMapper.class
        })
public interface JuegoMapper {

    // Mapea de Entidad a DTO completo
    @Mapping(source = "plataformasIgdb", target = "plataformasIgdb")
    @Mapping(source = "idiomasSoporte", target = "idiomasSoporte")
    // Mapeos para DLCs y Expansiones (usando JuegoRelacion)
    @Mapping(target = "dlcs", source = "relacionesOrigen", qualifiedByName = "mapRelacionesOrigenToDLCs")
    @Mapping(target = "expansiones", source = "relacionesOrigen", qualifiedByName = "mapRelacionesOrigenToExpansiones")
    // ***** CORRECCIÓN: Mapeo directo para 'similares' desde la nueva relación ManyToMany *****
    // MapStruct mapeará Set<Juego> a Set<JuegoSummaryDTO> automáticamente si los nombres coinciden.
    // Añadimos el mapeo explícito por claridad.
    @Mapping(source = "similares", target = "similares")
    JuegoDTO toDTO(Juego juego);

    // Mapea de Entidad a DTO Resumen
    JuegoSummaryDTO toSummaryDTO(Juego juego);

    // --- Métodos personalizados para mapear relacionesOrigen a DLCs y Expansiones ---
    // ***** NO TOCAMOS ESTOS, siguen siendo válidos para DLCs y Expansiones *****
    @Named("mapRelacionesOrigenToDLCs")
    default Set<JuegoSummaryDTO> mapRelacionesOrigenToDLCs(Set<JuegoRelacion> relacionesOrigen) {
        if (relacionesOrigen == null) {
            return null;
        }
        return relacionesOrigen.stream()
                .filter(rel -> rel.getId().getTipoRelacion() == GameTypeEnum.DLC)
                .map(JuegoRelacion::getJuegoDestino)
                .map(this::toSummaryDTO)
                .collect(Collectors.toSet());
    }

    @Named("mapRelacionesOrigenToExpansiones")
    default Set<JuegoSummaryDTO> mapRelacionesOrigenToExpansiones(Set<JuegoRelacion> relacionesOrigen) {
        if (relacionesOrigen == null) {
            return null;
        }
        return relacionesOrigen.stream()
                .filter(rel -> rel.getId().getTipoRelacion() == GameTypeEnum.EXPANSION)
                .map(JuegoRelacion::getJuegoDestino)
                .map(this::toSummaryDTO)
                .collect(Collectors.toSet());
    }

    // ***** CORRECCIÓN: Eliminado el método mapRelacionesOrigenToSimilares *****
    // Ya no es necesario porque 'similares' se mapea directamente desde la entidad.

}