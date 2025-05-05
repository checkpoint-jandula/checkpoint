package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.junction.JuegoRelacion;
import mp.tfg.mycheckpoint.entity.embedded.JuegoRelacionId;
import mp.tfg.mycheckpoint.entity.enums.GameTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JuegoRelacionRepository extends JpaRepository<JuegoRelacion, JuegoRelacionId> {

    // Buscar todos los DLCs/Expansiones/Similares de un juego origen
    List<JuegoRelacion> findById_JuegoOrigenId(Long juegoOrigenId);
    List<JuegoRelacion> findById_JuegoOrigenIdAndId_TipoRelacion(Long juegoOrigenId, GameTypeEnum tipoRelacion);

    // Buscar todos los juegos base/similares de los que este juego es DLC/Expansion/Similar
    List<JuegoRelacion> findById_JuegoDestinoId(Long juegoDestinoId);
    List<JuegoRelacion> findById_JuegoDestinoIdAndId_TipoRelacion(Long juegoDestinoId, GameTypeEnum tipoRelacion);
}
