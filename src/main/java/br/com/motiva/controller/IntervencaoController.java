/**
 * IntervencaoController.java
 * Endpoints REST de /api/intervencoes -- delega tudo ao IntervencaoService.
 */
package br.com.motiva.controller;

import br.com.motiva.dto.IntervencaoRequest;
import br.com.motiva.dto.IntervencaoResponse;
import br.com.motiva.model.StatusIntervencao;
import br.com.motiva.service.IntervencaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/intervencoes")
public class IntervencaoController {

    private final IntervencaoService service;

    public IntervencaoController(IntervencaoService service) {
        this.service = service;
    }

    // ===== BLOCO 1: leitura =====
    @GetMapping
    public List<IntervencaoResponse> listar(@RequestParam(required = false) String tipo,
                                            @RequestParam(required = false) StatusIntervencao status,
                                            @RequestParam(required = false) Long idTrecho) {
        return service.listar(tipo, status, idTrecho);
    }

    @GetMapping("/{id}")
    public IntervencaoResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // ===== BLOCO 2: criar =====
    @PostMapping
    public ResponseEntity<IntervencaoResponse> criar(@Valid @RequestBody IntervencaoRequest req) {
        IntervencaoResponse criada = service.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    // ===== BLOCO 3: atualizar e remover =====
    @PutMapping("/{id}")
    public IntervencaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody IntervencaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
