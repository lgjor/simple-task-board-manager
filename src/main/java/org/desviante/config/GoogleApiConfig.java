package org.desviante.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader; // CORRECTION: Add the missing import
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Configuração da API do Google para integração com Google Tasks.
 * 
 * <p>Esta classe configura a integração com a API do Google Tasks através de
 * OAuth2, incluindo fluxo de autorização e serviço autenticado.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see com.google.api.services.tasks.Tasks
 * @see com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
 */
@Configuration
@Profile("!test")
@Log
public class GoogleApiConfig {

    // --- Constants (no changes) ---
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(TasksScopes.TASKS);
    private static final String CREDENTIALS_FILE_PATH = "/auth/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + ".credentials" + File.separator + "simple-task-board-manager";
    private static final String USER_ID = "user";

    /**
     * Construtor padrão da configuração da API do Google.
     * 
     * <p>Esta classe não requer inicialização especial.</p>
     */
    public GoogleApiConfig() {
        // Configuração automática via anotações Spring
    }

    /**
     * Cria o bean principal do fluxo de autorização Google OAuth2.
     *
     * <p>Configura o fluxo de autorização não-interativo para carregar credenciais
     * salvas. Este bean é condicional e só é criado quando a integração Google
     * está habilitada via propriedade.</p>
     *
     * <p>O fluxo utiliza:</p>
     * <ul>
     *   <li>Arquivo de credenciais em /auth/credentials.json</li>
     *   <li>Armazenamento de tokens em ~/.credentials/simple-task-board-manager</li>
     *   <li>Escopo TASKS para acesso à API do Google Tasks</li>
     * </ul>
     *
     * @return GoogleAuthorizationCodeFlow configurado ou null se credenciais não disponíveis
     * @throws IOException se houver erro ao ler credenciais
     * @throws GeneralSecurityException se houver erro de segurança
     * @see com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
     * @see com.google.api.client.util.store.FileDataStoreFactory
     */
    @Bean
    @ConditionalOnProperty(name = "google.api.enabled", havingValue = "true", matchIfMissing = false)
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() throws IOException, GeneralSecurityException {
        final InputStream in = GoogleApiConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            log.warning("Google API credentials not found. Google Tasks integration will be disabled.");
            return null;
        }
        
        try {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            return new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
        } catch (Exception e) {
            log.warning("Failed to initialize Google API flow: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cria o serviço autenticado do Google Tasks.
     *
     * <p>Configura o serviço final do Google Tasks usando credenciais salvas.
     * Este bean é não-interativo e seguro para processamento AOT. Tenta carregar
     * credenciais existentes e retorna null se não estiverem disponíveis.</p>
     *
     * <p>Características de segurança:</p>
     * <ul>
     *   <li>Carregamento não-interativo de credenciais</li>
     *   <li>Refresh automático de tokens expirados</li>
     *   <li>Fallback gracioso quando autenticação falha</li>
     * </ul>
     *
     * @param flow fluxo de autorização Google configurado
     * @return Tasks service autenticado ou null se não disponível
     * @throws GeneralSecurityException se houver erro de segurança
     * @throws IOException se houver erro de I/O
     * @see com.google.api.services.tasks.Tasks
     * @see com.google.api.client.auth.oauth2.Credential
     */
    @Bean
    @ConditionalOnProperty(name = "google.api.enabled", havingValue = "true", matchIfMissing = false)
    public Tasks tasksService(
            GoogleAuthorizationCodeFlow flow
    ) throws GeneralSecurityException, IOException {
        // If no flow is available, return null
        if (flow == null) {
            log.warning("Google API flow not available. Google Tasks integration will be disabled.");
            return null;
        }

        try {
            // This is the critical change. We only LOAD the credential.
            Credential credential = flow.loadCredential(USER_ID);

            // If no credential exists, we return null instead of throwing an exception
            if (credential == null) {
                log.warning("User credentials not found at " + TOKENS_DIRECTORY_PATH + ". Google Tasks integration will be disabled.");
                return null;
            }

            // If the token is expired, the Google client library will attempt to refresh it automatically
            // using the refresh token, which is a non-interactive network call. This is safe.

            log.info("Successfully loaded existing user credentials.");
            return new Tasks.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                    // Use a hardcoded application name
                    .setApplicationName("SimpleTaskBoardManager")
                    .build();
        } catch (Exception e) {
            log.warning("Failed to create Google Tasks service: " + e.getMessage());
            return null;
        }
    }
}