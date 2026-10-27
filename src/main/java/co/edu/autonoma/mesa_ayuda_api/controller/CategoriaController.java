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

import co.edu.autonoma.mesa_ayuda_api.dto.CategoriaRequest;
import co.edu.autonoma.mesa_ayuda_api.dto.CategoriaResponse;
import co.edu.autonoma.mesa_ayuda_api.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscarPorId(@PathVariable Integer id) {
        return categoriaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@RequestBody CategoriaRequest peticion) {
        CategoriaResponse creada = categoriaService.crear(peticion);
        return ResponseEntity
                .created(URI.create("/api/categorias/" + creada.id()))
                .body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> reemplazar(
            @PathVariable Integer id,
            @RequestBody CategoriaRequest peticion) {
        return categoriaService.reemplazar(id, peticion)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        return categoriaService.desactivar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}
