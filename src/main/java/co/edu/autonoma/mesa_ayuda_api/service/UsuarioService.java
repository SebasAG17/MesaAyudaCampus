package co.edu.autonoma.mesa_ayuda_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.autonoma.mesa_ayuda_api.dto.UsuarioRequest;
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

    public UsuarioResponse crear(UsuarioRequest peticion) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre(peticion.nombre());
        usuario.setCorreo(peticion.correo());
        usuario.setRol(peticion.rol());
        usuario.setActivo(peticion.activo() == null || peticion.activo());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public Optional<UsuarioResponse> reemplazar(Integer id, UsuarioRequest peticion) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombre(peticion.nombre());
                    usuario.setCorreo(peticion.correo());
                    usuario.setRol(peticion.rol());
                    usuario.setActivo(peticion.activo() == null || peticion.activo());
                    return aRespuesta(usuarioRepository.save(usuario));
                });
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
