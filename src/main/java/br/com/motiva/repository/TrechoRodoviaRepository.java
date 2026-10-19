/**
 * TrechoRodoviaRepository.java
 * Substitui o TrechoRodoviaDAO (119 linhas) -- o Spring Data gera a implementação e o SQL em tempo de execução.
 */
package br.com.motiva.repository;

import br.com.motiva.model.TrechoRodovia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrechoRodoviaRepository extends JpaRepository<TrechoRodovia, Long> {

    // ===== BLOCO 1: derived_queries (SQL gerado a partir do nome do método) =====
    List<TrechoRodovia> findByCodigoRodoviaIgnoreCase(String codigoRodovia);

    List<TrechoRodovia> findByIndiceDesgasteGreaterThanEqualOrderByIndiceDesgasteDesc(Double minimo);

    List<TrechoRodovia> findByAcidentesAnoGreaterThanEqual(Integer minimo);
}
