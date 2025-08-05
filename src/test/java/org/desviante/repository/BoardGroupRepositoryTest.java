package org.desviante.repository;

import org.desviante.config.DataConfig;
import org.desviante.model.BoardGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(classes = DataConfig.class)
@Sql(scripts = "/test-schema.sql")
@Transactional // Garante que cada teste rode em uma transação isolada e seja revertido
class BoardGroupRepositoryTest {

    @Autowired
    private BoardGroupRepository boardGroupRepository;

    @Test
    @DisplayName("Deve injetar o BoardGroupRepository corretamente")
    void shouldInjectBoardGroupRepository() {
        // ASSERT
        assertNotNull(boardGroupRepository, "BoardGroupRepository deve ser injetado");
    }

    @Test
    @DisplayName("Deve salvar um grupo com sucesso")
    void shouldSaveGroupSuccessfully() {
        // Given
        BoardGroup testGroup = new BoardGroup();
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setColor("#FF5733");
        testGroup.setIcon("📁");
        testGroup.setCreationDate(LocalDateTime.now());
        // Removido setDefault - não precisamos mais de grupo padrão

        // When
        BoardGroup savedGroup = boardGroupRepository.save(testGroup);

        // Then
        assertThat(savedGroup).isNotNull();
        assertThat(savedGroup.getId()).isNotNull();
        assertThat(savedGroup.getName()).isEqualTo("Test Group");
        assertThat(savedGroup.getDescription()).isEqualTo("Test Description");
        assertThat(savedGroup.getColor()).isEqualTo("#FF5733");
        assertThat(savedGroup.getIcon()).isEqualTo("📁");
        // Removido assert isDefault - não precisamos mais de grupo padrão
    }

    @Test
    @DisplayName("Deve retornar todos os grupos ordenados por nome")
    void shouldReturnAllGroupsOrderedByName() {
        // Given
        BoardGroup group1 = new BoardGroup();
        group1.setName("B Group");
        group1.setDescription("Description B");
        group1.setColor("#FF5733");
        group1.setIcon("📁");
        group1.setCreationDate(LocalDateTime.now());
        // Removido setDefault - não precisamos mais de grupo padrão

        BoardGroup group2 = new BoardGroup();
        group2.setName("A Group");
        group2.setDescription("Description A");
        group2.setColor("#33FF57");
        group2.setIcon("🎯");
        group2.setCreationDate(LocalDateTime.now());
        // Removido setDefault - não precisamos mais de grupo padrão

        boardGroupRepository.save(group1);
        boardGroupRepository.save(group2);

        // When
        List<BoardGroup> result = boardGroupRepository.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("A Group");
        assertThat(result.get(1).getName()).isEqualTo("B Group");
    }

    @Test
    @DisplayName("Deve encontrar grupo por nome (case-insensitive)")
    void shouldFindGroupByNameCaseInsensitive() {
        // Arrange - inserir um grupo de teste
        BoardGroup testGroup = new BoardGroup();
        testGroup.setName("Grupo Teste");
        testGroup.setDescription("Descrição de teste");
        testGroup.setColor("#FF5733");
        testGroup.setIcon("📁");
        testGroup.setCreationDate(LocalDateTime.now());
        
        BoardGroup savedGroup = boardGroupRepository.save(testGroup);
        
        // Act & Assert - buscar com diferentes cases
        Optional<BoardGroup> result1 = boardGroupRepository.findByName("Grupo Teste");
        Optional<BoardGroup> result2 = boardGroupRepository.findByName("grupo teste");
        Optional<BoardGroup> result3 = boardGroupRepository.findByName("GRUPO TESTE");
        
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertEquals(savedGroup.getId(), result1.get().getId());
        assertEquals(savedGroup.getId(), result2.get().getId());
        assertEquals(savedGroup.getId(), result3.get().getId());
    }

    @Test
    @DisplayName("Deve retornar empty quando nome não existe")
    void shouldReturnEmptyWhenNameDoesNotExist() {
        // Act
        Optional<BoardGroup> result = boardGroupRepository.findByName("Nome Inexistente");
        
        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve encontrar grupo por nome excluindo ID específico")
    void shouldFindGroupByNameExcludingId() {
        // Arrange - inserir dois grupos
        BoardGroup group1 = new BoardGroup();
        group1.setName("Grupo Duplicado");
        group1.setDescription("Primeiro grupo");
        group1.setColor("#FF5733");
        group1.setIcon("📁");
        group1.setCreationDate(LocalDateTime.now());
        
        BoardGroup group2 = new BoardGroup();
        group2.setName("Grupo Duplicado");
        group2.setDescription("Segundo grupo");
        group2.setColor("#33FF57");
        group2.setIcon("🎯");
        group2.setCreationDate(LocalDateTime.now());
        
        BoardGroup savedGroup1 = boardGroupRepository.save(group1);
        BoardGroup savedGroup2 = boardGroupRepository.save(group2);
        
        // Act & Assert - buscar excluindo o primeiro grupo
        Optional<BoardGroup> result = boardGroupRepository.findByNameExcludingId("Grupo Duplicado", savedGroup1.getId());
        
        assertTrue(result.isPresent());
        assertEquals(savedGroup2.getId(), result.get().getId());
        assertEquals("Segundo grupo", result.get().getDescription());
    }

    @Test
    @DisplayName("Deve retornar empty quando nome não existe excluindo ID")
    void shouldReturnEmptyWhenNameDoesNotExistExcludingId() {
        // Act
        Optional<BoardGroup> result = boardGroupRepository.findByNameExcludingId("Nome Inexistente", 999L);
        
        // Assert
        assertTrue(result.isEmpty());
    }
} 