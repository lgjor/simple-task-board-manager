package org.desviante.calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o enum CalendarEventType.
 * 
 * <p>Esta classe testa todos os métodos e comportamentos do enum CalendarEventType,
 * incluindo métodos de conveniência, validações e propriedades específicas.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventType
 */
@DisplayName("CalendarEventType")
class CalendarEventTypeTest {

    @Nested
    @DisplayName("Propriedades Básicas")
    class BasicPropertiesTests {

        @Test
        @DisplayName("Deve ter display name correto para todos os tipos")
        void shouldHaveCorrectDisplayNameForAllTypes() {
            assertEquals("Card", CalendarEventType.CARD.getDisplayName());
            assertEquals("Task", CalendarEventType.TASK.getDisplayName());
            assertEquals("Reunião", CalendarEventType.MEETING.getDisplayName());
            assertEquals("Lembrete", CalendarEventType.REMINDER.getDisplayName());
            assertEquals("Prazo", CalendarEventType.DEADLINE.getDisplayName());
            assertEquals("Personalizado", CalendarEventType.CUSTOM.getDisplayName());
        }

        @Test
        @DisplayName("Deve ter descrição correta para todos os tipos")
        void shouldHaveCorrectDescriptionForAllTypes() {
            assertNotNull(CalendarEventType.CARD.getDescription());
            assertNotNull(CalendarEventType.TASK.getDescription());
            assertNotNull(CalendarEventType.MEETING.getDescription());
            assertNotNull(CalendarEventType.REMINDER.getDescription());
            assertNotNull(CalendarEventType.DEADLINE.getDescription());
            assertNotNull(CalendarEventType.CUSTOM.getDescription());
            
            assertFalse(CalendarEventType.CARD.getDescription().isEmpty());
            assertFalse(CalendarEventType.TASK.getDescription().isEmpty());
            assertFalse(CalendarEventType.MEETING.getDescription().isEmpty());
            assertFalse(CalendarEventType.REMINDER.getDescription().isEmpty());
            assertFalse(CalendarEventType.DEADLINE.getDescription().isEmpty());
            assertFalse(CalendarEventType.CUSTOM.getDescription().isEmpty());
        }
    }

    @Nested
    @DisplayName("Métodos de Validação")
    class ValidationTests {

        @Test
        @DisplayName("Deve identificar tipos relacionados ao sistema corretamente")
        void shouldIdentifySystemRelatedTypesCorrectly() {
            assertTrue(CalendarEventType.CARD.isSystemRelated());
            assertTrue(CalendarEventType.TASK.isSystemRelated());
            assertFalse(CalendarEventType.MEETING.isSystemRelated());
            assertFalse(CalendarEventType.REMINDER.isSystemRelated());
            assertFalse(CalendarEventType.DEADLINE.isSystemRelated());
            assertFalse(CalendarEventType.CUSTOM.isSystemRelated());
        }

        @Test
        @DisplayName("Deve identificar tipos editáveis pelo usuário corretamente")
        void shouldIdentifyUserEditableTypesCorrectly() {
            assertFalse(CalendarEventType.CARD.isUserEditable());
            assertFalse(CalendarEventType.TASK.isUserEditable());
            assertTrue(CalendarEventType.MEETING.isUserEditable());
            assertTrue(CalendarEventType.REMINDER.isUserEditable());
            assertTrue(CalendarEventType.DEADLINE.isUserEditable());
            assertTrue(CalendarEventType.CUSTOM.isUserEditable());
        }
    }

    @Nested
    @DisplayName("Propriedades Visuais")
    class VisualPropertiesTests {

        @Test
        @DisplayName("Deve ter código de ícone para todos os tipos")
        void shouldHaveIconCodeForAllTypes() {
            assertNotNull(CalendarEventType.CARD.getIconCode());
            assertNotNull(CalendarEventType.TASK.getIconCode());
            assertNotNull(CalendarEventType.MEETING.getIconCode());
            assertNotNull(CalendarEventType.REMINDER.getIconCode());
            assertNotNull(CalendarEventType.DEADLINE.getIconCode());
            assertNotNull(CalendarEventType.CUSTOM.getIconCode());
            
            assertFalse(CalendarEventType.CARD.getIconCode().isEmpty());
            assertFalse(CalendarEventType.TASK.getIconCode().isEmpty());
            assertFalse(CalendarEventType.MEETING.getIconCode().isEmpty());
            assertFalse(CalendarEventType.REMINDER.getIconCode().isEmpty());
            assertFalse(CalendarEventType.DEADLINE.getIconCode().isEmpty());
            assertFalse(CalendarEventType.CUSTOM.getIconCode().isEmpty());
        }

