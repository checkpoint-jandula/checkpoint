package mp.tfg.mycheckpoint.repository;

import mp.tfg.mycheckpoint.entity.junction.ListaJuego;
import mp.tfg.mycheckpoint.entity.embedded.ListaJuegoId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListaJuegoRepository extends JpaRepository<ListaJuego, ListaJuegoId> { // ID es ListaJuegoId

    // Buscar todos los juegos de una lista (con paginación)
    Page<ListaJuego> findById_ListaId(Long listaId, Pageable pageable);
    List<ListaJuego> findById_ListaId(Long listaId); // Sin paginación


    // Buscar todas las listas que contienen un juego
    List<ListaJuego> findById_JuegoId(Long juegoId);

    // Contar juegos en una lista
    long countById_ListaId(Long listaId);
}
