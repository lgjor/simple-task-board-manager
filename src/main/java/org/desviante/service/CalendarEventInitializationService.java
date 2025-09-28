package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.calendar.CalendarEvent;
import org.desviante.calendar.CalendarEventManager;
import org.desviante.calendar.CalendarEventPriority;
import org.desviante.calendar.CalendarEventType;
import org.desviante.model.Card;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço de inicialização para carregar eventos do calendário na inicialização do sistema.
 * 
 * <p>Este serviço é responsável por carregar automaticamente todos os eventos
 * do calendário baseados nos cards com data de agendamento quando o sistema
 * é inicializado, garantindo que os eventos sejam exibidos corretamente.</p>
 * 
 * <p><strong>Funcionalidades:</strong></p>
 * <ul>
 *   <li>Carregamento automático na inicialização</li>
 *   <li>Sincronização com cards existentes</li>
 *   <li>Prevenção de duplicação de eventos</li>
 *   <li>Logging detalhado do processo</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventManager
 * @see CardService
 */
@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class CalendarEventInitializationService {

    private final CalendarEventManager calendarEventManager;
    private final CardService cardService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Carrega automaticamente eventos do calendário na inicialização do sistema.
     * 
     * <p>Este método é executado automaticamente quando a aplicação está pronta
     * para receber requisições. Ele busca todos os cards com data de agendamento
     * e cria os eventos correspondentes no calendário.</p>
     * 
     * <p><strong>Processo:</strong></p>
     * <ol>
     *   <li>Busca todos os cards com data de agendamento</li>
     *   <li>Verifica se já existem eventos para cada card</li>
     *   <li>Cria eventos apenas para cards que não possuem eventos</li>
     *   <li>Registra o processo no log</li>
     * </ol>
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadCalendarEventsOnStartup() {
        // Verificar se estamos em ambiente de teste
        String activeProfile = System.getProperty("spring.profiles.active");
        if ("test".equals(activeProfile)) {
            log.info("🔄 Pulando carregamento de eventos do calendário em ambiente de teste");
            return;
        }
        log.info("🔄 Iniciando carregamento de eventos do calendário na inicialização...");
        
        try {
            // Garantir que a tabela calendar_events existe
            ensureCalendarEventsTableExists();
            
            // Limpar eventos órfãos (eventos sem card correspondente com data de agendamento)
            cleanupOrphanedEvents();
            
            // Buscar todos os cards com data de agendamento
            List<Card> cardsWithScheduledDate = cardService.getAllCardsWithScheduledDate();
            log.info("📅 Encontrados {} cards com data de agendamento", cardsWithScheduledDate.size());
            
            int eventsCreated = 0;
            int eventsSkipped = 0;
            
            for (Card card : cardsWithScheduledDate) {
                try {
                    // Verificar se já existe evento para este card
                    List<CalendarEvent> existingEvents = calendarEventManager.findByRelatedEntity(
                            card.getId(), "CARD");
                    
                    if (existingEvents.isEmpty()) {
                        // Criar evento para o card
                        CalendarEvent event = createCalendarEventFromCard(card);
                        calendarEventManager.save(event);
                        eventsCreated++;
                        log.debug("✅ Evento criado para card: {} - {}", card.getId(), card.getTitle());
                    } else {
                        eventsSkipped++;
                        log.debug("⏭️ Evento já existe para card: {} - {}", card.getId(), card.getTitle());
                    }
                    
                } catch (Exception e) {
                    log.error("❌ Erro ao processar card {}: {}", card.getId(), e.getMessage(), e);
                }
            }
            
            log.info("🎉 Carregamento de eventos concluído - Criados: {}, Ignorados: {}", 
                    eventsCreated, eventsSkipped);
            
        } catch (Exception e) {
            log.error("❌ Erro durante carregamento de eventos do calendário: {}", e.getMessage(), e);
        }
    }

    /**
     * Cria um evento do calendário baseado nas informações de um card.
     * 
     * @param card card para criação do evento
     * @return evento do calendário criado
     */
    private CalendarEvent createCalendarEventFromCard(Card card) {
        LocalDateTime scheduledDate = card.getScheduledDate();
        LocalDateTime dueDate = card.getDueDate();
        
        // Determinar se é evento de dia inteiro (meia-noite)
        boolean allDay = scheduledDate.getHour() == 0 && scheduledDate.getMinute() == 0;
        
        // Calcular prioridade baseada na urgência do card
        CalendarEventPriority priority = calculateEventPriority(card);
        
        // Determinar cor baseada na urgência
        String color = calculateEventColor(card);
        
        CalendarEvent event = new CalendarEvent();
        event.setTitle(card.getTitle());
        event.setDescription(card.getDescription());
        event.setStartDateTime(scheduledDate);
        event.setEndDateTime(allDay ? scheduledDate.plusHours(1) : 
                           (dueDate != null ? dueDate : scheduledDate.plusHours(1)));
        event.setAllDay(allDay);
        event.setType(CalendarEventType.CARD);
        event.setPriority(priority);
        event.setColor(color);
        event.setRelatedEntityId(card.getId());
        event.setRelatedEntityType("CARD");
        event.setActive(true);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        
        return event;
    }

    /**
     * Calcula a prioridade do evento baseada na urgência do card.
     * 
     * @param card card para cálculo da prioridade
     * @return prioridade do evento
     */
    private CalendarEventPriority calculateEventPriority(Card card) {
        int urgencyLevel = card.getUrgencyLevel();
        
        return switch (urgencyLevel) {
            case 4 -> CalendarEventPriority.URGENT;      // Vencido
            case 3 -> CalendarEventPriority.HIGH;        // Vence hoje
            case 2 -> CalendarEventPriority.HIGH;        // Vence em 1 dia
            case 1 -> CalendarEventPriority.STANDARD;    // Vence em 2-3 dias
            default -> CalendarEventPriority.LOW;        // Sem urgência
        };
    }

    /**
     * Calcula a cor do evento baseada na urgência do card.
     * 
     * @param card card para cálculo da cor
     * @return cor em formato hexadecimal
     */
    private String calculateEventColor(Card card) {
        int urgencyLevel = card.getUrgencyLevel();
        
        return switch (urgencyLevel) {
            case 4 -> "#FF0000";  // Vermelho - vencido
            case 3 -> "#FF6600";  // Laranja - vence hoje
            case 2 -> "#FFAA00";  // Amarelo - vence em 1 dia
            case 1 -> "#00AAFF";  // Azul - vence em 2-3 dias
            default -> "#00AA00"; // Verde - sem urgência
        };
    }

    /**
     * Garante que a tabela calendar_events existe no banco de dados.
     * 
     * <p>Este método verifica se a tabela existe e a cria se necessário.
     * É executado antes de qualquer operação com eventos do calendário.</p>
     */
    private void ensureCalendarEventsTableExists() {
        try {
            log.info("🔧 Verificando se a tabela calendar_events existe...");
            
            // Verificar se a tabela existe
            String checkTableSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALENDAR_EVENTS'";
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            
            if (tableCount == null || tableCount == 0) {
                log.info("➕ Tabela calendar_events não existe. Criando...");
                
                // Criar a tabela
                String createTableSql = """
                    CREATE TABLE calendar_events (
                        id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                        title               VARCHAR(255) NOT NULL,
                        description         TEXT,
                        start_date_time     TIMESTAMP NOT NULL,
                        end_date_time       TIMESTAMP NOT NULL,
                        all_day             BOOLEAN NOT NULL DEFAULT FALSE,
                        event_type          VARCHAR(50) NOT NULL DEFAULT 'CARD',
                        priority            VARCHAR(20) NOT NULL DEFAULT 'LOW',
                        color               VARCHAR(7),
                        related_entity_id   BIGINT,
                        related_entity_type VARCHAR(50),
                        active              BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        
                        CONSTRAINT fk_calendar_events_to_cards FOREIGN KEY (related_entity_id) REFERENCES cards(id) ON DELETE CASCADE
                    )
                    """;
                
                jdbcTemplate.execute(createTableSql);
                log.info("✅ Tabela calendar_events criada com sucesso!");
                
                // Criar índices
                String createIndex1Sql = "CREATE INDEX idx_calendar_events_related_entity ON calendar_events(related_entity_id, related_entity_type)";
                String createIndex2Sql = "CREATE INDEX idx_calendar_events_start_date ON calendar_events(start_date_time)";
                String createIndex3Sql = "CREATE INDEX idx_calendar_events_active ON calendar_events(active)";
                
                jdbcTemplate.execute(createIndex1Sql);
                jdbcTemplate.execute(createIndex2Sql);
                jdbcTemplate.execute(createIndex3Sql);
                log.info("✅ Índices da tabela calendar_events criados com sucesso!");
                
            } else {
                log.info("✅ Tabela calendar_events já existe");
            }
            
        } catch (Exception e) {
            log.error("❌ Erro ao verificar/criar tabela calendar_events: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao garantir existência da tabela calendar_events", e);
        }
    }

    /**
     * Remove eventos órfãos do calendário.
     * 
     * <p>Eventos órfãos são eventos que não possuem um card correspondente
     * com data de agendamento. Isso pode acontecer quando:
     * - Um card é deletado mas o evento não é removido
     * - Um card perde sua data de agendamento mas o evento não é removido
     * - Dados inconsistentes por falhas em operações anteriores</p>
     */
    private void cleanupOrphanedEvents() {
        try {
            log.info("🧹 Iniciando limpeza de eventos órfãos...");
            
            // Buscar todos os eventos ativos do calendário
            var allEvents = calendarEventManager.findAll();
            log.info("📊 Total de eventos no calendário: {}", allEvents.size());
            
            int orphanedCount = 0;
            
            for (var event : allEvents) {
                log.info("🔍 Verificando evento: {} (ID: {}, Related Entity ID: {}, Type: {})", 
                        event.getTitle(), event.getId(), event.getRelatedEntityId(), event.getRelatedEntityType());
                
                // Verificar se o evento está relacionado a um card
                if ("Card".equals(event.getRelatedEntityType()) && event.getRelatedEntityId() != null) {
                    Long cardId = event.getRelatedEntityId();
                    
                    // Verificar se o card existe e tem data de agendamento
                    var cardOpt = cardService.getCardById(cardId);
                    if (cardOpt.isEmpty()) {
                        log.warn("⚠️ Card não encontrado para evento: {} (Card ID: {})", event.getTitle(), cardId);
                        // Evento é órfão - remover
                        boolean removed = calendarEventManager.deleteById(event.getId());
                        if (removed) {
                            orphanedCount++;
                            log.info("🗑️ Evento órfão removido (card não existe): {} (Card ID: {})", event.getTitle(), cardId);
                        } else {
                            log.warn("⚠️ Falha ao remover evento órfão: {} (Card ID: {})", event.getTitle(), cardId);
                        }
                    } else {
                        Card card = cardOpt.get();
                        log.info("📋 Card encontrado: {} (ID: {}, Scheduled: {})", card.getTitle(), card.getId(), card.getScheduledDate());
                        
                        if (card.getScheduledDate() == null) {
                            log.warn("⚠️ Card sem data de agendamento para evento: {} (Card ID: {})", event.getTitle(), cardId);
                            // Evento é órfão - remover
                            boolean removed = calendarEventManager.deleteById(event.getId());
                            if (removed) {
                                orphanedCount++;
                                log.info("🗑️ Evento órfão removido (card sem agendamento): {} (Card ID: {})", event.getTitle(), cardId);
                            } else {
                                log.warn("⚠️ Falha ao remover evento órfão: {} (Card ID: {})", event.getTitle(), cardId);
                            }
                        } else {
                            log.info("✅ Evento válido mantido: {} (Card ID: {}, Scheduled: {})", event.getTitle(), cardId, card.getScheduledDate());
                        }
                    }
                } else {
                    log.warn("⚠️ Evento não relacionado a card: {} (Type: {}, Related ID: {})", 
                            event.getTitle(), event.getRelatedEntityType(), event.getRelatedEntityId());
                }
            }
            
            log.info("✅ Limpeza de eventos órfãos concluída - Removidos: {}", orphanedCount);
            
        } catch (Exception e) {
            log.error("❌ Erro durante limpeza de eventos órfãos: {}", e.getMessage(), e);
            // Não lançar exceção para não interromper a inicialização
        }
    }
}
