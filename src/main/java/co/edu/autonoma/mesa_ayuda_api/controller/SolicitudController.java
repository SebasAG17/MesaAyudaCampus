package co.edu.autonoma.mesa_ayuda_api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.mesa_ayuda_api.dto.SolicitudRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.AsignacionRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.CambioEstadoRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.HistorialEstadoResponse;
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

    @GetMapping("/{id}/historial")
    public List<HistorialEstadoResponse> listarHistorial(@PathVariable Integer id) {
        return solicitudService.listarHistorial(id);
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

    /** X-Usuario-Id identifica al usuario que actúa hasta incorporar autenticación. */
    @PatchMapping("/{id}/asignacion")
    public ResponseEntity<SolicitudResponse> asignarTecnico(
            @PathVariable Integer id,
            @RequestHeader("X-Usuario-Id") Integer coordinadorId,
            @RequestBody AsignacionRequest peticion) {
        return ResponseEntity.ok(solicitudService.asignarTecnico(id, coordinadorId, peticion.tecnicoId()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponse> cambiarEstado(
            @PathVariable Integer id,
            @RequestHeader("X-Usuario-Id") Integer usuarioId,
            @RequestBody CambioEstadoRequest peticion) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(
                id, usuarioId, peticion.estado(), peticion.comentarioId()));
    }

}
