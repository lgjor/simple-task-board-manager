package org.desviante.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Serviço de monitoramento de arquivos para detectar mudanças.
 * 
 * <p>Esta classe monitora arquivos específicos em diretórios para detectar
 * mudanças e executar callbacks quando necessário.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
public class FileWatcherService implements InitializingBean, DisposableBean {
    
    private WatchService watchService;
    private ExecutorService executorService;
    private volatile boolean isRunning = false;
    private Path watchedDirectory;
    private String watchedFileName;
    private Consumer<Path> fileChangeCallback;
    
    /**
     * Construtor padrão do serviço de monitoramento de arquivos.
     * 
     * <p>Este serviço não requer inicialização especial.</p>
     */
    public FileWatcherService() {
        // Serviço automático de monitoramento
    }
    
    /**
     * Inicializa o serviço de monitoramento.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.executorService = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "FileWatcher-Thread");
                t.setDaemon(true);
                return t;
            });
            log.info("Serviço de monitoramento de arquivos inicializado");
        } catch (IOException e) {
            log.error("Erro ao inicializar serviço de monitoramento de arquivos", e);
        }
    }
    
    /**
     * Inicia o monitoramento de um arquivo específico.
     * 
     * @param directory diretório a ser monitorado
     * @param fileName nome do arquivo a ser monitorado
     * @param callback callback executado quando o arquivo é alterado
     */
    public void startWatching(Path directory, String fileName, Consumer<Path> callback) {
        if (isRunning) {
            log.warn("Serviço de monitoramento já está em execução");
            return;
        }
        
        this.watchedDirectory = directory;
        this.watchedFileName = fileName;
        this.fileChangeCallback = callback;
        
        try {
            // Registra o diretório para monitoramento
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            log.info("Monitoramento iniciado para: {}", directory.resolve(fileName));
            
            // Inicia o monitoramento em thread separada
            isRunning = true;
            executorService.submit(this::watchLoop);
            
        } catch (IOException e) {
            log.error("Erro ao iniciar monitoramento do diretório: {}", directory, e);
        }
    }
    
    /**
     * Para o monitoramento de arquivos.
     */
    public void stopWatching() {
        isRunning = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("Erro ao fechar WatchService", e);
            }
        }
        log.info("Monitoramento de arquivos parado");
    }
    
    /**
     * Loop principal de monitoramento.
     */
    private void watchLoop() {
        log.debug("Iniciando loop de monitoramento de arquivos");
        
        while (isRunning) {
            try {
                WatchKey key = watchService.take();
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();
                        
                        // Verifica se é o arquivo que estamos monitorando
                        if (watchedFileName.equals(fileName.toString())) {
                            Path fullPath = watchedDirectory.resolve(fileName);
                            log.info("Arquivo de configuração alterado: {}", fullPath);
                            
                            // Notifica a alteração
                            if (fileChangeCallback != null) {
                                try {
                                    fileChangeCallback.accept(fullPath);
                                } catch (Exception e) {
                                    log.error("Erro ao executar callback de alteração de arquivo", e);
                                }
                            }
                        }
                    }
                }
                
                // Reseta a chave para continuar monitorando
                if (!key.reset()) {
                    log.warn("Chave de monitoramento não pode ser resetada");
                    break;
                }
                
            } catch (InterruptedException e) {
                log.debug("Thread de monitoramento interrompida");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Erro no loop de monitoramento de arquivos", e);
                // Aguarda um pouco antes de continuar
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.debug("Loop de monitoramento de arquivos finalizado");
    }
    
    /**
     * Verifica se o serviço está monitorando arquivos.
     * 
     * @return true se estiver monitorando, false caso contrário
     */
    public boolean isWatching() {
        return isRunning;
    }
    
    /**
     * Obtém o caminho do arquivo sendo monitorado.
     * 
     * @return caminho completo do arquivo monitorado
     */
    public Path getWatchedFilePath() {
        return watchedDirectory != null && watchedFileName != null 
            ? watchedDirectory.resolve(watchedFileName) 
            : null;
    }
    
    /**
     * Limpa recursos do serviço.
     */
    @Override
    public void destroy() {
        stopWatching();
        if (executorService != null) {
            executorService.shutdown();
        }
        log.info("Serviço de monitoramento de arquivos finalizado");
    }
}
