package co.edu.autonoma.mesa_ayuda_api.dto;

import java.time.LocalDateTime;

public record SolicitudResponse(
        Integer id,
        String titulo,
        String descripcion,
        String ubicacion,
        String categoria,
        String impacto,
        String prioridad,
        String estado,
        String solicitante,
        String tecnico,
        LocalDateTime creadaEn,
        LocalDateTime cerradaEn
) {
}
