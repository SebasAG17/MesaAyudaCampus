package co.edu.autonoma.mesa_ayuda_api.domain;

public final class ReglasEstadoSolicitud {

    private ReglasEstadoSolicitud() {
    }

    public static boolean puedeCambiar(
            EstadoSolicitud actual,
            EstadoSolicitud destino,
            Rol rol,
            Integer usuarioId,
            Integer tecnicoAsignadoId,
            Integer solicitanteId) {
        if (rol == Rol.COORDINADOR) {
            return true;
        }
        if ((actual == EstadoSolicitud.ASIGNADA && destino == EstadoSolicitud.EN_PROCESO)
                || (actual == EstadoSolicitud.EN_PROCESO && destino == EstadoSolicitud.RESUELTA)) {
            return rol == Rol.TECNICO && usuarioId != null && usuarioId.equals(tecnicoAsignadoId);
        }
        if (actual == EstadoSolicitud.RESUELTA
                && (destino == EstadoSolicitud.CERRADA || destino == EstadoSolicitud.EN_PROCESO)) {
            return rol == Rol.SOLICITANTE && usuarioId != null && usuarioId.equals(solicitanteId);
        }
        return false;
    }
}
