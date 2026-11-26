package co.edu.autonoma.mesa_ayuda_api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;
import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;
import co.edu.autonoma.mesa_ayuda_api.domain.Prioridad;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudResponse;
import co.edu.autonoma.mesa_ayuda_api.entities.CategoriaEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;
import co.edu.autonoma.mesa_ayuda_api.repository.CategoriaRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.ComentarioRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistorialEstadoService historialEstadoService;

    @Mock
    private ComentarioRepository comentarioRepository;

    @InjectMocks
    private SolicitudService service;

    @Test
    void buscarPorId_existente_devuelveLaSolicitud() {
        // Arrange
        SolicitudEntity solicitud = new SolicitudEntity();
        solicitud.setTitulo("Tomacorriente sin energía");
        solicitud.setPrioridad(Prioridad.ALTA);
        solicitud.iniciar(); // queda ABIERTA
        when(repository.findById(3)).thenReturn(Optional.of(solicitud));

        // Act
        Optional<SolicitudResponse> resultado = service.buscarPorId(3);

        // Assert
        assertThat(resultado).isPresent();
        SolicitudResponse respuesta = resultado.get();
        assertThat(respuesta.titulo()).isEqualTo("Tomacorriente sin energía");
        assertThat(respuesta.estado()).isEqualTo("ABIERTA");
        assertThat(respuesta.prioridad()).isEqualTo("ALTA");
    }

    @Test
    void buscarPorId_inexistente_devuelveVacio() {
        // Arrange
        when(repository.findById(99)).thenReturn(Optional.empty());

        // Act
        Optional<SolicitudResponse> resultado = service.buscarPorId(99);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    void crear_solicitudNueva_quedaAbiertaConFechaActualYPrioridadCalculada() {
        // Arrange
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNombre("Electricidad");
        categoria.setPrioridadBase(Prioridad.ALTA);
        categoria.setActiva(true);
        UsuarioEntity solicitante = new UsuarioEntity();
        solicitante.setNombre("Ana");
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(solicitante));
        // save devuelve el mismo objeto que recibe, como haría la base
        when(repository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        SolicitudRequest peticion = new SolicitudRequest(
                "Tomacorriente sin energía", "No hay energía en el aula 204.", "Bloque A - Aula 204",
                1, Impacto.ALTO, 2);

        // Act
        Optional<SolicitudResponse> resultado = service.crear(peticion);

        // Assert
        assertThat(resultado).isPresent();
        SolicitudResponse respuesta = resultado.get();
        assertThat(respuesta.titulo()).isEqualTo("Tomacorriente sin energía");
        assertThat(respuesta.estado()).isEqualTo("ABIERTA");
        assertThat(respuesta.prioridad()).isEqualTo("CRITICA");
        assertThat(respuesta.creadaEn())
                .isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void reemplazar_existente_guardaLaSolicitudEncontrada() {
        // Arrange
        SolicitudEntity solicitud = new SolicitudEntity();
        solicitud.iniciar(); // ABIERTA: todavía se puede editar
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setNombre("Electricidad");
        categoria.setPrioridadBase(Prioridad.ALTA);
        categoria.setActiva(true);
        when(repository.findById(3)).thenReturn(Optional.of(solicitud));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(repository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        SolicitudRequest peticion = new SolicitudRequest(
                "Tomacorriente dañado", "Sale humo del tomacorriente.", "Bloque A - Aula 204",
                1, Impacto.MEDIO, 2);

        // Act
        service.reemplazar(3, peticion);

        // Assert
        verify(repository).save(solicitud);
    }

    @Test
    void reemplazar_solicitudResuelta_noGuardaNada() {
        // Arrange: la solicitud recorre su ciclo hasta RESUELTA
        SolicitudEntity solicitud = new SolicitudEntity();
        solicitud.iniciar();
        solicitud.transicionarA(EstadoSolicitud.ASIGNADA);
        solicitud.transicionarA(EstadoSolicitud.EN_PROCESO);
        solicitud.transicionarA(EstadoSolicitud.RESUELTA);
        when(repository.findById(3)).thenReturn(Optional.of(solicitud));
        SolicitudRequest peticion = new SolicitudRequest(
                "Tomacorriente dañado", "Sale humo del tomacorriente.", "Bloque A - Aula 204",
                1, Impacto.MEDIO, 2);

        // Act
        assertThrows(ReglaNegocioException.class, () -> service.reemplazar(3, peticion));

        // Assert
        verify(repository, never()).save(any());
    }
}
