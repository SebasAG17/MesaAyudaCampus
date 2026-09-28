package co.edu.autonoma.mesa_ayuda_api.service;

import org.springframework.stereotype.Service;

import co.edu.autonoma.mesa_ayuda_api.dto.EstadoResponse;

@Service
public class EstadoService {

    public EstadoResponse consultarEstado() {
        return new EstadoResponse(
                "mesa-ayuda-api",
                "disponible"
        );
    }

}
