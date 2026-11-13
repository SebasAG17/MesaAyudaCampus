package co.edu.autonoma.mesa_ayuda_api.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;

class SolicitudEntityTest {

    @Test
    void transicionarA_desdeAbiertaHaciaAsignada_cambiaEstadoYDevuelveElAnterior() {
        // Arrange
        SolicitudEntity solicitud = new SolicitudEntity();
        solicitud.iniciar(); // queda en ABIERTA

        // Act
        EstadoSolicitud anterior = solicitud.transicionarA(EstadoSolicitud.ASIGNADA);

        // Assert
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.ASIGNADA);
        assertThat(anterior).isEqualTo(EstadoSolicitud.ABIERTA);
    }
}
