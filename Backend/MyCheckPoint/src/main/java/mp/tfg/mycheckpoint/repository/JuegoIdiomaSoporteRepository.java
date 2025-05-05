package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.junction.JuegoIdiomaSoporte;
import mp.tfg.mycheckpoint.entity.embedded.JuegoIdiomaSoporteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JuegoIdiomaSoporteRepository extends JpaRepository<JuegoIdiomaSoporte, JuegoIdiomaSoporteId> {

    // Buscar todos los soportes de idioma para un juego
    List<JuegoIdiomaSoporte> findById_JuegoId(Long juegoId);

    // Buscar todos los juegos que soportan un idioma específico (y opcionalmente un tipo)
    List<JuegoIdiomaSoporte> findById_IdiomaId(Long idiomaId);
    List<JuegoIdiomaSoporte> findById_IdiomaIdAndId_TipoSoporte(Long idiomaId, Integer tipoSoporte);
}
