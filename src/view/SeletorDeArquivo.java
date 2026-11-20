package view;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

// escolha do arquivo da partida salva, com o JFileChooser. Devolve só o caminho: quem
// grava e lê o arquivo é o Model
public final class SeletorDeArquivo {
    private static final String EXTENSAO = "txt";

    private SeletorDeArquivo() {
    }

    // null se cancelaram. Se o nome vier sem extensão, ganha ".txt"
    public static String escolherParaSalvar(Component dona) {
        JFileChooser seletor = criar("Salvar partida");
        seletor.setSelectedFile(new File("partida." + EXTENSAO));
        if (seletor.showSaveDialog(dona) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        String caminho = seletor.getSelectedFile().getAbsolutePath();
        return caminho.toLowerCase().endsWith("." + EXTENSAO) ? caminho : caminho + "." + EXTENSAO;
    }

    // null se cancelaram
    public static String escolherParaAbrir(Component dona) {
        JFileChooser seletor = criar("Continuar partida salva");
        if (seletor.showOpenDialog(dona) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return seletor.getSelectedFile().getAbsolutePath();
    }

    // abre na pasta de onde o jogo está rodando
    private static JFileChooser criar(String titulo) {
        JFileChooser seletor = new JFileChooser(new File("."));
        seletor.setDialogTitle(titulo);
        seletor.setFileFilter(new FileNameExtensionFilter("Partida salva (*." + EXTENSAO + ")", EXTENSAO));
        return seletor;
    }
}