        @Test
        @DisplayName("Deve ter cor padrão válida para todos os tipos")
        void shouldHaveValidDefaultColorForAllTypes() {
            assertNotNull(CalendarEventType.CARD.getDefaultColor());
            assertNotNull(CalendarEventType.TASK.getDefaultColor());
            assertNotNull(CalendarEventType.MEETING.getDefaultColor());
            assertNotNull(CalendarEventType.REMINDER.getDefaultColor());
            assertNotNull(CalendarEventType.DEADLINE.getDefaultColor());
            assertNotNull(CalendarEventType.CUSTOM.getDefaultColor());
            
            // Verifica se as cores estão no formato hexadecimal
            assertTrue(CalendarEventType.CARD.getDefaultColor().startsWith("#"));
            assertTrue(CalendarEventType.TASK.getDefaultColor().startsWith("#"));
            assertTrue(CalendarEventType.MEETING.getDefaultColor().startsWith("#"));
            assertTrue(CalendarEventType.REMINDER.getDefaultColor().startsWith("#"));
            assertTrue(CalendarEventType.DEADLINE.getDefaultColor().startsWith("#"));
            assertTrue(CalendarEventType.CUSTOM.getDefaultColor().startsWith("#"));
        }

        @Test
        @DisplayName("Deve ter cores diferentes para cada tipo")
        void shouldHaveDifferentColorsForEachType() {
            String cardColor = CalendarEventType.CARD.getDefaultColor();
            String taskColor = CalendarEventType.TASK.getDefaultColor();
            String meetingColor = CalendarEventType.MEETING.getDefaultColor();
            String reminderColor = CalendarEventType.REMINDER.getDefaultColor();
            String deadlineColor = CalendarEventType.DEADLINE.getDefaultColor();
            String customColor = CalendarEventType.CUSTOM.getDefaultColor();
            
            assertNotEquals(cardColor, taskColor);
            assertNotEquals(cardColor, meetingColor);
            assertNotEquals(cardColor, reminderColor);
            assertNotEquals(cardColor, deadlineColor);
            assertNotEquals(cardColor, customColor);
            
            assertNotEquals(taskColor, meetingColor);
            assertNotEquals(taskColor, reminderColor);
            assertNotEquals(taskColor, deadlineColor);
            assertNotEquals(taskColor, customColor);
            
            assertNotEquals(meetingColor, reminderColor);
            assertNotEquals(meetingColor, deadlineColor);
            assertNotEquals(meetingColor, customColor);
            
            assertNotEquals(reminderColor, deadlineColor);
            assertNotEquals(reminderColor, customColor);
            
            assertNotEquals(deadlineColor, customColor);
        }
    }

    @Nested
    @DisplayName("Método toString")
    class ToStringTests {

        @Test
        @DisplayName("Deve retornar display name no toString")
        void shouldReturnDisplayNameInToString() {
            assertEquals("Card", CalendarEventType.CARD.toString());
            assertEquals("Task", CalendarEventType.TASK.toString());
            assertEquals("Reunião", CalendarEventType.MEETING.toString());
            assertEquals("Lembrete", CalendarEventType.REMINDER.toString());
            assertEquals("Prazo", CalendarEventType.DEADLINE.toString());
            assertEquals("Personalizado", CalendarEventType.CUSTOM.toString());
        }
    }

    @Nested
    @DisplayName("Cenários de Borda")
    class EdgeCaseTests {

        @Test
        @DisplayName("Deve manter consistência entre isSystemRelated e isUserEditable")
        void shouldMaintainConsistencyBetweenSystemRelatedAndUserEditable() {
            for (CalendarEventType type : CalendarEventType.values()) {
                if (type.isSystemRelated()) {
                    assertFalse(type.isUserEditable(), 
                        "Tipo " + type + " é relacionado ao sistema mas é editável pelo usuário");
                } else {
                    assertTrue(type.isUserEditable(), 
                        "Tipo " + type + " não é relacionado ao sistema mas não é editável pelo usuário");
                }
            }
        }

        @Test
        @DisplayName("Deve ter valores únicos para todas as propriedades")
        void shouldHaveUniqueValuesForAllProperties() {
            // Verifica se todos os display names são únicos
            long uniqueDisplayNames = java.util.Arrays.stream(CalendarEventType.values())
                    .map(CalendarEventType::getDisplayName)
                    .distinct()
                    .count();
            assertEquals(CalendarEventType.values().length, uniqueDisplayNames);
            
            // Verifica se todos os códigos de ícone são únicos
            long uniqueIconCodes = java.util.Arrays.stream(CalendarEventType.values())
                    .map(CalendarEventType::getIconCode)
                    .distinct()
                    .count();
            assertEquals(CalendarEventType.values().length, uniqueIconCodes);
            
            // Verifica se todas as cores padrão são únicas
            long uniqueColors = java.util.Arrays.stream(CalendarEventType.values())
                    .map(CalendarEventType::getDefaultColor)
                    .distinct()
                    .count();
            assertEquals(CalendarEventType.values().length, uniqueColors);
        }
    }
}