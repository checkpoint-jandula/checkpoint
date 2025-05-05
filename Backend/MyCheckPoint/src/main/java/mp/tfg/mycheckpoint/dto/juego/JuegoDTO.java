package mp.tfg.mycheckpoint.dto.juego;

import lombok.*;
import mp.tfg.mycheckpoint.dto.igdb.*; // Importar DTOs para entidades relacionadas con IGDB
import mp.tfg.mycheckpoint.entity.enums.GameTypeEnum;
import mp.tfg.mycheckpoint.entity.enums.StatusEnum;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JuegoDTO {

    private Long id;
    private Long idigdb;
    private String nombre;
    private String slug;
    private GameTypeEnum gameType; // O String si mapeas el Enum a String en el DTO
    private String resumen;
    private String historia;
    private Float totalRating;
    private Integer totalRatingCount;
    private LocalDate fechaLanzamiento;
    private String coverUrl;
    private StatusEnum status; // O String si mapeas el Enum a String en el DTO
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private OffsetDateTime fechaEliminacion;

    // Incluir DTOs para las entidades relacionadas
    private Set<GeneroDTO> generos;
    private Set<MotorGraficoDTO> motores;
    private Set<ModoJuegoDTO> modosJuego;
    private Set<CompaniaDTO> companias;
    private Set<KeywordDTO> keywords;
    // Aquí se mapea a DTOs de la tabla de unión si quieres sus atributos extra (ej: fechaLanzamiento por plataforma)
    // O simplemente a DTOs de PlataformaIGDB si no necesitas el atributo extra en este DTO
    private Set<JuegoPlataformaIGDBDTO> plataformasIgdb; // Necesitarías crear JuegoPlataformaIGDBDTO
    private Set<TemaDTO> temas;
    private Set<PerspectivaJugadorDTO> perspectivas;
    private Set<ArtworkDTO> imagenes;
    private Set<VideoDTO> videos;
    private Set<WebDTO> webs;
    // Aquí mapeas a DTOs de la tabla de unión si quieres sus atributos extra (ej: tipoSoporte)
    // O simplemente a DTOs de Idioma si no necesitas el atributo extra en este DTO
    private Set<JuegoIdiomaSoporteDTO> idiomasSoporte; // Necesitarías crear JuegoIdiomaSoporteDTO
    private Set<FranquiciaDTO> franquicias;

    // Relaciones autoreferenciales (podrían ser JuegoSummaryDTO para evitar recursión infinita)
    private Set<JuegoSummaryDTO> dlcs; // Esto implicaría mapear JuegoRelacion a una colección de JuegoSummaryDTOs
    private Set<JuegoSummaryDTO> expansiones; // Esto implicaría mapear JuegoRelacion a una colección de JuegoSummaryDTOs
    private Set<JuegoSummaryDTO> similares; // Esto implicaría mapear JuegoRelacion a una colección de JuegoSummaryDTOs

    // No se incluyen relaciones inversas como juegosUsuario, listasJuego, tierListsJuego, ranking, duracion
    // a menos que sea un DTO de detalle muy específico
}