package org.desviante.service;

import org.desviante.config.AppMetadataConfig;
import org.desviante.model.CardType;
import org.desviante.model.enums.ProgressType;
import org.desviante.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o DefaultConfigurationService.
 * 
 * <p>Testa as operações de configuração padrão da aplicação, incluindo
 * validação de tipos de progresso padrão e proteção contra alterações
 * quando tipos estão em uso.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see DefaultConfigurationService
 * @see ProgressType
 * @see CardType
 */
@ExtendWith(MockitoExtension.class)
class DefaultConfigurationServiceTest {

    @Mock
    private AppMetadataConfig metadataConfig;

    @Mock
    private CardTypeService cardTypeService;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private DefaultConfigurationService defaultConfigurationService;

    private CardType testCardType;

    @BeforeEach
    void setUp() {
        testCardType = CardType.builder()
                .id(1L)
                .name("Tarefa")
                .unitLabel("etapas")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve obter tipo de progresso padrão dos metadados quando configurado")
    void shouldGetDefaultProgressTypeFromMetadataWhenConfigured() {
        // Arrange
        when(metadataConfig.getDefaultProgressType()).thenReturn(Optional.of(ProgressType.CHECKLIST));

        // Act
        ProgressType result = defaultConfigurationService.getDefaultProgressType();

        // Assert
        assertEquals(ProgressType.CHECKLIST, result);
        verify(metadataConfig).getDefaultProgressType();
    }

    @Test
    @DisplayName("Deve usar NONE como tipo de progresso padrão quando não configurado")
    void shouldUseNoneAsDefaultProgressTypeWhenNotConfigured() {
        // Arrange
        when(metadataConfig.getDefaultProgressType()).thenReturn(Optional.empty());

        // Act
        ProgressType result = defaultConfigurationService.getDefaultProgressType();

        // Assert
        assertEquals(ProgressType.NONE, result);
        verify(metadataConfig).getDefaultProgressType();
    }

    @Test
    @DisplayName("Deve permitir alteração de tipo de progresso padrão quando não está em uso")
    void shouldAllowChangingDefaultProgressTypeWhenNotInUse() throws Exception {
        // Arrange
        when(metadataConfig.getDefaultProgressType()).thenReturn(Optional.of(ProgressType.PERCENTAGE));
        when(cardRepository.existsByProgressType(ProgressType.PERCENTAGE)).thenReturn(false);
        doNothing().when(metadataConfig).updateMetadata(any());

        // Act
        defaultConfigurationService.setDefaultProgressType(ProgressType.CHECKLIST);

        // Assert
        verify(metadataConfig).getDefaultProgressType();
        verify(cardRepository).existsByProgressType(ProgressType.PERCENTAGE);
        verify(metadataConfig).updateMetadata(any());
    }

    @Test
    @DisplayName("Deve impedir alteração de tipo de progresso padrão quando está em uso")
    void shouldPreventChangingDefaultProgressTypeWhenInUse() throws Exception {
        // Arrange
        when(metadataConfig.getDefaultProgressType()).thenReturn(Optional.of(ProgressType.PERCENTAGE));
        when(cardRepository.existsByProgressType(ProgressType.PERCENTAGE)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            defaultConfigurationService.setDefaultProgressType(ProgressType.CHECKLIST);
        });

        String expectedMessage = "Não é possível alterar o tipo de progresso padrão 'Percentual' pois ele está sendo usado por cards no sistema. Migre os cards para outro tipo de progresso antes de alterar a configuração padrão.";
        assertEquals(expectedMessage, exception.getMessage());

        // Verify
        verify(metadataConfig).getDefaultProgressType();
        verify(cardRepository).existsByProgressType(ProgressType.PERCENTAGE);
        verify(metadataConfig, never()).updateMetadata(any());
    }

    @Test
    @DisplayName("Deve permitir alteração quando não há tipo padrão configurado")
    void shouldAllowChangingWhenNoDefaultProgressTypeConfigured() throws Exception {
        // Arrange
        when(metadataConfig.getDefaultProgressType()).thenReturn(Optional.empty());
        doNothing().when(metadataConfig).updateMetadata(any());

        // Act
        defaultConfigurationService.setDefaultProgressType(ProgressType.CHECKLIST);

        // Assert
        verify(metadataConfig).getDefaultProgressType();
        verify(cardRepository, never()).existsByProgressType(any());
        verify(metadataConfig).updateMetadata(any());
    }

    @Test
    @DisplayName("Deve permitir alteração para o mesmo tipo de progresso")
    void shouldAllowChangingToSameProgressType() throws Exception {
        // Arrange
        when(metadataConfig.getDefaultProgressType()).thenReturn(Optional.of(ProgressType.CHECKLIST));
        doNothing().when(metadataConfig).updateMetadata(any());

        // Act
        defaultConfigurationService.setDefaultProgressType(ProgressType.CHECKLIST);

        // Assert
        verify(metadataConfig).getDefaultProgressType();
        verify(cardRepository, never()).existsByProgressType(any());
        verify(metadataConfig).updateMetadata(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de progresso é null")
    void shouldThrowExceptionWhenProgressTypeIsNull() throws Exception {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            defaultConfigurationService.setDefaultProgressType(null);
        });

        assertEquals("Tipo de progresso não pode ser null", exception.getMessage());

        // Verify
        verify(metadataConfig, never()).getDefaultProgressType();
        verify(cardRepository, never()).existsByProgressType(any());
        verify(metadataConfig, never()).updateMetadata(any());
    }

    @Test
    @DisplayName("Deve validar configurações padrão com sucesso")
    void shouldValidateDefaultConfigurationsSuccessfully() {
        // Arrange
        when(metadataConfig.getDefaultProgressType()).thenReturn(Optional.of(ProgressType.CHECKLIST));
        when(cardTypeService.getAllCardTypes()).thenReturn(Arrays.asList(testCardType));

        // Act
        boolean result = defaultConfigurationService.validateDefaultConfigurations();

        // Assert
        assertTrue(result);
        verify(metadataConfig).getDefaultProgressType();
        verify(cardTypeService).getAllCardTypes();
    }

    @Test
    @DisplayName("Deve fornecer informações das configurações padrão")
    void shouldProvideDefaultConfigurationsInfo() {
        // Arrange
        when(metadataConfig.getDefaultProgressType()).thenReturn(Optional.of(ProgressType.CHECKLIST));
        when(metadataConfig.getMetadataFilePath()).thenReturn(Paths.get("/path/to/metadata"));
        when(cardTypeService.getAllCardTypes()).thenReturn(Arrays.asList(testCardType));

        // Act
        String info = defaultConfigurationService.getDefaultConfigurationsInfo();

        // Assert
        assertNotNull(info);
        assertTrue(info.contains("Tarefa"));
        assertTrue(info.contains("CHECKLIST"));
        assertTrue(info.contains("metadata")); // Simplificado para evitar problemas de encoding
    }
}
