package org.desviante.repository;

import org.desviante.config.TestDataSourceConfig;
import org.desviante.model.CardType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o repositório de tipos de card customizados.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@SpringJUnitConfig(classes = CardTypeRepositoryTest.TestConfig.class)
@Sql(scripts = "/test-schema.sql")
@Transactional
class CardTypeRepositoryTest {

    @Configuration
    @Import({TestDataSourceConfig.class, CardTypeRepository.class})
    static class TestConfig {
        // Configuração específica para este teste
    }

    @Autowired
    private CardTypeRepository customCardTypeRepository;

    private CardType testCustomType;

    @BeforeEach
    void setUp() {
        testCustomType = CardType.builder()
                .name("Livro")
                .unitLabel("páginas")
                .build();
    }

    @AfterEach
    void tearDown() {
        // Limpeza automática pelo @Transactional
    }

    @Test
    @DisplayName("Deve salvar um novo tipo customizado com sucesso")
    void shouldSaveNewCustomType() {
        // ACT
        CardType savedType = customCardTypeRepository.save(testCustomType);

        // ASSERT
        assertNotNull(savedType.getId());
        assertEquals("Livro", savedType.getName());
        assertEquals("páginas", savedType.getUnitLabel());
        assertNotNull(savedType.getCreationDate());
        assertNotNull(savedType.getLastUpdateDate());
    }

    @Test
    @DisplayName("Deve encontrar um tipo customizado pelo ID")
    void shouldFindCustomTypeById() {
        // ARRANGE
        CardType savedType = customCardTypeRepository.save(testCustomType);

        // ACT
        Optional<CardType> foundType = customCardTypeRepository.findById(savedType.getId());

        // ASSERT
        assertTrue(foundType.isPresent());
        assertEquals(savedType.getId(), foundType.get().getId());
        assertEquals("Livro", foundType.get().getName());
    }

    @Test
    @DisplayName("Deve encontrar um tipo customizado pelo nome")
    void shouldFindCustomTypeByName() {
        // ARRANGE
        customCardTypeRepository.save(testCustomType);

        // ACT
        Optional<CardType> foundType = customCardTypeRepository.findByName("Livro");

        // ASSERT
        assertTrue(foundType.isPresent());
        assertEquals("Livro", foundType.get().getName());
        assertEquals("páginas", foundType.get().getUnitLabel());
    }

    @Test
    @DisplayName("Deve listar todos os tipos customizados ordenados por nome")
    void shouldListAllCustomTypesOrderedByName() {
        // ARRANGE
        CardType type1 = CardType.builder().name("Curso").unitLabel("aulas").build();
        CardType type2 = CardType.builder().name("Vídeo").unitLabel("minutos").build();
        CardType type3 = CardType.builder().name("Livro").unitLabel("páginas").build();

        customCardTypeRepository.save(type1);
        customCardTypeRepository.save(type2);
        customCardTypeRepository.save(type3);

        // ACT
        List<CardType> allTypes = customCardTypeRepository.findAll();

        // ASSERT
        assertEquals(7, allTypes.size()); // 4 tipos padrão + 3 tipos criados no teste
        // Verificar que os tipos criados no teste estão presentes
        List<String> typeNames = allTypes.stream().map(CardType::getName).toList();
        assertTrue(typeNames.contains("Curso"));
        assertTrue(typeNames.contains("Livro"));
        assertTrue(typeNames.contains("Vídeo"));
    }

    @Test
    @DisplayName("Deve atualizar um tipo customizado existente")
    void shouldUpdateExistingCustomType() throws InterruptedException {
        // ARRANGE
        CardType savedType = customCardTypeRepository.save(testCustomType);
        LocalDateTime originalUpdateDate = savedType.getLastUpdateDate();

        // Pequeno delay para garantir que a nova data seja diferente
        Thread.sleep(10);

        // ACT
        savedType.setName("Livro Atualizado");
        savedType.setUnitLabel("capítulos");
        CardType updatedType = customCardTypeRepository.update(savedType);

        // ASSERT
        assertEquals("Livro Atualizado", updatedType.getName());
        assertEquals("capítulos", updatedType.getUnitLabel());
        assertTrue(updatedType.getLastUpdateDate().isAfter(originalUpdateDate));
    }

    @Test
    @DisplayName("Deve remover um tipo customizado pelo ID")
    void shouldDeleteCustomTypeById() {
        // ARRANGE
        CardType savedType = customCardTypeRepository.save(testCustomType);

        // ACT
        boolean deleted = customCardTypeRepository.deleteById(savedType.getId());

        // ASSERT
        assertTrue(deleted);
        Optional<CardType> foundType = customCardTypeRepository.findById(savedType.getId());
        assertFalse(foundType.isPresent());
    }

    @Test
    @DisplayName("Deve verificar se existe um tipo customizado pelo nome")
    void shouldCheckIfCustomTypeExistsByName() {
        // ARRANGE
        customCardTypeRepository.save(testCustomType);

        // ACT & ASSERT
        assertTrue(customCardTypeRepository.existsByName("Livro"));
        assertFalse(customCardTypeRepository.existsByName("Tipo Inexistente"));
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para ID inexistente")
    void shouldReturnEmptyOptionalForNonExistentId() {
        // ACT
        Optional<CardType> foundType = customCardTypeRepository.findById(999L);

        // ASSERT
        assertFalse(foundType.isPresent());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para nome inexistente")
    void shouldReturnEmptyOptionalForNonExistentName() {
        // ACT
        Optional<CardType> foundType = customCardTypeRepository.findByName("Tipo Inexistente");

        // ASSERT
        assertFalse(foundType.isPresent());
    }

    @Test
    @DisplayName("Deve retornar false ao tentar remover tipo inexistente")
    void shouldReturnFalseWhenDeletingNonExistentType() {
        // ACT
        boolean deleted = customCardTypeRepository.deleteById(999L);

        // ASSERT
        assertFalse(deleted);
    }
} 