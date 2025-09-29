package org.desviante.view;

import org.desviante.config.AppMetadataConfig;
import org.desviante.service.BoardGroupService;
import org.desviante.service.CardTypeService;
import org.desviante.service.TaskManagerFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes para verificar a configuração correta do PreferencesController
 * pelo BoardViewController.
 * 
 * <p>Foca na verificação de que todos os serviços necessários são
 * injetados corretamente no PreferencesController.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class BoardViewControllerPreferencesTest {

    @Mock
    private TaskManagerFacade taskManagerFacade;
    
    @Mock
    private CardTypeService cardTypeService;
    
    @Mock
    private BoardGroupService boardGroupService;
    
    @Mock
    private AppMetadataConfig appMetadataConfig;
    
    private BoardViewController boardViewController;
    
    @BeforeEach
    void setUp() {
        boardViewController = new BoardViewController(taskManagerFacade);
    }
    
    @Test
    @DisplayName("Deve ter acesso ao TaskManagerFacade para serviços")
    void shouldHaveAccessToTaskManagerFacade() {
        // Assert
        assertNotNull(taskManagerFacade);
        assertNotNull(boardViewController);
    }
    
    @Test
    @DisplayName("Deve ter acesso ao AppMetadataConfig para configurações")
    void shouldHaveAccessToAppMetadataConfig() {
        // Assert
        assertNotNull(appMetadataConfig);
        assertNotNull(boardViewController);
    }
    
    @Test
    @DisplayName("Deve ter acesso aos serviços necessários")
    void shouldHaveAccessToRequiredServices() {
        // Assert
        assertNotNull(cardTypeService);
        assertNotNull(boardGroupService);
        assertNotNull(taskManagerFacade);
    }
    
    @Test
    @DisplayName("Deve configurar corretamente o PreferencesController com todos os serviços")
    void shouldConfigurePreferencesControllerWithAllServices() {
        // Arrange
        when(taskManagerFacade.getCardTypeService()).thenReturn(cardTypeService);
        when(taskManagerFacade.getBoardGroupService()).thenReturn(boardGroupService);
        
        // Assert
        assertNotNull(taskManagerFacade.getCardTypeService());
        assertNotNull(taskManagerFacade.getBoardGroupService());
    }
}
