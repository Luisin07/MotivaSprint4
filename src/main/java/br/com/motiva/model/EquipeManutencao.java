/**
 * EquipeManutencao.java
 * Entity da equipe de manutenção -- mapeia T_MOT_EQUIPE (era o record + EquipeManutencaoDAO na Sprint 3).
 */
package br.com.motiva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_MOT_EQUIPE")
public class EquipeManutencao {

    // ===== BLOCO 1: chave_primaria =====
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqEquipe")
    @SequenceGenerator(name = "seqEquipe", sequenceName = "SEQ_MOT_EQUIPE", allocationSize = 1)
    @Column(name = "ID_EQUIPE")
    private Long id;

    // ===== BLOCO 2: colunas =====
    @Column(name = "NM_EQUIPE", nullable = false, length = 80)
    private String nome;

    @Column(name = "NM_RESPONSAVEL", nullable = false, length = 80)
    private String responsavel;

    @Column(name = "QT_MEMBROS", nullable = false)
    private Integer qtMembros;

    @Column(name = "DS_ESPECIALIDADE", nullable = false, length = 60)
    private String especialidade;

    // CHAR(1) 'S'/'N' no Oracle vira boolean no Java
    @Convert(converter = SimNaoConverter.class)
    @Column(name = "ST_ATIVA", nullable = false, length = 1)
    private Boolean ativa;

    // ===== BLOCO 3: construtores =====
    protected EquipeManutencao() { }

    public EquipeManutencao(String nome, String responsavel, Integer qtMembros,
                            String especialidade, Boolean ativa) {
        this.nome = nome;
        this.responsavel = responsavel;
        this.qtMembros = qtMembros;
        this.especialidade = especialidade;
        this.ativa = ativa;
    }

    // ===== BLOCO 4: comportamento =====
    public void atualizarDados(String nome, String responsavel, Integer qtMembros,
                               String especialidade, Boolean ativa) {
        this.nome = nome;
        this.responsavel = responsavel;
        this.qtMembros = qtMembros;
        this.especialidade = especialidade;
        this.ativa = ativa;
    }

    // ===== BLOCO 5: getters =====
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getResponsavel() { return responsavel; }
    public Integer getQtMembros() { return qtMembros; }
    public String getEspecialidade() { return especialidade; }
    public Boolean getAtiva() { return ativa; }
}
