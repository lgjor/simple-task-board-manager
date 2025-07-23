package org.desviante.config;

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import lombok.extern.slf4j.Slf4j;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
@Profile("!test")
public class GoogleApiConfig {

    // --- Constantes de Configuração ---
    private static final String APPLICATION_NAME = "Simple Task Board Manager";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(TasksScopes.TASKS);
    private static final String CREDENTIALS_FILE_PATH = "/auth/credentials.json";

    // Diretório de tokens robusto, no home do usuário para não poluir o projeto.
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + ".credentials" + File.separator + "simple-task-board-manager";

    // Constantes para valores que antes eram "mágicos"
    private static final int LOCAL_SERVER_PORT = 8889;
    private static final String USER_ID = "user";


    /**
     * Cria um bean do serviço Tasks, autenticado e pronto para uso.
     * Este método lida com o fluxo de autorização OAuth 2.0.
     */
    @Bean
    public Tasks tasksService() throws GeneralSecurityException, IOException {
        // 1. Carrega os segredos do cliente do arquivo credentials.json
        final InputStream in = GoogleApiConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // 2. Constrói o fluxo de autorização
        // A FileDataStoreFactory armazena o token de acesso do usuário para que ele não precise
        // autorizar a cada vez. O diretório agora é seguro e fora do projeto.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // 3. Autoriza o usuário usando um servidor local com porta definida
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(LOCAL_SERVER_PORT).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER_ID);

        // 4. Retorna o serviço Tasks construído com as credenciais.
        return new Tasks.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}