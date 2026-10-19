/**
 * RecursoNaoEncontradoException.java
 * Lançada pelos services quando o ID não existe -- o GlobalExceptionHandler devolve 404.
 */
package br.com.motiva.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com ID " + id + " não encontrado(a)");
    }
}
