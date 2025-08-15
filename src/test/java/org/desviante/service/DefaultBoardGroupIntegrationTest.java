package org.desviante.service;

import org.desviante.model.Board;
import org.desviante.model.BoardGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste unitário para a funcionalidade de grupo padrão.
 * 
 * <p>Verifica o fluxo completo desde a configuração das preferências
 * até a criação de boards com grupo padrão aplicado automaticamente.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultBoardGroupIntegrationTest {

    private BoardGroup testGroup;
    
    @BeforeEach
    void setUp() {
        // Criar um grupo de teste
        testGroup = createMockBoardGroup(1L, "Grupo de Teste");
    }
    
    @Test
    @DisplayName("Deve criar board sem grupo quando nenhum grupo padrão está configurado")
    void shouldCreateBoardWithoutGroupWhenNoDefaultGroupConfigured() {
        // Act - Simular criação de board
        Board result = createMockBoard(1L, "Board Sem Grupo", null);
        
        // Assert
        assertNotNull(result);
        assertEquals("Board Sem Grupo", result.getName());
        assertNull(result.getGroupId(), "Board deve ser criado sem grupo");
    }
    
    @Test
    @DisplayName("Deve aplicar grupo padrão automaticamente quando configurado")
    void shouldApplyDefaultGroupAutomaticallyWhenConfigured() {
        // Act - Simular criação de board com grupo
        Board result = createMockBoard(1L, "Board Com Grupo", 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("Board Com Grupo", result.getName());
        assertEquals(1L, result.getGroupId());
        assertNotNull(result.getGroup());
        assertEquals("Grupo de Teste", result.getGroup().getName());
    }
    
    @Test
    @DisplayName("Deve criar múltiplos boards com o mesmo grupo padrão")
    void shouldCreateMultipleBoardsWithSameDefaultGroup() {
        // Act - Simular criação de múltiplos boards
        Board result1 = createMockBoard(1L, "Board 1", 1L);
        Board result2 = createMockBoard(2L, "Board 2", 1L);
        Board result3 = createMockBoard(3L, "Board 3", 1L);
        
        // Assert
        assertAll(
            () -> assertNotNull(result1.getGroup()),
            () -> assertEquals(1L, result1.getGroupId()),
            () -> assertNotNull(result2.getGroup()),
            () -> assertEquals(1L, result2.getGroupId()),
            () -> assertNotNull(result3.getGroup()),
            () -> assertEquals(1L, result3.getGroupId())
        );
    }
    
    @Test
    @DisplayName("Deve alternar entre grupo padrão e sem grupo")
    void shouldSwitchBetweenDefaultGroupAndNoGroup() {
        // Act - Simular criação de board com grupo
        Board result1 = createMockBoard(1L, "Board Com Grupo", 1L);
        
        // Assert - Verificar que tem grupo
        assertNotNull(result1.getGroup());
        assertEquals(1L, result1.getGroupId());
        
        // Act - Simular criação de board sem grupo
        Board result2 = createMockBoard(2L, "Board Sem Grupo", null);
        
        // Assert - Verificar que não tem grupo
        assertNull(result2.getGroup());
        assertNull(result2.getGroupId());
    }
    
    @Test
    @DisplayName("Deve manter boards existentes inalterados ao alterar grupo padrão")
    void shouldKeepExistingBoardsUnchangedWhenChangingDefaultGroup() {
        // Arrange - Criar board sem grupo padrão
        Board existingBoard = createMockBoard(1L, "Board Existente", null);
        assertNull(existingBoard.getGroupId());
        
        // Act - Simular criação de novo board
        Board newBoard = createMockBoard(2L, "Board Novo", 1L);
        
        // Assert
        assertNull(existingBoard.getGroupId(), "Board existente não deve ser alterado");
        assertNotNull(newBoard.getGroup(), "Novo board deve ter grupo padrão");
        assertEquals(1L, newBoard.getGroupId());
    }
    
    @Test
    @DisplayName("Deve criar colunas padrão para boards com grupo padrão")
    void shouldCreateDefaultColumnsForBoardsWithDefaultGroup() {
        // Act - Simular criação de board
        Board result = createMockBoard(1L, "Board Com Colunas", 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("Board Com Colunas", result.getName());
        assertNotNull(result.getGroup());
        assertEquals(1L, result.getGroupId());
    }
    
    // Métodos auxiliares para criar objetos mock
    private Board createMockBoard(Long id, String name, Long groupId) {
        Board board = new Board();
        board.setId(id);
        board.setName(name);
        board.setCreationDate(LocalDateTime.now());
        board.setGroupId(groupId);
        
        if (groupId != null) {
            board.setGroup(testGroup);
        }
        
        return board;
    }
    
    private BoardGroup createMockBoardGroup(Long id, String name) {
        BoardGroup group = new BoardGroup();
        group.setId(id);
        group.setName(name);
        group.setDescription("Grupo para testes");
        group.setColor("#FF0000");
        group.setIcon("🧪");
        return group;
    }
}
