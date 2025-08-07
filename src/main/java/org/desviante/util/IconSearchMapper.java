package org.desviante.util;

import java.util.*;

/**
 * Mapeador para busca de ícones por palavras-chave e categorias.
 * 
 * <p>Fornece mapeamento entre códigos de ícones e suas descrições
 * semânticas para permitir busca por palavras-chave como "livros",
 * "casa", "trabalho", etc.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class IconSearchMapper {
    
    private static final Map<String, List<String>> ICON_CATEGORIES = new HashMap<>();
    private static final Map<String, String> ICON_DESCRIPTIONS = new HashMap<>();
    
    static {
        initializeIconMappings();
    }
    
    /**
     * Inicializa os mapeamentos de ícones para categorias e descrições.
     */
    private static void initializeIconMappings() {
        // Categoria: Livros e Educação
        addToCategory("1f4da", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4d5", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4d6", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4d7", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4d8", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4d9", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4da", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4db", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4dc", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4dd", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4de", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        addToCategory("1f4df", "livros", "biblioteca", "estudo", "educação", "leitura", "conhecimento");
        
        // Categoria: Casa e Família
        addToCategory("1f3e0", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e1", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e2", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e3", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e4", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e5", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e6", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e7", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e8", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3e9", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3ea", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3eb", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3ec", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3ed", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3ee", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3ef", "casa", "lar", "família", "residência", "moradia", "home");
        addToCategory("1f3f0", "casa", "lar", "família", "residência", "moradia", "home");
        
        // Categoria: Trabalho e Negócios
        addToCategory("1f4bc", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4bd", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4be", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4bf", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c0", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c1", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c2", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c3", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c4", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c5", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c6", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c7", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c8", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4c9", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4ca", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4cb", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4cc", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4cd", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4ce", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4cf", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4d0", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4d1", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4d2", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4d3", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        addToCategory("1f4d4", "trabalho", "negócios", "escritório", "profissão", "carreira", "business");
        
        // Categoria: Tecnologia e Computação
        addToCategory("1f4bb", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4bd", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4be", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4bf", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c0", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c1", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c2", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c3", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c4", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c5", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c6", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c7", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c8", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4c9", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4ca", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4cb", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4cc", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4cd", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4ce", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4cf", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4d0", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4d1", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4d2", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4d3", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        addToCategory("1f4d4", "tecnologia", "computador", "pc", "laptop", "notebook", "tech");
        
        // Categoria: Esportes e Atividades
        addToCategory("26bd", "esportes", "futebol", "bola", "jogo", "atividade", "sport");
        addToCategory("26be", "esportes", "baseball", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3c0", "esportes", "basquete", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3c8", "esportes", "futebol", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3c9", "esportes", "rugby", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3ca", "esportes", "vôlei", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3cb", "esportes", "tênis", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3cc", "esportes", "golfe", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3cd", "esportes", "moto", "veículo", "transporte", "atividade", "sport");
        addToCategory("1f3ce", "esportes", "corrida", "carro", "veículo", "atividade", "sport");
        addToCategory("1f3cf", "esportes", "hóquei", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3d0", "esportes", "hóquei", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3d1", "esportes", "vôlei", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3d2", "esportes", "hóquei", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3d3", "esportes", "ping-pong", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3d4", "esportes", "badminton", "bola", "jogo", "atividade", "sport");
        addToCategory("1f3d5", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3d6", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3d7", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3d8", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3d9", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3da", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3db", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3dc", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3dd", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3de", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3df", "esportes", "boxe", "luta", "atividade", "sport");
        addToCategory("1f3e0", "esportes", "boxe", "luta", "atividade", "sport");
        
        // Categoria: Comida e Bebidas
        addToCategory("1f374", "comida", "alimentação", "refeição", "jantar", "almoço", "food");
        addToCategory("1f375", "bebida", "café", "chá", "líquido", "drink");
        addToCategory("1f376", "bebida", "vinho", "álcool", "líquido", "drink");
        addToCategory("1f377", "bebida", "vinho", "álcool", "líquido", "drink");
        addToCategory("1f378", "bebida", "cocktail", "álcool", "líquido", "drink");
        addToCategory("1f379", "bebida", "suco", "líquido", "drink");
        addToCategory("1f37a", "bebida", "cerveja", "álcool", "líquido", "drink");
        addToCategory("1f37b", "bebida", "cerveja", "álcool", "líquido", "drink");
        addToCategory("1f37c", "bebida", "leite", "líquido", "drink");
        addToCategory("1f37d", "comida", "alimentação", "refeição", "jantar", "almoço", "food");
        addToCategory("1f37e", "bebida", "champagne", "álcool", "líquido", "drink");
        addToCategory("1f37f", "comida", "pipoca", "alimentação", "snack", "food");
        
        // Categoria: Transporte
        addToCategory("1f680", "transporte", "avião", "viagem", "locomoção", "vehicle");
        addToCategory("1f681", "transporte", "avião", "viagem", "locomoção", "vehicle");
        addToCategory("1f682", "transporte", "trem", "viagem", "locomoção", "vehicle");
        addToCategory("1f683", "transporte", "trem", "viagem", "locomoção", "vehicle");
        addToCategory("1f684", "transporte", "trem", "viagem", "locomoção", "vehicle");
        addToCategory("1f685", "transporte", "trem", "viagem", "locomoção", "vehicle");
        addToCategory("1f686", "transporte", "trem", "viagem", "locomoção", "vehicle");
        addToCategory("1f687", "transporte", "metrô", "viagem", "locomoção", "vehicle");
        addToCategory("1f688", "transporte", "trem", "viagem", "locomoção", "vehicle");
        addToCategory("1f689", "transporte", "trem", "viagem", "locomoção", "vehicle");
        addToCategory("1f68a", "transporte", "ônibus", "viagem", "locomoção", "vehicle");
        addToCategory("1f68b", "transporte", "ônibus", "viagem", "locomoção", "vehicle");
        addToCategory("1f68c", "transporte", "ônibus", "viagem", "locomoção", "vehicle");
        addToCategory("1f68d", "transporte", "ônibus", "viagem", "locomoção", "vehicle");
        addToCategory("1f68e", "transporte", "ônibus", "viagem", "locomoção", "vehicle");
        addToCategory("1f68f", "transporte", "ônibus", "viagem", "locomoção", "vehicle");
        addToCategory("1f690", "transporte", "ônibus", "viagem", "locomoção", "vehicle");
        addToCategory("1f691", "transporte", "ambulância", "emergência", "vehicle");
        addToCategory("1f692", "transporte", "carro", "emergência", "vehicle");
        addToCategory("1f693", "transporte", "carro", "emergência", "vehicle");
        addToCategory("1f694", "transporte", "carro", "emergência", "vehicle");
        addToCategory("1f695", "transporte", "carro", "emergência", "vehicle");
        addToCategory("1f696", "transporte", "carro", "emergência", "vehicle");
        addToCategory("1f697", "transporte", "carro", "emergência", "vehicle");
        addToCategory("1f698", "transporte", "carro", "emergência", "vehicle");
        addToCategory("1f699", "transporte", "carro", "emergência", "vehicle");
        addToCategory("1f69a", "transporte", "caminhão", "emergência", "vehicle");
        addToCategory("1f69b", "transporte", "caminhão", "emergência", "vehicle");
        addToCategory("1f69c", "transporte", "caminhão", "emergência", "vehicle");
        addToCategory("1f69d", "transporte", "caminhão", "emergência", "vehicle");
        addToCategory("1f69e", "transporte", "caminhão", "emergência", "vehicle");
        addToCategory("1f69f", "transporte", "caminhão", "emergência", "vehicle");
        addToCategory("1f6a0", "transporte", "caminhão", "emergência", "vehicle");
        addToCategory("1f6a1", "transporte", "caminhão", "emergência", "vehicle");
        addToCategory("1f6a2", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6a3", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6a4", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6a5", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6a6", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6a7", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6a8", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6a9", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6aa", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6ab", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6ac", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6ad", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6ae", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6af", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b0", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b1", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b2", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b3", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b4", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b5", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b6", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b7", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b8", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6b9", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6ba", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6bb", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6bc", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6bd", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6be", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6bf", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6c0", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6c1", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6c2", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6c3", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6c4", "transporte", "navio", "emergência", "vehicle");
        addToCategory("1f6c5", "transporte", "navio", "emergência", "vehicle");
        
        // Adicionar descrições para alguns ícones específicos
        ICON_DESCRIPTIONS.put("1f4da", "Livros");
        ICON_DESCRIPTIONS.put("1f3e0", "Casa");
        ICON_DESCRIPTIONS.put("1f4bc", "Maleta de Trabalho");
        ICON_DESCRIPTIONS.put("1f4bb", "Computador");
        ICON_DESCRIPTIONS.put("26bd", "Bola de Futebol");
        ICON_DESCRIPTIONS.put("1f374", "Garfo e Faca");
        ICON_DESCRIPTIONS.put("1f680", "Foguete");
    }
    
    /**
     * Adiciona um ícone a uma categoria com palavras-chave.
     */
    private static void addToCategory(String iconCode, String... keywords) {
        ICON_CATEGORIES.put(iconCode, Arrays.asList(keywords));
    }
    
    /**
     * Busca ícones por palavra-chave.
     * 
     * @param searchTerm termo de busca
     * @return lista de códigos de ícones que correspondem à busca
     */
    public static List<String> searchIcons(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerSearch = searchTerm.toLowerCase().trim();
        List<String> results = new ArrayList<>();
        
        // Buscar por código do ícone
        for (String iconCode : ICON_CATEGORIES.keySet()) {
            if (iconCode.toLowerCase().contains(lowerSearch)) {
                results.add(iconCode);
            }
        }
        
        // Buscar por palavras-chave
        for (Map.Entry<String, List<String>> entry : ICON_CATEGORIES.entrySet()) {
            String iconCode = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (keyword.toLowerCase().contains(lowerSearch)) {
                    if (!results.contains(iconCode)) {
                        results.add(iconCode);
                    }
                    break;
                }
            }
        }
        
        return results;
    }
    
    /**
     * Obtém a descrição de um ícone.
     * 
     * @param iconCode código do ícone
     * @return descrição do ícone ou o código se não houver descrição
     */
    public static String getIconDescription(String iconCode) {
        return ICON_DESCRIPTIONS.getOrDefault(iconCode, iconCode);
    }
    
    /**
     * Obtém as palavras-chave de um ícone.
     * 
     * @param iconCode código do ícone
     * @return lista de palavras-chave ou lista vazia se não houver
     */
    public static List<String> getIconKeywords(String iconCode) {
        return ICON_CATEGORIES.getOrDefault(iconCode, new ArrayList<>());
    }
}
