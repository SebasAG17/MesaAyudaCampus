package co.edu.autonoma.mesa_ayuda_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;
import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;
import co.edu.autonoma.mesa_ayuda_api.domain.Prioridad;
import co.edu.autonoma.mesa_ayuda_api.domain.Rol;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudRequest;
import co.edu.autonoma.mesa_ayuda_api.entities.CategoriaEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.ComentarioEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.exception.AccesoDenegadoException;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;
import co.edu.autonoma.mesa_ayuda_api.repository.CategoriaRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.ComentarioRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

class TransicionEstadoServiceTest {

    private SolicitudRepository solicitudRepository;
    private CategoriaRepository categoriaRepository;
    private UsuarioRepository usuarioRepository;
    private HistorialEstadoService historialEstadoService;
    private ComentarioRepository comentarioRepository;
    private SolicitudService servicio;

    @BeforeEach
    void preparar() {
        solicitudRepository = mock(SolicitudRepository.class);
        categoriaRepository = mock(CategoriaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        historialEstadoService = mock(HistorialEstadoService.class);
        comentarioRepository = mock(ComentarioRepository.class);
        servicio = new SolicitudService(
                solicitudRepository, categoriaRepository, usuarioRepository, historialEstadoService, comentarioRepository);
    }

    @Test
    void crearRegistraEstadoInicial() {
        UsuarioEntity solicitante = usuario(2, Rol.SOLICITANTE, true);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(solicitante));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.crear(new SolicitudRequest("Falla", "Descripción", "Bloque A", 1, Impacto.MEDIO, 2));

        verify(historialEstadoService).registrar(
                any(SolicitudEntity.class), isNull(), eq(EstadoSolicitud.ABIERTA), eq(solicitante), isNull());
    }

