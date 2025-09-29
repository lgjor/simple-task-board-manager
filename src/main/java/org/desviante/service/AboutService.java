package org.desviante.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.desviante.util.WindowManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Serviço para gerenciar a exibição da tela "Sobre" da aplicação.
 * 
 * <p>Este serviço é responsável por carregar e exibir a tela About
 * de forma modal, seguindo os padrões de design da aplicação.</p>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Carregamento do arquivo FXML da tela About</li>
 *   <li>Configuração da janela modal</li>
 *   <li>Integração com o WindowManager para controle de janelas</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see WindowManager
 */
@Service
@Slf4j
public class AboutService {

    private final WindowManager windowManager;
    
    private static final String ABOUT_FXML_PATH = "/view/about.fxml";
    private static final String ABOUT_CSS_PATH = "/css/about.css";
    private static final String WINDOW_TITLE = "Sobre - Simple Task Board Manager";
    private static final double WINDOW_WIDTH = 600;
    private static final double WINDOW_HEIGHT = 700;

    /**
     * Construtor que recebe as dependências necessárias.
     * 
     * @param windowManager Gerenciador de janelas da aplicação
     */
    @Autowired
    public AboutService(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * Exibe a tela About como uma janela modal.
     * 
     * <p>Este método carrega o arquivo FXML, configura a janela
     * e a exibe de forma modal sobre a janela principal.</p>
     * 
     * @return true se a tela foi exibida com sucesso, false caso contrário
     */
    public boolean showAboutDialog() {
        try {
            log.debug("Carregando tela About...");
            
            // Carrega o FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ABOUT_FXML_PATH));
            Parent root = loader.load();
            
            // Cria a cena
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            // Aplica o CSS
            scene.getStylesheets().add(getClass().getResource(ABOUT_CSS_PATH).toExternalForm());
            
            // Cria e configura a janela
            Stage aboutStage = new Stage();
            aboutStage.setTitle(WINDOW_TITLE);
            aboutStage.setScene(scene);
            aboutStage.initModality(Modality.APPLICATION_MODAL);
            aboutStage.initStyle(StageStyle.DECORATED);
            aboutStage.setResizable(false);
            aboutStage.setMinWidth(WINDOW_WIDTH);
            aboutStage.setMinHeight(WINDOW_HEIGHT);
            
            // Centraliza a janela
            aboutStage.centerOnScreen();
            
            // Registra a janela no WindowManager
            windowManager.registerWindow(aboutStage, WINDOW_TITLE);
            
            // Exibe a janela
            aboutStage.show();
            
            log.info("Tela About exibida com sucesso");
            return true;
            
        } catch (IOException e) {
            log.error("Erro ao carregar a tela About", e);
            return false;
        } catch (Exception e) {
            log.error("Erro inesperado ao exibir a tela About", e);
            return false;
        }
    }

    /**
     * Verifica se a tela About está atualmente visível.
     * 
     * @return true se a tela About estiver visível, false caso contrário
     */
    public boolean isAboutDialogVisible() {
        return windowManager.hasOpenWindows();
    }

    /**
     * Fecha a tela About se estiver aberta.
     * 
     * @return true se a tela foi fechada, false se não estava aberta
     */
    public boolean closeAboutDialog() {
        if (windowManager.hasOpenWindows()) {
            windowManager.closeAllSecondaryWindows();
            return true;
        }
        return false;
    }
}
