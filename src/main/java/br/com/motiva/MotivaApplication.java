/**
 * MotivaApplication.java
 * Ponto de entrada da API -- substitui o Main.java da Sprint 3 (a interface agora é HTTP/JSON).
 */
package br.com.motiva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MotivaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotivaApplication.class, args);
    }
}
