package org.desviante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.config.AppMetadataConfig;
import org.desviante.model.CardType;
import org.desviante.model.enums.ProgressType;
import org.desviante.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciar configurações padrão da aplicação.
 * 
 * <p>Este serviço utiliza os metadados carregados para fornecer
 * configurações padrão para criação de cards e outras funcionalidades
 * da aplicação.</p>
 * 
 * <p>Principais responsabilidades:</p>
 * <ul>
 *   <li>Fornecer tipo de card padrão para novos cards</li>
 *   <li>Fornecer tipo de progresso padrão para novos cards</li>
 *   <li>Gerenciar configurações de interface padrão</li>
 *   <li>Validar configurações carregadas</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultConfigurationService {
    
    private final AppMetadataConfig metadataConfig;
    private final CardTypeService cardTypeService;
    private final CardRepository cardRepository;
    
    /**
     * Obtém o tipo de card padrão para novos cards.
     * 
     * <p>Se não houver um tipo padrão configurado nos metadados,
     * retorna o primeiro tipo disponível no sistema.</p>
     * 
     * @return tipo de card padrão
     */
    public CardType getDefaultCardType() {
        // Primeiro, tenta obter do metadados
        Optional<Long> defaultCardTypeId = metadataConfig.getDefaultCardTypeId();
        
        if (defaultCardTypeId.isPresent()) {
            try {
                CardType cardType = cardTypeService.getCardTypeById(defaultCardTypeId.get());
                log.debug("Usando tipo de card padrão dos metadados: {}", cardType.getName());
                return cardType;
            } catch (Exception e) {
                log.warn("Tipo de card padrão configurado nos metadados não foi encontrado: {}", defaultCardTypeId.get());
            }
        }
        
        // Fallback: usa o primeiro tipo disponível
        try {
            List<CardType> allCardTypes = cardTypeService.getAllCardTypes();
            if (!allCardTypes.isEmpty()) {
                CardType firstCardType = allCardTypes.get(0);
                log.debug("Usando primeiro tipo de card disponível como padrão: {}", firstCardType.getName());
                return firstCardType;
            }
        } catch (Exception e) {
            log.error("Erro ao buscar tipos de card disponíveis", e);
        }
        
        // Último fallback: cria um tipo padrão básico
        log.warn("Nenhum tipo de card encontrado, criando tipo padrão básico");
        return createFallbackCardType();
    }
    
    /**
     * Obtém o tipo de progresso padrão para novos cards.
     * 
     * @return tipo de progresso padrão
     */
    public ProgressType getDefaultProgressType() {
        Optional<ProgressType> defaultProgressType = metadataConfig.getDefaultProgressType();
        
        if (defaultProgressType.isPresent()) {
            log.debug("Usando tipo de progresso padrão dos metadados: {}", defaultProgressType.get());
            return defaultProgressType.get();
        }
        
        // Fallback: usa NONE como padrão
        log.debug("Usando tipo de progresso padrão: NONE");
        return ProgressType.NONE;
    }
    
    /**
     * Obtém o ID do tipo de card padrão.
     * 
     * @return ID do tipo de card padrão ou null se não definido
     */
    public Long getDefaultCardTypeId() {
        CardType defaultCardType = getDefaultCardType();
        return defaultCardType != null ? defaultCardType.getId() : null;
    }
    
    /**
     * Define o tipo de card padrão nos metadados.
     * 
     * @param cardTypeId ID do tipo de card a ser definido como padrão
     * @throws Exception se houver erro ao salvar os metadados
     */
    public void setDefaultCardType(Long cardTypeId) throws Exception {
        // Valida se o tipo de card existe
        try {
            CardType cardType = cardTypeService.getCardTypeById(cardTypeId);
            
            // Atualiza os metadados
            metadataConfig.updateMetadata(metadata -> metadata.setDefaultCardTypeId(cardTypeId));
            log.info("Tipo de card padrão definido como: {} (ID: {})", cardType.getName(), cardTypeId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Tipo de card com ID " + cardTypeId + " não encontrado", e);
        }
    }
    
    /**
     * Define o tipo de progresso padrão nos metadados.
     * 
     * @param progressType tipo de progresso a ser definido como padrão
     * @throws Exception se houver erro ao salvar os metadados
     */
    public void setDefaultProgressType(ProgressType progressType) throws Exception {
        if (progressType == null) {
            throw new IllegalArgumentException("Tipo de progresso não pode ser null");
        }
        
        // Verificar se o tipo de progresso atual está sendo usado como padrão
        Optional<ProgressType> currentDefault = metadataConfig.getDefaultProgressType();
        if (currentDefault.isPresent() && currentDefault.get() != progressType) {
            // Verificar se o tipo atual está sendo usado em cards
            if (isProgressTypeInUse(currentDefault.get())) {
                throw new IllegalArgumentException("Não é possível alterar o tipo de progresso padrão '" + 
                    currentDefault.get().getDisplayName() + "' pois ele está sendo usado por cards no sistema. " +
                    "Migre os cards para outro tipo de progresso antes de alterar a configuração padrão.");
            }
        }
        
        // Atualiza os metadados
        metadataConfig.updateMetadata(metadata -> metadata.setDefaultProgressType(progressType));
        log.info("Tipo de progresso padrão definido como: {}", progressType);
    }
    
    /**
     * Cria um tipo de card padrão básico como fallback.
     * 
     * @return tipo de card padrão básico
     */
    private CardType createFallbackCardType() {
        try {
            // Tenta criar um tipo básico
            CardType savedType = cardTypeService.createCardType("Tarefa", "etapas");
            log.info("Tipo de card padrão básico criado: {} (ID: {})", savedType.getName(), savedType.getId());
            
            // Define como padrão nos metadados
            setDefaultCardType(savedType.getId());
            
            return savedType;
            
        } catch (Exception e) {
            log.error("Erro ao criar tipo de card padrão básico", e);
            
            // Retorna um objeto básico sem persistir
            return CardType.builder()
                    .id(1L)
                    .name("Tarefa")
                    .unitLabel("etapas")
                    .build();
        }
    }
    
    /**
     * Verifica se as configurações padrão estão válidas.
     * 
     * @return true se válidas, false caso contrário
     */
    public boolean validateDefaultConfigurations() {
        try {
            // Verifica se o tipo de card padrão existe
            CardType defaultCardType = getDefaultCardType();
            if (defaultCardType == null) {
                log.error("Configuração inválida: tipo de card padrão não encontrado");
                return false;
            }
            
            // Verifica se o tipo de progresso padrão é válido
            ProgressType defaultProgressType = getDefaultProgressType();
            if (defaultProgressType == null) {
                log.error("Configuração inválida: tipo de progresso padrão é null");
                return false;
            }
            
            log.debug("Configurações padrão validadas com sucesso");
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao validar configurações padrão", e);
            return false;
        }
    }
    
    /**
     * Obtém informações sobre as configurações padrão atuais.
     * 
     * @return string com informações das configurações
     */
    public String getDefaultConfigurationsInfo() {
        try {
            CardType defaultCardType = getDefaultCardType();
            ProgressType defaultProgressType = getDefaultProgressType();
            
            return String.format(
                "Configurações Padrão:\n" +
                "- Tipo de Card: %s (ID: %d)\n" +
                "- Tipo de Progresso: %s\n" +
                "- Arquivo de Metadados: %s",
                defaultCardType != null ? defaultCardType.getName() : "N/A",
                defaultCardType != null ? defaultCardType.getId() : -1,
                defaultProgressType != null ? defaultProgressType.name() : "N/A",
                metadataConfig.getMetadataFilePath()
            );
            
        } catch (Exception e) {
            return "Erro ao obter informações das configurações: " + e.getMessage();
        }
    }

    /**
     * Verifica se um tipo de progresso está sendo usado por algum card no sistema.
     * 
     * @param progressType o tipo de progresso a ser verificado
     * @return true se estiver sendo usado, false caso contrário
     */
    private boolean isProgressTypeInUse(ProgressType progressType) {
        return cardRepository.existsByProgressType(progressType);
    }
}
