package co.edu.autonoma.mesa_ayuda_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.HistorialEstadoEntity;
import co.edu.autonoma.mesa_ayuda_api.repository.HistorialEstadoRepository;

@Service
public class HistorialEstadoService {

    private final HistorialEstadoRepository historialEstadoRepository;

    public HistorialEstadoService(HistorialEstadoRepository historialEstadoRepository) {
        this.historialEstadoRepository = historialEstadoRepository;
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
