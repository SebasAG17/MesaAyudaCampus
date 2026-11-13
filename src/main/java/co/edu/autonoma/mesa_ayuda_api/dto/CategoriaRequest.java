package co.edu.autonoma.mesa_ayuda_api.dto;

import co.edu.autonoma.mesa_ayuda_api.domain.Prioridad;

public record CategoriaRequest(
        String nombre,
        Prioridad prioridadBase,
        Boolean activa
) {
}
