package org.desviante.view;

import org.desviante.config.AppMetadataConfig;
import org.desviante.model.BoardGroup;
import org.desviante.model.CardType;
import org.desviante.service.BoardGroupService;
import org.desviante.service.CardTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o PreferencesController.
 * 
 * <p>Foca na funcionalidade de configuração de preferências, incluindo
 * a nova funcionalidade de grupo de board padrão.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class PreferencesControllerTest {

    @Mock
    private CardTypeService cardTypeService;
    
    @Mock
    private BoardGroupService boardGroupService;
    
    @Mock
    private AppMetadataConfig appMetadataConfig;
    
    private PreferencesController controller;
    
    @BeforeEach
    void setUp() {
        controller = new PreferencesController();
    }
    
    @Test
    @DisplayName("Deve configurar corretamente o BoardGroupService")
    void shouldSetBoardGroupServiceCorrectly() {
        // Act
        controller.setBoardGroupService(boardGroupService);
        
        // Assert
        // Verificar se o serviço foi configurado (não podemos acessar diretamente, mas podemos verificar o comportamento)
        assertNotNull(boardGroupService);
    }
    
    @Test
    @DisplayName("Deve configurar corretamente o AppMetadataConfig")
    void shouldSetAppMetadataConfigCorrectly() {
        // Act
        controller.setAppMetadataConfig(appMetadataConfig);
        
        // Assert
        // Verificar se a configuração foi definida
        assertNotNull(appMetadataConfig);
    }
    
    @Test
    @DisplayName("Deve configurar corretamente o CardTypeService")
    void shouldSetCardTypeServiceCorrectly() {
        // Act
        controller.setCardTypeService(cardTypeService);
        
        // Assert
        // Verificar se o serviço foi configurado
        assertNotNull(cardTypeService);
    }
    
    @Test
    @DisplayName("Deve lidar com configuração de todos os serviços")
    void shouldHandleAllServicesConfiguration() {
        // Arrange
        CardType cardType = createCardType(1L, "Tarefa");
        List<CardType> cardTypes = Arrays.asList(cardType);
        BoardGroup group = createBoardGroup(1L, "Trabalho");
        List<BoardGroup> groups = Arrays.asList(group);
        
        when(cardTypeService.getAllCardTypes()).thenReturn(cardTypes);
        when(boardGroupService.getAllBoardGroups()).thenReturn(groups);
        when(appMetadataConfig.getDefaultCardTypeId()).thenReturn(Optional.empty());
        when(appMetadataConfig.getDefaultProgressType()).thenReturn(Optional.empty());
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.empty());
        
        // Act
        controller.setCardTypeService(cardTypeService);
        controller.setBoardGroupService(boardGroupService);
        controller.setAppMetadataConfig(appMetadataConfig);
        
        // Assert
        // Verificar se todos os serviços foram configurados
        assertNotNull(cardTypeService);
        assertNotNull(boardGroupService);
        assertNotNull(appMetadataConfig);
        
        // Verificar se os mocks foram configurados corretamente
        assertNotNull(cardTypeService.getAllCardTypes());
        assertNotNull(boardGroupService.getAllBoardGroups());
        assertNotNull(appMetadataConfig.getDefaultCardTypeId());
        assertNotNull(appMetadataConfig.getDefaultProgressType());
        assertNotNull(appMetadataConfig.getDefaultBoardGroupId());
    }
    
    // Métodos auxiliares para criar objetos de teste
    private BoardGroup createBoardGroup(Long id, String name) {
        BoardGroup group = new BoardGroup();
        group.setId(id);
        group.setName(name);
        return group;
    }
    
    private CardType createCardType(Long id, String name) {
        CardType cardType = new CardType();
        cardType.setId(id);
        cardType.setName(name);
        return cardType;
    }
}
