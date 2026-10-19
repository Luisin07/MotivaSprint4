/**
 * RelatorioGeradoResponse.java
 * Resposta do POST /api/relatorios -- o relatório salvo + a lista de trechos classificados (equivale ao console da Sprint 3).
 */
package br.com.motiva.dto;

import java.util.List;

public record RelatorioGeradoResponse(
        RelatorioResponse relatorio,
        List<TrechoResponse> trechosClassificados
) { }
