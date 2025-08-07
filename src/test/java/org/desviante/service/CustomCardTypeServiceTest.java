package org.desviante.service;

import org.desviante.config.DataConfig;
import org.desviante.config.TestDataSourceConfig;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.CardType;
import org.desviante.repository.CardTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o serviço de tipos de card customizados.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@SpringJUnitConfig(classes = CustomCardTypeServiceTest.TestConfig.class)
@Sql(scripts = "/test-schema.sql")
@Transactional
class CustomCardTypeServiceTest {

    @Configuration
    @Import({TestDataSourceConfig.class, CardTypeService.class, CardTypeRepository.class})
    static class TestConfig {
        // Configuração específica para este teste
    }

    @Autowired
    private CardTypeService customCardTypeService;

    @Autowired
    private CardTypeRepository customCardTypeRepository;

    @AfterEach
    void tearDown() {
        // Limpeza automática pelo @Transactional
    }

    @Test
    @DisplayName("Deve criar um novo tipo customizado com sucesso")
    void shouldCreateNewCustomType() {
        // ACT
        CardType newType = customCardTypeService.createCardType("Livro", "páginas");

        // ASSERT
        assertNotNull(newType.getId());
        assertEquals("Livro", newType.getName());
        assertEquals("páginas", newType.getUnitLabel());
        assertNotNull(newType.getCreationDate());
        assertNotNull(newType.getLastUpdateDate());
    }

    @Test
    @DisplayName("Deve listar todos os tipos customizados")
    void shouldListAllCustomTypes() {
        // ARRANGE
        customCardTypeService.createCardType("Livro", "páginas");
        customCardTypeService.createCardType("Vídeo", "minutos");
        customCardTypeService.createCardType("Curso", "aulas");

        // ACT
        List<CardType> allTypes = customCardTypeService.getAllCardTypes();

        // ASSERT
        assertEquals(7, allTypes.size()); // 4 tipos padrão + 3 tipos criados no teste
        assertTrue(allTypes.stream().anyMatch(type -> "Livro".equals(type.getName())));
        assertTrue(allTypes.stream().anyMatch(type -> "Vídeo".equals(type.getName())));
        assertTrue(allTypes.stream().anyMatch(type -> "Curso".equals(type.getName())));
    }

    @Test
    @DisplayName("Deve encontrar um tipo customizado pelo ID")
    void shouldFindCustomTypeById() {
        // ARRANGE
        CardType createdType = customCardTypeService.createCardType("Livro", "páginas");

        // ACT
        CardType foundType = customCardTypeService.getCardTypeById(createdType.getId());

        // ASSERT
        assertEquals(createdType.getId(), foundType.getId());
        assertEquals("Livro", foundType.getName());
        assertEquals("páginas", foundType.getUnitLabel());
    }

    @Test
    @DisplayName("Deve atualizar um tipo customizado existente")
    void shouldUpdateExistingCustomType() throws InterruptedException {
        // ARRANGE
        CardType createdType = customCardTypeService.createCardType("Livro", "páginas");

        // Pequeno delay para garantir que a nova data seja diferente
        Thread.sleep(10);

        // ACT
        CardType updatedType = customCardTypeService.updateCardType(
            createdType.getId(), "Livro Atualizado", "capítulos");

        // ASSERT
        assertEquals(createdType.getId(), updatedType.getId());
        assertEquals("Livro Atualizado", updatedType.getName());
        assertEquals("capítulos", updatedType.getUnitLabel());
        assertTrue(updatedType.getLastUpdateDate().isAfter(createdType.getLastUpdateDate()));
    }

    @Test
    @DisplayName("Deve remover um tipo customizado")
    void shouldDeleteCustomType() {
        // ARRANGE
        CardType createdType = customCardTypeService.createCardType("Livro", "páginas");

        // ACT
        boolean deleted = customCardTypeService.deleteCardType(createdType.getId());

        // ASSERT
        assertTrue(deleted);
        assertThrows(ResourceNotFoundException.class, 
            () -> customCardTypeService.getCardTypeById(createdType.getId()));
    }

