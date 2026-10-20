package co.edu.autonoma.mesa_ayuda_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.mesa_ayuda_api.dto.CheckResponse;
import co.edu.autonoma.mesa_ayuda_api.service.CheckService;

@RestController
@RequestMapping("/api/check")
public class CheckController {

    private final CheckService checkService;

    public CheckController(CheckService checkService) {
        this.checkService = checkService;
    }

    @GetMapping
    public ResponseEntity<CheckResponse> verificar() {
        CheckResponse respuesta = checkService.verificarBaseDatos();
        HttpStatus estado = respuesta.error() == null
                ? HttpStatus.OK
                : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(estado).body(respuesta);
    }

}
