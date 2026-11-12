package co.edu.autonoma.mesa_ayuda_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;
import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.ComentarioEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.HistorialEstadoEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.repository.HistorialEstadoRepository;

@Service
public class HistorialEstadoService {

    private final HistorialEstadoRepository historialEstadoRepository;

    public HistorialEstadoService(HistorialEstadoRepository historialEstadoRepository) {
        this.historialEstadoRepository = historialEstadoRepository;
    }

    @Transactional
    public void registrar(
            SolicitudEntity solicitud,
            EstadoSolicitud anterior,
            EstadoSolicitud nuevo,
            UsuarioEntity usuario,
            ComentarioEntity comentario) {
        HistorialEstadoEntity historial = new HistorialEstadoEntity();
        historial.setSolicitud(solicitud);
        historial.setEstadoAnterior(anterior);
        historial.setEstadoNuevo(nuevo);
        historial.setUsuario(usuario);
        historial.setComentario(comentario);
        historial.setOcurridoEn(LocalDateTime.now());
        historialEstadoRepository.save(historial);
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

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> listarPorSolicitud(Integer solicitudId) {
        return historialEstadoRepository.findBySolicitudIdOrderByOcurridoEnAscIdAsc(solicitudId)
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    private HistorialEstadoResponse aRespuesta(HistorialEstadoEntity historial) {
        return new HistorialEstadoResponse(
                historial.getId(),
                historial.getSolicitud() == null ? null : historial.getSolicitud().getId(),
                historial.getEstadoAnterior() == null ? null : historial.getEstadoAnterior().name(),
                historial.getEstadoNuevo() == null ? null : historial.getEstadoNuevo().name(),
                historial.getUsuario() == null ? null : historial.getUsuario().getNombre(),
                historial.getComentario() == null ? null : historial.getComentario().getId(),
                historial.getOcurridoEn());
    }
}
