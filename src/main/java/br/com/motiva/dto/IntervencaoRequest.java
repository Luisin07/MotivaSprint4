/**
 * IntervencaoRequest.java
 * Contrato de entrada do POST/PUT /api/intervencoes -- recebe só os IDs de trecho e equipe.
 */
package br.com.motiva.dto;

import br.com.motiva.model.StatusIntervencao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record IntervencaoRequest(
        @NotNull(message = "idTrecho é obrigatório")
        @Positive(message = "idTrecho inválido")
        Long idTrecho,

        @NotNull(message = "idEquipe é obrigatório")
        @Positive(message = "idEquipe inválido")
        Long idEquipe,

        @NotBlank(message = "tipo é obrigatório")
        @Size(max = 60, message = "tipo tem no máximo 60 caracteres")
        String tipo,

        @NotNull(message = "dataInicio é obrigatória (formato yyyy-MM-dd)")
        LocalDate dataInicio,

        LocalDate dataFim,

        @NotNull(message = "status é obrigatório: PLANEJADA, EM_ANDAMENTO, CONCLUIDA ou CANCELADA")
        StatusIntervencao status
) { }
