/**
 * IntervencaoResponse.java
 * Contrato de saída da intervenção -- achata trecho/equipe para não expor as entities inteiras.
 */
package br.com.motiva.dto;

import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.StatusIntervencao;

import java.time.LocalDate;

public record IntervencaoResponse(
        Long id,
        Long idTrecho,
        String codigoRodovia,
        Long idEquipe,
        String nomeEquipe,
        String tipo,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusIntervencao status
) {

    // ===== BLOCO 1: conversao_entity_para_dto (chamar dentro de @Transactional por causa do LAZY) =====
    public static IntervencaoResponse de(IntervencaoOperacional i) {
        return new IntervencaoResponse(i.getId(),
                i.getTrecho().getId(), i.getTrecho().getCodigoRodovia(),
                i.getEquipe().getId(), i.getEquipe().getNome(),
                i.getTipo(), i.getDataInicio(), i.getDataFim(), i.getStatus());
    }
}
