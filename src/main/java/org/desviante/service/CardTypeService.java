package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.config.AppMetadataConfig;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.exception.CardTypeInUseException;
import org.desviante.model.CardType;
import org.desviante.model.Card;
import org.desviante.repository.CardTypeRepository;
import org.desviante.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de negócio relacionadas aos tipos de card.
 * 
 * <p>Responsável por implementar a lógica de negócio para criação, atualização,
 * listagem e remoção de tipos de card. Esta camada de serviço garante a
 * integridade dos dados através de validações antes das operações de persistência.</p>
 * 
 * <p>Implementa regras de negócio importantes como validação de nomes únicos,
 * controle de datas de criação e atualização, e verificação de dependências
 * antes da remoção de tipos de card.</p>
 * 
 * <p>Utiliza transações para garantir consistência dos dados, com operações
 * de leitura marcadas como readOnly para otimização de performance.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CardType
 * @see CardTypeRepository
 * @see CardRepository
 * @see CardTypeInUseException
 */
@Service
@RequiredArgsConstructor
public class CardTypeService {

    private final CardTypeRepository cardTypeRepository;
    private final CardRepository cardRepository;
    private final AppMetadataConfig appMetadataConfig;

    /**
     * Lista todos os tipos de card disponíveis.
     *
     * @return lista de todos os tipos de card ordenados por nome
     */
    @Transactional(readOnly = true)
    public List<CardType> getAllCardTypes() {
        return cardTypeRepository.findAll();
    }

    /**
     * Busca um tipo de card pelo ID.
     *
     * @param id identificador do tipo de card
     * @return tipo de card encontrado
     * @throws ResourceNotFoundException se o tipo não for encontrado
     */
    @Transactional(readOnly = true)
    public CardType getCardTypeById(Long id) {
        return cardTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de card com ID " + id + " não encontrado"));
    }

    /**
     * Cria um novo tipo de card
     *
     * @param name nome do tipo de card (deve ser único)
     * @param unitLabel label da unidade de progresso
     * @return tipo de card criado
     * @throws IllegalArgumentException se o nome já existir ou for inválido
     */
    @Transactional
    public CardType createCardType(String name, String unitLabel) {
        // Validações básicas
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do tipo de card não pode ser vazio");
        }
        
        // Se o unitLabel estiver vazio, usar "unidade" como padrão
        if (unitLabel == null || unitLabel.trim().isEmpty()) {
            unitLabel = "unidade";
        }
        
        // Verifica se já existe um tipo com este nome
        if (cardTypeRepository.existsByName(name.trim())) {
            throw new IllegalArgumentException("Já existe um tipo de card com o nome '" + name + "'");
        }
        
        // Cria um novo tipo de Card
        CardType newType = CardType.builder()
                .name(name.trim())
                .unitLabel(unitLabel.trim())
                .build();
        
