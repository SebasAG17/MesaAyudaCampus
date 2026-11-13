package co.edu.autonoma.mesa_ayuda_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.mesa_ayuda_api.domain.CalculadoraPrioridad;
import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;
import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;
import co.edu.autonoma.mesa_ayuda_api.domain.ReglasEstadoSolicitud;
import co.edu.autonoma.mesa_ayuda_api.domain.Rol;
import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoResponse;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.CategoriaEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.ComentarioEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;
import co.edu.autonoma.mesa_ayuda_api.exception.AccesoDenegadoException;
import co.edu.autonoma.mesa_ayuda_api.exception.RecursoNoEncontradoException;
import co.edu.autonoma.mesa_ayuda_api.repository.CategoriaRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.ComentarioRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialEstadoService historialEstadoService;
    private final ComentarioRepository comentarioRepository;

    public SolicitudService(
            SolicitudRepository solicitudRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository,
            HistorialEstadoService historialEstadoService,
            ComentarioRepository comentarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialEstadoService = historialEstadoService;
        this.comentarioRepository = comentarioRepository;
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
        solicitud.setSolicitante(solicitante.get());
        solicitud.setCreadaEn(LocalDateTime.now());
        solicitud.iniciar();
        SolicitudEntity creada = solicitudRepository.save(solicitud);
        historialEstadoService.registrar(creada, null, EstadoSolicitud.ABIERTA, solicitante.get(), null);
        return Optional.of(aRespuesta(creada));
    }

    @Transactional
    public Optional<SolicitudResponse> reemplazar(Integer id, SolicitudRequest peticion) {
        Optional<SolicitudEntity> existente = solicitudRepository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }
        SolicitudEntity solicitud = existente.get();
        if (solicitud.getEstado() == EstadoSolicitud.RESUELTA || solicitud.getEstado() == EstadoSolicitud.CERRADA) {
            throw new ReglaNegocioException("No se puede editar una solicitud resuelta o cerrada.");
        }
        Optional<CategoriaEntity> categoria = categoriaRepository.findById(peticion.categoriaId());
        if (categoria.isEmpty()) {
            return Optional.empty();
        }
        validarCategoriaYImpacto(categoria.get(), peticion.impacto());

        solicitud.setTitulo(peticion.titulo());
        solicitud.setDescripcion(peticion.descripcion());
        solicitud.setUbicacion(peticion.ubicacion());
        solicitud.setCategoria(categoria.get());
        solicitud.setImpacto(peticion.impacto());
        solicitud.setPrioridad(CalculadoraPrioridad.calcular(categoria.get().getPrioridadBase(), peticion.impacto()));
        return Optional.of(aRespuesta(solicitudRepository.save(solicitud)));
    }

    @Transactional
    public SolicitudResponse asignarTecnico(Integer solicitudId, Integer coordinadorId, Integer tecnicoId) {
        SolicitudEntity solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud indicada."));
        UsuarioEntity coordinador = usuarioRepository.findById(coordinadorId)
                .filter(UsuarioEntity::isActivo)
                .filter(usuario -> usuario.getRol() == Rol.COORDINADOR)
                .orElseThrow(() -> new AccesoDenegadoException("Se requiere un coordinador activo para asignar técnicos."));
        if (tecnicoId == null) {
            throw new IllegalArgumentException("El campo tecnicoId es obligatorio.");
        }
        UsuarioEntity tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new ReglaNegocioException("No existe el técnico indicado."));
        if (!tecnico.isActivo() || tecnico.getRol() == null || !tecnico.getRol().puedeSerAsignado()) {
            throw new ReglaNegocioException("El usuario asignado debe ser un técnico activo.");
        }
        if (solicitud.getEstado() != EstadoSolicitud.ABIERTA
                && solicitud.getEstado() != EstadoSolicitud.ASIGNADA) {
            throw new ReglaNegocioException("Solo se puede asignar una solicitud abierta o ya asignada.");
        }

        solicitud.setTecnico(tecnico);
        if (solicitud.getEstado() == EstadoSolicitud.ABIERTA) {
            EstadoSolicitud estadoAnterior = solicitud.transicionarA(EstadoSolicitud.ASIGNADA);
            historialEstadoService.registrar(solicitud, estadoAnterior, solicitud.getEstado(), coordinador, null);
        }
        return aRespuesta(solicitudRepository.save(solicitud));
    }

    @Transactional
    public SolicitudResponse cambiarEstado(
            Integer solicitudId,
            Integer usuarioId,
            EstadoSolicitud destino,
            Integer comentarioId) {
        SolicitudEntity solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud indicada."));
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .filter(UsuarioEntity::isActivo)
                .orElseThrow(() -> new AccesoDenegadoException("El usuario no existe o está inactivo."));
        if (destino == null) {
            throw new IllegalArgumentException("El estado destino es obligatorio.");
        }
        if (destino == EstadoSolicitud.ASIGNADA) {
            throw new ReglaNegocioException("Use /api/solicitudes/{id}/asignacion para asignar la solicitud.");
        }

        EstadoSolicitud anterior = solicitud.getEstado();
        if (anterior == null || !anterior.puedePasarA(destino)) {
            solicitud.transicionarA(destino);
        }
        if (!ReglasEstadoSolicitud.puedeCambiar(
                anterior,
                destino,
                usuario.getRol(),
                usuarioId,
                solicitud.getTecnico() == null ? null : solicitud.getTecnico().getId(),
                solicitud.getSolicitante() == null ? null : solicitud.getSolicitante().getId())) {
            throw new AccesoDenegadoException("El usuario no tiene permiso para realizar esta transición.");
        }

        ComentarioEntity comentario = buscarComentarioDeSolicitud(comentarioId, solicitud);
        anterior = solicitud.transicionarA(destino);
        historialEstadoService.registrar(solicitud, anterior, destino, usuario, comentario);
        return aRespuesta(solicitudRepository.save(solicitud));
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> listarHistorial(Integer solicitudId) {
        if (!solicitudRepository.existsById(solicitudId)) {
            throw new RecursoNoEncontradoException("No existe la solicitud indicada.");
        }
        return historialEstadoService.listarPorSolicitud(solicitudId);
    }

    private ComentarioEntity buscarComentarioDeSolicitud(Integer comentarioId, SolicitudEntity solicitud) {
        if (comentarioId == null) {
            return null;
        }
        ComentarioEntity comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ReglaNegocioException("No existe el comentario indicado."));
        if (comentario.getSolicitud() == null
                || !solicitud.getId().equals(comentario.getSolicitud().getId())) {
            throw new ReglaNegocioException("El comentario no pertenece a la solicitud indicada.");
        }
        return comentario;
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
                solicitud.getEstado() == null ? null : solicitud.getEstado().name(),
                solicitud.getSolicitante() == null ? null : solicitud.getSolicitante().getNombre(),
                solicitud.getTecnico() == null ? null : solicitud.getTecnico().getNombre(),
                solicitud.getCreadaEn(),
                solicitud.getCerradaEn());
    }

}
