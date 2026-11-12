package co.edu.autonoma.mesa_ayuda_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;
import co.edu.autonoma.mesa_ayuda_api.domain.Prioridad;
import co.edu.autonoma.mesa_ayuda_api.domain.Rol;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudRequest;
import co.edu.autonoma.mesa_ayuda_api.entities.CategoriaEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.HistorialEstadoEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.exception.AccesoDenegadoException;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;
import co.edu.autonoma.mesa_ayuda_api.repository.CategoriaRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.HistorialEstadoRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

class SolicitudServiceTest {

    private SolicitudRepository solicitudRepository;
    private CategoriaRepository categoriaRepository;
    private UsuarioRepository usuarioRepository;
    private HistorialEstadoRepository historialEstadoRepository;
    private SolicitudService servicio;

    @BeforeEach
    void preparar() {
        solicitudRepository = mock(SolicitudRepository.class);
        categoriaRepository = mock(CategoriaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        historialEstadoRepository = mock(HistorialEstadoRepository.class);
        servicio = new SolicitudService(
                solicitudRepository, categoriaRepository, usuarioRepository, historialEstadoRepository);
    }

    @Test
    void guardaLaPrioridadCalculada() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoriaActiva()));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario("Ana", Rol.SOLICITANTE, true)));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.crear(peticion(Impacto.ALTO));

        ArgumentCaptor<SolicitudEntity> capturador = ArgumentCaptor.forClass(SolicitudEntity.class);
        verify(solicitudRepository).save(capturador.capture());
        assertEquals(Prioridad.CRITICA, capturador.getValue().getPrioridad());
        assertNull(capturador.getValue().getTecnico());
    }

    @Test
    void rechazaCategoriaInactiva() {
        CategoriaEntity categoria = categoriaActiva();
        categoria.setActiva(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuario("Ana", Rol.SOLICITANTE, true)));

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(peticion(Impacto.ALTO)));
        verify(solicitudRepository, never()).save(any(SolicitudEntity.class));
    }

    @Test
    void asignaTecnicoYGuardaHistorialAlAbrirAsignacion() {
        SolicitudEntity solicitud = solicitud("ABIERTA");
        UsuarioEntity coordinador = usuario("Carlos", Rol.COORDINADOR, true);
        UsuarioEntity tecnico = usuario("Tomás", Rol.TECNICO, true);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(coordinador));
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(tecnico));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var respuesta = servicio.asignarTecnico(10, 3, 4);

        assertEquals("ASIGNADA", respuesta.estado());
        assertEquals("Tomás", respuesta.tecnico());
        ArgumentCaptor<HistorialEstadoEntity> capturador = ArgumentCaptor.forClass(HistorialEstadoEntity.class);
        verify(historialEstadoRepository).save(capturador.capture());
        assertEquals("ABIERTA", capturador.getValue().getEstadoAnterior());
        assertEquals("ASIGNADA", capturador.getValue().getEstadoNuevo());
        assertEquals(coordinador, capturador.getValue().getUsuario());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void reasignaSinGuardarHistorialCuandoYaEstaAsignada() {
        SolicitudEntity solicitud = solicitud("ASIGNADA");
        UsuarioEntity tecnico = usuario("Nuevo técnico", Rol.TECNICO, true);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(usuario("Carlos", Rol.COORDINADOR, true)));
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(tecnico));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var respuesta = servicio.asignarTecnico(10, 3, 4);

        assertEquals("ASIGNADA", respuesta.estado());
        assertEquals("Nuevo técnico", respuesta.tecnico());
        verify(historialEstadoRepository, never()).save(any(HistorialEstadoEntity.class));
    }

    @Test
    void rechazaQuienNoEsCoordinador() {
        for (Rol rol : new Rol[] { Rol.SOLICITANTE, Rol.TECNICO }) {
            when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud("ABIERTA")));
            when(usuarioRepository.findById(3)).thenReturn(Optional.of(usuario("Actor", rol, true)));

            assertThrows(AccesoDenegadoException.class, () -> servicio.asignarTecnico(10, 3, 4));
        }
    }

    @Test
    void rechazaActorInexistenteOInactivo() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud("ABIERTA")));
        when(usuarioRepository.findById(3)).thenReturn(Optional.empty());
        assertThrows(AccesoDenegadoException.class, () -> servicio.asignarTecnico(10, 3, 4));

        when(usuarioRepository.findById(3)).thenReturn(Optional.of(usuario("Carlos", Rol.COORDINADOR, false)));
        assertThrows(AccesoDenegadoException.class, () -> servicio.asignarTecnico(10, 3, 4));
    }

    @Test
    void rechazaTecnicoAusente() {
        prepararSolicitudYCoordinador();
        assertThrows(IllegalArgumentException.class, () -> servicio.asignarTecnico(10, 3, null));
    }

    @Test
    void rechazaTecnicoConRolIncorrectoOInactivo() {
        for (UsuarioEntity usuario : new UsuarioEntity[] {
                usuario("Solicitante", Rol.SOLICITANTE, true),
                usuario("Coordinador", Rol.COORDINADOR, true),
                usuario("Técnico inactivo", Rol.TECNICO, false) }) {
            prepararSolicitudYCoordinador();
            when(usuarioRepository.findById(4)).thenReturn(Optional.of(usuario));

            assertThrows(ReglaNegocioException.class, () -> servicio.asignarTecnico(10, 3, 4));
        }
    }

    @Test
    void rechazaTecnicoInexistente() {
        prepararSolicitudYCoordinador();
        when(usuarioRepository.findById(4)).thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class, () -> servicio.asignarTecnico(10, 3, 4));
    }

    @Test
    void rechazaSolicitudFueraDeEstadosPermitidos() {
        for (String estado : new String[] { "EN_PROCESO", "RESUELTA", "CERRADA" }) {
            prepararSolicitudYCoordinador();
            when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(estado)));
            when(usuarioRepository.findById(4)).thenReturn(Optional.of(usuario("Tomás", Rol.TECNICO, true)));

            assertThrows(ReglaNegocioException.class, () -> servicio.asignarTecnico(10, 3, 4));
        }
    }

    private void prepararSolicitudYCoordinador() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud("ABIERTA")));
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(usuario("Carlos", Rol.COORDINADOR, true)));
    }

    private CategoriaEntity categoriaActiva() {
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1);
        categoria.setNombre("Electricidad");
        categoria.setPrioridadBase(Prioridad.ALTA);
        categoria.setActiva(true);
        return categoria;
    }

    private SolicitudEntity solicitud(String estado) {
        SolicitudEntity solicitud = new SolicitudEntity();
        solicitud.setId(10);
        solicitud.setTitulo("Falla");
        solicitud.setDescripcion("Descripción");
        solicitud.setUbicacion("Bloque A");
        solicitud.setCategoria(categoriaActiva());
        solicitud.setImpacto(Impacto.MEDIO);
        solicitud.setPrioridad(Prioridad.ALTA);
        solicitud.setEstado(estado);
        solicitud.setSolicitante(usuario("Ana", Rol.SOLICITANTE, true));
        return solicitud;
    }

    private UsuarioEntity usuario(String nombre, Rol rol, boolean activo) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre(nombre);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        return usuario;
    }

    private SolicitudRequest peticion(Impacto impacto) {
        return new SolicitudRequest("Falla", "Descripción", "Bloque A", 1, impacto, "ABIERTA", 2);
    }
}
