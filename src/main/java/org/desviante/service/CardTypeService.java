package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.CardType;
import org.desviante.repository.CardTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
 */
@Service
@RequiredArgsConstructor
public class CardTypeService {

    private final CardTypeRepository cardTypeRepository;

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
     * Remove um tipo de card.
     *
     * @param id identificador do tipo de card a ser removido
     * @return true se o tipo foi removido com sucesso
     * @throws ResourceNotFoundException se o tipo não for encontrado
     * @throws IllegalStateException se o tipo estiver sendo usado por cards
     */
    @Transactional
    public boolean deleteCardType(Long id) {
        // Verifica se o tipo existe
        CardType existingType = getCardTypeById(id);
        
        // TODO: Verificar se existem cards usando este tipo antes de remover
        // Esta verificação será implementada quando integrarmos com a entidade Card
        
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
} 