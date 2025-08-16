package org.desviante.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Gerenciador de ícones para a aplicação.
 * 
 * <p>Esta classe gerencia o carregamento e acesso aos ícones disponíveis
 * no sistema, incluindo ícones emoji e outros tipos de ícones.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class IconManager {
    
    private static final String EMOJI_DIRECTORY = "/icons/emoji/";
    private static List<String> availableIcons = null;
    
    /**
     * Construtor padrão do gerenciador de ícones.
     * 
     * <p>Esta classe não requer inicialização especial.</p>
     */
    public IconManager() {
        // Gerenciador estático de ícones
    }
    
    /**
     * Obtém a lista de todos os ícones disponíveis no sistema.
     * 
     * @return lista de códigos de ícones disponíveis
     */
    public static List<String> getAvailableIcons() {
        if (availableIcons == null) {
            availableIcons = loadAvailableIcons();
        }
        return new ArrayList<>(availableIcons);
    }
    
    /**
     * Carrega a lista de ícones disponíveis do diretório de recursos.
     * 
     * @return lista de códigos de ícones
     */
    private static List<String> loadAvailableIcons() {
        List<String> icons = new ArrayList<>();
        
        try {
            // Carregar lista completa de ícones do arquivo de recursos
            URL iconListUrl = IconManager.class.getResource("/icon_list.txt");
            if (iconListUrl != null) {
                try (Scanner scanner = new Scanner(iconListUrl.openStream(), "UTF-8")) {
                    while (scanner.hasNextLine()) {
                        String iconCode = scanner.nextLine().trim();
                        if (!iconCode.isEmpty()) {
                            icons.add(iconCode);
                        }
                    }
                }
            } else {
                // Fallback: usar lista hardcoded se o arquivo não estiver disponível
                System.err.println("Arquivo icon_list.txt não encontrado, usando lista hardcoded");
                String[] fallbackIcons = {
                    "1f004", "1f0cf", "1f170", "1f171", "1f17e", "1f17f", "1f18e", "1f191", "1f192", "1f193",
                    "1f194", "1f195", "1f196", "1f197", "1f198", "1f199", "1f19a", "1f1e6-1f1e8", "1f1e6-1f1e9",
                    "1f1e6-1f1ea", "1f1e6-1f1eb", "1f1e6-1f1ec", "1f1e6-1f1ee", "1f1e6-1f1f1", "1f1e6-1f1f2",
                    "1f1e6-1f1f4", "1f1e6-1f1f6", "1f1e6-1f1f7", "1f1e6-1f1f8", "1f1e6-1f1f9", "1f1e6-1f1fa",
                    "1f1e6-1f1fc", "1f1e6-1f1fd", "1f1e6-1f1ff", "1f1e6", "1f1e7-1f1e6", "1f1e7-1f1e7",
                    "1f1e7-1f1e9", "1f1e7-1f1ea", "1f1e7-1f1eb", "1f1e7-1f1ec", "1f1e7-1f1ed", "1f1e7-1f1ee",
                    "1f1e7-1f1ef", "1f1e7-1f1f1", "1f1e7-1f1f2", "1f1e7-1f1f3", "1f1e7-1f1f4", "1f1e7-1f1f6",
                    "1f1e7-1f1f7", "1f1e7-1f1f8", "1f1e7-1f1f9", "1f1e7-1f1fb", "1f1e7-1f1fc", "1f1e7-1f1fe",
                    "1f1e7-1f1ff", "1f1e7", "1f1e8-1f1e6", "1f1e8-1f1e8", "1f1e8-1f1e9", "1f1e8-1f1eb",
                    "1f1e8-1f1ec", "1f1e8-1f1ed", "1f1e8-1f1ee", "1f1e8-1f1f0", "1f1e8-1f1f1", "1f1e8-1f1f2",
                    "1f1e8-1f1f3", "1f1e8-1f1f4", "1f1e8-1f1f5", "1f1e8-1f1f7", "1f1e8-1f1fa", "1f1e8-1f1fb",
                    "1f1e8-1f1fc", "1f1e8-1f1fd", "1f1e8-1f1fe", "1f1e8-1f1ff", "1f1e8", "1f1e9-1f1ea",
                    "1f1e9-1f1ec", "1f1e9-1f1ef", "1f1e9-1f1f0", "1f1e9-1f1f2", "1f1e9-1f1f4", "1f1e9-1f1ff",
                    "1f1e9", "1f1ea-1f1e6", "1f1ea-1f1e8", "1f1ea-1f1ea", "1f1ea-1f1ec", "1f1ea-1f1ed",
                    "1f1ea-1f1f7", "1f1ea-1f1f8", "1f1ea-1f1f9", "1f1ea-1f1fa", "1f1ea", "1f1eb-1f1ee",
                    "1f1eb-1f1ef", "1f1eb-1f1f0", "1f1eb-1f1f2", "1f1eb-1f1f4", "1f1eb-1f1f7", "1f1eb",
                    "1f1ec-1f1e6", "1f1ec-1f1e7", "1f1ec-1f1e9", "1f1ec-1f1ea", "1f1ec-1f1eb", "1f1ec-1f1ec",
                    "1f1ec-1f1ed", "1f1ec-1f1ee", "1f1ec-1f1f1", "1f1ec-1f1f2", "1f1ec-1f1f3", "1f1ec-1f1f5",
                    "1f1ec-1f1f6", "1f1ec-1f1f7", "1f1ec-1f1f8", "1f1ec-1f1f9", "1f1ec-1f1fa", "1f1ec-1f1fc",
                    "1f1ec-1f1fe", "1f1ec", "1f1ed-1f1f0", "1f1ed-1f1f2", "1f1ed-1f1f3", "1f1ed-1f1f7",
                    "1f1ed-1f1f9", "1f1ed-1f1fa", "1f1ed", "1f1ee-1f1e8", "1f1ee-1f1e9", "1f1ee-1f1ea",
                    "1f1ee-1f1f1", "1f1ee-1f1f2", "1f1ee-1f1f3", "1f1ee-1f1f4", "1f1ee-1f1f6", "1f1ee-1f1f7",
                    "1f1ee-1f1f8", "1f1ee-1f1f9", "1f1ee", "1f1ef-1f1ea", "1f1ef-1f1f2", "1f1ef-1f1f4",
                    "1f1ef-1f1f5", "1f1ef", "1f1f0-1f1ea", "1f1f0-1f1ec", "1f1f0-1f1ed", "1f1f0-1f1ee",
                    "1f1f0-1f1f2", "1f1f0-1f1f3", "1f1f0-1f1f5", "1f1f0-1f1f7", "1f1f0-1f1fc", "1f1f0-1f1fe",
                    "1f1f0-1f1ff", "1f1f0", "1f1f1-1f1e6", "1f1f1-1f1e7", "1f1f1-1f1e8", "1f1f1-1f1e9",
                    "1f1f1-1f1ea", "1f1f1-1f1eb", "1f1f1-1f1ec", "1f1f1-1f1ed", "1f1f1-1f1ee", "1f1f1-1f1ef",
                    "1f1f1-1f1f0", "1f1f1-1f1f1", "1f1f1-1f1f2", "1f1f1-1f1f3", "1f1f1-1f1f4", "1f1f1-1f1f5",
                    "1f1f1-1f1f6", "1f1f1-1f1f7", "1f1f1-1f1f8", "1f1f1-1f1f9", "1f1f1-1f1fa", "1f1f1-1f1fb",
                    "1f1f1-1f1fc", "1f1f1-1f1fd", "1f1f1-1f1fe", "1f1f1-1f1ff", "1f1f1", "1f1f2-1f1e6",
                    "1f1f2-1f1e7", "1f1f2-1f1e8", "1f1f2-1f1e9", "1f1f2-1f1ea", "1f1f2-1f1eb", "1f1f2-1f1ec",
                    "1f1f2-1f1ed", "1f1f2-1f1ee", "1f1f2-1f1ef", "1f1f2-1f1f0", "1f1f2-1f1f1", "1f1f2-1f1f2",
                    "1f1f2-1f1f3", "1f1f2-1f1f4", "1f1f2-1f1f5", "1f1f2-1f1f6", "1f1f2-1f1f7", "1f1f2-1f1f8",
                    "1f1f2-1f1f9", "1f1f2-1f1fa", "1f1f2-1f1fb", "1f1f2-1f1fc", "1f1f2-1f1fd", "1f1f2-1f1fe",
                    "1f1f2-1f1ff", "1f1f2", "1f1f3-1f1e6", "1f1f3-1f1e7", "1f1f3-1f1e8", "1f1f3-1f1e9",
                    "1f1f3-1f1ea", "1f1f3-1f1eb", "1f1f3-1f1ec", "1f1f3-1f1ed", "1f1f3-1f1ee", "1f1f3-1f1ef",
                    "1f1f3-1f1f0", "1f1f3-1f1f1", "1f1f3-1f1f2", "1f1f3-1f1f3", "1f1f3-1f1f4", "1f1f3-1f1f5",
                    "1f1f3-1f1f6", "1f1f3-1f1f7", "1f1f3-1f1f8", "1f1f3-1f1f9", "1f1f3-1f1fa", "1f1f3-1f1fb",
                    "1f1f3-1f1fc", "1f1f3-1f1fd", "1f1f3-1f1fe", "1f1f3-1f1ff", "1f1f3", "1f1f4-1f1e6",
                    "1f1f4-1f1e7", "1f1f4-1f1e8", "1f1f4-1f1e9", "1f1f4-1f1ea", "1f1f4-1f1eb", "1f1f4-1f1ec",
                    "1f1f4-1f1ed", "1f1f4-1f1ee", "1f1f4-1f1ef", "1f1f4-1f1f0", "1f1f4-1f1f1", "1f1f4-1f1f2",
                    "1f1f4-1f1f3", "1f1f4-1f1f4", "1f1f4-1f1f5", "1f1f4-1f1f6", "1f1f4-1f1f7", "1f1f4-1f1f8",
                    "1f1f4-1f1f9", "1f1f4-1f1fa", "1f1f4-1f1fb", "1f1f4-1f1fc", "1f1f4-1f1fd", "1f1f4-1f1fe",
                    "1f1f4-1f1ff", "1f1f4", "1f1f5-1f1e6", "1f1f5-1f1e7", "1f1f5-1f1e8", "1f1f5-1f1e9",
                    "1f1f5-1f1ea", "1f1f5-1f1eb", "1f1f5-1f1ec", "1f1f5-1f1ed", "1f1f5-1f1ee", "1f1f5-1f1ef",
                    "1f1f5-1f1f0", "1f1f5-1f1f1", "1f1f5-1f1f2", "1f1f5-1f1f3", "1f1f5-1f1f4", "1f1f5-1f1f5",
                    "1f1f5-1f1f6", "1f1f5-1f1f7", "1f1f5-1f1f8", "1f1f5-1f1f9", "1f1f5-1f1fa", "1f1f5-1f1fb",
                    "1f1f5-1f1fc", "1f1f5-1f1fd", "1f1f5-1f1fe", "1f1f5-1f1ff", "1f1f5", "1f1f6-1f1e6",
                    "1f1f6-1f1e7", "1f1f6-1f1e8", "1f1f6-1f1e9", "1f1f6-1f1ea", "1f1f6-1f1eb", "1f1f6-1f1ec",
                    "1f1f6-1f1ed", "1f1f6-1f1ee", "1f1f6-1f1ef", "1f1f6-1f1f0", "1f1f6-1f1f1", "1f1f6-1f1f2",
                    "1f1f6-1f1f3", "1f1f6-1f1f4", "1f1f6-1f1f5", "1f1f6-1f1f6", "1f1f6-1f1f7", "1f1f6-1f1f8",
                    "1f1f6-1f1f9", "1f1f6-1f1fa", "1f1f6-1f1fb", "1f1f6-1f1fc", "1f1f6-1f1fd", "1f1f6-1f1fe",
                    "1f1f6-1f1ff", "1f1f6", "1f1f7-1f1e6", "1f1f7-1f1e7", "1f1f7-1f1e8", "1f1f7-1f1e9",
                    "1f1f7-1f1ea", "1f1f7-1f1eb", "1f1f7-1f1ec", "1f1f7-1f1ed", "1f1f7-1f1ee", "1f1f7-1f1ef",
                    "1f1f7-1f1f0", "1f1f7-1f1f1", "1f1f7-1f1f2", "1f1f7-1f1f3", "1f1f7-1f1f4", "1f1f7-1f1f5",
                    "1f1f7-1f1f6", "1f1f7-1f1f7", "1f1f7-1f1f8", "1f1f7-1f1f9", "1f1f7-1f1fa", "1f1f7-1f1fb",
                    "1f1f7-1f1fc", "1f1f7-1f1fd", "1f1f7-1f1fe", "1f1f7-1f1ff", "1f1f7", "1f1f8-1f1e6",
                    "1f1f8-1f1e7", "1f1f8-1f1e8", "1f1f8-1f1e9", "1f1f8-1f1ea", "1f1f8-1f1eb", "1f1f8-1f1ec",
                    "1f1f8-1f1ed", "1f1f8-1f1ee", "1f1f8-1f1ef", "1f1f8-1f1f0", "1f1f8-1f1f1", "1f1f8-1f1f2",
                    "1f1f8-1f1f3", "1f1f8-1f1f4", "1f1f8-1f1f5", "1f1f8-1f1f6", "1f1f8-1f1f7", "1f1f8-1f1f8",
                    "1f1f8-1f1f9", "1f1f8-1f1fa", "1f1f8-1f1fb", "1f1f8-1f1fc", "1f1f8-1f1fd", "1f1f8-1f1fe",
                    "1f1f8-1f1ff", "1f1f8", "1f1f9-1f1e6", "1f1f9-1f1e7", "1f1f9-1f1e8", "1f1f9-1f1e9",
                    "1f1f9-1f1ea", "1f1f9-1f1eb", "1f1f9-1f1ec", "1f1f9-1f1ed", "1f1f9-1f1ee", "1f1f9-1f1ef",
                    "1f1f9-1f1f0", "1f1f9-1f1f1", "1f1f9-1f1f2", "1f1f9-1f1f3", "1f1f9-1f1f4", "1f1f9-1f1f5",
                    "1f1f9-1f1f6", "1f1f9-1f1f7", "1f1f9-1f1f8", "1f1f9-1f1f9", "1f1f9-1f1fa", "1f1f9-1f1fb",
                    "1f1f9-1f1fc", "1f1f9-1f1fd", "1f1f9-1f1fe", "1f1f9-1f1ff", "1f1f9", "1f1fa-1f1e6",
                    "1f1fa-1f1e7", "1f1fa-1f1e8", "1f1fa-1f1e9", "1f1fa-1f1ea", "1f1fa-1f1eb", "1f1fa-1f1ec",
                    "1f1fa-1f1ed", "1f1fa-1f1ee", "1f1fa-1f1ef", "1f1fa-1f1f0", "1f1fa-1f1f1", "1f1fa-1f1f2",
                    "1f1fa-1f1f3", "1f1fa-1f1f4", "1f1fa-1f1f5", "1f1fa-1f1f6", "1f1fa-1f1f7", "1f1fa-1f1f8",
                    "1f1fa-1f1f9", "1f1fa-1f1fa", "1f1fa-1f1fb", "1f1fa-1f1fc", "1f1fa-1f1fd", "1f1fa-1f1fe",
                    "1f1fa-1f1ff", "1f1fa", "1f1fb-1f1e6", "1f1fb-1f1e7", "1f1fb-1f1e8", "1f1fb-1f1e9",
                    "1f1fb-1f1ea", "1f1fb-1f1eb", "1f1fb-1f1ec", "1f1fb-1f1ed", "1f1fb-1f1ee", "1f1fb-1f1ef",
                    "1f1fb-1f1f0", "1f1fb-1f1f1", "1f1fb-1f1f2", "1f1fb-1f1f3", "1f1fb-1f1f4", "1f1fb-1f1f5",
                    "1f1fb-1f1f6", "1f1fb-1f1f7", "1f1fb-1f1f8", "1f1fb-1f1f9", "1f1fb-1f1fa", "1f1fb-1f1fb",
                    "1f1fb-1f1fc", "1f1fb-1f1fd", "1f1fb-1f1fe", "1f1fb-1f1ff", "1f1fb", "1f1fc-1f1e6",
                    "1f1fc-1f1e7", "1f1fc-1f1e8", "1f1fc-1f1e9", "1f1fc-1f1ea", "1f1fc-1f1eb", "1f1fc-1f1ec",
                    "1f1fc-1f1ed", "1f1fc-1f1ee", "1f1fc-1f1ef", "1f1fc-1f1f0", "1f1fc-1f1f1", "1f1fc-1f1f2",
                    "1f1fc-1f1f3", "1f1fc-1f1f4", "1f1fc-1f1f5", "1f1fc-1f1f6", "1f1fc-1f1f7", "1f1fc-1f1f8",
                    "1f1fc-1f1f9", "1f1fc-1f1fa", "1f1fc-1f1fb", "1f1fc-1f1fc", "1f1fc-1f1fd", "1f1fc-1f1fe",
                    "1f1fc-1f1ff", "1f1fc", "1f1fd-1f1e6", "1f1fd-1f1e7", "1f1fd-1f1e8", "1f1fd-1f1e9",
                    "1f1fd-1f1ea", "1f1fd-1f1eb", "1f1fd-1f1ec", "1f1fd-1f1ed", "1f1fd-1f1ee", "1f1fd-1f1ef",
                    "1f1fd-1f1f0", "1f1fd-1f1f1", "1f1fd-1f1f2", "1f1fd-1f1f3", "1f1fd-1f1f4", "1f1fd-1f1f5",
                    "1f1fd-1f1f6", "1f1fd-1f1f7", "1f1fd-1f1f8", "1f1fd-1f1f9", "1f1fd-1f1fa", "1f1fd-1f1fb",
                    "1f1fd-1f1fc", "1f1fd-1f1fd", "1f1fd-1f1fe", "1f1fd-1f1ff", "1f1fd", "1f1fe-1f1e6",
                    "1f1fe-1f1e7", "1f1fe-1f1e8", "1f1fe-1f1e9", "1f1fe-1f1ea", "1f1fe-1f1eb", "1f1fe-1f1ec",
                    "1f1fe-1f1ed", "1f1fe-1f1ee", "1f1fe-1f1ef", "1f1fe-1f1f0", "1f1fe-1f1f1", "1f1fe-1f1f2",
                    "1f1fe-1f1f3", "1f1fe-1f1f4", "1f1fe-1f1f5", "1f1fe-1f1f6", "1f1fe-1f1f7", "1f1fe-1f1f8",
                    "1f1fe-1f1f9", "1f1fe-1f1fa", "1f1fe-1f1fb", "1f1fe-1f1fc", "1f1fe-1f1fd", "1f1fe-1f1fe",
                    "1f1fe-1f1ff", "1f1fe", "1f1ff-1f1e6", "1f1ff-1f1e7", "1f1ff-1f1e8", "1f1ff-1f1e9",
                    "1f1ff-1f1ea", "1f1ff-1f1eb", "1f1ff-1f1ec", "1f1ff-1f1ed", "1f1ff-1f1ee", "1f1ff-1f1ef",
                    "1f1ff-1f1f0", "1f1ff-1f1f1", "1f1ff-1f1f2", "1f1ff-1f1f3", "1f1ff-1f1f4", "1f1ff-1f1f5",
                    "1f1ff-1f1f6", "1f1ff-1f1f7", "1f1ff-1f1f8", "1f1ff-1f1f9", "1f1ff-1f1fa", "1f1ff-1f1fb",
                    "1f1ff-1f1fc", "1f1ff-1f1fd", "1f1ff-1f1fe", "1f1ff-1f1ff", "1f1ff"
                };
                
                for (String iconCode : fallbackIcons) {
                    if (iconExists(iconCode)) {
                        icons.add(iconCode);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícones: " + e.getMessage());
        }
        
        return icons;
    }
    
    /**
     * Carrega uma imagem PNG do diretório de recursos.
     * 
     * @param iconCode código do ícone (sem extensão .png)
     * @return Image carregada ou null se não encontrada
     */
    public static Image loadIcon(String iconCode) {
        try {
            String imagePath = EMOJI_DIRECTORY + iconCode + ".png";
            URL imageUrl = IconManager.class.getResource(imagePath);
            
            if (imageUrl != null) {
                return new Image(imageUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone " + iconCode + ": " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Cria um ImageView com tamanho específico para o ícone.
     * 
     * @param iconCode código do ícone
     * @param width largura desejada
     * @param height altura desejada
     * @return ImageView configurado ou null se ícone não encontrado
     */
    public static ImageView createIconView(String iconCode, double width, double height) {
        Image image = loadIcon(iconCode);
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return imageView;
        }
        return null;
    }
    
    /**
     * Cria um ImageView com tamanho padrão de 16x16 pixels.
     * 
     * @param iconCode código do ícone
     * @return ImageView configurado ou null se ícone não encontrado
     */
    public static ImageView createIconView(String iconCode) {
        return createIconView(iconCode, 16, 16);
    }

    /**
     * Cria um ImageView removendo um fundo próximo ao branco (chroma key simpificado).
     * Útil para emojis PNG com fundo branco.
     *
     * @param iconCode código do ícone
     * @param width largura desejada
     * @param height altura desejada
     * @param tolerance tolerância (0..1) para considerar como branco (ex.: 0.15)
     * @return ImageView com fundo transparente quando possível; null se não encontrado
     */
    public static ImageView createIconViewWithoutWhiteBackground(String iconCode, double width, double height, double tolerance) {
        Image src = loadIcon(iconCode);
        if (src == null) {
            return null;
        }

        int w = (int) src.getWidth();
        int h = (int) src.getHeight();
        PixelReader reader = src.getPixelReader();
        if (reader == null) {
            return null;
        }
        WritableImage out = new WritableImage(w, h);
        PixelWriter writer = out.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = reader.getColor(x, y);
                // Considera "quase branco" se RGB próximo de 1.0 e alfa alto
                boolean nearWhite = (1.0 - c.getRed() < tolerance)
                        && (1.0 - c.getGreen() < tolerance)
                        && (1.0 - c.getBlue() < tolerance)
                        && c.getOpacity() > 0.8;
                if (nearWhite) {
                    writer.setColor(x, y, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.0));
                } else {
                    writer.setColor(x, y, c);
                }
            }
        }

        ImageView view = new ImageView(out);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }
    
    /**
     * Verifica se um ícone existe no sistema.
     * 
     * @param iconCode código do ícone
     * @return true se o ícone existe, false caso contrário
     */
    public static boolean iconExists(String iconCode) {
        if (iconCode == null || iconCode.trim().isEmpty()) {
            return false;
        }
        
        String imagePath = EMOJI_DIRECTORY + iconCode.trim() + ".png";
        return IconManager.class.getResource(imagePath) != null;
    }
}
