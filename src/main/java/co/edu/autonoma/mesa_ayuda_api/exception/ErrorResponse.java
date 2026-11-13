package co.edu.autonoma.mesa_ayuda_api.exception;

import java.time.LocalDateTime;

public record ErrorResponse(int estado, String error, String mensaje, LocalDateTime timestamp) {
}
