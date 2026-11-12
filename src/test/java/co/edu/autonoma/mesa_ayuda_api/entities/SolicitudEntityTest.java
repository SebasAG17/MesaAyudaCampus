package co.edu.autonoma.mesa_ayuda_api.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;

class SolicitudEntityTest {

    @Test
    void transicionInvalidaNoCambiaElEstado() {
        SolicitudEntity solicitud = new SolicitudEntity();
        solicitud.iniciar();

        assertThrows(ReglaNegocioException.class, () -> solicitud.transicionarA(EstadoSolicitud.CERRADA));
        assertEquals(EstadoSolicitud.ABIERTA, solicitud.getEstado());
    }
}
