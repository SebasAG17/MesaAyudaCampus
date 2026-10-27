package co.edu.autonoma.mesa_ayuda_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.ComentarioEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.HistorialEstadoEntity;
import co.edu.autonoma.mesa_ayuda_api.repository.ComentarioRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.HistorialEstadoRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

@Service
public class HistorialEstadoService {

    private final HistorialEstadoRepository historialEstadoRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioRepository comentarioRepository;

    public HistorialEstadoService(
            HistorialEstadoRepository historialEstadoRepository,
            SolicitudRepository solicitudRepository,
            UsuarioRepository usuarioRepository,
            ComentarioRepository comentarioRepository) {
        this.historialEstadoRepository = historialEstadoRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioRepository = comentarioRepository;
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> listar() {
        return historialEstadoRepository.findAll()
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<HistorialEstadoResponse> buscarPorId(Integer id) {
        return historialEstadoRepository.findById(id)
                .map(this::aRespuesta);
    }

    /** Vacio si la solicitud o el usuario referenciados no existen. */
    @Transactional
    public Optional<HistorialEstadoResponse> crear(HistorialEstadoRequest peticion) {
        Optional<SolicitudEntity> solicitud = solicitudRepository.findById(peticion.solicitudId());
        Optional<UsuarioEntity> usuario = usuarioRepository.findById(peticion.usuarioId());
        if (solicitud.isEmpty() || usuario.isEmpty()) {
            return Optional.empty();
        }

        HistorialEstadoEntity historial = new HistorialEstadoEntity();
        historial.setSolicitud(solicitud.get());
        historial.setEstadoAnterior(peticion.estadoAnterior());
        historial.setEstadoNuevo(peticion.estadoNuevo());
        historial.setUsuario(usuario.get());
        historial.setComentario(buscarComentario(peticion.comentarioId()));
        historial.setOcurridoEn(LocalDateTime.now());
        return Optional.of(aRespuesta(historialEstadoRepository.save(historial)));
    }

    @Transactional
    public Optional<HistorialEstadoResponse> reemplazar(Integer id, HistorialEstadoRequest peticion) {
        Optional<HistorialEstadoEntity> existente = historialEstadoRepository.findById(id);
        Optional<SolicitudEntity> solicitud = solicitudRepository.findById(peticion.solicitudId());
        Optional<UsuarioEntity> usuario = usuarioRepository.findById(peticion.usuarioId());
        if (existente.isEmpty() || solicitud.isEmpty() || usuario.isEmpty()) {
            return Optional.empty();
        }

        HistorialEstadoEntity historial = existente.get();
        historial.setSolicitud(solicitud.get());
        historial.setEstadoAnterior(peticion.estadoAnterior());
        historial.setEstadoNuevo(peticion.estadoNuevo());
        historial.setUsuario(usuario.get());
        historial.setComentario(buscarComentario(peticion.comentarioId()));
        return Optional.of(aRespuesta(historialEstadoRepository.save(historial)));
    }

    @Transactional
    public boolean eliminar(Integer id) {
        if (!historialEstadoRepository.existsById(id)) {
            return false;
        }
        historialEstadoRepository.deleteById(id);
        return true;
    }

    private ComentarioEntity buscarComentario(Integer comentarioId) {
        return comentarioId == null ? null : comentarioRepository.findById(comentarioId).orElse(null);
    }

    private HistorialEstadoResponse aRespuesta(HistorialEstadoEntity historial) {
        return new HistorialEstadoResponse(
                historial.getId(),
                historial.getSolicitud() == null ? null : historial.getSolicitud().getId(),
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getUsuario() == null ? null : historial.getUsuario().getNombre(),
                historial.getComentario() == null ? null : historial.getComentario().getId(),
                historial.getOcurridoEn());
    }

}
