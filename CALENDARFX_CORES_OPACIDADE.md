# Configuração de Cores e Opacidade no CalendarFX

## Visão Geral

Este documento explica como usar as novas funcionalidades de configuração de cores e opacidade personalizadas no CalendarFX, implementadas para resolver o comentário TODO no `CalendarFXAdapter`.

## Problema Resolvido

O comentário TODO na linha 144 do `CalendarFXAdapter.java` indicava que a configuração de cores e opacidade seria implementada quando a API do CalendarFX permitisse. Esta funcionalidade foi implementada usando uma abordagem de fábrica de visualização personalizada.

## Solução Implementada

### 1. Classe DayEntryViewPersonalizada

Criada uma classe personalizada que estende `DayEntryView` para permitir configuração de cores e opacidade:

```java
// Exemplo de uso
DayEntryViewPersonalizada customView = new DayEntryViewPersonalizada(entry);
customView.setColor("#FF0000"); // Cor vermelha
customView.setEntryOpacity(0.7); // 70% de opacidade
customView.setColorAndOpacity("#00FF00", 0.8); // Verde com 80% de opacidade
```

### 2. Métodos Disponíveis

- `setColor(String colorHex)`: Define cor de fundo em formato hexadecimal
- `setEntryOpacity(double opacity)`: Define opacidade (0.0 a 1.0)
- `setColorAndOpacity(String colorHex, double opacity)`: Define cor e opacidade juntas
- `applyCustomStyle(...)`: Aplica estilo completo baseado em tipo e prioridade

### 3. Configuração no CalendarFXAdapter

O `CalendarFXAdapter` foi atualizado com:

- Método `configureCustomEntryViewFactory(DayView dayView)`: Configura a fábrica personalizada
- Método `applyCustomEntryStyle(...)`: Aplica estilos baseados no tipo e prioridade do evento
- Remoção do comentário TODO e implementação real da funcionalidade

## Como Usar

### 1. Configuração Básica

```java
@Autowired
private CalendarFXAdapter calendarFXAdapter;

public void setupCalendar(CalendarView calendarView) {
    // Criar CalendarSource
    var calendarSource = calendarFXAdapter.createCalendarSource();
    calendarView.getCalendarSources().add(calendarSource);
    
    // Configurar fábrica de visualização personalizada
    DayView dayView = calendarView.getDayPage().getDetailedDayView().getDayView();
    calendarFXAdapter.configureCustomEntryViewFactory(dayView);
}
```

### 2. Cores por Tipo de Evento

As cores são automaticamente aplicadas baseadas no tipo do evento:

- **CARD**: Azul (#007bff)
- **TASK**: Verde (#28a745)
- **CUSTOM**: Amarelo (#ffc107)
- **MEETING**: Vermelho (#dc3545)
- **REMINDER**: Roxo (#6f42c1)

### 3. Opacidade por Prioridade

A opacidade é aplicada baseada na prioridade:

- **LOW**: 70% de opacidade
- **STANDARD**: 100% de opacidade
- **HIGH**: 100% de opacidade
- **URGENT**: 100% de opacidade

### 4. Estilos Especiais

- **Eventos Recorrentes**: Borda personalizada
- **Eventos de Dia Inteiro**: Estilo de borda sólida
- **Eventos Inativos**: Ocultos automaticamente

## Exemplo Completo

Veja a classe `CalendarUsageExample` para um exemplo completo de implementação com diferentes tipos de eventos e configurações.

## Arquivos Modificados

1. `src/main/java/org/desviante/calendar/adapter/CalendarFXAdapter.java`
   - Removido comentário TODO
   - Adicionado método `applyCustomEntryStyle`
   - Adicionado método `configureCustomEntryViewFactory`

2. `src/main/java/org/desviante/calendar/view/DayEntryViewPersonalizada.java` (novo)
   - Classe personalizada para configuração de cores e opacidade

3. `src/main/java/org/desviante/calendar/example/CalendarUsageExample.java` (novo)
   - Exemplo de uso da funcionalidade

## Benefícios

- ✅ Resolve o comentário TODO
- ✅ Implementa configuração real de cores e opacidade
- ✅ Mantém compatibilidade com a API do CalendarFX
- ✅ Suporte a diferentes tipos de eventos
- ✅ Configuração automática baseada em tipo e prioridade
- ✅ Documentação completa com exemplos

## Notas Técnicas

- A solução usa CSS do JavaFX para aplicar estilos
- A fábrica de visualização é configurada no `DayView`
- Os estilos são aplicados automaticamente baseados no `CalendarEventDTO`
- A implementação é compatível com todas as versões do CalendarFX que suportam `setEntryViewFactory`
