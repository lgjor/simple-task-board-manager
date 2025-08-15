package org.desviante.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o AppMetadataConfig.
 * 
 * <p>Foca na funcionalidade de gerenciamento de metadados, incluindo
 * o novo campo defaultBoardGroupId.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class AppMetadataConfigTest {

    @Mock
    private FileWatcherService fileWatcherService;
    
    private AppMetadataConfig appMetadataConfig;
    private Path tempConfigDir;
    
    @BeforeEach
    void setUp() throws IOException {
        // Criar diretório temporário para testes
        tempConfigDir = Files.createTempDirectory("test-config");
        
        // Criar instância do AppMetadataConfig
        appMetadataConfig = new AppMetadataConfig();
        
        // Injetar dependências mock
        ReflectionTestUtils.setField(appMetadataConfig, "fileWatcherService", fileWatcherService);
        ReflectionTestUtils.setField(appMetadataConfig, "metadataDirectoryPath", tempConfigDir.toString());
    }
    
    @Test
    @DisplayName("Deve criar metadados padrão com defaultBoardGroupId como null")
    void shouldCreateDefaultMetadataWithNullDefaultBoardGroupId() {
        // Act
        AppMetadata metadata = new AppMetadata();
        metadata.setMetadataVersion("1.0");
        metadata.setDefaultCardTypeId(null);
        metadata.setDefaultProgressType(null);
        metadata.setDefaultBoardGroupId(null);
        metadata.setInstallationDirectory("/test/install");
        metadata.setUserDataDirectory("/test/user");
        
        // Assert
        assertNotNull(metadata);
        assertEquals("1.0", metadata.getMetadataVersion());
        assertNull(metadata.getDefaultCardTypeId());
        assertNull(metadata.getDefaultProgressType());
        assertNull(metadata.getDefaultBoardGroupId()); // Novo campo deve ser null por padrão
        assertNotNull(metadata.getInstallationDirectory());
        assertNotNull(metadata.getUserDataDirectory());
    }
    
    @Test
    @DisplayName("Deve retornar Optional.empty() quando defaultBoardGroupId não está configurado")
    void shouldReturnEmptyOptionalWhenDefaultBoardGroupIdNotConfigured() {
        // Arrange
        AppMetadata metadata = new AppMetadata();
        metadata.setDefaultBoardGroupId(null);
        
        ReflectionTestUtils.setField(appMetadataConfig, "currentMetadata", metadata);
        
        // Act
        Optional<Long> result = appMetadataConfig.getDefaultBoardGroupId();
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Deve retornar Optional com valor quando defaultBoardGroupId está configurado")
    void shouldReturnOptionalWithValueWhenDefaultBoardGroupIdConfigured() {
        // Arrange
        AppMetadata metadata = new AppMetadata();
        metadata.setDefaultBoardGroupId(123L);
        
        ReflectionTestUtils.setField(appMetadataConfig, "currentMetadata", metadata);
        
        // Act
        Optional<Long> result = appMetadataConfig.getDefaultBoardGroupId();
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(123L, result.get());
    }
    
    @Test
    @DisplayName("Deve atualizar defaultBoardGroupId corretamente")
    void shouldUpdateDefaultBoardGroupIdCorrectly() throws IOException {
        // Arrange
        AppMetadata metadata = new AppMetadata();
        metadata.setDefaultBoardGroupId(null);
        
        ReflectionTestUtils.setField(appMetadataConfig, "currentMetadata", metadata);
        ReflectionTestUtils.setField(appMetadataConfig, "metadataFilePath", tempConfigDir.resolve("test-metadata.json"));
        
        // Act
        appMetadataConfig.updateMetadata(m -> m.setDefaultBoardGroupId(456L));
        
        // Assert
        assertEquals(456L, metadata.getDefaultBoardGroupId());
    }
    
    @Test
    @DisplayName("Deve definir defaultBoardGroupId como null quando solicitado")
    void shouldSetDefaultBoardGroupIdAsNullWhenRequested() throws IOException {
        // Arrange
        AppMetadata metadata = new AppMetadata();
        metadata.setDefaultBoardGroupId(789L);
        
        ReflectionTestUtils.setField(appMetadataConfig, "currentMetadata", metadata);
        ReflectionTestUtils.setField(appMetadataConfig, "metadataFilePath", tempConfigDir.resolve("test-metadata.json"));
        
        // Act
        appMetadataConfig.updateMetadata(m -> m.setDefaultBoardGroupId(null));
        
        // Assert
        assertNull(metadata.getDefaultBoardGroupId());
    }
    
    @Test
    @DisplayName("Deve manter outros campos inalterados ao atualizar defaultBoardGroupId")
    void shouldKeepOtherFieldsUnchangedWhenUpdatingDefaultBoardGroupId() throws IOException {
        // Arrange
        AppMetadata metadata = new AppMetadata();
        metadata.setDefaultCardTypeId(111L);
        metadata.setDefaultProgressType(org.desviante.model.enums.ProgressType.CHECKLIST);
        metadata.setDefaultBoardGroupId(222L);
        metadata.setInstallationDirectory("/test/install");
        
        ReflectionTestUtils.setField(appMetadataConfig, "currentMetadata", metadata);
        ReflectionTestUtils.setField(appMetadataConfig, "metadataFilePath", tempConfigDir.resolve("test-metadata.json"));
        
        // Act
        appMetadataConfig.updateMetadata(m -> m.setDefaultBoardGroupId(333L));
        
        // Assert
        assertEquals(111L, metadata.getDefaultCardTypeId());
        assertEquals(org.desviante.model.enums.ProgressType.CHECKLIST, metadata.getDefaultProgressType());
        assertEquals(333L, metadata.getDefaultBoardGroupId()); // Deve ter sido alterado
        assertEquals("/test/install", metadata.getInstallationDirectory());
    }
    
    @Test
    @DisplayName("Deve lidar com valores extremos para defaultBoardGroupId")
    void shouldHandleExtremeValuesForDefaultBoardGroupId() throws IOException {
        // Arrange
        AppMetadata metadata = new AppMetadata();
        ReflectionTestUtils.setField(appMetadataConfig, "currentMetadata", metadata);
        ReflectionTestUtils.setField(appMetadataConfig, "metadataFilePath", tempConfigDir.resolve("test-metadata.json"));
        
        // Act & Assert - Valor máximo
        appMetadataConfig.updateMetadata(m -> m.setDefaultBoardGroupId(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, metadata.getDefaultBoardGroupId());
        
        // Act & Assert - Valor mínimo
        appMetadataConfig.updateMetadata(m -> m.setDefaultBoardGroupId(Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, metadata.getDefaultBoardGroupId());
        
        // Act & Assert - Zero
        appMetadataConfig.updateMetadata(m -> m.setDefaultBoardGroupId(0L));
        assertEquals(0L, metadata.getDefaultBoardGroupId());
    }
}
