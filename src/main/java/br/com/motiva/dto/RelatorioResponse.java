/**
 * RelatorioResponse.java
 * Contrato de saída de um relatório do histórico (T_MOT_RELATORIO).
 */
package br.com.motiva.dto;

import br.com.motiva.model.RelatorioPrioridade;

import java.time.LocalDateTime;

public record RelatorioResponse(
        Long id,
        LocalDateTime dataGeracao,
        Integer qtUrgente,
        Integer qtCritico,
        Integer qtAtencao,
        Integer qtNormal,
        Integer total,
        String resumo
) {

    // ===== BLOCO 1: conversao_entity_para_dto =====
    public static RelatorioResponse de(RelatorioPrioridade r) {
        int total = r.getQtUrgente() + r.getQtCritico() + r.getQtAtencao() + r.getQtNormal();
        return new RelatorioResponse(r.getId(), r.getDataGeracao(), r.getQtUrgente(), r.getQtCritico(),
                r.getQtAtencao(), r.getQtNormal(), total, r.getResumo());
    }
}
