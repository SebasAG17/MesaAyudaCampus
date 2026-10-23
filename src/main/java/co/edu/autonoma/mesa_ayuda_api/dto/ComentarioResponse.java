package co.edu.autonoma.mesa_ayuda_api.dto;

import java.time.LocalDateTime;

public record ComentarioResponse(
        Integer id,
        Integer solicitudId,
        String autor,
        String texto,
        boolean interno,
        boolean esSolucion,
        LocalDateTime creadoEn
) {
}
