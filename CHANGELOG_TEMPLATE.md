# 📋 Template do Changelog

Este documento serve como guia para manter o changelog do projeto organizado e consistente.

## 📝 Como Documentar Mudanças

### Estrutura de Versão
```
### [vX.Y.Z] - YYYY-MM-DD

#### ✨ Novas Funcionalidades
- **Nome da Funcionalidade**: Descrição detalhada

#### 🔧 Correções Técnicas
- **Problema Resolvido**: Descrição da correção

#### 🎨 Melhorias de UX
- **Melhoria**: Descrição da melhoria na experiência do usuário

#### 🐛 Correções de Bugs
- **Bug**: Descrição do bug corrigido

#### ⚠️ Mudanças Importantes
- **Breaking Change**: Descrição de mudanças que quebram compatibilidade
```

### Emojis para Categorização

- **✨ Novas Funcionalidades**: `✨`
- **🔧 Correções Técnicas**: `🔧`
- **🎨 Melhorias de UX**: `🎨`
- **🐛 Correções de Bugs**: `🐛`
- **⚠️ Mudanças Importantes**: `⚠️`
- **📚 Documentação**: `📚`
- **🧪 Testes**: `🧪`
- **⚡ Performance**: `⚡`
- **🔒 Segurança**: `🔒`

### Exemplo de Entrada

```markdown
### [v1.2.1] - 2025-01-09

#### ✨ Novas Funcionalidades
- **Filtros Avançados**: Adicionada funcionalidade de filtros por status e data
- **Exportação de Dados**: Possibilidade de exportar boards em formato CSV

#### 🔧 Correções Técnicas
- **Validação de Dados**: Corrigido problema de validação em campos de progresso
- **Performance**: Otimizada consulta de cards para melhor performance

#### 🎨 Melhorias de UX
- **Interface Responsiva**: Melhorada adaptação em telas menores
- **Feedback Visual**: Adicionadas animações suaves para transições

#### 🐛 Correções de Bugs
- **Movimentação de Cards**: Corrigido bug que impedia movimentação em colunas vazias
- **Persistência**: Corrigido problema de salvamento em casos específicos
```

## 📋 Checklist para Nova Versão

Antes de adicionar uma nova entrada no changelog:

- [ ] **Versão Definida**: Número de versão seguindo semver (X.Y.Z)
- [ ] **Data Correta**: Data de lançamento da versão
- [ ] **Categorização**: Mudanças organizadas por tipo (funcionalidades, correções, etc.)
- [ ] **Descrições Claras**: Explicações detalhadas mas concisas
- [ ] **Emojis Consistentes**: Uso correto dos emojis para categorização
- [ ] **Ordem Cronológica**: Versões mais recentes no topo
- [ ] **Links Relevantes**: Referências a issues ou PRs quando aplicável

## 🔗 Integração com Issues

Para melhor rastreabilidade, inclua referências a issues:

```markdown
- **Controles de Movimentação**: Adicionados botões ↑/↓ para mover cards (#123)
- **Validação de Dados**: Corrigido erro de validação (#124, #125)
```

## 📊 Histórico de Versões

Mantenha um registro das principais versões:

- **v1.2.0**: Controles de movimentação e melhorias de UX
- **v1.1.0**: Sistema de progresso e validação
- **v1.0.0**: Lançamento inicial com funcionalidades básicas

---

**💡 Dica**: Mantenha o changelog sempre atualizado para facilitar o acompanhamento das evoluções do projeto!
