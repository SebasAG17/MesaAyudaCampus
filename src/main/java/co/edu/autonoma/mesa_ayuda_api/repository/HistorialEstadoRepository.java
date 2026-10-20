package co.edu.autonoma.mesa_ayuda_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.autonoma.mesa_ayuda_api.entities.HistorialEstadoEntity;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstadoEntity, Integer> {

}
