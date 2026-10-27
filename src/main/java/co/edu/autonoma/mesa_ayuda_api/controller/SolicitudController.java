package co.edu.autonoma.mesa_ayuda_api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudResponse;
import co.edu.autonoma.mesa_ayuda_api.service.SolicitudService;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public List<SolicitudResponse> listar() {
        return solicitudService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> buscarPorId(@PathVariable Integer id) {
        return solicitudService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@RequestBody SolicitudRequest peticion) {
        return solicitudService.crear(peticion)
                .map(creada -> ResponseEntity
                        .created(URI.create("/api/solicitudes/" + creada.id()))
                        .body(creada))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudResponse> reemplazar(
            @PathVariable Integer id,
            @RequestBody SolicitudRequest peticion) {
        return solicitudService.reemplazar(id, peticion)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        return solicitudService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}
