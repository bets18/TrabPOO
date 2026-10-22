package view;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

// carrega as imagens da pasta assets uma única vez, já reduzidas para o tamanho de exibição
// (as originais têm até 6000 px). A chave do cache é o caminho + a largura pedida.
final class Imagens {
    private static final String PASTA = "assets/";
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private Imagens() {
    }

    static BufferedImage carregar(String caminho, int largura) {
        String chave = caminho + "@" + largura;
        BufferedImage imagem = CACHE.get(chave);
        if (imagem == null) {
            imagem = reduzir(lerArquivo(caminho), largura);
            CACHE.put(chave, imagem);
        }
        return imagem;
    }

    private static BufferedImage lerArquivo(String caminho) {
        try {
            BufferedImage original = ImageIO.read(new File(PASTA + caminho));
            if (original == null) {
                throw new IllegalStateException("Formato de imagem não reconhecido: " + PASTA + caminho);
            }
            return original;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler " + PASTA + caminho
                    + " (o programa deve rodar a partir da pasta do projeto)", e);
        }
    }

    private static BufferedImage reduzir(BufferedImage original, int largura) {
        int altura = (int) Math.round(original.getHeight() * (double) largura / original.getWidth());
        BufferedImage reduzida = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = reduzida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, largura, altura, null);
        g.dispose();
        return reduzida;
    }
}
