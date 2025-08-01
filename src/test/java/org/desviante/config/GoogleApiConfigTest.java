package org.desviante.config;

import com.google.api.services.tasks.Tasks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Este teste agora só será executado se o arquivo de credenciais existir.
// Isso evita que o build quebre em ambientes onde o arquivo não está configurado.
@EnabledIf("org.desviante.config.GoogleApiConfigTest#credentialsFileExists")
@SpringJUnitConfig(classes = {AppConfig.class, GoogleApiConfig.class})
class GoogleApiConfigTest {

    @Autowired
    // Agora que o teste é condicional, podemos remover 'required = false' para um teste mais estrito.
    private Tasks tasksService;

    @Test
    @DisplayName("Deve carregar o contexto do Spring e criar o bean do serviço Tasks")
    void contextLoadsAndCreatesTasksService() {
        assertNotNull(tasksService, "O bean do serviço Tasks não foi injetado. Verifique a configuração e o arquivo credentials.json.");
    }

    /**
     * Método de condição para o JUnit 5. Retorna true se o arquivo de credenciais for encontrado no classpath.
     * @return true se o arquivo existe, false caso contrário.
     */
    static boolean credentialsFileExists() {
        try (var stream = GoogleApiConfigTest.class.getResourceAsStream("/auth/credentials.json")) {
            boolean exists = stream != null;
            if (!exists) {
                // Imprime um aviso útil para o desenvolvedor
                System.out.println("AVISO: Teste de integração com Google API ignorado. Arquivo 'credentials.json' não encontrado em 'src/main/resources/auth/'.");
            }
            return exists;
        } catch (Exception e) {
            return false;
        }
    }
}