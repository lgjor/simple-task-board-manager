package org.desviante.integration.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import org.desviante.persistence.entity.TaskEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class GoogleTaskIntegration {
    private static final String APPLICATION_NAME = "Simple Task Board Manager";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(TasksScopes.TASKS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final Integer LOCAL_PORT = 8889;

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleTaskIntegration.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(LOCAL_PORT).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static Tasks getGoogleTasksService() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Método que estava sendo chamado mas não existia
    public static void authenticateUser() throws Exception {
        // Apenas testa a autenticação criando o serviço
        getGoogleTasksService();
        System.out.println("Authentication successful!");
    }

    public static Task createTaskFromCard(String listTitle, TaskEntity taskEntity) throws Exception {
        Tasks service = getGoogleTasksService();

        // Buscar a lista correta pelo título
        List<TaskList> taskLists = service.tasklists().list().execute().getItems();
        if (taskLists == null || taskLists.isEmpty()) {
            throw new RuntimeException("No task lists found");
        }

        TaskList foundList = taskLists.stream()
                .filter(list -> list.getTitle().equalsIgnoreCase(listTitle))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        System.out.println("Lista não encontrada. Criando lista: " + listTitle);
                        TaskList newList = new TaskList();
                        newList.setTitle(listTitle);
                        try {
                            return service.tasklists().insert(newList).execute();
                        } catch (IOException e) {
                            throw new RuntimeException("Falha ao criar nova lista de tarefas: " + e.getMessage(), e);
                        }
                    } catch (RuntimeException re) {
                        throw re;
                    }
                });

        // Criar tarefa do Google Tasks
        Task googleTask = new Task();
        googleTask.setTitle(taskEntity.getTitle());
        googleTask.setNotes(taskEntity.getNotes());

        OffsetDateTime due = taskEntity.getDue();
        if (due != null) {
            String dueString = due.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            googleTask.setDue(dueString);
        }

        Task createdTask = service.tasks().insert(foundList.getId(), googleTask).execute();

        System.out.println("Task created successfully: " + googleTask.getTitle() + ", Google Task ID: " + createdTask.getId());

        return createdTask;
    }
}