/**
 * RelatorioController.java
 * Endpoints REST de /api/relatorios -- gerar (POST), histórico e consulta por período (GET).
 * A classificação URGENTE/CRITICO/ATENCAO/NORMAL vive no MotorPrioridade, nunca aqui.
 */
package br.com.motiva.controller;

import br.com.motiva.dto.RelatorioGeradoResponse;
import br.com.motiva.dto.RelatorioResponse;
import br.com.motiva.service.GeradorRelatorio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final GeradorRelatorio gerador;

    public RelatorioController(GeradorRelatorio gerador) {
        this.gerador = gerador;
    }

    // ===== BLOCO 1: gerar (201: cria uma linha nova no histórico) =====
    @PostMapping
    public ResponseEntity<RelatorioGeradoResponse> gerar() {
        RelatorioGeradoResponse gerado = gerador.gerar();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(gerado.relatorio().id()).toUri();
        return ResponseEntity.created(location).body(gerado);
    }

    // ===== BLOCO 2: historico =====
    @GetMapping
    public List<RelatorioResponse> historico() {
        return gerador.listarHistorico();
    }

    // ===== BLOCO 3: periodo (declarado antes de /{id} só por legibilidade; o Spring prioriza o literal) =====
    @GetMapping("/periodo")
    public List<RelatorioResponse> porPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return gerador.consultarPeriodo(inicio, fim);
    }

    @GetMapping("/{id}")
    public RelatorioResponse buscar(@PathVariable Long id) {
        return gerador.buscarPorId(id);
    }
}
