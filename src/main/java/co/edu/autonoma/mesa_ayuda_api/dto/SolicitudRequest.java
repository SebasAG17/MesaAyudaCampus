package co.edu.autonoma.mesa_ayuda_api.dto;

public record SolicitudRequest(
        String titulo,
        String descripcion,
        String ubicacion,
        Integer categoriaId,
        String impacto,
        String prioridad,
        String estado,
        Integer solicitanteId,
        Integer tecnicoId
) {
}
