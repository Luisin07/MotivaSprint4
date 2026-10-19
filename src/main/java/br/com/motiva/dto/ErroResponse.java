/**
 * ErroResponse.java
 * Corpo padrão de erro devolvido pelo GlobalExceptionHandler (400/404/409/500).
 */
package br.com.motiva.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) { }
