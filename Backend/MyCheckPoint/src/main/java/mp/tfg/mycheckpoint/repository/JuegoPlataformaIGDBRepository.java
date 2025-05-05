package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.junction.JuegoPlataformaIGDB;
import mp.tfg.mycheckpoint.entity.embedded.JuegoPlataformaIGDBId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JuegoPlataformaIGDBRepository extends JpaRepository<JuegoPlataformaIGDB, JuegoPlataformaIGDBId> {

    // Buscar todas las plataformas IGDB para un juego específico
    List<JuegoPlataformaIGDB> findById_JuegoId(Long juegoId);

    // Buscar todos los juegos para una plataforma IGDB específica
    List<JuegoPlataformaIGDB> findById_PlataformaIgdbId(Long plataformaIgdbId);
}
