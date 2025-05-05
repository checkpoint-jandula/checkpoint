package mp.tfg.mycheckpoint.entity.enums;

// Mapea checkpoint_estado_juego_usuario_enum
public enum EstadoEnum {
    COMPLETADO_NO_SELECCIONAR,
    COMPLETADO_HISTORIA,
    COMPLETADO_HISTORIA_SECUNDARIAS,
    COMPLETADO_100,
    ARCHIVADO_NO_SELECCIONAR,
    ARCHIVADO_ABANDONADO,
    ARCHIVADO_NO_JUGANDO,
    DESEADO, // Estado por defecto según el SQL
    JUGANDO_NO_SELECCIONAR,
    JUGANDO_PAUSADO,
    JUGANDO_INFINITO
}