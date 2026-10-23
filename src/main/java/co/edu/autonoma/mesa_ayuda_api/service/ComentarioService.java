package co.edu.autonoma.mesa_ayuda_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.mesa_ayuda_api.dto.ComentarioResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.ComentarioEntity;
import co.edu.autonoma.mesa_ayuda_api.repository.ComentarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository) {
        this.comentarioRepository = comentarioRepository;
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
