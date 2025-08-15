package org.desviante.service;

import org.desviante.config.AppMetadataConfig;
import org.desviante.model.Board;
import org.desviante.model.BoardGroup;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.service.dto.BoardSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes específicos para a funcionalidade de grupo padrão no TaskManagerFacade.
 * 
 * <p>Foca na verificação de que o grupo padrão configurado é aplicado
 * automaticamente aos novos boards criados.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class TaskManagerFacadeDefaultGroupTest {

    @Mock
    private BoardService boardService;
    
    @Mock
    private BoardColumnService columnService;
    
    @Mock
    private CardService cardService;
    
    @Mock
    private TaskService taskService;
    
    @Mock
    private BoardGroupService boardGroupService;
    
    @Mock
    private CardTypeService cardTypeService;
    
    @Mock
    private org.desviante.repository.CheckListItemRepository checkListItemRepository;
    
    @Mock
    private AppMetadataConfig appMetadataConfig;
    
    private TaskManagerFacade taskManagerFacade;
    
    @BeforeEach
    void setUp() {
        taskManagerFacade = new TaskManagerFacade(
            boardService, columnService, cardService, taskService,
            boardGroupService, cardTypeService, checkListItemRepository, appMetadataConfig
        );
    }
    
    @Test
    @DisplayName("Deve criar board sem grupo quando nenhum grupo padrão está configurado")
    void shouldCreateBoardWithoutGroupWhenNoDefaultGroupConfigured() {
        // Arrange
        Board newBoard = createTestBoard(1L, "Meu Board", null);
        when(boardService.createBoard("Meu Board")).thenReturn(newBoard);
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.empty());
        
        // Act
        BoardSummaryDTO result = taskManagerFacade.createNewBoard("Meu Board");
        
        // Assert
        assertNotNull(result);
        assertEquals("Meu Board", result.name());
        assertNull(result.group()); // Board deve ser criado sem grupo
        
        // Verificar que o board não foi atualizado com grupo
        verify(boardService, never()).updateBoard(any(Board.class));
        
        // Verificar que as colunas foram criadas
        verify(columnService).createColumn("A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        verify(columnService).createColumn("Em Andamento", 1, BoardColumnKindEnum.PENDING, 1L);
        verify(columnService).createColumn("Concluído", 2, BoardColumnKindEnum.FINAL, 1L);
    }
    
    @Test
    @DisplayName("Deve aplicar grupo padrão automaticamente quando configurado")
    void shouldApplyDefaultGroupAutomaticallyWhenConfigured() {
        // Arrange
        Board newBoard = createTestBoard(1L, "Meu Board", null);
        Board updatedBoard = createTestBoard(1L, "Meu Board", 123L);
        
        when(boardService.createBoard("Meu Board")).thenReturn(newBoard);
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.of(123L));
        when(boardService.updateBoard(any(Board.class))).thenReturn(updatedBoard);
        
        // Act
        BoardSummaryDTO result = taskManagerFacade.createNewBoard("Meu Board");
        
        // Assert
        assertNotNull(result);
        assertEquals("Meu Board", result.name());
        
        // Verificar que o board foi atualizado com o grupo padrão
        verify(boardService).updateBoard(any(Board.class));
        
        // Verificar que as colunas foram criadas
        verify(columnService).createColumn("A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        verify(columnService).createColumn("Em Andamento", 1, BoardColumnKindEnum.PENDING, 1L);
        verify(columnService).createColumn("Concluído", 2, BoardColumnKindEnum.FINAL, 1L);
    }
    
    @Test
    @DisplayName("Deve lidar com grupo padrão configurado como null")
    void shouldHandleDefaultGroupConfiguredAsNull() {
        // Arrange
        Board newBoard = createTestBoard(1L, "Meu Board", null);
        when(boardService.createBoard("Meu Board")).thenReturn(newBoard);
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.empty());
        
        // Act
        BoardSummaryDTO result = taskManagerFacade.createNewBoard("Meu Board");
        
        // Assert
        assertNotNull(result);
        assertEquals("Meu Board", result.name());
        assertNull(result.group()); // Board deve ser criado sem grupo
        
        // Verificar que o board não foi atualizado
        verify(boardService, never()).updateBoard(any(Board.class));
        
        // Verificar que as colunas foram criadas
        verify(columnService).createColumn("A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        verify(columnService).createColumn("Em Andamento", 1, BoardColumnKindEnum.PENDING, 1L);
        verify(columnService).createColumn("Concluído", 2, BoardColumnKindEnum.FINAL, 1L);
    }
    
    @Test
    @DisplayName("Deve aplicar grupo padrão com ID zero")
    void shouldApplyDefaultGroupWithZeroId() {
        // Arrange
        Board newBoard = createTestBoard(1L, "Meu Board", null);
        Board updatedBoard = createTestBoard(1L, "Meu Board", 0L);
        
        when(boardService.createBoard("Meu Board")).thenReturn(newBoard);
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.of(0L));
        when(boardService.updateBoard(any(Board.class))).thenReturn(updatedBoard);
        
        // Act
        BoardSummaryDTO result = taskManagerFacade.createNewBoard("Meu Board");
        
        // Assert
        assertNotNull(result);
        assertEquals("Meu Board", result.name());
        
        // Verificar que o board foi atualizado com o grupo padrão (mesmo sendo ID 0)
        verify(boardService).updateBoard(any(Board.class));
    }
    
    @Test
    @DisplayName("Deve manter comportamento existente para outros métodos")
    void shouldMaintainExistingBehaviorForOtherMethods() {
        // Arrange
        Board newBoard = createTestBoard(1L, "Meu Board", null);
        when(boardService.createBoard("Meu Board")).thenReturn(newBoard);
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.empty());
        
        // Act
        BoardSummaryDTO result = taskManagerFacade.createNewBoard("Meu Board");
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Meu Board", result.name());
        assertEquals("Vazio", result.status()); // Status padrão para board vazio
        assertEquals(0, result.percentInitial());
        assertEquals(0, result.percentPending());
        assertEquals(0, result.percentFinal());
    }
    
    // Método auxiliar para criar boards de teste
    private Board createTestBoard(Long id, String name, Long groupId) {
        Board board = new Board();
        board.setId(id);
        board.setName(name);
        board.setCreationDate(LocalDateTime.now());
        board.setGroupId(groupId);
        
        if (groupId != null) {
            BoardGroup group = new BoardGroup();
            group.setId(groupId);
            group.setName("Grupo " + groupId);
            board.setGroup(group);
        }
        
        return board;
    }
}
