/**
 * IntervencaoOperacional.java
 * Entity da intervenção -- mapeia T_MOT_INTERVENCAO e liga um trecho a uma equipe via @ManyToOne.
 * Na Sprint 3 eram dois ints (idTrecho, idEquipe); agora o JPA resolve as FKs como objetos.
 */
package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "T_MOT_INTERVENCAO")
public class IntervencaoOperacional {

    // ===== BLOCO 1: chave_primaria =====
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqIntervencao")
    @SequenceGenerator(name = "seqIntervencao", sequenceName = "SEQ_MOT_INTERVENCAO", allocationSize = 1)
    @Column(name = "ID_INTERVENCAO")
    private Long id;

    // ===== BLOCO 2: relacionamentos (FK_MOT_INTERV_TRECHO e FK_MOT_INTERV_EQUIPE) =====
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TRECHO", nullable = false)
    private TrechoRodovia trecho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_EQUIPE", nullable = false)
    private EquipeManutencao equipe;

    // ===== BLOCO 3: colunas =====
    @Column(name = "DS_TIPO", nullable = false, length = 60)
    private String tipo;

    @Column(name = "DT_INICIO", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "DT_FIM")
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "DS_STATUS", nullable = false, length = 20)
    private StatusIntervencao status;

    // ===== BLOCO 4: construtores =====
    protected IntervencaoOperacional() { }

    public IntervencaoOperacional(TrechoRodovia trecho, EquipeManutencao equipe, String tipo,
                                  LocalDate dataInicio, LocalDate dataFim, StatusIntervencao status) {
        this.trecho = trecho;
        this.equipe = equipe;
        this.tipo = tipo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
    }

    // ===== BLOCO 5: comportamento =====
    public void atualizarDados(TrechoRodovia trecho, EquipeManutencao equipe, String tipo,
                               LocalDate dataInicio, LocalDate dataFim, StatusIntervencao status) {
        this.trecho = trecho;
        this.equipe = equipe;
        this.tipo = tipo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
    }

    // ===== BLOCO 6: getters =====
    public Long getId() { return id; }
    public TrechoRodovia getTrecho() { return trecho; }
    public EquipeManutencao getEquipe() { return equipe; }
    public String getTipo() { return tipo; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public StatusIntervencao getStatus() { return status; }
}
