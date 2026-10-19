/**
 * MotorPrioridadeTest.java
 * Teste unitário dos limiares do motor de prioridade (sem Spring, sem banco).
 */
package br.com.motiva.service;

import br.com.motiva.model.NivelPrioridade;
import br.com.motiva.model.TrechoRodovia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MotorPrioridadeTest {

    private final MotorPrioridade motor = new MotorPrioridade();

    private TrechoRodovia trecho(double desgaste, int acidentes) {
        return new TrechoRodovia("SP-000", 0.0, 1.0, desgaste, acidentes, 1000);
    }

    // ===== BLOCO 1: limites_de_desgaste (limiar é inclusivo: >=) =====
    @Test
    void classificaPorDesgaste() {
        assertEquals(NivelPrioridade.URGENTE, motor.classificar(trecho(80, 0)));
        assertEquals(NivelPrioridade.CRITICO, motor.classificar(trecho(79.99, 0)));
        assertEquals(NivelPrioridade.CRITICO, motor.classificar(trecho(60, 0)));
        assertEquals(NivelPrioridade.ATENCAO, motor.classificar(trecho(40, 0)));
        assertEquals(NivelPrioridade.NORMAL, motor.classificar(trecho(39.99, 0)));
    }

    // ===== BLOCO 2: acidentes_sobem_o_nivel_mesmo_com_desgaste_baixo =====
    @Test
    void classificaPorAcidentes() {
        assertEquals(NivelPrioridade.URGENTE, motor.classificar(trecho(10, 15)));
        assertEquals(NivelPrioridade.CRITICO, motor.classificar(trecho(10, 10)));
        assertEquals(NivelPrioridade.ATENCAO, motor.classificar(trecho(10, 5)));
        assertEquals(NivelPrioridade.NORMAL, motor.classificar(trecho(10, 4)));
    }
}