    @Test
    @DisplayName("Deve verificar se existe um tipo customizado pelo nome")
    void shouldCheckIfCustomTypeExistsByName() {
        // ARRANGE
        customCardTypeService.createCardType("Livro", "páginas");

        // ACT & ASSERT
        assertTrue(customCardTypeService.existsByName("Livro"));
        assertFalse(customCardTypeService.existsByName("Tipo Inexistente"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar tipo com nome vazio")
    void shouldThrowExceptionWhenCreatingTypeWithEmptyName() {
        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> customCardTypeService.createCardType("", "páginas"));
        
        assertEquals("Nome do tipo de card não pode ser vazio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve usar 'unidade' como padrão quando label estiver vazio")
    void shouldUseDefaultUnitLabelWhenEmpty() {
        // ACT
        CardType newType = customCardTypeService.createCardType("Livro", "");

        // ASSERT
        assertEquals("Livro", newType.getName());
        assertEquals("unidade", newType.getUnitLabel());
    }

    @Test
    @DisplayName("Deve usar 'unidade' como padrão quando label for null")
    void shouldUseDefaultUnitLabelWhenNull() {
        // ACT
        CardType newType = customCardTypeService.createCardType("Livro", null);

        // ASSERT
        assertEquals("Livro", newType.getName());
        assertEquals("unidade", newType.getUnitLabel());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar tipo com nome duplicado")
    void shouldThrowExceptionWhenCreatingTypeWithDuplicateName() {
        // ARRANGE
        customCardTypeService.createCardType("Livro", "páginas");

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> customCardTypeService.createCardType("Livro", "capítulos"));
        
        assertEquals("Já existe um tipo de card com o nome 'Livro'", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar tipo inexistente")
    void shouldThrowExceptionWhenFindingNonExistentType() {
        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> customCardTypeService.getCardTypeById(999L));
        
        assertEquals("Tipo de card com ID 999 não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar tipo inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentType() {
        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> customCardTypeService.updateCardType(999L, "Novo Nome", "nova unidade"));
        
        assertEquals("Tipo de card com ID 999 não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar tipo com nome duplicado")
    void shouldThrowExceptionWhenUpdatingTypeWithDuplicateName() {
        // ARRANGE
        CardType type1 = customCardTypeService.createCardType("Livro", "páginas");
        customCardTypeService.createCardType("Vídeo", "minutos");

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> customCardTypeService.updateCardType(type1.getId(), "Vídeo", "páginas"));
        
        assertEquals("Já existe um tipo de card com o nome 'Vídeo'", exception.getMessage());
    }

    @Test
    @DisplayName("Deve normalizar espaços em branco no nome e label")
    void shouldNormalizeWhitespaceInNameAndLabel() {
        // ACT
        CardType newType = customCardTypeService.createCardType("  Livro  ", "  páginas  ");

        // ASSERT
        assertEquals("Livro", newType.getName());
        assertEquals("páginas", newType.getUnitLabel());
    }

    @Test
    @DisplayName("Deve permitir atualizar tipo mantendo o mesmo nome")
    void shouldAllowUpdatingTypeWithSameName() {
        // ARRANGE
        CardType createdType = customCardTypeService.createCardType("Livro", "páginas");

        // ACT
        CardType updatedType = customCardTypeService.updateCardType(
            createdType.getId(), "Livro", "capítulos");

        // ASSERT
        assertEquals("Livro", updatedType.getName());
        assertEquals("capítulos", updatedType.getUnitLabel());
    }

    @Test
    @DisplayName("Deve usar 'unidade' como padrão ao atualizar com label vazio")
    void shouldUseDefaultUnitLabelWhenUpdatingWithEmptyLabel() {
        // ARRANGE
        CardType createdType = customCardTypeService.createCardType("Livro", "páginas");

        // ACT
        CardType updatedType = customCardTypeService.updateCardType(
            createdType.getId(), "Livro", "");

        // ASSERT
        assertEquals("Livro", updatedType.getName());
        assertEquals("unidade", updatedType.getUnitLabel());
    }
} 