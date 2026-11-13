package co.edu.autonoma.mesa_ayuda_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.autonoma.mesa_ayuda_api.domain.Prioridad;
import co.edu.autonoma.mesa_ayuda_api.dto.CategoriaRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.CategoriaResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.CategoriaEntity;
import co.edu.autonoma.mesa_ayuda_api.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    public Optional<CategoriaResponse> buscarPorId(Integer id) {
        return categoriaRepository.findById(id)
                .map(this::aRespuesta);
    }

    public CategoriaResponse crear(CategoriaRequest peticion) {
        validarPrioridadBase(peticion.prioridadBase());
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNombre(peticion.nombre());
        categoria.setPrioridadBase(peticion.prioridadBase());
        categoria.setActiva(peticion.activa() == null || peticion.activa());
        return aRespuesta(categoriaRepository.save(categoria));
    }

    public Optional<CategoriaResponse> reemplazar(Integer id, CategoriaRequest peticion) {
        validarPrioridadBase(peticion.prioridadBase());
        return categoriaRepository.findById(id)
                .map(categoria -> {
                    categoria.setNombre(peticion.nombre());
                    // El cambio solo afecta las solicitudes que se registren o editen después.
                    categoria.setPrioridadBase(peticion.prioridadBase());
                    categoria.setActiva(peticion.activa() == null || peticion.activa());
                    return aRespuesta(categoriaRepository.save(categoria));
                });
    }

    /** Borrado logico: conserva la fila y la marca como inactiva. */
    public boolean desactivar(Integer id) {
        return categoriaRepository.findById(id)
                .map(categoria -> {
                    categoria.setActiva(false);
                    categoriaRepository.save(categoria);
                    return true;
                })
                .orElse(false);
    }

    private CategoriaResponse aRespuesta(CategoriaEntity categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getPrioridadBase() == null ? null : categoria.getPrioridadBase().name(),
                categoria.isActiva());
    }

    private void validarPrioridadBase(Prioridad prioridadBase) {
        if (prioridadBase == null || prioridadBase == Prioridad.CRITICA) {
            throw new IllegalArgumentException("La prioridad base debe ser BAJA, MEDIA o ALTA.");
        }
    }

}
