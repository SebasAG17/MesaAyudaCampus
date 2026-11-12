package co.edu.autonoma.mesa_ayuda_api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudResponse;
import co.edu.autonoma.mesa_ayuda_api.exception.AccesoDenegadoException;
import co.edu.autonoma.mesa_ayuda_api.exception.RecursoNoEncontradoException;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;
import co.edu.autonoma.mesa_ayuda_api.service.SolicitudService;

@WebMvcTest(SolicitudController.class)
class SolicitudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitudService solicitudService;

    @Test
    void asignacionCorrectaResponde200() throws Exception {
        when(solicitudService.asignarTecnico(10, 7, 2)).thenReturn(respuestaAsignada());

        mockMvc.perform(patch("/api/solicitudes/10/asignacion")
                        .header("X-Usuario-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ASIGNADA"))
                .andExpect(jsonPath("$.tecnico").value("Técnico"));
    }

    @Test
    void headerAusenteResponde400ConErrorResponse() throws Exception {
        mockMvc.perform(patch("/api/solicitudes/10/asignacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Falta el encabezado requerido: X-Usuario-Id."));
    }

    @Test
    void coordinadorNoAutorizadoResponde403ConErrorResponse() throws Exception {
        when(solicitudService.asignarTecnico(10, 8, 2))
                .thenThrow(new AccesoDenegadoException("Se requiere un coordinador activo."));

        mockMvc.perform(patch("/api/solicitudes/10/asignacion")
                        .header("X-Usuario-Id", 8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":2}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403))
                .andExpect(jsonPath("$.mensaje").value("Se requiere un coordinador activo."));
    }

    @Test
    void solicitudInexistenteResponde404ConErrorResponse() throws Exception {
        when(solicitudService.asignarTecnico(10, 7, 2))
                .thenThrow(new RecursoNoEncontradoException("No existe la solicitud indicada."));

        mockMvc.perform(patch("/api/solicitudes/10/asignacion")
                        .header("X-Usuario-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":2}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe la solicitud indicada."));
    }

    @Test
    void reglaDeNegocioResponde422ConErrorResponse() throws Exception {
        when(solicitudService.asignarTecnico(10, 7, 2))
                .thenThrow(new ReglaNegocioException("El usuario asignado debe ser un técnico activo."));

        mockMvc.perform(patch("/api/solicitudes/10/asignacion")
                        .header("X-Usuario-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.estado").value(422))
                .andExpect(jsonPath("$.mensaje").value("El usuario asignado debe ser un técnico activo."));
    }

    private SolicitudResponse respuestaAsignada() {
        return new SolicitudResponse(10, "Falla", "Descripción", "Bloque A", "Electricidad",
                "MEDIO", "ALTA", "ASIGNADA", "Solicitante", "Técnico", null, null);
    }
}
