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

import co.edu.autonoma.mesa_ayuda_api.dto.ComentarioRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.ComentarioResponse;
import co.edu.autonoma.mesa_ayuda_api.service.ComentarioService;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public List<ComentarioResponse> listar() {
        return comentarioService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioResponse> buscarPorId(@PathVariable Integer id) {
        return comentarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ComentarioResponse> crear(@RequestBody ComentarioRequest peticion) {
        return comentarioService.crear(peticion)
                .map(creado -> ResponseEntity
                        .created(URI.create("/api/comentarios/" + creado.id()))
                        .body(creado))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComentarioResponse> reemplazar(
            @PathVariable Integer id,
            @RequestBody ComentarioRequest peticion) {
        return comentarioService.reemplazar(id, peticion)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        return comentarioService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}
