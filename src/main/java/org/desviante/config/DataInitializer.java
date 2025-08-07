package org.desviante.config;

import lombok.RequiredArgsConstructor;
import org.desviante.model.BoardGroup;
import org.desviante.model.CardType;
import org.desviante.service.BoardGroupService;
import org.desviante.service.CardTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inicializador de dados padrão da aplicação.
 * 
 * <p>Responsável por criar dados iniciais necessários para o funcionamento
 * da aplicação, como tipos de card padrão e grupos de board que serão 
 * disponíveis para os usuários.</p>
 * 
 * <p>Este componente é executado automaticamente após a inicialização do Spring,
 * garantindo que os dados essenciais estejam disponíveis na primeira execução.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see org.springframework.boot.CommandLineRunner
 * @see org.desviante.service.CardTypeService
 * @see org.desviante.service.BoardGroupService
 * @see org.desviante.model.CardType
 * @see org.desviante.model.BoardGroup
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CardTypeService cardTypeService;
    private final BoardGroupService boardGroupService;

    /**
     * Executa a inicialização dos dados padrão após a aplicação estar pronta.
     * 
     * @param args argumentos da linha de comando (não utilizados)
     */
    @Override
    public void run(String... args) {
        log.info("Iniciando inicialização de dados padrão...");
        
        try {
            initializeDefaultCardTypes();
            initializeDefaultBoardGroups();
            log.info("Inicialização de dados padrão concluída com sucesso.");
        } catch (Exception e) {
            log.error("Erro durante a inicialização de dados padrão: {}", e.getMessage(), e);
        }
    }

    /**
     * Inicializa os tipos de card padrão do sistema.
     * 
     * <p>Cria os seguintes tipos padrão se não existirem:</p>
     * <ul>
     *   <li>Card - tipo genérico para tarefas simples</li>
     *   <li>Livro - para leitura e estudo de livros</li>
     *   <li>Curso - para cursos e treinamentos</li>
     *   <li>Vídeo - para vídeos e conteúdo audiovisual</li>
     * </ul>
     */
    private void initializeDefaultCardTypes() {
        List<CardTypeData> defaultTypes = List.of(
            new CardTypeData("Card", "unidade"),
            new CardTypeData("Livro", "páginas"),
            new CardTypeData("Curso", "aulas"),
            new CardTypeData("Vídeo", "minutos")
        );

        for (CardTypeData typeData : defaultTypes) {
            try {
                if (!cardTypeService.existsByName(typeData.name())) {
                    CardType newType = cardTypeService.createCardType(
                        typeData.name(), 
                        typeData.unitLabel()
                    );
                    log.info("Tipo padrão criado: {} (ID: {})", newType.getName(), newType.getId());
                } else {
                    log.debug("Tipo padrão já existe: {}", typeData.name());
                }
            } catch (Exception e) {
                log.warn("Erro ao criar tipo padrão '{}': {}", typeData.name(), e.getMessage());
            }
        }
    }

    /**
     * Inicializa os grupos de board padrão do sistema.
     * 
     * <p>Cria os seguintes grupos padrão se não existirem:</p>
     * <ul>
     *   <li>Projetos pessoais - para projetos pessoais e hobbies</li>
     *   <li>Livros - para leitura e estudo</li>
     *   <li>Trabalho - para tarefas profissionais</li>
     * </ul>
     */
    private void initializeDefaultBoardGroups() {
        List<BoardGroupData> defaultGroups = List.of(
            new BoardGroupData("Projetos pessoais", "Projetos pessoais e hobbies", "1f4bb"), // 💻 Computador
            new BoardGroupData("Livros", "Leitura e estudo de livros", "1f4da"), // 📚 Livro
            new BoardGroupData("Trabalho", "Tarefas profissionais e trabalho", "1f528") // 🔨 Martelo
        );

        for (BoardGroupData groupData : defaultGroups) {
            try {
                if (!boardGroupService.existsByName(groupData.name())) {
                    BoardGroup newGroup = boardGroupService.createBoardGroup(
                        groupData.name(), 
                        groupData.description(), 
                        groupData.icon()
                    );
                    log.info("Grupo padrão criado: {} (ID: {})", newGroup.getName(), newGroup.getId());
                } else {
                    log.debug("Grupo padrão já existe: {}", groupData.name());
                }
            } catch (Exception e) {
                log.warn("Erro ao criar grupo padrão '{}': {}", groupData.name(), e.getMessage());
            }
        }
    }

    /**
     * Classe interna para representar os dados de um tipo de card padrão.
     */
    private record CardTypeData(String name, String unitLabel) {}

    /**
     * Classe interna para representar os dados de um grupo de board padrão.
     */
    private record BoardGroupData(String name, String description, String icon) {}
} 