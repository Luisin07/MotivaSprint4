/**
 * TrechoService.java
 * Regras de negócio do trecho (validações da Sprint 1 + prioridade) -- chamado pelo TrechoController.
 */
package br.com.motiva.service;

import br.com.motiva.dto.TrechoRequest;
import br.com.motiva.dto.TrechoResponse;
import br.com.motiva.exception.ConflitoException;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.NivelPrioridade;
import br.com.motiva.model.TrechoRodovia;
import br.com.motiva.repository.IntervencaoOperacionalRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrechoService {

    private final TrechoRodoviaRepository trechoRepository;
    private final IntervencaoOperacionalRepository intervencaoRepository;
    private final MotorPrioridade motor;

    public TrechoService(TrechoRodoviaRepository trechoRepository,
                         IntervencaoOperacionalRepository intervencaoRepository,
                         MotorPrioridade motor) {
        this.trechoRepository = trechoRepository;
        this.intervencaoRepository = intervencaoRepository;
        this.motor = motor;
    }

    // ===== BLOCO 1: consultas (filtros opcionais por rodovia e por prioridade) =====
    @Transactional(readOnly = true)
    public List<TrechoResponse> listar(String rodovia, NivelPrioridade prioridade) {
        List<TrechoRodovia> trechos = (rodovia == null || rodovia.isBlank())
                ? trechoRepository.findAll()
                : trechoRepository.findByCodigoRodoviaIgnoreCase(rodovia.trim());

        return trechos.stream()
                .map(this::paraResponse)
                .filter(r -> prioridade == null || r.prioridade() == prioridade)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrechoResponse buscarPorId(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<TrechoResponse> listarPorDesgasteMinimo(Double minimo) {
        if (minimo == null || minimo < 0 || minimo > 100) {
            throw new RegraNegocioException("O parâmetro 'minimo' deve estar entre 0 e 100");
        }
        return trechoRepository.findByIndiceDesgasteGreaterThanEqualOrderByIndiceDesgasteDesc(minimo)
                .stream().map(this::paraResponse).toList();
    }

    // ===== BLOCO 2: criar =====
    @Transactional
    public TrechoResponse criar(TrechoRequest req) {
        validarRegras(req);
        TrechoRodovia novo = new TrechoRodovia(req.codigoRodovia().trim().toUpperCase(), req.kmInicial(),
                req.kmFinal(), req.indiceDesgaste(), req.acidentesAno(), req.fluxoDiario());
        return paraResponse(trechoRepository.save(novo));
    }

    // ===== BLOCO 3: atualizar (dirty checking do JPA: sem save explícito, o UPDATE sai no commit) =====
    @Transactional
    public TrechoResponse atualizar(Long id, TrechoRequest req) {
        TrechoRodovia trecho = buscarEntidade(id);
        validarRegras(req);
        trecho.atualizarDados(req.codigoRodovia().trim().toUpperCase(), req.kmInicial(), req.kmFinal(),
                req.indiceDesgaste(), req.acidentesAno(), req.fluxoDiario());
        return paraResponse(trecho);
    }

    // ===== BLOCO 4: remover (bloqueia se houver intervenção: era o ORA-02292 da Sprint 3) =====
    @Transactional
    public void remover(Long id) {
        TrechoRodovia trecho = buscarEntidade(id);
        if (intervencaoRepository.existsByTrechoId(id)) {
            throw new ConflitoException("Trecho " + id + " possui intervenções vinculadas; remova-as antes");
        }
        trechoRepository.delete(trecho);
    }

    // ===== BLOCO 5: validacoes_de_negocio (regra que cruza campos não cabe em anotação do DTO) =====
    private void validarRegras(TrechoRequest req) {
        if (req.kmFinal() <= req.kmInicial()) {
            throw new RegraNegocioException("kmFinal (" + req.kmFinal() + ") deve ser maior que kmInicial (" + req.kmInicial() + ")");
        }
        if (req.indiceDesgaste() < 0 || req.indiceDesgaste() > 100) {
            throw new RegraNegocioException("indiceDesgaste deve estar entre 0 e 100");
        }
    }

    // ===== BLOCO 6: auxiliares =====
    TrechoRodovia buscarEntidade(Long id) {
        return trechoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trecho", id));
    }

    private TrechoResponse paraResponse(TrechoRodovia t) {
        return TrechoResponse.de(t, motor.classificar(t));
    }
}
