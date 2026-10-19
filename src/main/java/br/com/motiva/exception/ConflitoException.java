/**
 * ConflitoException.java
 * Lançada ao tentar apagar registro que ainda tem filhos (o antigo ORA-02292) -- vira 409.
 */
package br.com.motiva.exception;

public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
