package org.desviante.calendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o enum CalendarEventPriority.
 * 
 * <p>Esta classe testa todos os métodos e comportamentos do enum CalendarEventPriority,
 * incluindo comparações, propriedades visuais e métodos de conveniência.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventPriority
 */
@DisplayName("CalendarEventPriority")
class CalendarEventPriorityTest {

    @Nested
    @DisplayName("Propriedades Básicas")
    class BasicPropertiesTests {

        @Test
        @DisplayName("Deve ter display name correto para todas as prioridades")
        void shouldHaveCorrectDisplayNameForAllPriorities() {
            assertEquals("Baixa", CalendarEventPriority.LOW.getDisplayName());
            assertEquals("Padrão", CalendarEventPriority.STANDARD.getDisplayName());
            assertEquals("Alta", CalendarEventPriority.HIGH.getDisplayName());
            assertEquals("Urgente", CalendarEventPriority.URGENT.getDisplayName());
        }

        @Test
        @DisplayName("Deve ter nível numérico correto para todas as prioridades")
        void shouldHaveCorrectLevelForAllPriorities() {
            assertEquals(1, CalendarEventPriority.LOW.getLevel());
            assertEquals(2, CalendarEventPriority.STANDARD.getLevel());
            assertEquals(3, CalendarEventPriority.HIGH.getLevel());
            assertEquals(4, CalendarEventPriority.URGENT.getLevel());
        }

        @Test
        @DisplayName("Deve ter descrição correta para todas as prioridades")
        void shouldHaveCorrectDescriptionForAllPriorities() {
            assertNotNull(CalendarEventPriority.LOW.getDescription());
            assertNotNull(CalendarEventPriority.STANDARD.getDescription());
            assertNotNull(CalendarEventPriority.HIGH.getDescription());
            assertNotNull(CalendarEventPriority.URGENT.getDescription());
            
            assertFalse(CalendarEventPriority.LOW.getDescription().isEmpty());
            assertFalse(CalendarEventPriority.STANDARD.getDescription().isEmpty());
            assertFalse(CalendarEventPriority.HIGH.getDescription().isEmpty());
            assertFalse(CalendarEventPriority.URGENT.getDescription().isEmpty());
        }
    }

    @Nested
    @DisplayName("Métodos de Comparação")
    class ComparisonTests {

        @Test
        @DisplayName("Deve comparar prioridades corretamente")
        void shouldComparePrioritiesCorrectly() {
            // Teste isHigherThan
            assertTrue(CalendarEventPriority.HIGH.isHigherThan(CalendarEventPriority.STANDARD));
            assertTrue(CalendarEventPriority.URGENT.isHigherThan(CalendarEventPriority.LOW));
            assertTrue(CalendarEventPriority.URGENT.isHigherThan(CalendarEventPriority.HIGH));
            
            assertFalse(CalendarEventPriority.LOW.isHigherThan(CalendarEventPriority.STANDARD));
            assertFalse(CalendarEventPriority.STANDARD.isHigherThan(CalendarEventPriority.HIGH));
            assertFalse(CalendarEventPriority.HIGH.isHigherThan(CalendarEventPriority.URGENT));
            
            // Teste isLowerThan
            assertTrue(CalendarEventPriority.LOW.isLowerThan(CalendarEventPriority.STANDARD));
            assertTrue(CalendarEventPriority.STANDARD.isLowerThan(CalendarEventPriority.HIGH));
            assertTrue(CalendarEventPriority.HIGH.isLowerThan(CalendarEventPriority.URGENT));
            
            assertFalse(CalendarEventPriority.URGENT.isLowerThan(CalendarEventPriority.HIGH));
            assertFalse(CalendarEventPriority.HIGH.isLowerThan(CalendarEventPriority.STANDARD));
            assertFalse(CalendarEventPriority.STANDARD.isLowerThan(CalendarEventPriority.LOW));
            
            // Teste isEqualTo
            assertTrue(CalendarEventPriority.LOW.isEqualTo(CalendarEventPriority.LOW));
            assertTrue(CalendarEventPriority.STANDARD.isEqualTo(CalendarEventPriority.STANDARD));
            assertTrue(CalendarEventPriority.HIGH.isEqualTo(CalendarEventPriority.HIGH));
            assertTrue(CalendarEventPriority.URGENT.isEqualTo(CalendarEventPriority.URGENT));
            
            assertFalse(CalendarEventPriority.LOW.isEqualTo(CalendarEventPriority.STANDARD));
            assertFalse(CalendarEventPriority.STANDARD.isEqualTo(CalendarEventPriority.HIGH));
            assertFalse(CalendarEventPriority.HIGH.isEqualTo(CalendarEventPriority.URGENT));
        }
    }

