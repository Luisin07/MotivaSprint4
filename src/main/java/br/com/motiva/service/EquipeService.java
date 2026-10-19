/**
 * EquipeService.java
 * Regras de negócio da equipe de manutenção -- chamado pelo EquipeController e pelo IntervencaoService.
 */
package br.com.motiva.service;

import br.com.motiva.dto.EquipeRequest;
import br.com.motiva.dto.EquipeResponse;
import br.com.motiva.exception.ConflitoException;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.repository.EquipeManutencaoRepository;
import br.com.motiva.repository.IntervencaoOperacionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipeService {

    private final EquipeManutencaoRepository equipeRepository;
    private final IntervencaoOperacionalRepository intervencaoRepository;

    public EquipeService(EquipeManutencaoRepository equipeRepository,
                         IntervencaoOperacionalRepository intervencaoRepository) {
        this.equipeRepository = equipeRepository;
        this.intervencaoRepository = intervencaoRepository;
    }

    // ===== BLOCO 1: consultas (filtros opcionais por situação e especialidade) =====
    @Transactional(readOnly = true)
    public List<EquipeResponse> listar(Boolean ativa, String especialidade) {
        List<EquipeManutencao> equipes;
        if (especialidade != null && !especialidade.isBlank()) {
            equipes = equipeRepository.findByEspecialidadeIgnoreCase(especialidade.trim());
        } else if (ativa != null) {
            equipes = equipeRepository.findByAtiva(ativa);
        } else {
            equipes = equipeRepository.findAll();
        }
        return equipes.stream()
                .filter(e -> ativa == null || ativa.equals(e.getAtiva()))
                .map(EquipeResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipeResponse buscarPorId(Long id) {
        return EquipeResponse.de(buscarEntidade(id));
    }

    // ===== BLOCO 2: criar (ativa nula vira true, igual ao DEFAULT 'S' da tabela) =====
    @Transactional
    public EquipeResponse criar(EquipeRequest req) {
        EquipeManutencao nova = new EquipeManutencao(req.nome().trim(), req.responsavel().trim(),
                req.qtMembros(), req.especialidade().trim(), req.ativa() == null || req.ativa());
        return EquipeResponse.de(equipeRepository.save(nova));
    }

    // ===== BLOCO 3: atualizar =====
    @Transactional
    public EquipeResponse atualizar(Long id, EquipeRequest req) {
        EquipeManutencao equipe = buscarEntidade(id);
        equipe.atualizarDados(req.nome().trim(), req.responsavel().trim(), req.qtMembros(),
                req.especialidade().trim(), req.ativa() == null ? equipe.getAtiva() : req.ativa());
        return EquipeResponse.de(equipe);
    }

    // ===== BLOCO 4: remover (equipe com histórico de intervenção não some: inative com PUT ativa=false) =====
    @Transactional
    public void remover(Long id) {
        EquipeManutencao equipe = buscarEntidade(id);
        if (intervencaoRepository.existsByEquipeId(id)) {
            throw new ConflitoException("Equipe " + id + " possui intervenções vinculadas; inative-a com ativa=false");
        }
        equipeRepository.delete(equipe);
    }

    // ===== BLOCO 5: auxiliares =====
    EquipeManutencao buscarEntidade(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipe", id));
    }
}
