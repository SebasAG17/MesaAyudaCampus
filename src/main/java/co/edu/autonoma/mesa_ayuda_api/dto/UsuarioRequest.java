package co.edu.autonoma.mesa_ayuda_api.dto;

public record UsuarioRequest(
        String nombre,
        String correo,
        String rol,
        Boolean activo
) {
}
