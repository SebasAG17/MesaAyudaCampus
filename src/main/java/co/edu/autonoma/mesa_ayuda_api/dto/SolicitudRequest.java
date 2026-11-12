package co.edu.autonoma.mesa_ayuda_api.dto;

import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;

public record SolicitudRequest(
        String titulo,
        String descripcion,
        String ubicacion,
        Integer categoriaId,
        Impacto impacto,
        String estado,
        Integer solicitanteId,
        Integer tecnicoId
) {
}
