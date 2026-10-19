/**
 * EquipeRequest.java
 * Contrato de entrada do POST/PUT /api/equipes -- ativa é opcional (default true, igual ao DEFAULT 'S').
 */
package br.com.motiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EquipeRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 80, message = "nome tem no máximo 80 caracteres")
        String nome,

        @NotBlank(message = "responsavel é obrigatório")
        @Size(max = 80, message = "responsavel tem no máximo 80 caracteres")
        String responsavel,

        @NotNull(message = "qtMembros é obrigatório")
        @Positive(message = "qtMembros deve ser maior que zero")
        Integer qtMembros,

        @NotBlank(message = "especialidade é obrigatória")
        @Size(max = 60, message = "especialidade tem no máximo 60 caracteres")
        String especialidade,

        Boolean ativa
) { }
