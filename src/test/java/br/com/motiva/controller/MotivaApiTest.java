/**
 * MotivaApiTest.java
 * Testes de API ponta a ponta com @SpringBootTest + MockMvc (Aula 15) usando H2 em memória.
 * Cada teste roda numa transação que sofre rollback, então um não suja o outro.
 */
package br.com.motiva.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MotivaApiTest {

    @Autowired
    private MockMvc mvc;

    // ===== BLOCO 1: auxiliares (JSON montado à mão para não depender da versão do Jackson) =====
    private String trechoJson(String rodovia, double kmIni, double kmFim, double desgaste, int acidentes) {
        return """
                {"codigoRodovia":"%s","kmInicial":%s,"kmFinal":%s,"indiceDesgaste":%s,"acidentesAno":%d,"fluxoDiario":50000}
                """.formatted(rodovia, kmIni, kmFim, desgaste, acidentes);
    }

    private long criar(String url, String json) throws Exception {
        String corpo = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    private long criarEquipe(boolean ativa) throws Exception {
        return criar("/api/equipes", """
                {"nome":"Equipe Teste","responsavel":"Fulano","qtMembros":4,"especialidade":"Roçada","ativa":%s}
                """.formatted(ativa));
    }

    private String intervencaoJson(long idTrecho, long idEquipe) {
        return """
                {"idTrecho":%d,"idEquipe":%d,"tipo":"Roçada mecanizada","dataInicio":"2026-10-01","status":"PLANEJADA"}
                """.formatted(idTrecho, idEquipe);
    }

    // ===== BLOCO 2: crud_trecho_feliz (201 -> 200 -> 200 -> 204 -> 404) =====
    @Test
    void crudCompletoDeTrecho() throws Exception {
        String corpo = mvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                        .content(trechoJson("sp-348", 10, 25.5, 85, 2)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/trechos/")))
                .andExpect(jsonPath("$.codigoRodovia").value("SP-348"))
                .andExpect(jsonPath("$.prioridade").value("URGENTE"))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(corpo, "$.id")).longValue();

        mvc.perform(get("/api/trechos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extensaoKm").value(15.5));

        mvc.perform(put("/api/trechos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(trechoJson("SP-348", 10, 25.5, 20, 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioridade").value("NORMAL"));

        mvc.perform(delete("/api/trechos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/trechos/" + id)).andExpect(status().isNotFound());
    }

    // ===== BLOCO 3: erros_404_e_400 =====
    @Test
    void recursoInexistenteDa404() throws Exception {
        mvc.perform(get("/api/trechos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(containsString("999999")));
        mvc.perform(put("/api/equipes/999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"responsavel\":\"Y\",\"qtMembros\":1,\"especialidade\":\"Z\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/intervencoes/999999")).andExpect(status().isNotFound());
    }

    @Test
    void beanValidationDa400ComCampos() throws Exception {
        mvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kmInicial\":-1,\"kmFinal\":5,\"indiceDesgaste\":150,\"acidentesAno\":0,\"fluxoDiario\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.codigoRodovia").exists())
                .andExpect(jsonPath("$.campos.kmInicial").exists())
                .andExpect(jsonPath("$.campos.indiceDesgaste").exists());
    }

    @Test
    void regraDoServiceDa400() throws Exception {
        mvc.perform(post("/api/trechos").contentType(MediaType.APPLICATION_JSON)
                        .content(trechoJson("SP-330", 50, 40, 30, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("kmFinal")));
    }

    @Test
    void parametroComTipoErradoDa400() throws Exception {
        mvc.perform(get("/api/trechos/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/trechos").param("prioridade", "XYZ")).andExpect(status().isBadRequest());
    }

    // ===== BLOCO 4: regras_de_intervencao (equipe inativa 400, trecho com filho 409) =====
    @Test
    void equipeInativaNaoRecebeIntervencao() throws Exception {
        long trecho = criar("/api/trechos", trechoJson("BR-116", 200, 215, 68, 6));
        long equipeInativa = criarEquipe(false);

        mvc.perform(post("/api/intervencoes").contentType(MediaType.APPLICATION_JSON)
                        .content(intervencaoJson(trecho, equipeInativa)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("inativa")));
    }

    @Test
    void naoApagaTrechoComIntervencao() throws Exception {
        long trecho = criar("/api/trechos", trechoJson("SP-280", 30, 41, 52, 3));
        long equipe = criarEquipe(true);
        criar("/api/intervencoes", intervencaoJson(trecho, equipe));

        mvc.perform(get("/api/intervencoes").param("tipo", "Roçada mecanizada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoRodovia").value("SP-280"));

        mvc.perform(delete("/api/trechos/" + trecho)).andExpect(status().isConflict());
    }

    // ===== BLOCO 5: relatorio (gera, persiste, lista histórico e filtra por período) =====
    @Test
    void geraRelatorioEConsultaHistorico() throws Exception {
        criar("/api/trechos", trechoJson("SP-348", 10, 25.5, 85, 12));
        criar("/api/trechos", trechoJson("SP-070", 15, 28, 22, 1));

        mvc.perform(post("/api/relatorios"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relatorio.qtUrgente").value(1))
                .andExpect(jsonPath("$.relatorio.qtNormal").value(1))
                .andExpect(jsonPath("$.relatorio.total").value(2))
                .andExpect(jsonPath("$.trechosClassificados[0].prioridade").value("URGENTE"));

        mvc.perform(get("/api/relatorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        String hoje = LocalDate.now().toString();
        mvc.perform(get("/api/relatorios/periodo").param("inicio", hoje).param("fim", hoje))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/api/relatorios/periodo").param("inicio", "2026-12-31").param("fim", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }
}
