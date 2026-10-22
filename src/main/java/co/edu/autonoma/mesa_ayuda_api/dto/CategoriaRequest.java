package co.edu.autonoma.mesa_ayuda_api.dto;

public record CategoriaRequest(
        String nombre,
        String prioridadBase,
        Boolean activa
) {
}
