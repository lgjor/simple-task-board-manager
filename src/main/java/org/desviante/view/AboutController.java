package org.desviante.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.awt.Desktop;
import java.net.URI;
import java.io.IOException;

/**
 * Controlador para a tela "Sobre" da aplicação.
 * 
 * <p>Esta classe gerencia a interface de usuário para exibir informações
 * sobre o aplicativo, incluindo versão, desenvolvedor, tecnologias utilizadas
 * e funcionalidades principais.</p>
 * 
 * <p><strong>Responsabilidades Principais:</strong></p>
 * <ul>
 *   <li>Exibição de informações sobre o aplicativo</li>
 *   <li>Gerenciamento de links externos (GitHub, Licença, Changelog)</li>
 *   <li>Controle da janela de informações</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Component
@Slf4j
public class AboutController {

    @FXML private Label appTitleLabel;
    @FXML private Label appVersionLabel;
    @FXML private Label appTaglineLabel;
    @FXML private TextArea appDescriptionArea;
    @FXML private Label developerLabel;
    @FXML private Label copyrightLabel;
    @FXML private Hyperlink githubLink;
    @FXML private Hyperlink licenseLink;
    @FXML private Hyperlink changelogLink;

    /**
     * Inicializa o controller após o FXML ser carregado.
     * 
     * <p>Este método é chamado automaticamente pelo JavaFX após o carregamento
     * do arquivo FXML. Ele configura o conteúdo da tela com as informações
     * do aplicativo.</p>
     */
    @FXML
    public void initialize() {
        setupStaticTexts();
        setupAppDescription();
        setupLinks();
        log.debug("Tela About inicializada com sucesso");
    }

    private void setupStaticTexts() {
        if (appTitleLabel != null) {
            appTitleLabel.setText("Simple Task Board Manager");
        }
        if (appVersionLabel != null) {
            try {
                String version = getClass().getPackage().getImplementationVersion();
                if (version == null || version.isBlank()) {
                    version = "";
                }
                appVersionLabel.setText(version.isBlank() ? "" : "Versão " + version);
            } catch (Exception e) {
                appVersionLabel.setText("");
            }
        }
        if (developerLabel != null) {
            developerLabel.setText("Aú Desviante - Lucas Godoy");
        }
        if (copyrightLabel != null) {
            java.time.Year year = java.time.Year.now();
            copyrightLabel.setText("© " + year + " Aú Desviante. Licenciado sob GPL-3.0");
        }
    }

    /**
     * Configura a descrição do aplicativo.
     */
    private void setupAppDescription() {
        String description = """
            Foque no que é importante e melhore sua produtividade. Com interface minimalista e funcionalidades essenciais, organize suas tarefas em boards visuais e acompanhe o progresso de forma eficiente.
            """;
        
        appDescriptionArea.setText(description);
        appDescriptionArea.setEditable(false);
    }

    /**
     * Configura os links externos da aplicação.
     */
    private void setupLinks() {
        // Links serão configurados pelos métodos de ação
    }

    /**
     * Abre o repositório GitHub do projeto.
     */
    @FXML
    private void openGitHub() {
        openUrl("https://github.com/lgjor/simple-task-board-manager");
    }

    /**
     * Abre a licença do projeto.
     */
    @FXML
    private void openLicense() {
        openUrl("https://github.com/lgjor/simple-task-board-manager/blob/main/LICENSE");
    }

    /**
     * Abre o changelog do projeto.
     */
    @FXML
    private void openChangelog() {
        openUrl("https://github.com/lgjor/simple-task-board-manager/releases");
    }

    /**
     * Abre uma URL no navegador padrão do sistema.
     * 
     * @param url A URL a ser aberta
     */
    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                log.info("URL aberta com sucesso: {}", url);
            } else {
                showUrlError(url);
            }
        } catch (IOException | java.net.URISyntaxException e) {
            log.error("Erro ao abrir URL: {}", url, e);
            showUrlError(url);
        }
    }

    /**
     * Exibe uma mensagem de erro quando não é possível abrir a URL.
     * 
     * @param url A URL que não pôde ser aberta
     */
    private void showUrlError(String url) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText("Link Externo");
        alert.setContentText("Não foi possível abrir o link automaticamente.\n" +
                           "Por favor, copie e cole o seguinte endereço no seu navegador:\n\n" + url);
        
        alert.showAndWait();
    }

    /**
     * Fecha a janela About.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) appTitleLabel.getScene().getWindow();
        stage.close();
        log.debug("Tela About fechada");
    }
}
