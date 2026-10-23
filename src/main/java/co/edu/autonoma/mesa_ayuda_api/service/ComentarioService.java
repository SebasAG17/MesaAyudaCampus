package co.edu.autonoma.mesa_ayuda_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.mesa_ayuda_api.dto.ComentarioRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.ComentarioResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.ComentarioEntity;
import co.edu.autonoma.mesa_ayuda_api.repository.ComentarioRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            SolicitudRepository solicitudRepository,
            UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listar() {
        return comentarioRepository.findAll()
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ComentarioResponse> buscarPorId(Integer id) {
        return comentarioRepository.findById(id)
                .map(this::aRespuesta);
    }

    /** Vacio si la solicitud o el autor referenciados no existen. */
    @Transactional
    public Optional<ComentarioResponse> crear(ComentarioRequest peticion) {
        Optional<SolicitudEntity> solicitud = solicitudRepository.findById(peticion.solicitudId());
        Optional<UsuarioEntity> autor = usuarioRepository.findById(peticion.autorId());
        if (solicitud.isEmpty() || autor.isEmpty()) {
            return Optional.empty();
        }

        ComentarioEntity comentario = new ComentarioEntity();
        comentario.setSolicitud(solicitud.get());
        comentario.setAutor(autor.get());
        comentario.setTexto(peticion.texto());
        comentario.setInterno(Boolean.TRUE.equals(peticion.interno()));
        comentario.setEsSolucion(Boolean.TRUE.equals(peticion.esSolucion()));
        comentario.setCreadoEn(LocalDateTime.now());
        return Optional.of(aRespuesta(comentarioRepository.save(comentario)));
    }

    private ComentarioResponse aRespuesta(ComentarioEntity comentario) {
        return new ComentarioResponse(
                comentario.getId(),
                comentario.getSolicitud() == null ? null : comentario.getSolicitud().getId(),
                comentario.getAutor() == null ? null : comentario.getAutor().getNombre(),
                comentario.getTexto(),
                comentario.isInterno(),
                comentario.isEsSolucion(),
                comentario.getCreadoEn());
    }

}