    @Nested
    @DisplayName("Propriedades Visuais")
    class VisualPropertiesTests {

        @Test
        @DisplayName("Deve ter cor válida para todas as prioridades")
        void shouldHaveValidColorForAllPriorities() {
            assertNotNull(CalendarEventPriority.LOW.getColor());
            assertNotNull(CalendarEventPriority.STANDARD.getColor());
            assertNotNull(CalendarEventPriority.HIGH.getColor());
            assertNotNull(CalendarEventPriority.URGENT.getColor());
            
            // Verifica se as cores estão no formato hexadecimal
            assertTrue(CalendarEventPriority.LOW.getColor().startsWith("#"));
            assertTrue(CalendarEventPriority.STANDARD.getColor().startsWith("#"));
            assertTrue(CalendarEventPriority.HIGH.getColor().startsWith("#"));
            assertTrue(CalendarEventPriority.URGENT.getColor().startsWith("#"));
        }

        @Test
        @DisplayName("Deve ter cor de fundo válida para todas as prioridades")
        void shouldHaveValidBackgroundColorForAllPriorities() {
            assertNotNull(CalendarEventPriority.LOW.getBackgroundColor());
            assertNotNull(CalendarEventPriority.STANDARD.getBackgroundColor());
            assertNotNull(CalendarEventPriority.HIGH.getBackgroundColor());
            assertNotNull(CalendarEventPriority.URGENT.getBackgroundColor());
            
            // Verifica se as cores estão no formato hexadecimal
            assertTrue(CalendarEventPriority.LOW.getBackgroundColor().startsWith("#"));
            assertTrue(CalendarEventPriority.STANDARD.getBackgroundColor().startsWith("#"));
            assertTrue(CalendarEventPriority.HIGH.getBackgroundColor().startsWith("#"));
            assertTrue(CalendarEventPriority.URGENT.getBackgroundColor().startsWith("#"));
        }

        @Test
        @DisplayName("Deve ter código de ícone para todas as prioridades")
        void shouldHaveIconCodeForAllPriorities() {
            assertNotNull(CalendarEventPriority.LOW.getIconCode());
            assertNotNull(CalendarEventPriority.STANDARD.getIconCode());
            assertNotNull(CalendarEventPriority.HIGH.getIconCode());
            assertNotNull(CalendarEventPriority.URGENT.getIconCode());
            
            assertFalse(CalendarEventPriority.LOW.getIconCode().isEmpty());
            assertFalse(CalendarEventPriority.STANDARD.getIconCode().isEmpty());
            assertFalse(CalendarEventPriority.HIGH.getIconCode().isEmpty());
            assertFalse(CalendarEventPriority.URGENT.getIconCode().isEmpty());
        }

        @Test
        @DisplayName("Deve ter cores diferentes para cada prioridade")
        void shouldHaveDifferentColorsForEachPriority() {
            String lowColor = CalendarEventPriority.LOW.getColor();
            String standardColor = CalendarEventPriority.STANDARD.getColor();
            String highColor = CalendarEventPriority.HIGH.getColor();
            String urgentColor = CalendarEventPriority.URGENT.getColor();
            
            assertNotEquals(lowColor, standardColor);
            assertNotEquals(lowColor, highColor);
            assertNotEquals(lowColor, urgentColor);
            assertNotEquals(standardColor, highColor);
            assertNotEquals(standardColor, urgentColor);
            assertNotEquals(highColor, urgentColor);
        }

