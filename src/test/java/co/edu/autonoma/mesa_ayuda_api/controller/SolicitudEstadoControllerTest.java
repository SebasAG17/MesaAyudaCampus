package co.edu.autonoma.mesa_ayuda_api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoResponse;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudResponse;
import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;
import co.edu.autonoma.mesa_ayuda_api.exception.AccesoDenegadoException;
import co.edu.autonoma.mesa_ayuda_api.exception.RecursoNoEncontradoException;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;
import co.edu.autonoma.mesa_ayuda_api.service.SolicitudService;

@WebMvcTest(SolicitudController.class)
class SolicitudEstadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitudService solicitudService;

    @Test
    void cambiaEstadoYResponde200() throws Exception {
        when(solicitudService.cambiarEstado(10, 4, EstadoSolicitud.EN_PROCESO, null))
                .thenReturn(respuesta("EN_PROCESO"));

        mockMvc.perform(patch("/api/solicitudes/10/estado")
                        .header("X-Usuario-Id", 4)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PROCESO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    void estadoInvalidoResponde400ConErrorResponse() throws Exception {
        mockMvc.perform(patch("/api/solicitudes/10/estado")
                        .header("X-Usuario-Id", 4)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"NO_EXISTE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void estadoAusenteResponde400ConErrorResponse() throws Exception {
        when(solicitudService.cambiarEstado(10, 4, null, null))
                .thenThrow(new IllegalArgumentException("El estado destino es obligatorio."));

        mockMvc.perform(patch("/api/solicitudes/10/estado")
                        .header("X-Usuario-Id", 4)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("El estado destino es obligatorio."));
    }

    @Test
    void headerAusenteResponde400ConErrorResponse() throws Exception {
        mockMvc.perform(patch("/api/solicitudes/10/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PROCESO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void usuarioSinPermisoResponde403ConErrorResponse() throws Exception {
        when(solicitudService.cambiarEstado(10, 8, EstadoSolicitud.EN_PROCESO, null))
                .thenThrow(new AccesoDenegadoException("El usuario no tiene permiso para realizar esta transición."));

        mockMvc.perform(patch("/api/solicitudes/10/estado")
                        .header("X-Usuario-Id", 8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PROCESO\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void solicitudInexistenteResponde404ConErrorResponse() throws Exception {
        when(solicitudService.cambiarEstado(10, 4, EstadoSolicitud.EN_PROCESO, null))
                .thenThrow(new RecursoNoEncontradoException("No existe la solicitud indicada."));

        mockMvc.perform(patch("/api/solicitudes/10/estado")
                        .header("X-Usuario-Id", 4)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PROCESO\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void transicionNoPermitidaResponde422ConErrorResponse() throws Exception {
        when(solicitudService.cambiarEstado(10, 7, EstadoSolicitud.CERRADA, null))
                .thenThrow(new ReglaNegocioException("No se puede cambiar de ABIERTA a CERRADA."));

        mockMvc.perform(patch("/api/solicitudes/10/estado")
                        .header("X-Usuario-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CERRADA\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.estado").value(422))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void consultaHistorialOrdenadoResponde200() throws Exception {
        when(solicitudService.listarHistorial(10)).thenReturn(List.of(
                new HistorialEstadoResponse(1, 10, null, "ABIERTA", "Ana", null, null),
                new HistorialEstadoResponse(2, 10, "ABIERTA", "ASIGNADA", "Carlos", null, null)));

        mockMvc.perform(get("/api/solicitudes/10/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoNuevo").value("ABIERTA"))
                .andExpect(jsonPath("$[1].estadoNuevo").value("ASIGNADA"));
    }

    @Test
    void solicitudSinCambiosDevuelveHistorialVacio() throws Exception {
        when(solicitudService.listarHistorial(10)).thenReturn(List.of());

        mockMvc.perform(get("/api/solicitudes/10/historial"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void solicitudSinExistirAlConsultarHistorialResponde404() throws Exception {
        when(solicitudService.listarHistorial(10))
                .thenThrow(new RecursoNoEncontradoException("No existe la solicitud indicada."));

        mockMvc.perform(get("/api/solicitudes/10/historial"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    private SolicitudResponse respuesta(String estado) {
        return new SolicitudResponse(10, "Falla", "Descripción", "Bloque A", "Electricidad",
                "MEDIO", "MEDIA", estado, "Ana", "Tomás", null, null);
    }
}
