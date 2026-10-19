/**
 * RelatorioPrioridadeRepository.java
 * Substitui o RelatorioPrioridadeDAO (125 linhas) -- histórico e consulta por período.
 */
package br.com.motiva.repository;

import br.com.motiva.model.RelatorioPrioridade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RelatorioPrioridadeRepository extends JpaRepository<RelatorioPrioridade, Long> {

    // ===== BLOCO 1: derived_queries (mais recente primeiro, igual ao ORDER BY da Sprint 3) =====
    List<RelatorioPrioridade> findAllByOrderByDataGeracaoDesc();

    List<RelatorioPrioridade> findByDataGeracaoBetweenOrderByDataGeracaoDesc(LocalDateTime inicio, LocalDateTime fim);
}
