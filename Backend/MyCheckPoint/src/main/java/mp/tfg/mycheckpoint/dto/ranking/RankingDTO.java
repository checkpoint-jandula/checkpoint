package mp.tfg.mycheckpoint.dto.ranking;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingDTO {

    // Nota: La entidad Ranking tiene 'idRanking' como PK, pero la OpenAPI usa 'juego_id'
    // en el esquema Ranking. Parece que la PK interna (idRanking) no se expone,
    // y el juego_id se usa como identificador lógico del ranking en la API.
    // Siguiendo la OpenAPI, exponemos juegoId y los campos de ranking.
    // Si necesitas exponer el idRanking interno por alguna razón, puedes añadirlo.

    private Long juegoId; // ID del juego al que pertenece este ranking
    private Float puntuacionMedia; // O Double, dependiendo de cómo lo manejes después de AVG
    private Integer numeroVotos;
    // La fechaModificacion puede ser útil para saber cuándo se actualizó por última vez
    private OffsetDateTime fechaModificacion;


    // No incluimos el objeto Juego completo aquí, a menos que sea un DTO de detalle.
}