package org.desviante.service.dto;

/**
 * DTO para encapsular dados de requisição para criação de um novo Card.
 * 
 * <p>Representa os dados necessários para criar um novo card no sistema,
 * encapsulando as informações que a interface do usuário envia para o
 * backend. Este DTO é utilizado como estrutura de entrada para operações
 * de criação de cards, fornecendo todos os dados essenciais para a
 * persistência no banco de dados.</p>
 * 
 * <p>Contém o título, descrição e tipo de card que serão exibidos na interface,
 * além do identificador da coluna pai onde o card será criado. O tipo do card
 * determina se ele suporta acompanhamento de progresso e quais campos específicos
 * estarão disponíveis.</p>
 * 
 * <p>Utilizado principalmente pela TaskManagerFacade para receber dados
 * da interface do usuário e delegar a criação do card para o CardService,
 * mantendo a separação entre a camada de apresentação e a lógica de negócio.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see TaskManagerFacade
 * @see CardService
 * @see CardType
 */
public record CreateCardRequestDTO(
        String title,               // Título do card a ser criado
        String description,         // Descrição detalhada do card
        Long parentColumnId,        // ID da coluna onde o card será criado
        Long cardTypeId           // ID do tipo de card a ser criado
) {}