        @Test
        @DisplayName("Deve ter ícones diferentes para cada prioridade")
        void shouldHaveDifferentIconsForEachPriority() {
            String lowIcon = CalendarEventPriority.LOW.getIconCode();
            String standardIcon = CalendarEventPriority.STANDARD.getIconCode();
            String highIcon = CalendarEventPriority.HIGH.getIconCode();
            String urgentIcon = CalendarEventPriority.URGENT.getIconCode();
            
            assertNotEquals(lowIcon, standardIcon);
            assertNotEquals(lowIcon, highIcon);
            assertNotEquals(lowIcon, urgentIcon);
            assertNotEquals(standardIcon, highIcon);
            assertNotEquals(standardIcon, urgentIcon);
            assertNotEquals(highIcon, urgentIcon);
        }
    }

    @Nested
    @DisplayName("Métodos de Conveniência")
    class ConvenienceMethodsTests {

        @Test
        @DisplayName("Deve retornar prioridade padrão correta")
        void shouldReturnCorrectDefaultPriority() {
            assertEquals(CalendarEventPriority.STANDARD, CalendarEventPriority.getDefault());
        }

        @Test
        @DisplayName("Deve retornar prioridade mais alta correta")
        void shouldReturnCorrectHighestPriority() {
            assertEquals(CalendarEventPriority.URGENT, CalendarEventPriority.getHighest());
        }

        @Test
        @DisplayName("Deve retornar prioridade mais baixa correta")
        void shouldReturnCorrectLowestPriority() {
            assertEquals(CalendarEventPriority.LOW, CalendarEventPriority.getLowest());
        }
    }

    @Nested
    @DisplayName("Método toString")
    class ToStringTests {

        @Test
        @DisplayName("Deve retornar display name no toString")
        void shouldReturnDisplayNameInToString() {
            assertEquals("Baixa", CalendarEventPriority.LOW.toString());
            assertEquals("Padrão", CalendarEventPriority.STANDARD.toString());
            assertEquals("Alta", CalendarEventPriority.HIGH.toString());
            assertEquals("Urgente", CalendarEventPriority.URGENT.toString());
        }
    }

    @Nested
    @DisplayName("Cenários de Borda")
    class EdgeCaseTests {

        @Test
        @DisplayName("Deve manter ordem crescente dos níveis")
        void shouldMaintainAscendingOrderOfLevels() {
            CalendarEventPriority[] priorities = CalendarEventPriority.values();
            
            for (int i = 0; i < priorities.length - 1; i++) {
                assertTrue(priorities[i].getLevel() < priorities[i + 1].getLevel(),
                    "Prioridade " + priorities[i] + " deve ter nível menor que " + priorities[i + 1]);
            }
        }

        @Test
        @DisplayName("Deve ter valores únicos para todas as propriedades")
        void shouldHaveUniqueValuesForAllProperties() {
            // Verifica se todos os display names são únicos
            long uniqueDisplayNames = java.util.Arrays.stream(CalendarEventPriority.values())
                    .map(CalendarEventPriority::getDisplayName)
                    .distinct()
                    .count();
            assertEquals(CalendarEventPriority.values().length, uniqueDisplayNames);
            
            // Verifica se todos os códigos de ícone são únicos
            long uniqueIconCodes = java.util.Arrays.stream(CalendarEventPriority.values())
                    .map(CalendarEventPriority::getIconCode)
                    .distinct()
                    .count();
            assertEquals(CalendarEventPriority.values().length, uniqueIconCodes);
            
            // Verifica se todas as cores são únicas
            long uniqueColors = java.util.Arrays.stream(CalendarEventPriority.values())
                    .map(CalendarEventPriority::getColor)
                    .distinct()
                    .count();
            assertEquals(CalendarEventPriority.values().length, uniqueColors);
        }

        @Test
        @DisplayName("Deve ter consistência entre cor e cor de fundo")
        void shouldHaveConsistencyBetweenColorAndBackgroundColor() {
            for (CalendarEventPriority priority : CalendarEventPriority.values()) {
                String color = priority.getColor();
                String backgroundColor = priority.getBackgroundColor();
                
                assertNotNull(color);
                assertNotNull(backgroundColor);
                assertNotEquals(color, backgroundColor);
                
                // Verifica se ambas são cores hexadecimais válidas
                assertTrue(color.matches("^#[0-9A-Fa-f]{6}$"));
                assertTrue(backgroundColor.matches("^#[0-9A-Fa-f]{6}$"));
            }
        }
    }
}
