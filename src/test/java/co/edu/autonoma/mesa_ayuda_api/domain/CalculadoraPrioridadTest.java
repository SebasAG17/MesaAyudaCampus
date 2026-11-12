package co.edu.autonoma.mesa_ayuda_api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;

class CalculadoraPrioridadTest {

    @ParameterizedTest
    @CsvSource({
            "BAJA, BAJO, BAJA",
            "BAJA, MEDIO, BAJA",
            "BAJA, ALTO, MEDIA",
            "MEDIA, BAJO, BAJA",
            "MEDIA, MEDIO, MEDIA",
            "MEDIA, ALTO, ALTA",
            "ALTA, BAJO, MEDIA",
            "ALTA, MEDIO, ALTA",
            "ALTA, ALTO, CRITICA",
            "CRITICA, BAJO, ALTA",
            "CRITICA, MEDIO, CRITICA",
            "CRITICA, ALTO, CRITICA"
    })
    void calculaLasDoceCombinaciones(String prioridadBase, String impacto, String esperada) {
        assertEquals(Prioridad.valueOf(esperada), CalculadoraPrioridad.calcular(
                Prioridad.valueOf(prioridadBase), Impacto.valueOf(impacto)));
    }

    @Test
    void rechazaArgumentosNulos() {
        assertThrows(ReglaNegocioException.class, () -> CalculadoraPrioridad.calcular(null, Impacto.MEDIO));
        assertThrows(ReglaNegocioException.class, () -> CalculadoraPrioridad.calcular(Prioridad.MEDIA, null));
    }
}
