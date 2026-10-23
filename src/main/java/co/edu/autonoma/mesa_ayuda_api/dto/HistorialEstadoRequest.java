package co.edu.autonoma.mesa_ayuda_api.dto;

public record HistorialEstadoRequest(
        Integer solicitudId,
        String estadoAnterior,
        String estadoNuevo,
        Integer usuarioId,
        Integer comentarioId
) {
}
