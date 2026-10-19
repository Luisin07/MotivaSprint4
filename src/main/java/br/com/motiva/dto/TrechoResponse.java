/**
 * TrechoResponse.java
 * Contrato de saída do trecho -- inclui a prioridade calculada pelo MotorPrioridade (não é coluna do banco).
 */
package br.com.motiva.dto;

import br.com.motiva.model.NivelPrioridade;
import br.com.motiva.model.TrechoRodovia;

public record TrechoResponse(
        Long id,
        String codigoRodovia,
        Double kmInicial,
        Double kmFinal,
        Double extensaoKm,
        Double indiceDesgaste,
        Integer acidentesAno,
        Integer fluxoDiario,
        NivelPrioridade prioridade
) {

    // ===== BLOCO 1: conversao_entity_para_dto =====
    public static TrechoResponse de(TrechoRodovia t, NivelPrioridade prioridade) {
        return new TrechoResponse(t.getId(), t.getCodigoRodovia(), t.getKmInicial(), t.getKmFinal(),
                t.extensaoKm(), t.getIndiceDesgaste(), t.getAcidentesAno(), t.getFluxoDiario(), prioridade);
    }
}
