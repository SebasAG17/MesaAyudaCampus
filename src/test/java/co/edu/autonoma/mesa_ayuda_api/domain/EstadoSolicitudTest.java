package co.edu.autonoma.mesa_ayuda_api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EstadoSolicitudTest {

    @ParameterizedTest
    @CsvSource({
            "ABIERTA, ABIERTA, false",
            "ABIERTA, ASIGNADA, true",
            "ABIERTA, EN_PROCESO, false",
            "ABIERTA, RESUELTA, false",
            "ABIERTA, CERRADA, false",
            "ASIGNADA, ABIERTA, false",
            "ASIGNADA, ASIGNADA, false",
            "ASIGNADA, EN_PROCESO, true",
            "ASIGNADA, RESUELTA, false",
            "ASIGNADA, CERRADA, false",
            "EN_PROCESO, ABIERTA, false",
            "EN_PROCESO, ASIGNADA, false",
            "EN_PROCESO, EN_PROCESO, false",
            "EN_PROCESO, RESUELTA, true",
            "EN_PROCESO, CERRADA, false",
            "RESUELTA, ABIERTA, false",
            "RESUELTA, ASIGNADA, false",
            "RESUELTA, EN_PROCESO, true",
            "RESUELTA, RESUELTA, false",
            "RESUELTA, CERRADA, true",
            "CERRADA, ABIERTA, false",
            "CERRADA, ASIGNADA, false",
            "CERRADA, EN_PROCESO, false",
            "CERRADA, RESUELTA, false",
            "CERRADA, CERRADA, false"
    })
    void permiteSoloLasTransicionesDefinidas(String origen, String destino, boolean esperada) {
        assertEquals(esperada,
                EstadoSolicitud.valueOf(origen).puedePasarA(EstadoSolicitud.valueOf(destino)));
    }
}
