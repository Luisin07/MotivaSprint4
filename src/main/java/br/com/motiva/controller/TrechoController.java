/**
 * TrechoController.java
 * Endpoints REST de /api/trechos -- só traduz HTTP <-> TrechoService, nenhuma regra de negócio aqui.
 */
package br.com.motiva.controller;

import br.com.motiva.dto.TrechoRequest;
import br.com.motiva.dto.TrechoResponse;
import br.com.motiva.model.NivelPrioridade;
import br.com.motiva.service.TrechoService;
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
@RequestMapping("/api/trechos")
public class TrechoController {

    private final TrechoService service;

    public TrechoController(TrechoService service) {
        this.service = service;
    }

    // ===== BLOCO 1: leitura (200 / 404) =====
    @GetMapping
    public List<TrechoResponse> listar(@RequestParam(required = false) String rodovia,
                                       @RequestParam(required = false) NivelPrioridade prioridade) {
        return service.listar(rodovia, prioridade);
    }

    @GetMapping("/{id}")
    public TrechoResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/desgaste")
    public List<TrechoResponse> porDesgasteMinimo(@RequestParam Double minimo) {
        return service.listarPorDesgasteMinimo(minimo);
    }

    // ===== BLOCO 2: criar (201 + header Location apontando para o recurso novo) =====
    @PostMapping
    public ResponseEntity<TrechoResponse> criar(@Valid @RequestBody TrechoRequest req) {
        TrechoResponse criado = service.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    // ===== BLOCO 3: atualizar e remover (200 / 204 / 404) =====
    @PutMapping("/{id}")
    public TrechoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TrechoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
