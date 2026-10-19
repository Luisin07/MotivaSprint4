/**
 * GlobalExceptionHandler.java
 * @RestControllerAdvice que transforma exceções em JSON padronizado (ErroResponse) com o código HTTP certo.
 * Mantém os controllers limpos: nenhum try/catch neles.
 */
package br.com.motiva.exception;

import br.com.motiva.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===== BLOCO 1: erros_de_negocio (404 / 400 / 409) =====
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    // ===== BLOCO 2: bean_validation (@Valid no DTO -> 400 com a lista de campos) =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos", req, campos);
    }

    // ===== BLOCO 3: requisicao_mal_formada (JSON quebrado, enum/data inválidos, ID não numérico) =====
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: confira o JSON, os tipos, o status e as datas (yyyy-MM-dd)", req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoErrado(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "Valor inválido para '" + ex.getName() + "': " + ex.getValue(), req, null);
    }

    // ===== BLOCO 4: integridade_do_banco (CHECK/FK do Oracle que escapou da validação do service) =====
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT, "Operação viola uma restrição do banco de dados", req, null);
    }

    // ===== BLOCO 5: generico (exceções do Spring MVC mantêm o status delas: 404 de rota, 405, parâmetro faltando) =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> generico(Exception ex, HttpServletRequest req) {
        if (ex instanceof ErrorResponse er) {
            return montar(er.getStatusCode(), er.getBody().getDetail(), req, null);
        }
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", req, null);
    }

    // ===== BLOCO 6: montar_resposta =====
    private ResponseEntity<ErroResponse> montar(HttpStatusCode status, String mensagem,
                                                HttpServletRequest req, Map<String, String> campos) {
        HttpStatus resolvido = HttpStatus.resolve(status.value());
        String erro = resolvido != null ? resolvido.getReasonPhrase() : String.valueOf(status.value());
        ErroResponse corpo = new ErroResponse(LocalDateTime.now(), status.value(), erro, mensagem,
                req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
