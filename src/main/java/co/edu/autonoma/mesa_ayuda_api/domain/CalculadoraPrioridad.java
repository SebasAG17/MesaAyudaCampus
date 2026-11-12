package co.edu.autonoma.mesa_ayuda_api.domain;

import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;

public class CalculadoraPrioridad {

    public static Prioridad calcular(Prioridad prioridadBase, Impacto impacto) {
        if (prioridadBase == null || impacto == null) {
            throw new ReglaNegocioException("La prioridad base y el impacto son obligatorios.");
        }

        int ajuste = switch (impacto) {
            case BAJO -> -1;
            case MEDIO -> 0;
            case ALTO -> 1;
        };
        int indice = Math.max(0, Math.min(prioridadBase.ordinal() + ajuste, Prioridad.values().length - 1));
        return Prioridad.values()[indice];
    }
}
