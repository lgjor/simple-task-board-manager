package org.desviante.config;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import com.google.api.services.tasks.TasksScopes;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.desviante.util.BrowserUtils;
import org.desviante.util.AutoAuthCallbackServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
@ConditionalOnProperty(name = "google.api.enabled", havingValue = "true", matchIfMissing = false)
@Log
public class GoogleApiConfig {

    public static final String APPLICATION_NAME = "Simple Task Board Manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(TasksScopes.TASKS);
    private static final String CREDENTIALS_FILE_PATH = "/auth/credentials.json";
    public static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + ".credentials" + File.separator + "simple-task-board-manager";

    /**
     * Construtor padrão da configuração da API do Google.
     * 
     * <p>Esta classe não requer inicialização especial.</p>
     */
    public GoogleApiConfig() {
        // Configuração automática via anotações Spring
    }

    /**
     * Cria uma credencial autorizada.
     *
     * @param httpTransport O transporte HTTP para as requisições.
     * @return Um objeto Credential autorizado.
     * @throws IOException Se o arquivo credentials.json não for encontrado ou lido.
     */
    public Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Carrega os segredos do cliente.
        InputStream in = GoogleApiConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            // Este erro não deveria acontecer se areCredentialsAvailable() for chamado antes.
            throw new IOException("Falha ao carregar o arquivo de credenciais: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Constrói o fluxo de autorização e o receptor de código local.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        
        // Primeiro tenta autorização silenciosa (credenciais existentes)
        try {
            Credential credential = flow.loadCredential("user");
            if (credential != null && credential.getRefreshToken() != null) {
                log.info("Credenciais existentes encontradas. Verificando validade...");
                // Tenta usar a credencial existente
                if (credential.refreshToken()) {
                    log.info("Credenciais válidas carregadas com sucesso.");
                    return credential;
                } else {
                    log.warning("Credenciais existentes expiradas. Iniciando novo fluxo de autorização.");
                }
            }
        } catch (Exception e) {
            log.warning("Erro ao carregar credenciais existentes: " + e.getMessage());
        }
        
        // Se não há credenciais válidas, inicia fluxo de autorização
        return authorizeWithBrowser(flow);
    }

    /**
     * Executa o fluxo de autorização com captura automática do código.
     * 
     * <p>Usa um servidor HTTP local personalizado para capturar automaticamente
     * o código de autorização do callback do Google, eliminando a necessidade
     * de intervenção manual do usuário.</p>
     *
     * @param flow O fluxo de autorização do Google.
     * @return Um objeto Credential autorizado.
     * @throws IOException Se o usuário cancelar ou se houver erro.
     */
    private Credential authorizeWithBrowser(GoogleAuthorizationCodeFlow flow) throws IOException {
        AutoAuthCallbackServer callbackServer = null;
        
        try {
            // Inicia o servidor de callback personalizado
            callbackServer = new AutoAuthCallbackServer();
            CompletableFuture<String> authCodeFuture = callbackServer.startAndWaitForAuthCode();
            
            // Aguarda o servidor estar pronto
            callbackServer.waitForServerReady();
            
            // Gera a URL de autorização
            String redirectUri = "http://localhost:8888/Callback";
            String authUrl = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();
            
            log.info("Abrindo navegador para autorização automática...");
            
            // Abre o navegador automaticamente
            boolean openedAutomatically = BrowserUtils.openUrlInBrowser(authUrl, 
                "Autorização Google Tasks", 
                "Abrindo navegador para autorização automática do Google Tasks...");
            
            if (!openedAutomatically) {
                log.warning("Navegador não foi aberto automaticamente. Aguardando autorização manual...");
            }
            
            // Aguarda o código de autorização
            log.info("Aguardando código de autorização do Google...");
            String authCode = authCodeFuture.get();
            
            log.info("Código de autorização recebido com sucesso!");
            
            // Cria e armazena a credencial
            return flow.createAndStoreCredential(
                flow.newTokenRequest(authCode).setRedirectUri(redirectUri).execute(), 
                "user"
            );
            
        } catch (Exception e) {
            log.severe("Erro durante autorização automática: " + e.getMessage());
            
            // Fallback para autorização manual se a automática falhar
            log.info("Tentando fallback para autorização manual...");
            return authorizeManually(flow);
            
        } finally {
            // Limpa o servidor de callback
            if (callbackServer != null) {
                callbackServer.stop();
            }
        }
    }

    /**
     * Executa o fluxo de autorização manual quando o automático falha.
     * Abre o navegador automaticamente e solicita o código de autorização.
     *
     * @param flow O fluxo de autorização do Google.
     * @return Um objeto Credential autorizado.
     * @throws IOException Se o usuário cancelar ou se houver erro.
     */
    private Credential authorizeManually(GoogleAuthorizationCodeFlow flow) throws IOException {
        String redirectUri = "http://localhost:8888/Callback";
        String url = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();

        // Tenta abrir o navegador automaticamente
        boolean openedAutomatically = BrowserUtils.openUrlInBrowser(url, 
            "Autenticação Google Tasks", 
            "Abrindo navegador para autorização do Google Tasks...");

        if (!openedAutomatically) {
            log.info("Navegador não foi aberto automaticamente. URL exibida manualmente.");
        }

        // Solicita o código de autorização do usuário
        Optional<String> code = showCodeInputDialog();

        if (code.isPresent() && !code.get().isBlank()) {
            return flow.createAndStoreCredential(flow.newTokenRequest(code.get()).setRedirectUri(redirectUri).execute(), "user");
        } else {
            throw new IOException("Autorização manual cancelada pelo usuário.");
        }
    }

