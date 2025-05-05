package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Plataforma; // Nuestra entidad Plataforma
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlataformaRepository extends JpaRepository<Plataforma, Long> {

    // Buscar por el ID externo (si es relevante buscar por él)
    Optional<Plataforma> findByIdigdb(Integer idigdb);

    // Buscar por nombre (útil para evitar duplicados o encontrar existentes)
    Optional<Plataforma> findByNombreIgnoreCase(String nombre);
}