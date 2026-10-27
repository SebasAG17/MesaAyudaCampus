package co.edu.autonoma.mesa_ayuda_api.dto;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String correo,
        String rol,
        boolean activo
) {
}
