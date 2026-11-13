package co.edu.autonoma.mesa_ayuda_api.exception;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;

import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;
import co.edu.autonoma.mesa_ayuda_api.domain.Prioridad;
import co.edu.autonoma.mesa_ayuda_api.domain.Rol;
import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException excepcion) {
        return crearRespuesta(HttpStatus.UNPROCESSABLE_ENTITY, excepcion.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(AccesoDenegadoException excepcion) {
        return crearRespuesta(HttpStatus.FORBIDDEN, excepcion.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(RecursoNoEncontradoException excepcion) {
        return crearRespuesta(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> manejarHeaderAusente(MissingRequestHeaderException excepcion) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "Falta el encabezado requerido: " + excepcion.getHeaderName() + ".");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarPeticionInvalida(IllegalArgumentException excepcion) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarMensajeNoLegible(HttpMessageNotReadableException excepcion) {
        String valores = "Impacto: " + nombres(Impacto.values())
                + ". Prioridad: " + nombres(Prioridad.values())
                + ". Prioridad base: " + nombres(Prioridad.BAJA, Prioridad.MEDIA, Prioridad.ALTA)
                + ". Rol: " + nombres(Rol.values())
                + ". Estado: " + nombres(EstadoSolicitud.values()) + ".";
        return crearRespuesta(HttpStatus.BAD_REQUEST, "El cuerpo contiene valores inválidos. " + valores);
    }

    private String nombres(Enum<?>... valores) {
        return String.join(", ", Arrays.stream(valores).map(Enum::name).toList());
    }

    private ResponseEntity<ErrorResponse> crearRespuesta(HttpStatus estado, String mensaje) {
        ErrorResponse respuesta = new ErrorResponse(
                estado.value(), estado.getReasonPhrase(), mensaje, LocalDateTime.now());
        return ResponseEntity.status(estado).body(respuesta);
    }
}
