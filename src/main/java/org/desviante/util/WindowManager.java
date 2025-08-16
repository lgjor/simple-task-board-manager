package org.desviante.util;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de janelas secundárias da aplicação.
 * 
 * <p>Esta classe gerencia o ciclo de vida de todas as janelas secundárias
 * da aplicação, garantindo que sejam fechadas adequadamente quando a
 * aplicação principal for encerrada.</p>
 * 
 * @author Aú Desviante
 * @version 1.0
 * @since 1.0
 */
@Component
public class WindowManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WindowManager.class);
    
    /**
     * Conjunto thread-safe para armazenar todas as janelas secundárias.
     * Usa ConcurrentHashMap.newKeySet() para garantir thread-safety.
     */
    private final Set<Stage> secondaryWindows = ConcurrentHashMap.newKeySet();
    
    /**
     * Construtor padrão do gerenciador de janelas.
     * 
     * <p>Este gerenciador não requer inicialização especial.</p>
     */
    public WindowManager() {
        // Gerenciador automático de janelas
    }
    
    /**
     * Registra uma nova janela secundária para gerenciamento.
     * 
     * <p>Este método deve ser chamado sempre que uma nova janela for criada.
     * A janela será automaticamente fechada quando a aplicação principal for encerrada.</p>
     * 
     * @param stage A janela secundária a ser registrada
     * @param windowTitle Título da janela para identificação nos logs
     */
    public void registerWindow(Stage stage, String windowTitle) {
        if (stage == null) {
            logger.warn("Tentativa de registrar janela nula");
            return;
        }
        
        // Configurar listener para remover a janela do registro quando fechada
        stage.setOnCloseRequest(event -> {
            secondaryWindows.remove(stage);
            logger.debug("Janela '{}' removida do registro", windowTitle);
        });
        
        // Adicionar ao conjunto de janelas gerenciadas
        secondaryWindows.add(stage);
        logger.debug("Janela '{}' registrada para gerenciamento (total: {})", 
                    windowTitle, secondaryWindows.size());
    }
    
    /**
     * Registra uma nova janela secundária sem título específico.
     * 
     * @param stage A janela secundária a ser registrada
     */
    public void registerWindow(Stage stage) {
        registerWindow(stage, "Sem título");
    }
    
    /**
     * Fecha todas as janelas secundárias registradas.
     * 
     * <p>Este método é chamado automaticamente quando a aplicação principal
     * está sendo encerrada para garantir que não haja janelas órfãs.</p>
     */
    public void closeAllSecondaryWindows() {
        if (secondaryWindows.isEmpty()) {
            logger.debug("Nenhuma janela secundária para fechar");
            return;
        }
        
        logger.info("Fechando {} janela(s) secundária(s)", secondaryWindows.size());
        
        // Fechar todas as janelas de forma segura
        secondaryWindows.forEach(stage -> {
            try {
                if (stage.isShowing()) {
                    stage.close();
                    logger.debug("Janela fechada com sucesso");
                }
            } catch (Exception e) {
                logger.warn("Erro ao fechar janela: {}", e.getMessage());
            }
        });
        
        // Limpar o conjunto
        secondaryWindows.clear();
        logger.info("Todas as janelas secundárias foram fechadas");
    }
    
    /**
     * Obtém o número de janelas secundárias atualmente abertas.
     * 
     * @return Número de janelas registradas
     */
    public int getOpenWindowCount() {
        return secondaryWindows.size();
    }
    
    /**
     * Verifica se há janelas secundárias abertas.
     * 
     * @return true se houver janelas abertas, false caso contrário
     */
    public boolean hasOpenWindows() {
        return !secondaryWindows.isEmpty();
    }
    
    /**
     * Fecha uma janela específica e a remove do registro.
     * 
     * @param stage A janela a ser fechada
     * @return true se a janela foi fechada com sucesso, false caso contrário
     */
    public boolean closeWindow(Stage stage) {
        if (stage == null || !secondaryWindows.contains(stage)) {
            return false;
        }
        
        try {
            if (stage.isShowing()) {
                stage.close();
            }
            secondaryWindows.remove(stage);
            logger.debug("Janela específica fechada e removida do registro");
            return true;
        } catch (Exception e) {
            logger.warn("Erro ao fechar janela específica: {}", e.getMessage());
            return false;
        }
    }
}
