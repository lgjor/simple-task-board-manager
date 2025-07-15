package org.desviante.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    // O nome "board-pu" deve ser o mesmo definido no seu persistence.xml
    private static final EntityManagerFactory FACTORY = buildEntityManagerFactory();

    private static EntityManagerFactory buildEntityManagerFactory() {
        // 1. Obter o diretório 'home' do usuário de forma segura para qualquer sistema operacional.
        String userHome = System.getProperty("user.home");

        // 2. Definir o caminho para a pasta 'MyBoards' e o arquivo do banco de dados.
        Path dbDirectoryPath = Paths.get(userHome, "MyBoards");
        Path dbFilePath = dbDirectoryPath.resolve("myboard.db");

        // 3. Garantir que o diretório exista. Se não existir, ele será criado.
        try {
            Files.createDirectories(dbDirectoryPath);
        } catch (IOException e) {
            // Se não for possível criar o diretório, a aplicação não pode continuar.
            throw new RuntimeException("Não foi possível criar o diretório do banco de dados em: " + dbDirectoryPath, e);
        }

        // 4. Criar a string da URL do JDBC dinamicamente.
        String jdbcUrl = "jdbc:sqlite:" + dbFilePath;

        // 5. Criar um mapa de propriedades para sobrescrever a configuração do persistence.xml.
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", jdbcUrl);

        // 6. Criar o EntityManagerFactory usando a unidade de persistência e as propriedades sobrescritas.
        return Persistence.createEntityManagerFactory("board-pu", properties);
    }

    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }

    public static void close() {
        if (FACTORY != null && FACTORY.isOpen()) {
            FACTORY.close();
        }
    }
}