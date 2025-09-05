package org.desviante.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Utilitário para abrir URLs no navegador padrão do sistema.
 * 
 * <p>Esta classe fornece métodos para abrir URLs automaticamente no navegador
 * padrão do usuário, com fallback para exibição manual da URL caso a abertura
 * automática falhe. É especialmente útil para fluxos de autenticação OAuth2
 * onde o usuário precisa ser redirecionado para uma página de autorização.</p>
 * 
 * <p>Características:</p>
 * <ul>
 *   <li>Abertura automática do navegador usando Desktop API</li>
 *   <li>Fallback gracioso para exibição manual da URL</li>
 *   <li>Suporte a diferentes sistemas operacionais</li>
 *   <li>Integração com JavaFX para exibição de diálogos</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Desktop
 * @see Platform
 */
@Slf4j
public class BrowserUtils {

    /**
     * Abre a URL especificada no navegador padrão do sistema.
     * 
     * <p>Primeiro tenta abrir automaticamente usando a Desktop API.
     * Se isso falhar, exibe um diálogo JavaFX com a URL para o usuário
     * copiar e colar manualmente no navegador.</p>
     * 
     * @param url A URL a ser aberta no navegador
     * @param title Título do diálogo de fallback (opcional)
     * @param message Mensagem do diálogo de fallback (opcional)
     * @return true se a URL foi aberta automaticamente, false se foi exibida manualmente
     */
    public static boolean openUrlInBrowser(String url, String title, String message) {
        try {
            // Tenta abrir automaticamente usando Desktop API
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                log.info("URL aberta automaticamente no navegador: {}", url);
                return true;
            } else {
                log.warn("Desktop API não suporta abertura de navegador. Exibindo URL manualmente.");
                showUrlDialog(url, title, message);
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            log.warn("Falha ao abrir URL automaticamente: {}", e.getMessage());
            showUrlDialog(url, title, message);
            return false;
        }
    }

    /**
     * Abre a URL especificada no navegador padrão do sistema.
     * 
     * <p>Versão simplificada que usa títulos e mensagens padrão.</p>
     * 
     * @param url A URL a ser aberta no navegador
     * @return true se a URL foi aberta automaticamente, false se foi exibida manualmente
     */
    public static boolean openUrlInBrowser(String url) {
        return openUrlInBrowser(url, "Abrir no Navegador", 
            "Copie a URL abaixo e cole no seu navegador:");
    }

    /**
     * Exibe um diálogo JavaFX com a URL para o usuário copiar manualmente.
     * 
     * <p>Este método é chamado como fallback quando a abertura automática
     * do navegador falha. O diálogo permite que o usuário copie a URL
     * e a cole manualmente no navegador.</p>
     * 
     * @param url A URL a ser exibida
     * @param title Título do diálogo
     * @param message Mensagem explicativa
     */
    private static void showUrlDialog(String url, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(message);
            alert.setContentText(url);
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(600, 200);
            
            // Adiciona botão para copiar a URL
            ButtonType copyButton = new ButtonType("Copiar URL");
            ButtonType okButton = new ButtonType("OK", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(copyButton, okButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == copyButton) {
                // Copia a URL para a área de transferência
                copyToClipboard(url);
            }
        });
    }

    /**
     * Copia o texto especificado para a área de transferência do sistema.
     * 
     * @param text O texto a ser copiado
     */
    private static void copyToClipboard(String text) {
        Platform.runLater(() -> {
            try {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(text);
                clipboard.setContent(content);
                log.info("URL copiada para a área de transferência: {}", text);
            } catch (Exception e) {
                log.warn("Falha ao copiar para a área de transferência: {}", e.getMessage());
            }
        });
    }
}
