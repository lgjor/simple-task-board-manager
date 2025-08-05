package org.desviante.service.dto;

/**
 * DTO para transferência de dados detalhados de um Card para a interface do usuário.
 * 
 * <p>Representa um card individual do Kanban board com todas as suas informações
 * essenciais para exibição na UI. Este DTO é utilizado para transferir dados
 * completos de um card entre o backend e a interface do usuário, incluindo
 * informações de identificação, conteúdo e datas de controle de ciclo de vida.</p>
 * 
 * <p>Contém o identificador único do card, título e descrição para exibição,
 * além de datas formatadas como strings para facilitar a renderização na UI.
 * As datas são convertidas de LocalDateTime para String para simplificar
 * a manipulação no frontend e evitar problemas de serialização.</p>
 * 
 * <p>Utilizado como componente do BoardColumnDetailDTO para compor a estrutura
 * completa de cards em uma coluna, sendo parte da hierarquia de dados que
 * permite a renderização completa do Kanban board na interface do usuário.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardColumnDetailDTO
 * @see BoardDetailDTO
 */
public record CardDetailDTO(
        Long id,                    // Identificador único do card
        String title,               // Título do card para exibição
        String description,         // Descrição detalhada do card
        String creationDate,        // Data de criação formatada como string
        String lastUpdateDate,      // Data da última atualização formatada como string
        String completionDate       // Data de conclusão formatada como string (pode ser null)
) {
}