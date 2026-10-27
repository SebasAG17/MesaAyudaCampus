package co.edu.autonoma.mesa_ayuda_api.dto;

public record CategoriaResponse(
        Integer id,
        String nombre,
        String prioridadBase,
        boolean activa
) {
}
