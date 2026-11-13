package co.edu.autonoma.mesa_ayuda_api.dto;

import co.edu.autonoma.mesa_ayuda_api.domain.Rol;

public record UsuarioRequest(
        String nombre,
        String correo,
        Rol rol,
        Boolean activo
) {
}
