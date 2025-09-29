package org.desviante.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe CalendarEvent.
 * 
 * <p>Esta classe testa todos os métodos e comportamentos da classe CalendarEvent,
 * incluindo validações, métodos de conveniência e lógica de negócio.</p>
 * 
 * <p><strong>Cobertura de Testes:</strong></p>
 * <ul>
 *   <li>Construtores e métodos de conveniência</li>
 *   <li>Validações de dados</li>
 *   <li>Métodos de verificação de atividade</li>
 *   <li>Métodos de cálculo de duração</li>
 *   <li>Métodos de relacionamento com entidades</li>
 *   <li>Métodos equals, hashCode e toString</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEvent
 */
@DisplayName("CalendarEvent")
class CalendarEventTest {

    private LocalDateTime testStartDateTime;
    private LocalDateTime testEndDateTime;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testStartDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        testEndDateTime = LocalDateTime.of(2024, 1, 15, 11, 0);
        testDate = LocalDate.of(2024, 1, 15);
    }

    @Nested
    @DisplayName("Construtores e Métodos de Conveniência")
    class ConstructorTests {

        @Test
        @DisplayName("Deve criar evento de dia inteiro corretamente")
        void shouldCreateAllDayEventCorrectly() {
            // Given
            String title = "Feriado Nacional";
            String description = "Dia de descanso";
            LocalDate date = LocalDate.of(2024, 1, 1);
            CalendarEventType type = CalendarEventType.CUSTOM;

            // When
            CalendarEvent event = CalendarEvent.createAllDayEvent(title, description, date, type);

            // Then
            assertNotNull(event);
            assertEquals(title, event.getTitle());
            assertEquals(description, event.getDescription());
            assertEquals(date.atStartOfDay(), event.getStartDateTime());
            assertEquals(date.atTime(23, 59, 59), event.getEndDateTime());
            assertTrue(event.isAllDay());
            assertEquals(type, event.getType());
            assertNotNull(event.getCreatedAt());
            assertNotNull(event.getUpdatedAt());
        }

        @Test
        @DisplayName("Deve criar evento com horário específico corretamente")
        void shouldCreateTimedEventCorrectly() {
            // Given
            String title = "Reunião de Projeto";
            String description = "Discussão sobre próximos passos";
            LocalDateTime start = LocalDateTime.of(2024, 1, 15, 14, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 15, 15, 30);
            CalendarEventType type = CalendarEventType.MEETING;

            // When
            CalendarEvent event = CalendarEvent.createTimedEvent(title, description, start, end, type);

            // Then
            assertNotNull(event);
            assertEquals(title, event.getTitle());
            assertEquals(description, event.getDescription());
            assertEquals(start, event.getStartDateTime());
            assertEquals(end, event.getEndDateTime());
            assertFalse(event.isAllDay());
            assertEquals(type, event.getType());
            assertNotNull(event.getCreatedAt());
            assertNotNull(event.getUpdatedAt());
        }

        @Test
        @DisplayName("Deve criar evento com builder corretamente")
        void shouldCreateEventWithBuilderCorrectly() {
            // Given & When
            CalendarEvent event = CalendarEvent.builder()
                    .id(1L)
                    .title("Teste")
                    .description("Descrição do teste")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .allDay(false)
                    .type(CalendarEventType.CARD)
                    .priority(CalendarEventPriority.HIGH)
                    .color("#FF5733")
                    .relatedEntityId(123L)
                    .relatedEntityType("CARD")
                    .recurring(false)
                    .active(true)
                    .build();

            // Then
            assertNotNull(event);
            assertEquals(1L, event.getId());
            assertEquals("Teste", event.getTitle());
            assertEquals("Descrição do teste", event.getDescription());
            assertEquals(testStartDateTime, event.getStartDateTime());
            assertEquals(testEndDateTime, event.getEndDateTime());
            assertFalse(event.isAllDay());
            assertEquals(CalendarEventType.CARD, event.getType());
            assertEquals(CalendarEventPriority.HIGH, event.getPriority());
            assertEquals("#FF5733", event.getColor());
            assertEquals(123L, event.getRelatedEntityId());
            assertEquals("CARD", event.getRelatedEntityType());
            assertFalse(event.isRecurring());
            assertTrue(event.isActive());
        }
    }

    @Nested
    @DisplayName("Validações")
    class ValidationTests {

        @Test
        @DisplayName("Deve validar evento válido corretamente")
        void shouldValidateValidEventCorrectly() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento Válido")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .build();

            // When & Then
            assertTrue(event.isValid());
        }

        @Test
        @DisplayName("Deve rejeitar evento com título vazio")
        void shouldRejectEventWithEmptyTitle() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .build();

            // When & Then
            assertFalse(event.isValid());
        }

        @Test
        @DisplayName("Deve rejeitar evento com título null")
        void shouldRejectEventWithNullTitle() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title(null)
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .build();

            // When & Then
            assertFalse(event.isValid());
        }

        @Test
        @DisplayName("Deve rejeitar evento com data de início null")
        void shouldRejectEventWithNullStartDateTime() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(null)
                    .endDateTime(testEndDateTime)
                    .build();

            // When & Then
            assertFalse(event.isValid());
        }

        @Test
        @DisplayName("Deve rejeitar evento com data de fim anterior à de início")
        void shouldRejectEventWithEndBeforeStart() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testEndDateTime)
                    .endDateTime(testStartDateTime)
                    .build();

            // When & Then
            assertFalse(event.isValid());
        }

        @Test
        @DisplayName("Deve aceitar evento com data de fim null")
        void shouldAcceptEventWithNullEndDateTime() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .endDateTime(null)
                    .build();

            // When & Then
            assertTrue(event.isValid());
        }
    }

    @Nested
    @DisplayName("Métodos de Atividade")
    class ActivityTests {

        @Test
        @DisplayName("Deve verificar atividade em data específica para evento de dia inteiro")
        void shouldCheckActivityOnSpecificDateForAllDayEvent() {
            // Given
            CalendarEvent event = CalendarEvent.createAllDayEvent(
                    "Feriado", "Descrição", testDate, CalendarEventType.CUSTOM);

            // When & Then
            assertTrue(event.isActiveOnDate(testDate));
            assertFalse(event.isActiveOnDate(testDate.plusDays(1)));
            assertFalse(event.isActiveOnDate(testDate.minusDays(1)));
        }

        @Test
        @DisplayName("Deve verificar atividade em data específica para evento com horário")
        void shouldCheckActivityOnSpecificDateForTimedEvent() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .allDay(false)
                    .active(true)
                    .build();

            // When & Then
            assertTrue(event.isActiveOnDate(testDate));
            assertFalse(event.isActiveOnDate(testDate.plusDays(1)));
            assertFalse(event.isActiveOnDate(testDate.minusDays(1)));
        }

        @Test
        @DisplayName("Deve retornar false para evento inativo")
        void shouldReturnFalseForInactiveEvent() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .active(false)
                    .build();

            // When & Then
            assertFalse(event.isActiveOnDate(testDate));
        }

        @Test
        @DisplayName("Deve verificar atividade em período específico")
        void shouldCheckActivityInSpecificPeriod() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .active(true)
                    .build();

            LocalDate periodStart = testDate.minusDays(1);
            LocalDate periodEnd = testDate.plusDays(1);

            // When & Then
            assertTrue(event.isActiveInPeriod(periodStart, periodEnd));
            assertFalse(event.isActiveInPeriod(testDate.plusDays(1), testDate.plusDays(2)));
        }
    }

    @Nested
    @DisplayName("Métodos de Duração")
    class DurationTests {

        @Test
        @DisplayName("Deve calcular duração em minutos corretamente")
        void shouldCalculateDurationInMinutesCorrectly() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .allDay(false)
                    .build();

            // When
            long duration = event.getDurationInMinutes();

            // Then
            assertEquals(60, duration);
        }

        @Test
        @DisplayName("Deve retornar 0 para evento de dia inteiro")
        void shouldReturnZeroForAllDayEvent() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testEndDateTime)
                    .allDay(true)
                    .build();

            // When
            long duration = event.getDurationInMinutes();

            // Then
            assertEquals(0, duration);
        }

        @Test
        @DisplayName("Deve retornar 0 para evento sem data de fim")
        void shouldReturnZeroForEventWithoutEndDateTime() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .endDateTime(null)
                    .allDay(false)
                    .build();

            // When
            long duration = event.getDurationInMinutes();

            // Then
            assertEquals(0, duration);
        }
    }

    @Nested
    @DisplayName("Métodos de Relacionamento")
    class RelationshipTests {

        @Test
        @DisplayName("Deve verificar relacionamento com entidade corretamente")
        void shouldCheckRelationshipWithEntityCorrectly() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .relatedEntityId(123L)
                    .relatedEntityType("CARD")
                    .build();

            // When & Then
            assertTrue(event.isRelatedTo(123L, "CARD"));
            assertFalse(event.isRelatedTo(456L, "CARD"));
            assertFalse(event.isRelatedTo(123L, "TASK"));
            assertFalse(event.isRelatedTo(null, "CARD"));
            assertFalse(event.isRelatedTo(123L, null));
        }

        @Test
        @DisplayName("Deve retornar false para relacionamento com entidade null")
        void shouldReturnFalseForRelationshipWithNullEntity() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .relatedEntityId(null)
                    .relatedEntityType(null)
                    .build();

            // When & Then
            assertFalse(event.isRelatedTo(123L, "CARD"));
            assertFalse(event.isRelatedTo(null, null));
        }
    }

    @Nested
    @DisplayName("Métodos de Atualização")
    class UpdateTests {

        @Test
        @DisplayName("Deve marcar evento como atualizado corretamente")
        void shouldMarkEventAsUpdatedCorrectly() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .updatedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                    .build();

            LocalDateTime beforeUpdate = event.getUpdatedAt();

            // When
            event.markAsUpdated();

            // Then
            assertNotNull(event.getUpdatedAt());
            assertTrue(event.getUpdatedAt().isAfter(beforeUpdate));
        }
    }

    @Nested
    @DisplayName("Métodos equals, hashCode e toString")
    class ObjectMethodsTests {

        @Test
        @DisplayName("Deve implementar equals corretamente")
        void shouldImplementEqualsCorrectly() {
            // Given
            CalendarEvent event1 = CalendarEvent.builder()
                    .id(1L)
                    .title("Evento")
                    .build();

            CalendarEvent event2 = CalendarEvent.builder()
                    .id(1L)
                    .title("Evento Diferente")
                    .build();

            CalendarEvent event3 = CalendarEvent.builder()
                    .id(2L)
                    .title("Evento")
                    .build();

            // When & Then
            assertEquals(event1, event2); // Mesmo ID
            assertNotEquals(event1, event3); // IDs diferentes
            assertNotEquals(event1, null);
            assertNotEquals(event1, "String");
        }

        @Test
        @DisplayName("Deve implementar hashCode corretamente")
        void shouldImplementHashCodeCorrectly() {
            // Given
            CalendarEvent event1 = CalendarEvent.builder().id(1L).build();
            CalendarEvent event2 = CalendarEvent.builder().id(1L).build();
            CalendarEvent event3 = CalendarEvent.builder().id(2L).build();

            // When & Then
            assertEquals(event1.hashCode(), event2.hashCode());
            assertNotEquals(event1.hashCode(), event3.hashCode());
        }

        @Test
        @DisplayName("Deve implementar toString corretamente")
        void shouldImplementToStringCorrectly() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .id(1L)
                    .title("Evento Teste")
                    .startDateTime(testStartDateTime)
                    .type(CalendarEventType.CARD)
                    .priority(CalendarEventPriority.HIGH)
                    .build();

            // When
            String result = event.toString();

            // Then
            assertNotNull(result);
            assertTrue(result.contains("CalendarEvent"));
            assertTrue(result.contains("id=1"));
            assertTrue(result.contains("title='Evento Teste'"));
            assertTrue(result.contains("startDateTime=" + testStartDateTime));
            assertTrue(result.contains("type=Card"));
            assertTrue(result.contains("priority=Alta"));
        }
    }

    @Nested
    @DisplayName("Cenários de Borda")
    class EdgeCaseTests {

        @Test
        @DisplayName("Deve lidar com evento de duração zero")
        void shouldHandleZeroDurationEvent() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento")
                    .startDateTime(testStartDateTime)
                    .endDateTime(testStartDateTime)
                    .allDay(false)
                    .build();

            // When & Then
            assertTrue(event.isValid());
            assertEquals(0, event.getDurationInMinutes());
        }

        @Test
        @DisplayName("Deve lidar com evento que cruza meia-noite")
        void shouldHandleEventCrossingMidnight() {
            // Given
            LocalDateTime start = LocalDateTime.of(2024, 1, 15, 23, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 16, 1, 0);
            CalendarEvent event = CalendarEvent.builder()
                    .title("Evento Noturno")
                    .startDateTime(start)
                    .endDateTime(end)
                    .allDay(false)
                    .build();

            // When & Then
            assertTrue(event.isValid());
            assertEquals(120, event.getDurationInMinutes());
            assertTrue(event.isActiveOnDate(LocalDate.of(2024, 1, 15)));
            assertTrue(event.isActiveOnDate(LocalDate.of(2024, 1, 16)));
        }

        @Test
        @DisplayName("Deve lidar com evento com título apenas com espaços")
        void shouldHandleEventWithWhitespaceOnlyTitle() {
            // Given
            CalendarEvent event = CalendarEvent.builder()
                    .title("   ")
                    .startDateTime(testStartDateTime)
                    .build();

            // When & Then
            assertFalse(event.isValid());
        }
    }
}
