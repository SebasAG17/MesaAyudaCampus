package co.edu.autonoma.mesa_ayuda_api.domain;

public enum Rol {
    SOLICITANTE,
    TECNICO,
    COORDINADOR;

    public boolean puedeSerAsignado() {
        return this == TECNICO;
    }
}
