package co.edu.autonoma.mesa_ayuda_api.dto;

public record ComentarioRequest(
        Integer solicitudId,
        Integer autorId,
        String texto,
        Boolean interno,
        Boolean esSolucion
) {
}
