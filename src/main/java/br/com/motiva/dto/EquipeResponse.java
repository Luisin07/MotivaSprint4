/**
 * EquipeResponse.java
 * Contrato de saída da equipe de manutenção.
 */
package br.com.motiva.dto;

import br.com.motiva.model.EquipeManutencao;

public record EquipeResponse(
        Long id,
        String nome,
        String responsavel,
        Integer qtMembros,
        String especialidade,
        Boolean ativa
) {

    // ===== BLOCO 1: conversao_entity_para_dto =====
    public static EquipeResponse de(EquipeManutencao e) {
        return new EquipeResponse(e.getId(), e.getNome(), e.getResponsavel(),
                e.getQtMembros(), e.getEspecialidade(), e.getAtiva());
    }
}