        return cardTypeRepository.save(newType);
    }

    /**
     * Atualiza um tipo de card existente.
     *
     * @param id identificador do tipo de card
     * @param name novo nome do tipo de card
     * @param unitLabel novo label da unidade
     * @return tipo de card atualizado
     * @throws ResourceNotFoundException se o tipo não for encontrado
     * @throws IllegalArgumentException se o nome já existir ou for inválido
     */
    @Transactional
    public CardType updateCardType(Long id, String name, String unitLabel) {
        // Busca o tipo existente
        CardType existingType = getCardTypeById(id);
        
        // Validações básicas
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do tipo de card não pode ser vazio");
        }
        
        // Se o unitLabel estiver vazio, usar "unidade" como padrão
        if (unitLabel == null || unitLabel.trim().isEmpty()) {
            unitLabel = "unidade";
        }
        
        // Verifica se já existe outro tipo com este nome (excluindo o atual)
        CardType existingWithName = cardTypeRepository.findByName(name.trim()).orElse(null);
        if (existingWithName != null && !existingWithName.getId().equals(id)) {
            throw new IllegalArgumentException("Já existe um tipo de card com o nome '" + name + "'");
        }
        
        // Atualiza os campos
        existingType.setName(name.trim());
        existingType.setUnitLabel(unitLabel.trim());
        existingType.setLastUpdateDate(LocalDateTime.now());
        
        return cardTypeRepository.update(existingType);
    }

    /**
     * Remove um tipo de card após verificar se não está sendo usado.
     *
     * <p>Esta operação realiza uma verificação de segurança antes da remoção,
     * garantindo que nenhum card esteja usando o tipo especificado. Se houver
     * cards dependentes, a operação é bloqueada e uma exceção é lançada com
     * informações detalhadas sobre os cards afetados.</p>
     * 
     * <p><strong>Verificações de Segurança:</strong></p>
     * <ul>
     *   <li>Verifica se o tipo de card existe</li>
     *   <li>Verifica se existem cards usando o tipo</li>
     *   <li>Bloqueia a remoção se houver dependências ativas</li>
     * </ul>
     * 
     * <p><strong>Tratamento de Erros:</strong></p>
     * <ul>
     *   <li>{@link ResourceNotFoundException}: Se o tipo não for encontrado</li>
     *   <li>{@link CardTypeInUseException}: Se o tipo estiver sendo usado por cards</li>
     * </ul>
     * 
     * @param id identificador do tipo de card a ser removido
     * @return {@code true} se o tipo foi removido com sucesso
     * @throws ResourceNotFoundException se o tipo não for encontrado
     * @throws CardTypeInUseException se o tipo estiver sendo usado por cards
     * 
     * @see CardRepository#existsByCardTypeId(Long)
     * @see CardRepository#countByCardTypeId(Long)
     * @see CardRepository#findByCardTypeId(Long)
     * @see CardTypeInUseException
     */
    @Transactional
    public boolean deleteCardType(Long id) {        
        // Verificar se o tipo de card existe
        CardType cardType = getCardTypeById(id);
        
        // Verificar se o tipo de card é o padrão
        Optional<Long> defaultCardTypeId = appMetadataConfig.getDefaultCardTypeId();
        if (defaultCardTypeId.isPresent() && defaultCardTypeId.get().equals(id)) {
            throw new CardTypeInUseException("Não é possível remover o tipo de card '" + cardType.getName() + 
                    "' pois ele está configurado como tipo padrão no sistema. " +
                    "Altere a configuração padrão antes de remover este tipo.");
        }

        // Verificar se existem cards usando este tipo antes de remover
        if (cardRepository.existsByCardTypeId(id)) {
            int cardCount = cardRepository.countByCardTypeId(id);
            List<Card> affectedCards = cardRepository.findByCardTypeId(id);
            
            // Construir mensagem detalhada sobre os cards afetados
            StringBuilder message = new StringBuilder();
            message.append("Não é possível remover o tipo de card '").append(cardType.getName()).append("' ");
            message.append("porque ele está sendo usado por ").append(cardCount).append(" card(s). ");
            message.append("Cards afetados: ");
            
            // Adicionar informações dos primeiros 5 cards para não sobrecarregar a mensagem
            int maxCardsToShow = Math.min(5, affectedCards.size());
            for (int i = 0; i < maxCardsToShow; i++) {
                Card card = affectedCards.get(i);
                if (i > 0) message.append(", ");
                message.append("'").append(card.getTitle()).append("'");
            }
            
            if (affectedCards.size() > maxCardsToShow) {
                message.append(" e mais ").append(affectedCards.size() - maxCardsToShow).append(" card(s)");
            }
            
            message.append(". Remova ou migre todos os cards para outro tipo antes de remover este tipo.");
            
            throw new CardTypeInUseException(message.toString());
        }
        
        // Se não há cards usando o tipo, proceder com a remoção
        boolean deleted = cardTypeRepository.deleteById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Tipo de Card com ID " + id + " não encontrado");
        }
        
        return true;
    }

    /**
     * Verifica se um tipo de card existe pelo nome.
     *
     * @param name nome do tipo de card
     * @return true se o tipo existe, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return cardTypeRepository.existsByName(name);
    }

    /**
     * Obtém o ID do tipo padrão Card.
     *
     * @return ID do tipo Card padrão
     */
    @Transactional(readOnly = true)
    public Long getDefaultCardTypeId() {
        return cardTypeRepository.findByName("Card")
                .map(CardType::getId)
                .orElse(null);
    }

    /**
     * Verifica se um tipo de card pode ser removido com segurança.
     * 
     * <p>Este método realiza a mesma verificação de segurança que o método
     * {@link #deleteCardType(Long)}, mas sem executar a remoção. É útil
     * para interfaces que precisam verificar a viabilidade da operação
     * antes de permitir que o usuário tente remover o tipo.</p>
     * 
     * <p><strong>Verificações Realizadas:</strong></p>
     * <ul>
     *   <li>Verifica se o tipo de card existe</li>
     *   <li>Verifica se existem cards usando o tipo</li>
     *   <li>Retorna informações detalhadas sobre a viabilidade da remoção</li>
     * </ul>
     * 
     * @param id identificador do tipo de card a ser verificado
     * @return {@link CardTypeRemovalCheck} contendo informações sobre a viabilidade da remoção
     * @throws ResourceNotFoundException se o tipo não for encontrado
     * 
     * @see CardTypeRemovalCheck
     * @see CardRepository#existsByCardTypeId(Long)
     * @see CardRepository#countByCardTypeId(Long)
     * @see CardRepository#findByCardTypeId(Long)
     */
    @Transactional(readOnly = true)
    public CardTypeRemovalCheck canDeleteCardType(Long id) {
        // Verificar se o tipo de card existe
        CardType cardType = getCardTypeById(id);
        
        // Verificar se o tipo de card é o padrão
        Optional<Long> defaultCardTypeId = appMetadataConfig.getDefaultCardTypeId();
        if (defaultCardTypeId.isPresent() && defaultCardTypeId.get().equals(id)) {
            return CardTypeRemovalCheck.builder()
                    .canDelete(false)
                    .cardType(cardType)
                    .cardCount(0)
                    .affectedCards(List.of())
                    .reason("Este tipo de card é o padrão e não pode ser removido.")
                    .build();
        }

        // Verificar se existem cards usando este tipo
        boolean hasCards = cardRepository.existsByCardTypeId(id);
        
        if (hasCards) {
            int cardCount = cardRepository.countByCardTypeId(id);
            List<Card> affectedCards = cardRepository.findByCardTypeId(id);
            
            return CardTypeRemovalCheck.builder()
                    .canDelete(false)
                    .cardType(cardType)
                    .cardCount(cardCount)
                    .affectedCards(affectedCards)
                    .reason("Tipo está sendo usado por " + cardCount + " card(s)")
                    .build();
        } else {
            return CardTypeRemovalCheck.builder()
                    .canDelete(true)
                    .cardType(cardType)
                    .cardCount(0)
                    .affectedCards(List.of())
                    .reason("Nenhum card está usando este tipo")
                    .build();
        }
    }

    /**
     * Classe interna para representar o resultado da verificação de remoção de tipo de card.
     * 
     * <p>Esta classe encapsula todas as informações necessárias para determinar
     * se um tipo de card pode ser removido com segurança e quais cards seriam
     * afetados pela remoção.</p>
     * 
     * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
     * @version 1.0
     * @since 1.0
     */
    public static class CardTypeRemovalCheck {
        private final boolean canDelete;
        private final CardType cardType;
        private final int cardCount;
        private final List<Card> affectedCards;
        private final String reason;
        
        private CardTypeRemovalCheck(Builder builder) {
            this.canDelete = builder.canDelete;
            this.cardType = builder.cardType;
            this.cardCount = builder.cardCount;
            this.affectedCards = builder.affectedCards;
            this.reason = builder.reason;
        }
        
        // Getters
        public boolean canDelete() { return canDelete; }
        public CardType getCardType() { return cardType; }
        public int getCardCount() { return cardCount; }
        public List<Card> getAffectedCards() { return affectedCards; }
        public String getReason() { return reason; }
        
        /**
         * Builder para construção de instâncias de CardTypeRemovalCheck.
         */
        public static class Builder {
            private boolean canDelete;
            private CardType cardType;
            private int cardCount;
            private List<Card> affectedCards;
            private String reason;
            
            public Builder canDelete(boolean canDelete) {
                this.canDelete = canDelete;
                return this;
            }
            
            public Builder cardType(CardType cardType) {
                this.cardType = cardType;
                return this;
            }
            
            public Builder cardCount(int cardCount) {
                this.cardCount = cardCount;
                return this;
            }
            
            public Builder affectedCards(List<Card> affectedCards) {
                this.affectedCards = affectedCards;
                return this;
            }
            
            public Builder reason(String reason) {
                this.reason = reason;
                return this;
            }
            
            public CardTypeRemovalCheck build() {
                return new CardTypeRemovalCheck(this);
            }
        }
        
        public static Builder builder() {
            return new Builder();
        }
    }
} 