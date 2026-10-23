package co.edu.autonoma.mesa_ayuda_api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoRequest;
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

    @PostMapping
    public ResponseEntity<HistorialEstadoResponse> crear(@RequestBody HistorialEstadoRequest peticion) {
        return historialEstadoService.crear(peticion)
                .map(creado -> ResponseEntity
                        .created(URI.create("/api/historial/" + creado.id()))
                        .body(creado))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialEstadoResponse> reemplazar(
            @PathVariable Integer id,
            @RequestBody HistorialEstadoRequest peticion) {
        return historialEstadoService.reemplazar(id, peticion)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
