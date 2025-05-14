package mp.tfg.mycheckpoint.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ReleaseStatus {
    RELEASED(0, "Released"),
    ALPHA(2, "Alpha"),
    BETA(3, "Beta"),
    EARLY_ACCESS(4, "Early Access"),
    OFFLINE(5, "Offline"),
    CANCELLED(6, "Cancelled"),
    RUMORED(7, "Rumored"),
    DELISTED(8, "Delisted"),
    UNKNOWN(-1, "Unknown"); // Fallback general

    private final int value;
    private final String displayName;

    private static final Logger logger = LoggerFactory.getLogger(ReleaseStatus.class);

    ReleaseStatus(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue // Para serializar este enum a tu valor entero (0-7) si es necesario
    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Mapea el valor entero del status de IGDB a nuestro enum ReleaseStatus.
     * IGDB usa: 0=NULL, 1=RELEASED, 2=ALPHA, 3=BETA, 4=EARLY_ACCESS, 5=OFFLINE, 6=CANCELLED, 7=RUMORED, 8=DELISTED
     * Tu enum usa: 0=RELEASED, 1=ALPHA, ..., 7=DELISTED
     */
    public static ReleaseStatus mapFromIgdbValue(Integer igdbStatusValue) {
        if (igdbStatusValue == null) {
            return UNKNOWN;
        }
        switch (igdbStatusValue) {
            case -1: return UNKNOWN; // IGDB's "GAME_STATUS_NULL"
            case 0: return RELEASED;
            case 2: return ALPHA;
            case 3: return BETA;
            case 4: return EARLY_ACCESS;
            case 5: return OFFLINE;
            case 6: return CANCELLED;
            case 7: return RUMORED;
            case 8: return DELISTED;
            default:
                logger.warn("Valor de status de IGDB desconocido recibido: {}", igdbStatusValue);
                return UNKNOWN;
        }
    }

    // Este @JsonCreator es para deserializar si el JSON contiene TUS valores (0-7)
    @JsonCreator
    public static ReleaseStatus fromValue(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (ReleaseStatus status : ReleaseStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        logger.warn("Valor entero desconocido para crear ReleaseStatus directamente: {}", value);
        return UNKNOWN;
    }
}