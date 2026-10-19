/**
 * RelatorioPrioridade.java
 * Entity do histórico de relatórios -- mapeia T_MOT_RELATORIO, gravada pelo service/GeradorRelatorio.
 */
package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_MOT_RELATORIO")
public class RelatorioPrioridade {

    // ===== BLOCO 1: chave_primaria =====
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqRelatorio")
    @SequenceGenerator(name = "seqRelatorio", sequenceName = "SEQ_MOT_RELATORIO", allocationSize = 1)
    @Column(name = "ID_RELATORIO")
    private Long id;

    // ===== BLOCO 2: colunas =====
    @Column(name = "DT_GERACAO", nullable = false)
    private LocalDateTime dataGeracao;

    @Column(name = "QT_URGENTE", nullable = false)
    private Integer qtUrgente;

    @Column(name = "QT_CRITICO", nullable = false)
    private Integer qtCritico;

    @Column(name = "QT_ATENCAO", nullable = false)
    private Integer qtAtencao;

    @Column(name = "QT_NORMAL", nullable = false)
    private Integer qtNormal;

    @Column(name = "DS_RESUMO", length = 1000)
    private String resumo;

    // ===== BLOCO 3: construtores =====
    protected RelatorioPrioridade() { }

    public RelatorioPrioridade(Integer qtUrgente, Integer qtCritico, Integer qtAtencao,
                               Integer qtNormal, String resumo) {
        this.qtUrgente = qtUrgente;
        this.qtCritico = qtCritico;
        this.qtAtencao = qtAtencao;
        this.qtNormal = qtNormal;
        this.resumo = resumo;
    }

    // ===== BLOCO 4: data_geracao (o JPA envia a coluna no INSERT, então o DEFAULT SYSTIMESTAMP não age) =====
    @PrePersist
    void preencherDataGeracao() {
        if (dataGeracao == null) {
            dataGeracao = LocalDateTime.now();
        }
    }

    // ===== BLOCO 5: getters =====
    public Long getId() { return id; }
    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public Integer getQtUrgente() { return qtUrgente; }
    public Integer getQtCritico() { return qtCritico; }
    public Integer getQtAtencao() { return qtAtencao; }
    public Integer getQtNormal() { return qtNormal; }
    public String getResumo() { return resumo; }
}
