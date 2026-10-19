/**
 * TrechoRequest.java
 * Contrato de entrada do POST/PUT /api/trechos -- validação de formato (Bean Validation).
 * Regra que cruza campos (kmFinal > kmInicial) fica no TrechoService.
 */
package br.com.motiva.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TrechoRequest(
        @NotBlank(message = "codigoRodovia é obrigatório")
        @Size(max = 15, message = "codigoRodovia tem no máximo 15 caracteres")
        String codigoRodovia,

        @NotNull(message = "kmInicial é obrigatório")
        @PositiveOrZero(message = "kmInicial não pode ser negativo")
        Double kmInicial,

        @NotNull(message = "kmFinal é obrigatório")
        @Positive(message = "kmFinal deve ser positivo")
        Double kmFinal,

        @NotNull(message = "indiceDesgaste é obrigatório")
        @DecimalMin(value = "0.0", message = "indiceDesgaste deve ser >= 0")
        @DecimalMax(value = "100.0", message = "indiceDesgaste deve ser <= 100")
        Double indiceDesgaste,

        @NotNull(message = "acidentesAno é obrigatório")
        @PositiveOrZero(message = "acidentesAno não pode ser negativo")
        Integer acidentesAno,

        @NotNull(message = "fluxoDiario é obrigatório")
        @Positive(message = "fluxoDiario deve ser positivo")
        Integer fluxoDiario
) { }