    /**
     * Verifica se o arquivo de credenciais está disponível.
     *
     * @return true se as credenciais estão disponíveis, false caso contrário
     */
    private boolean areCredentialsAvailable() {
        try (InputStream in = GoogleApiConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH)) {
            return in != null;
        } catch (Exception e) {
            log.warning("Erro ao verificar arquivo de credenciais: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cria o serviço autenticado do Google Tasks.
     *
     * <p>Configura o serviço final do Google Tasks usando credenciais salvas.
     * Se não existirem credenciais, retorna null e exibe instruções para
     * configuração manual.</p>
     *
     * <p>Características:</p>
     * <ul>
     *   <li>Carregamento não-interativo de credenciais existentes</li>
     *   <li>Refresh automático de tokens expirados</li>
     *   <li>Fallback gracioso quando autenticação falha</li>
     * </ul>
     *
     * @return Tasks service autenticado ou null se não disponível
     * @throws GeneralSecurityException se houver erro de segurança
     * @throws IOException se houver erro de I/O
     * @see com.google.api.services.tasks.Tasks
     * @see com.google.api.client.auth.oauth2.Credential
     */
    @Bean
    public Tasks tasksService(NetHttpTransport httpTransport) throws GeneralSecurityException, IOException {
        if (!areCredentialsAvailable()) {
            log.warning("Arquivo de credenciais do Google API ('/auth/credentials.json') não encontrado. A integração com Google Tasks será desativada.");
            showCredentialsInstructions();
            return null; // Não cria o bean, evitando a injeção.
        }

        try {
            // Tenta autorizar silenciosamente primeiro, sem interação com o usuário.
            // Isso é crucial para não bloquear a inicialização do Spring com diálogos de UI.
            Credential credential = authorizeSilently(httpTransport);

            if (credential == null) {
                log.info("Nenhuma credencial do Google encontrada. A autenticação será solicitada no primeiro uso.");
                return null; // Permite que a aplicação inicie. A autenticação será feita sob demanda.
            }

            return new Tasks.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException e) {
            log.severe("FALHA CRÍTICA NA AUTENTICAÇÃO COM GOOGLE API: " + e.getMessage());
            log.severe("A integração com Google Tasks será desativada. Causa provável: o usuário negou acesso, a porta 8888 está bloqueada/em uso, ou há um problema de rede.");
            log.throwing(GoogleApiConfig.class.getName(), "tasksService", e);
            showAuthenticationInstructions();
            return null; // Retorna null se a autorização falhar (ex: usuário nega acesso).
        }
    }

    /**
     * Tenta autorizar silenciosamente usando credenciais armazenadas.
     * Não inicia fluxo interativo.
     *
     * @param httpTransport O transporte HTTP.
     * @return Credencial autorizada ou null se não houver credenciais armazenadas.
     * @throws IOException em caso de erro de I/O.
     */
    private Credential authorizeSilently(final NetHttpTransport httpTransport) throws IOException {
        InputStream in = GoogleApiConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) return null;
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // Tenta carregar a credencial sem iniciar o fluxo interativo.
        return flow.loadCredential("user");
    }

    /**
     * Cria um bean para o transporte HTTP da API do Google.
     *
     * @return uma instância de NetHttpTransport
     * @throws GeneralSecurityException se houver erro de segurança
     * @throws IOException se houver erro de I/O
     */
    @Bean
    public NetHttpTransport googleNetHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    /**
     * Exibe instruções para configurar o arquivo de credenciais.
     */
    private void showCredentialsInstructions() {
        log.warning("=== INSTRUÇÕES PARA CONFIGURAR GOOGLE TASKS ===");
        log.warning("1. Acesse: https://console.cloud.google.com/");
        log.warning("2. Crie um projeto e ative a API do Google Tasks");
        log.warning("3. Crie credenciais OAuth 2.0 para 'Aplicativo de desktop'");
        log.warning("4. Baixe o arquivo JSON das credenciais");
        log.warning("5. Renomeie para 'credentials.json'");
        log.warning("6. Coloque em: src/main/resources/auth/credentials.json");
        log.warning("7. Reinicie a aplicação");
        log.warning("===============================================");
    }

    /**
     * Exibe instruções para fazer a autenticação inicial.
     */
    private void showAuthenticationInstructions() {
        log.warning("=== AUTENTICAÇÃO COM GOOGLE NECESSÁRIA ===");
        log.warning("A aplicação tentará abrir seu navegador para que você possa autorizar o acesso.");
        log.warning("Se o navegador não abrir, verifique se o arquivo 'credentials.json' está configurado corretamente.");
        log.warning("As credenciais de autorização serão salvas em: " + TOKENS_DIRECTORY_PATH);
        log.warning("Se o problema persistir, reinicie a aplicação.");
        log.warning("==========================================");
    }

    /**
     * Exibe um diálogo para o usuário inserir o código de autorização.
     *
     * @return Um Optional contendo o código inserido pelo usuário.
     */
    private Optional<String> showCodeInputDialog() {
        // Este método precisa ser síncrono para aguardar a entrada do usuário.
        // Usamos um truque com Platform.runLater e um objeto para guardar o resultado.
        final String[] result = new String[1];
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Código de Autorização");
            dialog.setHeaderText("Cole aqui o código de autorização que você recebeu do Google:");
            dialog.setContentText("Código:");
            dialog.showAndWait().ifPresent(code -> result[0] = code);
            latch.countDown();
        });

        try {
            latch.await(); // Espera o diálogo do JavaFX fechar
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Optional.ofNullable(result[0]);
    }
}