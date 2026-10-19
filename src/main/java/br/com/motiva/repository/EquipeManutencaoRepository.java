/**
 * EquipeManutencaoRepository.java
 * Substitui o EquipeManutencaoDAO (117 linhas) -- CRUD herdado de JpaRepository + derived queries.
 */
package br.com.motiva.repository;

import br.com.motiva.model.EquipeManutencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipeManutencaoRepository extends JpaRepository<EquipeManutencao, Long> {

    // ===== BLOCO 1: derived_queries =====
    List<EquipeManutencao> findByAtiva(Boolean ativa);

    List<EquipeManutencao> findByEspecialidadeIgnoreCase(String especialidade);
}
