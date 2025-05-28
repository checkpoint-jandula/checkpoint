package mp.tfg.mycheckpoint.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;



public enum GameType {
    GAME(0, "Juego Principal"), // 0 representa un JUEGO
    DLC(1, "Contenido Descargable"),    // 1 representa un DLC
    EXPANSION(2, "Expansión"), // 2 representa una EXPANSION
    BUNDLE(3, "Paquete"),
    STANDALONE_EXPANSION(4, "Expansión Independiente"),
    MOD(5, "Modificación"),
    EPISODE(6, "Episodio"),
    SEASON(7, "Temporada"),
    REMAKE(8, "Remake"),
    REMASTER(9, "Remasterización"),
    EXPANDED_GAME(10, "Juego Expandido"),
    PORT(11, "Adaptación"), // "Port" también es común
    FORK(12, "Derivación"), // O "Bifurcación"
    PACK(13, "Colección"), // Similar a Bundle, pero podría tener connotaciones diferentes según IGDB
    UPDATE(14, "Actualización"),
    UNKNOWN(15, "Desconocido");

    private final int value;
    private final String displayName;

    GameType(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue // Esto le dice a Jackson cómo serializar el enum a JSON (usará el valor int)
    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator // Esto le dice a Jackson cómo deserializar el valor JSON (int) al enum
    public static GameType fromValue(int value) {
        for (GameType type : GameType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para GameType: " + value);
    }

}
