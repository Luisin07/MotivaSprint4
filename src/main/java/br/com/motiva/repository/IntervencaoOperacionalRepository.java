/**
 * IntervencaoOperacionalRepository.java
 * Substitui o IntervencaoOperacionalDAO (125 linhas) -- inclui o findByTipo pedido no enunciado.
 */
package br.com.motiva.repository;

import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.StatusIntervencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntervencaoOperacionalRepository extends JpaRepository<IntervencaoOperacional, Long> {

    // ===== BLOCO 1: derived_queries =====
    List<IntervencaoOperacional> findByTipo(String tipo);

    List<IntervencaoOperacional> findByStatus(StatusIntervencao status);

    List<IntervencaoOperacional> findByTrechoId(Long idTrecho);

    // ===== BLOCO 2: checagens_de_fk (evita o ORA-02292 antes de apagar trecho/equipe) =====
    boolean existsByTrechoId(Long idTrecho);

    boolean existsByEquipeId(Long idEquipe);
}
