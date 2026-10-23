package co.edu.autonoma.mesa_ayuda_api.dto;

import java.time.LocalDateTime;

public record HistorialEstadoResponse(
        Integer id,
        Integer solicitudId,
        String estadoAnterior,
        String estadoNuevo,
        String usuario,
        Integer comentarioId,
        LocalDateTime ocurridoEn
) {
}
