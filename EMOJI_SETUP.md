# Configuração de Ícones PNG para Grupos

## 📋 Resumo

Implementamos um sistema de ícones PNG coloridos para os grupos de boards, substituindo os emojis Unicode que tinham problemas de renderização.

## 🎯 O que foi implementado

1. **Métodos de carregamento de imagens** no `BoardViewController`:
   - `loadEmojiImage(String emojiCode)` - Carrega PNG do diretório de recursos
   - `createEmojiImageView(String emojiCode)` - Cria ImageView 16x16 para o ComboBox

2. **ComboBox atualizado** para mostrar imagens PNG em vez de texto Unicode

3. **Lista de códigos de emoji** convertida para códigos PNG (ex: `1f4c1` para 📁)

## 📁 Estrutura de arquivos

```
src/main/resources/icons/emoji/
├── 1f4c1.png  (📁 pasta)
├── 1f4c2.png  (📂 pasta aberta)
├── 1f4bc.png  (💼 maleta)
├── 1f3af.png  (🎯 alvo)
├── 2b50.png   (⭐ estrela)
├── 1f525.png  (🔥 fogo)
├── 1f4a1.png  (💡 lâmpada)
├── 1f680.png  (🚀 foguete)
├── 1f3a8.png  (🎨 paleta)
├── 1f3ae.png  (🎮 videogame)
├── 1f4da.png  (📚 livros)
├── 1f393.png  (🎓 diploma)
├── 1f4bb.png  (💻 laptop)
├── 1f4f1.png  (📱 celular)
├── 1f3e0.png  (🏠 casa)
├── 1f3e2.png  (🏢 prédio)
├── 1f3ed.png  (🏭 fábrica)
├── 1f4ea.png  (📪 caixa de correio)
├── 1f3e5.png  (🏥 hospital)
└── 1f3eb.png  (🏫 escola)
```

## 🚀 Como baixar os PNGs

### Opção 1: Script PowerShell (Windows)
```powershell
powershell -ExecutionPolicy Bypass -File download_emojis.ps1
```

### Opção 2: Download manual

1. **Acesse o repositório Twemoji:**
   https://github.com/twitter/twemoji/tree/master/assets/72x72

2. **Baixe os seguintes arquivos PNG:**
   - `1f4c1.png` (📁 pasta)
   - `1f4c2.png` (📂 pasta aberta)
   - `1f4bc.png` (💼 maleta)
   - `1f3af.png` (🎯 alvo)
   - `2b50.png` (⭐ estrela)
   - `1f525.png` (🔥 fogo)
   - `1f4a1.png` (💡 lâmpada)
   - `1f680.png` (🚀 foguete)
   - `1f3a8.png` (🎨 paleta)
   - `1f3ae.png` (🎮 videogame)
   - `1f4da.png` (📚 livros)
   - `1f393.png` (🎓 diploma)
   - `1f4bb.png` (💻 laptop)
   - `1f4f1.png` (📱 celular)
   - `1f3e0.png` (🏠 casa)
   - `1f3e2.png` (🏢 prédio)
   - `1f3ed.png` (🏭 fábrica)
   - `1f4ea.png` (📪 caixa de correio)
   - `1f3e5.png` (🏥 hospital)
   - `1f3eb.png` (🏫 escola)

3. **Crie o diretório:**
   ```
   mkdir -p src/main/resources/icons/emoji
   ```

4. **Mova os PNGs para o diretório:**
   ```
   src/main/resources/icons/emoji/
   ```

### Opção 3: URLs diretas

Você pode baixar diretamente usando estas URLs:

- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f4c1.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f4c2.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f4bc.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f3af.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/2b50.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f525.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f4a1.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f680.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f3a8.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f3ae.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f4da.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f393.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f4bb.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f4f1.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f3e0.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f3e2.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f3ed.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f4ea.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f3e5.png
- https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/1f3eb.png

## 🧪 Como testar

1. **Baixe os PNGs** usando uma das opções acima

2. **Execute o aplicativo:**
   ```bash
   ./gradlew run
   ```

3. **Teste a funcionalidade:**
   - Clique em "Criar Grupo"
   - No campo "Ícone", você deve ver imagens PNG coloridas em vez de texto
   - Selecione um ícone e crie o grupo
   - Edite o grupo para verificar se os ícones aparecem corretamente

## 🔧 Código implementado

### Métodos adicionados ao BoardViewController:

```java
/**
 * Carrega uma imagem PNG do diretório de recursos
 */
private Image loadEmojiImage(String emojiCode) {
    try {
        String imagePath = "/icons/emoji/" + emojiCode + ".png";
        return new Image(getClass().getResourceAsStream(imagePath));
    } catch (Exception e) {
        System.err.println("Erro ao carregar imagem: " + emojiCode + ".png");
        return null;
    }
}

/**
 * Cria um ImageView com tamanho 16x16 para o ComboBox
 */
private ImageView createEmojiImageView(String emojiCode) {
    Image image = loadEmojiImage(emojiCode);
    if (image != null) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }
    return null;
}
```

### ComboBox configurado para mostrar imagens:

```java
// Configurar o ComboBox para mostrar imagens PNG
iconComboBox.setCellFactory(param -> new ListCell<String>() {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(null); // Não mostrar texto, apenas imagem
            ImageView imageView = createEmojiImageView(item);
            setGraphic(imageView);
            getStyleClass().setAll("icon-list-cell");
        }
    }
});
```

## 🎨 Vantagens da solução PNG

1. **✅ Renderização consistente** - Funciona em todos os sistemas operacionais
2. **✅ Cores vibrantes** - PNGs coloridos em vez de emojis pretos
3. **✅ Tamanho otimizado** - 32x32 pixels, leve e rápido
4. **✅ Qualidade profissional** - Ícones do Twemoji (Twitter/X)
5. **✅ Fácil manutenção** - Basta adicionar/remover arquivos PNG

## 🔄 Próximos passos

1. Baixe os PNGs usando uma das opções acima
2. Teste o aplicativo
3. Se funcionar bem, você pode adicionar mais ícones à lista `availableIcons`
4. Para adicionar novos ícones, consulte: https://emojipedia.org/ para encontrar os códigos Unicode 