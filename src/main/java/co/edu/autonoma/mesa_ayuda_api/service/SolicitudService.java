package co.edu.autonoma.mesa_ayuda_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;

    public SolicitudService(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public List<SolicitudResponse> listar() {
        return solicitudRepository.findAll()
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    public Optional<SolicitudResponse> buscarPorId(Integer id) {
        return solicitudRepository.findById(id)
                .map(this::aRespuesta);
    }

    private SolicitudResponse aRespuesta(SolicitudEntity solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getTitulo(),
                solicitud.getDescripcion(),
                solicitud.getUbicacion(),
                solicitud.getCategoria() == null ? null : solicitud.getCategoria().getNombre(),
                solicitud.getImpacto(),
                solicitud.getPrioridad(),
                solicitud.getEstado(),
                solicitud.getSolicitante() == null ? null : solicitud.getSolicitante().getNombre(),
                solicitud.getTecnico() == null ? null : solicitud.getTecnico().getNombre(),
                solicitud.getCreadaEn(),
                solicitud.getCerradaEn());
    }

}
