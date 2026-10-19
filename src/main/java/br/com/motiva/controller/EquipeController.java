/**
 * EquipeController.java
 * Endpoints REST de /api/equipes -- delega tudo ao EquipeService.
 */
package br.com.motiva.controller;

import br.com.motiva.dto.EquipeRequest;
import br.com.motiva.dto.EquipeResponse;
import br.com.motiva.service.EquipeService;
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
@RequestMapping("/api/equipes")
public class EquipeController {

    private final EquipeService service;

    public EquipeController(EquipeService service) {
        this.service = service;
    }

    // ===== BLOCO 1: leitura =====
    @GetMapping
    public List<EquipeResponse> listar(@RequestParam(required = false) Boolean ativa,
                                       @RequestParam(required = false) String especialidade) {
        return service.listar(ativa, especialidade);
    }

    @GetMapping("/{id}")
    public EquipeResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // ===== BLOCO 2: criar =====
    @PostMapping
    public ResponseEntity<EquipeResponse> criar(@Valid @RequestBody EquipeRequest req) {
        EquipeResponse criada = service.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    // ===== BLOCO 3: atualizar e remover =====
    @PutMapping("/{id}")
    public EquipeResponse atualizar(@PathVariable Long id, @Valid @RequestBody EquipeRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
