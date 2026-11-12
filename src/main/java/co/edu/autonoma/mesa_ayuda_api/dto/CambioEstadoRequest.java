package co.edu.autonoma.mesa_ayuda_api.dto;

import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;

public record CambioEstadoRequest(EstadoSolicitud estado, Integer comentarioId) {
}
