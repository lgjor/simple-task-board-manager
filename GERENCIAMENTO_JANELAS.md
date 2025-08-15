# Gerenciamento Automático de Janelas Secundárias

## Problema Resolvido

**Antes:** Ao fechar a janela principal, janelas secundárias permaneciam abertas
**Depois:** Todas as janelas secundárias são fechadas automaticamente

## Como Funciona

### 1. WindowManager
- Registra todas as janelas secundárias
- Fecha automaticamente ao sair
- Previne vazamentos de memória

### 2. Registro Automático
```java
Stage stage = new Stage();
windowManager.registerWindow(stage, "Título da Janela");
stage.show();
```

### 3. Fechamento Automático
- Ao fechar a janela principal
- Todas as secundárias são fechadas
- Recursos são liberados

## Janelas Gerenciadas

- Gerenciamento de Tipos de Card
- Gerenciamento de Grupos de Board  
- Preferências

## Vantagens

- Experiência de usuário consistente
- Sem janelas órfãs
- Prevenção de vazamentos
- Código limpo e centralizado
