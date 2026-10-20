package co.edu.autonoma.mesa_ayuda_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CheckResponse(
        String baseDatos,
        String motor,
        String nombre,
        String error
) {
}
