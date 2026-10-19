/**
 * TrechoRodovia.java
 * Entity do trecho de rodovia -- mapeia T_MOT_TRECHO (era o record + TrechoRodoviaDAO na Sprint 3).
 * A regra de prioridade saiu daqui e foi para service/MotorPrioridade.
 */
package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_MOT_TRECHO")
public class TrechoRodovia {

    // ===== BLOCO 1: chave_primaria (ligada à SEQ_MOT_TRECHO do script-criacao.sql) =====
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqTrecho")
    @SequenceGenerator(name = "seqTrecho", sequenceName = "SEQ_MOT_TRECHO", allocationSize = 1)
    @Column(name = "ID_TRECHO")
    private Long id;

    // ===== BLOCO 2: colunas =====
    @Column(name = "CD_RODOVIA", nullable = false, length = 15)
    private String codigoRodovia;

    @Column(name = "KM_INICIAL", nullable = false)
    private Double kmInicial;

    @Column(name = "KM_FINAL", nullable = false)
    private Double kmFinal;

    @Column(name = "NR_INDICE_DESGASTE", nullable = false)
    private Double indiceDesgaste;

    @Column(name = "QT_ACIDENTES_ANO", nullable = false)
    private Integer acidentesAno;

    @Column(name = "VL_FLUXO_DIARIO", nullable = false)
    private Integer fluxoDiario;

    // ===== BLOCO 3: construtores (JPA exige o vazio) =====
    protected TrechoRodovia() { }

    public TrechoRodovia(String codigoRodovia, Double kmInicial, Double kmFinal,
                         Double indiceDesgaste, Integer acidentesAno, Integer fluxoDiario) {
        this.codigoRodovia = codigoRodovia;
        this.kmInicial = kmInicial;
        this.kmFinal = kmFinal;
        this.indiceDesgaste = indiceDesgaste;
        this.acidentesAno = acidentesAno;
        this.fluxoDiario = fluxoDiario;
    }

    // ===== BLOCO 4: comportamento =====
    public double extensaoKm() {
        return kmFinal - kmInicial;
    }

    public void atualizarDados(String codigoRodovia, Double kmInicial, Double kmFinal,
                               Double indiceDesgaste, Integer acidentesAno, Integer fluxoDiario) {
        this.codigoRodovia = codigoRodovia;
        this.kmInicial = kmInicial;
        this.kmFinal = kmFinal;
        this.indiceDesgaste = indiceDesgaste;
        this.acidentesAno = acidentesAno;
        this.fluxoDiario = fluxoDiario;
    }

    // ===== BLOCO 5: getters (sem setters públicos: alteração só via atualizarDados) =====
    public Long getId() { return id; }
    public String getCodigoRodovia() { return codigoRodovia; }
    public Double getKmInicial() { return kmInicial; }
    public Double getKmFinal() { return kmFinal; }
    public Double getIndiceDesgaste() { return indiceDesgaste; }
    public Integer getAcidentesAno() { return acidentesAno; }
    public Integer getFluxoDiario() { return fluxoDiario; }
}
