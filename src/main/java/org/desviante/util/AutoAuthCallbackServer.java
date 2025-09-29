package org.desviante.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Servidor HTTP local para capturar automaticamente o código de autorização OAuth2.
 * 
 * <p>Este servidor cria um endpoint local que recebe o callback do Google após
 * a autorização do usuário. O código de autorização é extraído automaticamente
 * da URL de callback e disponibilizado para a aplicação sem intervenção manual.</p>
 * 
 * <p>Características:</p>
 * <ul>
 *   <li>Servidor HTTP local na porta 8888</li>
 *   <li>Captura automática do código de autorização</li>
 *   <li>Timeout configurável para evitar travamentos</li>
 *   <li>Página de sucesso amigável para o usuário</li>
 *   <li>Limpeza automática de recursos</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see HttpServer
 * @see CompletableFuture
 */
@Slf4j
public class AutoAuthCallbackServer {

    private static final int DEFAULT_PORT = 8888;
    private static final int DEFAULT_TIMEOUT_SECONDS = 300; // 5 minutos
    private static final String SUCCESS_HTML = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Autorização Concluída</title>
            <meta charset="UTF-8">
            <style>
                body { 
                    font-family: Arial, sans-serif; 
                    text-align: center; 
                    padding: 50px; 
                    background-color: #f5f5f5;
                }
                .container { 
                    background: white; 
                    padding: 30px; 
                    border-radius: 10px; 
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    max-width: 500px;
                    margin: 0 auto;
                }
                .success { 
                    color: #4CAF50; 
                    font-size: 24px; 
                    margin-bottom: 20px;
                }
                .message { 
                    color: #666; 
                    font-size: 16px;
                    margin-bottom: 20px;
                }
                .close-btn {
                    background-color: #4CAF50;
                    color: white;
                    padding: 10px 20px;
                    border: none;
                    border-radius: 5px;
                    cursor: pointer;
                    font-size: 16px;
                }
                .close-btn:hover {
                    background-color: #45a049;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="success">✅ Autorização Concluída!</div>
                <div class="message">
                    Sua conta do Google foi autorizada com sucesso.<br>
                    Você pode fechar esta janela e retornar à aplicação.
                </div>
                <button class="close-btn" onclick="window.close()">Fechar Janela</button>
            </div>
        </body>
        </html>
        """;

    private HttpServer server;
    private CompletableFuture<String> authCodeFuture;
    private CountDownLatch serverReadyLatch;
    private int actualPort;

    /**
     * Inicia o servidor de callback e aguarda o código de autorização.
     * 
     * @param port Porta para o servidor (padrão: 8888)
     * @param timeoutSeconds Timeout em segundos (padrão: 300)
     * @return CompletableFuture que será completado com o código de autorização
     * @throws IOException se houver erro ao iniciar o servidor
     */
    public CompletableFuture<String> startAndWaitForAuthCode(int port, int timeoutSeconds) throws IOException {
        this.authCodeFuture = new CompletableFuture<>();
        this.serverReadyLatch = new CountDownLatch(1);
        
        // Tenta diferentes portas se a padrão estiver ocupada
        this.actualPort = findAvailablePort(port);
        
        server = HttpServer.create(new InetSocketAddress(this.actualPort), 0);
        server.createContext("/Callback", new AuthCallbackHandler());
        server.setExecutor(null); // Usa thread padrão
        server.start();
        
        log.info("Servidor de callback iniciado na porta {}", this.actualPort);
        serverReadyLatch.countDown();
        
        // Configura timeout
        CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS)
            .execute(() -> {
                if (!authCodeFuture.isDone()) {
                    authCodeFuture.completeExceptionally(
                        new RuntimeException("Timeout aguardando código de autorização após " + timeoutSeconds + " segundos"));
                }
            });
        
        return authCodeFuture;
    }

    /**
     * Inicia o servidor com configurações padrão.
     * 
     * @return CompletableFuture que será completado com o código de autorização
     * @throws IOException se houver erro ao iniciar o servidor
     */
    public CompletableFuture<String> startAndWaitForAuthCode() throws IOException {
        return startAndWaitForAuthCode(DEFAULT_PORT, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Para o servidor e limpa os recursos.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Servidor de callback parado");
        }
    }

    /**
     * Retorna a porta real em que o servidor está rodando.
     * 
     * @return porta do servidor
     */
    public int getActualPort() {
        return actualPort;
    }

    /**
     * Aguarda o servidor estar pronto.
     * 
     * @throws InterruptedException se a thread for interrompida
     */
    public void waitForServerReady() throws InterruptedException {
        if (serverReadyLatch != null) {
            serverReadyLatch.await();
        }
    }

    /**
     * Tenta encontrar uma porta disponível, testando a partir da porta especificada.
     * 
     * @param startPort Porta inicial para teste
     * @return Porta disponível
     * @throws IOException se nenhuma porta estiver disponível
     */
    private int findAvailablePort(int startPort) throws IOException {
        int port = startPort;
        int maxAttempts = 10;
        
        for (int i = 0; i < maxAttempts; i++) {
            try {
                HttpServer testServer = HttpServer.create(new InetSocketAddress(port), 0);
                testServer.start();
                testServer.stop(0);
                return port;
            } catch (IOException e) {
                port++;
                log.debug("Porta {} ocupada, tentando {}", port - 1, port);
            }
        }
        
        throw new IOException("Nenhuma porta disponível encontrada entre " + startPort + " e " + (startPort + maxAttempts - 1));
    }

    /**
     * Handler HTTP para processar o callback de autorização.
     */
    private class AuthCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                URI requestURI = exchange.getRequestURI();
                String query = requestURI.getQuery();
                
                log.info("Callback recebido: {}", requestURI);
                
                if (query != null && query.contains("code=")) {
                    // Extrai o código de autorização
                    String authCode = extractAuthCode(query);
                    if (authCode != null && !authCode.isEmpty()) {
                        log.info("Código de autorização capturado com sucesso");
                        authCodeFuture.complete(authCode);
                        
                        // Responde com página de sucesso
                        sendSuccessResponse(exchange);
                    } else {
                        log.warn("Código de autorização não encontrado na query: {}", query);
                        sendErrorResponse(exchange, "Código de autorização não encontrado");
                    }
                } else if (query != null && query.contains("error=")) {
                    // Erro de autorização
                    String error = extractError(query);
                    log.error("Erro de autorização: {}", error);
                    authCodeFuture.completeExceptionally(new RuntimeException("Erro de autorização: " + error));
                    sendErrorResponse(exchange, "Erro de autorização: " + error);
                } else {
                    log.warn("Query inválida recebida: {}", query);
                    sendErrorResponse(exchange, "Query inválida");
                }
            } catch (Exception e) {
                log.error("Erro ao processar callback", e);
                authCodeFuture.completeExceptionally(e);
                sendErrorResponse(exchange, "Erro interno do servidor");
            }
        }

        /**
         * Extrai o código de autorização da query string.
         */
        private String extractAuthCode(String query) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("code=")) {
                    return param.substring(5); // Remove "code="
                }
            }
            return null;
        }

        /**
         * Extrai a mensagem de erro da query string.
         */
        private String extractError(String query) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("error=")) {
                    return param.substring(6); // Remove "error="
                }
            }
            return "Erro desconhecido";
        }

        /**
         * Envia resposta de sucesso para o navegador.
         */
        private void sendSuccessResponse(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, SUCCESS_HTML.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(SUCCESS_HTML.getBytes());
            }
        }

        /**
         * Envia resposta de erro para o navegador.
         */
        private void sendErrorResponse(HttpExchange exchange, String errorMessage) throws IOException {
            String errorHtml = "<html><body><h1>Erro de Autorização</h1><p>" + errorMessage + "</p></body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(400, errorHtml.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorHtml.getBytes());
            }
        }
    }
}
