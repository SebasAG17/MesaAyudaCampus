package co.edu.autonoma.mesa_ayuda_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoResponse;
import co.edu.autonoma.mesa_ayuda_api.service.HistorialEstadoService;

@RestController
@RequestMapping("/api/historial")
public class HistorialEstadoController {

    private final HistorialEstadoService historialEstadoService;

    public HistorialEstadoController(HistorialEstadoService historialEstadoService) {
        this.historialEstadoService = historialEstadoService;
    }

    @GetMapping
    public List<HistorialEstadoResponse> listar() {
        return historialEstadoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialEstadoResponse> buscarPorId(@PathVariable Integer id) {
        return historialEstadoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
