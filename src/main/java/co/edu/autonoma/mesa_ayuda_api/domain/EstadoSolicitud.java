package co.edu.autonoma.mesa_ayuda_api.domain;

import java.util.List;

public enum EstadoSolicitud {
    ABIERTA,
    ASIGNADA,
    EN_PROCESO,
    RESUELTA,
    CERRADA;

    public boolean puedePasarA(EstadoSolicitud destino) {
        return destino != null && destinosPermitidos().contains(destino);
    }

    public List<EstadoSolicitud> destinosPermitidos() {
        return switch (this) {
            case ABIERTA -> List.of(ASIGNADA);
            case ASIGNADA -> List.of(EN_PROCESO);
            case EN_PROCESO -> List.of(RESUELTA);
            case RESUELTA -> List.of(CERRADA, EN_PROCESO);
            case CERRADA -> List.of();
        };
    }

    public String destinosPermitidosComoTexto() {
        List<EstadoSolicitud> destinos = destinosPermitidos();
        return destinos.isEmpty()
                ? "ninguno"
                : String.join(", ", destinos.stream().map(Enum::name).toList());
    }
}
