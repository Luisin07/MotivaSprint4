/**
 * RegraNegocioException.java
 * Lançada quando uma validação de negócio do service falha -- o GlobalExceptionHandler devolve 400.
 */
package br.com.motiva.exception;

public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
