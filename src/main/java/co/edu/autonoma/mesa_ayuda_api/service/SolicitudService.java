package co.edu.autonoma.mesa_ayuda_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.mesa_ayuda_api.domain.CalculadoraPrioridad;
import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.CategoriaEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;
import co.edu.autonoma.mesa_ayuda_api.repository.CategoriaRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar() {
        return solicitudRepository.findAll()
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<SolicitudResponse> buscarPorId(Integer id) {
        return solicitudRepository.findById(id)
                .map(this::aRespuesta);
    }

    /** Vacio si la categoria o el solicitante referenciados no existen. */
    @Transactional
    public Optional<SolicitudResponse> crear(SolicitudRequest peticion) {
        Optional<CategoriaEntity> categoria = categoriaRepository.findById(peticion.categoriaId());
        Optional<UsuarioEntity> solicitante = usuarioRepository.findById(peticion.solicitanteId());
        if (categoria.isEmpty() || solicitante.isEmpty()) {
            return Optional.empty();
        }
        validarCategoriaYImpacto(categoria.get(), peticion.impacto());

        SolicitudEntity solicitud = new SolicitudEntity();
        solicitud.setTitulo(peticion.titulo());
        solicitud.setDescripcion(peticion.descripcion());
        solicitud.setUbicacion(peticion.ubicacion());
        solicitud.setCategoria(categoria.get());
        solicitud.setImpacto(peticion.impacto());
        solicitud.setPrioridad(CalculadoraPrioridad.calcular(categoria.get().getPrioridadBase(), peticion.impacto()));
        solicitud.setEstado(peticion.estado() == null ? "ABIERTA" : peticion.estado());
        solicitud.setSolicitante(solicitante.get());
        solicitud.setTecnico(buscarTecnico(peticion.tecnicoId()));
        solicitud.setCreadaEn(LocalDateTime.now());
        return Optional.of(aRespuesta(solicitudRepository.save(solicitud)));
    }

    @Transactional
    public Optional<SolicitudResponse> reemplazar(Integer id, SolicitudRequest peticion) {
        Optional<SolicitudEntity> existente = solicitudRepository.findById(id);
        Optional<CategoriaEntity> categoria = categoriaRepository.findById(peticion.categoriaId());
        Optional<UsuarioEntity> solicitante = usuarioRepository.findById(peticion.solicitanteId());
        if (existente.isEmpty() || categoria.isEmpty() || solicitante.isEmpty()) {
            return Optional.empty();
        }
        validarCategoriaYImpacto(categoria.get(), peticion.impacto());

        SolicitudEntity solicitud = existente.get();
        solicitud.setTitulo(peticion.titulo());
        solicitud.setDescripcion(peticion.descripcion());
        solicitud.setUbicacion(peticion.ubicacion());
        solicitud.setCategoria(categoria.get());
        solicitud.setImpacto(peticion.impacto());
        solicitud.setPrioridad(CalculadoraPrioridad.calcular(categoria.get().getPrioridadBase(), peticion.impacto()));
        solicitud.setEstado(peticion.estado() == null ? solicitud.getEstado() : peticion.estado());
        solicitud.setSolicitante(solicitante.get());
        solicitud.setTecnico(buscarTecnico(peticion.tecnicoId()));
        return Optional.of(aRespuesta(solicitudRepository.save(solicitud)));
    }

    /** Borrado fisico: la solicitud no tiene bandera de activo. */
    @Transactional
    public boolean eliminar(Integer id) {
        if (!solicitudRepository.existsById(id)) {
            return false;
        }
        solicitudRepository.deleteById(id);
        return true;
    }

    private UsuarioEntity buscarTecnico(Integer tecnicoId) {
        return tecnicoId == null ? null : usuarioRepository.findById(tecnicoId).orElse(null);
    }

    private void validarCategoriaYImpacto(CategoriaEntity categoria, Impacto impacto) {
        if (!categoria.isActiva()) {
            throw new ReglaNegocioException("No se puede registrar una solicitud con una categoría inactiva.");
        }
        if (impacto == null) {
            throw new IllegalArgumentException("El impacto es obligatorio: BAJO, MEDIO o ALTO.");
        }
    }

    private SolicitudResponse aRespuesta(SolicitudEntity solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getTitulo(),
                solicitud.getDescripcion(),
                solicitud.getUbicacion(),
                solicitud.getCategoria() == null ? null : solicitud.getCategoria().getNombre(),
                solicitud.getImpacto() == null ? null : solicitud.getImpacto().name(),
                solicitud.getPrioridad() == null ? null : solicitud.getPrioridad().name(),
                solicitud.getEstado(),
                solicitud.getSolicitante() == null ? null : solicitud.getSolicitante().getNombre(),
                solicitud.getTecnico() == null ? null : solicitud.getTecnico().getNombre(),
                solicitud.getCreadaEn(),
                solicitud.getCerradaEn());
    }

}
