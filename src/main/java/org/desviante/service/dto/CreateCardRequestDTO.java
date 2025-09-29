package org.desviante.service.dto;

import org.desviante.model.enums.ProgressType;

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
 * @param title título do card a ser criado
 * @param description descrição detalhada do card
 * @param parentColumnId ID da coluna onde o card será criado
 * @param cardTypeId ID do tipo de card a ser criado
 * @param progressType tipo de progresso do card (pode ser null)
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see org.desviante.service.TaskManagerFacade
 * @see org.desviante.service.CardService
 * @see org.desviante.model.CardType
 */
public record CreateCardRequestDTO(
        String title,
        String description,
        Long parentColumnId,
        Long cardTypeId,
        ProgressType progressType
) {}