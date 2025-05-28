package mp.tfg.mycheckpoint.dto.enums;

public enum UserGameStatus {
    COMPLETED,                  // COMPLETADO
    COMPLETED_MAIN_STORY,       // COMPLETADO_HISTORIA
    COMPLETED_MAIN_AND_SIDES,   // COMPLETADO_HISTORIA_SECUNDARIAS
    COMPLETED_100_PERCENT,      // COMPLETADO_100
    ARCHIVED,                   // ARCHIVADO (Genérico)
    ARCHIVED_ABANDONED,         // ARCHIVADO_ABANDONADO
    ARCHIVED_NOT_PLAYING,       // ARCHIVADO_NO_JUGANDO (Quizás para juegos que ya no puede jugar)
    WISHLIST,                   // DESEADO
    PLAYING,                    // JUGANDO
    PLAYING_PAUSED,             // JUGANDO_PAUSADO
    PLAYING_ENDLESS             // JUGANDO_INFINITO (Para juegos sin final claro, como MMOs o multijugador)
}
