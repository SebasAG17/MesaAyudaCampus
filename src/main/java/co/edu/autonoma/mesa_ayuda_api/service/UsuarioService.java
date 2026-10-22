package co.edu.autonoma.mesa_ayuda_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.autonoma.mesa_ayuda_api.dto.UsuarioResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    public Optional<UsuarioResponse> buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .map(this::aRespuesta);
    }

    private UsuarioResponse aRespuesta(UsuarioEntity usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo());
    }

}
