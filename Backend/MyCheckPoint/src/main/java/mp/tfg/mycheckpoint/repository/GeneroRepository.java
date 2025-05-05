package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneroRepository extends JpaRepository<Genero, Long> {
    Optional<Genero> findByIdigdb(Long idigdb);
    Optional<Genero> findByNombreIgnoreCase(String nombre);
}
