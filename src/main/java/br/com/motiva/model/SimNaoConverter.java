/**
 * SimNaoConverter.java
 * Converte a coluna CHAR(1) 'S'/'N' (ST_ATIVA) em Boolean -- usado por EquipeManutencao.
 */
package br.com.motiva.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SimNaoConverter implements AttributeConverter<Boolean, String> {

    // ===== BLOCO 1: java_para_banco =====
    @Override
    public String convertToDatabaseColumn(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? "S" : "N";
    }

    // ===== BLOCO 2: banco_para_java =====
    @Override
    public Boolean convertToEntityAttribute(String valor) {
        return "S".equalsIgnoreCase(valor == null ? null : valor.trim());
    }
}
