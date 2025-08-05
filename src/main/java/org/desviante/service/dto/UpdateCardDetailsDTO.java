package org.desviante.service.dto;

/**
 * DTO para encapsular dados de requisição para atualização de detalhes de um Card.
 * 
 * <p>Representa os dados necessários para atualizar informações de um card
 * existente no sistema, encapsulando as modificações que a interface do
 * usuário envia para o backend. Este DTO é utilizado como estrutura de
 * entrada para operações de edição de cards, permitindo a modificação
 * de título e descrição sem afetar outros atributos do card.</p>
 * 
 * <p>Contém apenas os campos que podem ser editados pelo usuário (título
 * e descrição), mantendo a integridade de outros atributos como datas
 * de criação, última atualização e posicionamento no board. A atualização
 * automática da data de última modificação é gerenciada pelo CardService.</p>
 * 
 * <p>Utilizado principalmente pela TaskManagerFacade para receber dados
 * de edição da interface do usuário e delegar a atualização para o
 * CardService, mantendo a separação entre apresentação e lógica de negócio.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see TaskManagerFacade
 * @see CardService
 */
public record UpdateCardDetailsDTO(
        String title,               // Novo título do card
        String description          // Nova descrição do card
) {
}