/**
 * MotorPrioridade.java
 * Motor de classificação da Sprint 2/3 -- antes vivia em TrechoRodovia.calcularPrioridade(), agora é um @Service.
 * Usado por TrechoService (prioridade de cada trecho) e GeradorRelatorio (contagem do relatório).
 */
package br.com.motiva.service;

import br.com.motiva.model.NivelPrioridade;
import br.com.motiva.model.TrechoRodovia;
import org.springframework.stereotype.Service;

@Service
public class MotorPrioridade {

    // ===== BLOCO 1: limiares (mesmos da Sprint 3: desgaste em % e acidentes por ano) =====
    static final double DESGASTE_URGENTE = 80;
    static final double DESGASTE_CRITICO = 60;
    static final double DESGASTE_ATENCAO = 40;
    static final int ACIDENTES_URGENTE = 15;
    static final int ACIDENTES_CRITICO = 10;
    static final int ACIDENTES_ATENCAO = 5;

    // ===== BLOCO 2: classificar (desgaste alto OU muito acidente já sobe o nível) =====
    public NivelPrioridade classificar(TrechoRodovia t) {
        double desgaste = t.getIndiceDesgaste();
        int acidentes = t.getAcidentesAno();

        if (desgaste >= DESGASTE_URGENTE || acidentes >= ACIDENTES_URGENTE) return NivelPrioridade.URGENTE;
        if (desgaste >= DESGASTE_CRITICO || acidentes >= ACIDENTES_CRITICO) return NivelPrioridade.CRITICO;
        if (desgaste >= DESGASTE_ATENCAO || acidentes >= ACIDENTES_ATENCAO) return NivelPrioridade.ATENCAO;
        return NivelPrioridade.NORMAL;
    }
}
