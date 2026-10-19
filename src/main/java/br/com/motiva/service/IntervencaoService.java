/**
 * IntervencaoService.java
 * Regras de negócio da intervenção (trecho/equipe existentes, equipe ativa, datas coerentes).
 * Reaproveita TrechoRodoviaRepository e EquipeManutencaoRepository para validar as FKs antes do banco.
 */
package br.com.motiva.service;

import br.com.motiva.dto.IntervencaoRequest;
import br.com.motiva.dto.IntervencaoResponse;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.EquipeManutencao;
import br.com.motiva.model.IntervencaoOperacional;
import br.com.motiva.model.StatusIntervencao;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.EquipeManutencaoRepository;
import br.com.motiva.repository.IntervencaoOperacionalRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class IntervencaoService {

    private final IntervencaoOperacionalRepository intervencaoRepository;
    private final TrechoRodoviaRepository trechoRepository;
    private final EquipeManutencaoRepository equipeRepository;

    public IntervencaoService(IntervencaoOperacionalRepository intervencaoRepository,
                              TrechoRodoviaRepository trechoRepository,
                              EquipeManutencaoRepository equipeRepository) {
        this.intervencaoRepository = intervencaoRepository;
        this.trechoRepository = trechoRepository;
        this.equipeRepository = equipeRepository;
    }

    // ===== BLOCO 1: consultas (um filtro por vez: tipo > status > trecho) =====
    @Transactional(readOnly = true)
    public List<IntervencaoResponse> listar(String tipo, StatusIntervencao status, Long idTrecho) {
        List<IntervencaoOperacional> lista;
        if (tipo != null && !tipo.isBlank()) {
            lista = intervencaoRepository.findByTipo(tipo.trim());
        } else if (status != null) {
            lista = intervencaoRepository.findByStatus(status);
        } else if (idTrecho != null) {
            lista = intervencaoRepository.findByTrechoId(idTrecho);
        } else {
            lista = intervencaoRepository.findAll();
        }
        return lista.stream().map(IntervencaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public IntervencaoResponse buscarPorId(Long id) {
        return IntervencaoResponse.de(buscarEntidade(id));
    }

    // ===== BLOCO 2: criar (equipe inativa não recebe intervenção nova) =====
    @Transactional
    public IntervencaoResponse criar(IntervencaoRequest req) {
        validarDatas(req);
        TrechoRodovia trecho = carregarTrecho(req.idTrecho());
        EquipeManutencao equipe = carregarEquipe(req.idEquipe());
        exigirEquipeAtiva(equipe);

        IntervencaoOperacional nova = new IntervencaoOperacional(trecho, equipe, req.tipo().trim(),
                req.dataInicio(), req.dataFim(), req.status());
        return IntervencaoResponse.de(intervencaoRepository.save(nova));
    }

    // ===== BLOCO 3: atualizar (só exige equipe ativa se a equipe foi trocada) =====
    @Transactional
    public IntervencaoResponse atualizar(Long id, IntervencaoRequest req) {
        IntervencaoOperacional intervencao = buscarEntidade(id);
        validarDatas(req);
        TrechoRodovia trecho = carregarTrecho(req.idTrecho());
        EquipeManutencao equipe = carregarEquipe(req.idEquipe());
        if (!Objects.equals(equipe.getId(), intervencao.getEquipe().getId())) {
            exigirEquipeAtiva(equipe);
        }

        intervencao.atualizarDados(trecho, equipe, req.tipo().trim(), req.dataInicio(), req.dataFim(), req.status());
        return IntervencaoResponse.de(intervencao);
    }

    // ===== BLOCO 4: remover =====
    @Transactional
    public void remover(Long id) {
        intervencaoRepository.delete(buscarEntidade(id));
    }

    // ===== BLOCO 5: validacoes_de_negocio =====
    private void validarDatas(IntervencaoRequest req) {
        if (req.dataFim() != null && req.dataFim().isBefore(req.dataInicio())) {
            throw new RegraNegocioException("dataFim não pode ser anterior a dataInicio");
        }
        if (req.status() == StatusIntervencao.CONCLUIDA && req.dataFim() == null) {
            throw new RegraNegocioException("Intervenção CONCLUIDA precisa de dataFim");
        }
    }

    private void exigirEquipeAtiva(EquipeManutencao equipe) {
        if (!Boolean.TRUE.equals(equipe.getAtiva())) {
            throw new RegraNegocioException("Equipe " + equipe.getId() + " (" + equipe.getNome() + ") está inativa");
        }
    }

    // ===== BLOCO 6: auxiliares (ID inexistente no corpo é erro do cliente: 400, não 404) =====
    private TrechoRodovia carregarTrecho(Long id) {
        return trechoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Trecho " + id + " não existe"));
    }

    private EquipeManutencao carregarEquipe(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Equipe " + id + " não existe"));
    }

    private IntervencaoOperacional buscarEntidade(Long id) {
        return intervencaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Intervenção", id));
    }
}
