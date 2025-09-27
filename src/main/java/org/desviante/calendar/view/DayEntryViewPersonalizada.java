package org.desviante.calendar.view;

import com.calendarfx.model.Entry;
import com.calendarfx.view.DayEntryView;
import lombok.extern.slf4j.Slf4j;

/**
 * Visualização personalizada de entrada de calendário para configurar cores e opacidade.
 * 
 * <p>Esta classe estende DayEntryView para permitir a configuração personalizada
 * de cores e opacidade dos eventos no CalendarFX, já que a API nativa não oferece
 * suporte direto para essas configurações.</p>
 * 
 * <p><strong>Funcionalidades:</strong></p>
 * <ul>
 *   <li>Configuração de cor de fundo personalizada</li>
 *   <li>Configuração de opacidade personalizada</li>
 *   <li>Configuração combinada de cor e opacidade</li>
 *   <li>Suporte a cores em formato hexadecimal</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see DayEntryView
 * @see Entry
 */
@Slf4j
public class DayEntryViewPersonalizada extends DayEntryView {

    /**
     * Construtor padrão.
     * 
     * @param entry entrada do calendário
     */
    public DayEntryViewPersonalizada(Entry<?> entry) {
        super(entry);
        log.debug("DayEntryViewPersonalizada criada para entrada: {}", entry.getTitle());
    }

    /**
     * Define a cor de fundo da entrada.
     * 
     * @param colorHex cor em formato hexadecimal (ex: "#FF0000")
     */
    public void setColor(String colorHex) {
        if (colorHex == null || colorHex.trim().isEmpty()) {
            log.warn("Tentativa de definir cor vazia ou null");
            return;
        }
        
        // Garantir que a cor tenha o formato correto
        String color = colorHex.startsWith("#") ? colorHex : "#" + colorHex;
        
        setStyle("-fx-background-color: " + color + ";");
        log.debug("Cor definida para entrada {}: {}", getEntry().getTitle(), color);
    }

    /**
     * Define a opacidade da entrada.
     * 
     * @param opacity opacidade (0.0 a 1.0)
     */
    public void setEntryOpacity(double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            log.warn("Opacidade inválida: {}. Deve estar entre 0.0 e 1.0", opacity);
            opacity = Math.max(0.0, Math.min(1.0, opacity));
        }
        
        String currentStyle = getStyle();
        String newStyle = currentStyle + " -fx-opacity: " + opacity + ";";
        setStyle(newStyle);
        log.debug("Opacidade definida para entrada {}: {}", getEntry().getTitle(), opacity);
    }

    /**
     * Define cor e opacidade da entrada em uma única operação.
     * 
     * @param colorHex cor em formato hexadecimal (ex: "#FF0000")
     * @param opacity opacidade (0.0 a 1.0)
     */
    public void setColorAndOpacity(String colorHex, double opacity) {
        if (colorHex == null || colorHex.trim().isEmpty()) {
            log.warn("Tentativa de definir cor vazia ou null");
            return;
        }
        
        if (opacity < 0.0 || opacity > 1.0) {
            log.warn("Opacidade inválida: {}. Deve estar entre 0.0 e 1.0", opacity);
            opacity = Math.max(0.0, Math.min(1.0, opacity));
        }
        
        // Garantir que a cor tenha o formato correto
        String color = colorHex.startsWith("#") ? colorHex : "#" + colorHex;
        
        setStyle("-fx-background-color: " + color + "; -fx-opacity: " + opacity + ";");
        log.debug("Cor e opacidade definidas para entrada {}: cor={}, opacidade={}", 
                 getEntry().getTitle(), color, opacity);
    }

    /**
     * Aplica estilo personalizado baseado em tipo e prioridade.
     * 
     * @param colorHex cor em formato hexadecimal
     * @param opacity opacidade (0.0 a 1.0)
     * @param isRecurring se o evento é recorrente
     * @param isAllDay se o evento é de dia inteiro
     */
    public void applyCustomStyle(String colorHex, double opacity, boolean isRecurring, boolean isAllDay) {
        StringBuilder style = new StringBuilder();
        
        // Cor de fundo
        if (colorHex != null && !colorHex.trim().isEmpty()) {
            String color = colorHex.startsWith("#") ? colorHex : "#" + colorHex;
            style.append("-fx-background-color: ").append(color).append("; ");
        }
        
        // Opacidade
        if (opacity >= 0.0 && opacity <= 1.0) {
            style.append("-fx-opacity: ").append(opacity).append("; ");
        }
        
        // Estilo para eventos recorrentes
        if (isRecurring) {
            style.append("-fx-border-color: derive(-fx-background-color, -20%); ");
            style.append("-fx-border-width: 1px; ");
        }
        
        // Estilo para eventos de dia inteiro
        if (isAllDay) {
            style.append("-fx-border-style: solid; ");
        }
        
        setStyle(style.toString());
        log.debug("Estilo personalizado aplicado para entrada {}: {}", getEntry().getTitle(), style.toString());
    }
}
