package org.desviante.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
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

@Configuration
@Profile("!test")
@Log
public class GoogleApiConfig {

    // --- Constants (no changes) ---
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(TasksScopes.TASKS);
    private static final String CREDENTIALS_FILE_PATH = "/auth/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + ".credentials" + File.separator + "simple-task-board-manager";
    private static final String USER_ID = "user";
    private static final int LOCAL_SERVER_PORT = 8889;

    /**
     * Creates the core GoogleAuthorizationCodeFlow bean. This is a non-interactive factory
     * for creating and loading credentials.
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
     * Creates the final, authenticated Tasks service bean.
     * This bean is now NON-INTERACTIVE and safe for AOT processing. It will attempt
     * to load existing credentials. If they don't exist, it will return null instead of throwing an exception.
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