    @Test
    void asignacionTransicionaYRegistraHistorial() {
        SolicitudEntity solicitud = solicitud(EstadoSolicitud.ABIERTA);
        UsuarioEntity coordinador = usuario(7, Rol.COORDINADOR, true);
        UsuarioEntity tecnico = usuario(4, Rol.TECNICO, true);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(coordinador));
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(tecnico));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var respuesta = servicio.asignarTecnico(10, 7, 4);

        assertEquals("ASIGNADA", respuesta.estado());
        verify(historialEstadoService).registrar(
                solicitud, EstadoSolicitud.ABIERTA, EstadoSolicitud.ASIGNADA, coordinador, null);
    }

    @Test
    void tecnicoAsignadoAvanzaHastaResueltaConHistorialEnCadaPaso() {
        SolicitudEntity solicitud = solicitud(EstadoSolicitud.ABIERTA);
        UsuarioEntity tecnico = usuario(4, Rol.TECNICO, true);
        solicitud.setTecnico(tecnico);
        solicitud.transicionarA(EstadoSolicitud.ASIGNADA);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(tecnico));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals("EN_PROCESO", servicio.cambiarEstado(10, 4, EstadoSolicitud.EN_PROCESO, null).estado());
        assertEquals("RESUELTA", servicio.cambiarEstado(10, 4, EstadoSolicitud.RESUELTA, null).estado());

        verify(historialEstadoService).registrar(
                solicitud, EstadoSolicitud.ASIGNADA, EstadoSolicitud.EN_PROCESO, tecnico, null);
        verify(historialEstadoService).registrar(
                solicitud, EstadoSolicitud.EN_PROCESO, EstadoSolicitud.RESUELTA, tecnico, null);
    }

    @Test
    void tecnicoNoAsignadoNoPuedeCambiarEstado() {
        SolicitudEntity solicitud = solicitud(EstadoSolicitud.ABIERTA);
        UsuarioEntity tecnicoAsignado = usuario(5, Rol.TECNICO, true);
        solicitud.setTecnico(tecnicoAsignado);
        solicitud.transicionarA(EstadoSolicitud.ASIGNADA);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(usuario(4, Rol.TECNICO, true)));

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.cambiarEstado(10, 4, EstadoSolicitud.EN_PROCESO, null));
        verify(historialEstadoService, never()).registrar(any(), any(), any(), any(), any());
    }

    @Test
    void solicitantePropietarioPuedeCerrarPeroOtroNo() {
        SolicitudEntity solicitud = solicitud(EstadoSolicitud.RESUELTA);
        UsuarioEntity propietario = usuario(2, Rol.SOLICITANTE, true);
        solicitud.setSolicitante(propietario);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(propietario));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals("CERRADA", servicio.cambiarEstado(10, 2, EstadoSolicitud.CERRADA, null).estado());
        verify(historialEstadoService).registrar(
                solicitud, EstadoSolicitud.RESUELTA, EstadoSolicitud.CERRADA, propietario, null);

        solicitud = solicitud(EstadoSolicitud.RESUELTA);
        solicitud.setSolicitante(propietario);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(usuario(3, Rol.SOLICITANTE, true)));
        assertThrows(AccesoDenegadoException.class,
                () -> servicio.cambiarEstado(10, 3, EstadoSolicitud.CERRADA, null));
    }

    @Test
    void solicitantePropietarioPuedeReabrirDesdeResuelta() {
        SolicitudEntity solicitud = solicitud(EstadoSolicitud.RESUELTA);
        UsuarioEntity propietario = solicitud.getSolicitante();
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(propietario));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals("EN_PROCESO", servicio.cambiarEstado(10, 2, EstadoSolicitud.EN_PROCESO, null).estado());
        verify(historialEstadoService).registrar(
                solicitud, EstadoSolicitud.RESUELTA, EstadoSolicitud.EN_PROCESO, propietario, null);
    }

    @Test
    void rechazaComentarioDeOtraSolicitud() {
        SolicitudEntity solicitud = solicitud(EstadoSolicitud.ASIGNADA);
        UsuarioEntity tecnico = usuario(4, Rol.TECNICO, true);
        solicitud.setTecnico(tecnico);
        ComentarioEntity comentario = new ComentarioEntity();
        SolicitudEntity otraSolicitud = solicitud(EstadoSolicitud.ABIERTA);
        otraSolicitud.setId(11);
        comentario.setSolicitud(otraSolicitud);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(tecnico));
        when(comentarioRepository.findById(9)).thenReturn(Optional.of(comentario));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstado(10, 4, EstadoSolicitud.EN_PROCESO, 9));
        verify(historialEstadoService, never()).registrar(any(), any(), any(), any(), any());
    }

    @Test
    void transicionInvalidaNoGuardaHistorial() {
        SolicitudEntity solicitud = solicitud(EstadoSolicitud.ABIERTA);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(usuario(7, Rol.COORDINADOR, true)));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstado(10, 7, EstadoSolicitud.CERRADA, null));
        verify(historialEstadoService, never()).registrar(any(), any(), any(), any(), any());
    }

    @Test
    void putNoEditaSolicitudesResueltasNiCerradas() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.RESUELTA)));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.reemplazar(10,
                        new SolicitudRequest("Falla editada", "Descripción", "Bloque B", 1, Impacto.ALTO, 2)));
        verify(solicitudRepository, never()).save(any(SolicitudEntity.class));
    }

    private SolicitudEntity solicitud(EstadoSolicitud estado) {
        SolicitudEntity solicitud = new SolicitudEntity();
        solicitud.iniciar();
        solicitud.setId(10);
        if (estado != EstadoSolicitud.ABIERTA) {
            EstadoSolicitud actual = EstadoSolicitud.ABIERTA;
            while (actual != estado) {
                EstadoSolicitud siguiente = switch (actual) {
                    case ABIERTA -> EstadoSolicitud.ASIGNADA;
                    case ASIGNADA -> EstadoSolicitud.EN_PROCESO;
                    case EN_PROCESO -> EstadoSolicitud.RESUELTA;
                    case RESUELTA -> EstadoSolicitud.CERRADA;
                    case CERRADA -> throw new IllegalArgumentException();
                };
                solicitud.transicionarA(siguiente);
                actual = siguiente;
            }
        }
        solicitud.setTitulo("Falla");
        solicitud.setDescripcion("Descripción");
        solicitud.setUbicacion("Bloque A");
        solicitud.setCategoria(categoria());
        solicitud.setImpacto(Impacto.MEDIO);
        solicitud.setPrioridad(Prioridad.MEDIA);
        solicitud.setSolicitante(usuario(2, Rol.SOLICITANTE, true));
        return solicitud;
    }

    private CategoriaEntity categoria() {
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNombre("Electricidad");
        categoria.setPrioridadBase(Prioridad.MEDIA);
        categoria.setActiva(true);
        return categoria;
    }

    private UsuarioEntity usuario(Integer id, Rol rol, boolean activo) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(id);
        usuario.setNombre("Usuario " + id);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        return usuario;
    }
}
