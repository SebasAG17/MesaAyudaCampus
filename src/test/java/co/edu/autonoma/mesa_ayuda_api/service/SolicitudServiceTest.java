package co.edu.autonoma.mesa_ayuda_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;
import co.edu.autonoma.mesa_ayuda_api.domain.Prioridad;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudRequest;
import co.edu.autonoma.mesa_ayuda_api.entities.CategoriaEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.SolicitudEntity;
import co.edu.autonoma.mesa_ayuda_api.entities.UsuarioEntity;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;
import co.edu.autonoma.mesa_ayuda_api.repository.CategoriaRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.SolicitudRepository;
import co.edu.autonoma.mesa_ayuda_api.repository.UsuarioRepository;

class SolicitudServiceTest {

    private SolicitudRepository solicitudRepository;
    private CategoriaRepository categoriaRepository;
    private UsuarioRepository usuarioRepository;
    private SolicitudService servicio;

    @BeforeEach
    void preparar() {
        solicitudRepository = mock(SolicitudRepository.class);
        categoriaRepository = mock(CategoriaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        servicio = new SolicitudService(solicitudRepository, categoriaRepository, usuarioRepository);
    }

    @Test
    void guardaLaPrioridadCalculada() {
        CategoriaEntity categoria = categoriaActiva();
        UsuarioEntity solicitante = new UsuarioEntity();
        solicitante.setNombre("Ana");
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(solicitante));
        when(solicitudRepository.save(any(SolicitudEntity.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.crear(peticion(Impacto.ALTO));

        var capturador = org.mockito.ArgumentCaptor.forClass(SolicitudEntity.class);
        verify(solicitudRepository).save(capturador.capture());
        assertEquals(Prioridad.CRITICA, capturador.getValue().getPrioridad());
    }

    @Test
    void rechazaCategoriaInactiva() {
        CategoriaEntity categoria = categoriaActiva();
        categoria.setActiva(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(new UsuarioEntity()));

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(peticion(Impacto.ALTO)));
        verify(solicitudRepository, never()).save(any(SolicitudEntity.class));
    }

    private CategoriaEntity categoriaActiva() {
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(1);
        categoria.setNombre("Electricidad");
        categoria.setPrioridadBase(Prioridad.ALTA);
        categoria.setActiva(true);
        return categoria;
    }

    private SolicitudRequest peticion(Impacto impacto) {
        return new SolicitudRequest("Falla", "Descripción", "Bloque A", 1, impacto,
                "ABIERTA", 2, null);
    }
}
