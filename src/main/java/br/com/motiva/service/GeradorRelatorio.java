/**
 * GeradorRelatorio.java
 * Gera o relatório de prioridade a partir dos trechos atuais e grava no histórico (T_MOT_RELATORIO).
 * Mesma lógica da Sprint 3, sem System.out: o "console" agora é o JSON devolvido pelo RelatorioController.
 */
package br.com.motiva.service;

import br.com.motiva.dto.RelatorioGeradoResponse;
import br.com.motiva.dto.RelatorioResponse;
import br.com.motiva.dto.TrechoResponse;
import br.com.motiva.exception.RecursoNaoEncontradoException;
import br.com.motiva.exception.RegraNegocioException;
import br.com.motiva.model.NivelPrioridade;
import br.com.motiva.model.RelatorioPrioridade;
import br.com.motiva.repository.RelatorioPrioridadeRepository;
import br.com.motiva.repository.TrechoRodoviaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class GeradorRelatorio {

    private static final LocalTime FIM_DO_DIA = LocalTime.of(23, 59, 59, 999_999_000);

    private final TrechoRodoviaRepository trechoRepository;
    private final RelatorioPrioridadeRepository relatorioRepository;
    private final MotorPrioridade motor;

    public GeradorRelatorio(TrechoRodoviaRepository trechoRepository,
                            RelatorioPrioridadeRepository relatorioRepository,
                            MotorPrioridade motor) {
        this.trechoRepository = trechoRepository;
        this.relatorioRepository = relatorioRepository;
        this.motor = motor;
    }

    // ===== BLOCO 1: gerar_relatorio (classifica, conta, persiste) =====
    @Transactional
    public RelatorioGeradoResponse gerar() {
        List<TrechoResponse> classificados = trechoRepository.findAll().stream()
                .map(t -> TrechoResponse.de(t, motor.classificar(t)))
                .sorted(Comparator.comparing(TrechoResponse::prioridade)
                        .thenComparing(TrechoResponse::indiceDesgaste, Comparator.reverseOrder()))
                .toList();

        Map<NivelPrioridade, Integer> contagem = new EnumMap<>(NivelPrioridade.class);
        for (NivelPrioridade n : NivelPrioridade.values()) contagem.put(n, 0);
        classificados.forEach(t -> contagem.merge(t.prioridade(), 1, Integer::sum));

        String resumo = montarResumo(classificados.size(), contagem);
        RelatorioPrioridade salvo = relatorioRepository.save(new RelatorioPrioridade(
                contagem.get(NivelPrioridade.URGENTE), contagem.get(NivelPrioridade.CRITICO),
                contagem.get(NivelPrioridade.ATENCAO), contagem.get(NivelPrioridade.NORMAL), resumo));

        return new RelatorioGeradoResponse(RelatorioResponse.de(salvo), classificados);
    }

    // ===== BLOCO 2: historico =====
    @Transactional(readOnly = true)
    public List<RelatorioResponse> listarHistorico() {
        return relatorioRepository.findAllByOrderByDataGeracaoDesc().stream()
                .map(RelatorioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public RelatorioResponse buscarPorId(Long id) {
        return relatorioRepository.findById(id)
                .map(RelatorioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Relatório", id));
    }

    // ===== BLOCO 3: consulta_por_periodo (fim inclusivo; microssegundos porque o TIMESTAMP do Oracle tem 6 casas) =====
    @Transactional(readOnly = true)
    public List<RelatorioResponse> consultarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("'inicio' (" + inicio + ") não pode ser depois de 'fim' (" + fim + ")");
        }
        return relatorioRepository
                .findByDataGeracaoBetweenOrderByDataGeracaoDesc(inicio.atStartOfDay(), fim.atTime(FIM_DO_DIA))
                .stream().map(RelatorioResponse::de).toList();
    }

    // ===== BLOCO 4: resumo (mesmo formato do console da Sprint 3) =====
    private String montarResumo(int total, Map<NivelPrioridade, Integer> c) {
        return String.format("Total: %d trechos | URGENTE: %d | CRITICO: %d | ATENCAO: %d | NORMAL: %d",
                total, c.get(NivelPrioridade.URGENTE), c.get(NivelPrioridade.CRITICO),
                c.get(NivelPrioridade.ATENCAO), c.get(NivelPrioridade.NORMAL));
    }
}
