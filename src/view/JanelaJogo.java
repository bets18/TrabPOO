package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

import model.HerdeirosDeKhanAPI;

// janela da partida: tabuleiro à esquerda e painel do jogador da vez à direita.
// o PDF limita as janelas a 1400 x 1050 pixels: a janela abre no maior tamanho permitido que
// caiba na área útil da tela (sem ficar atrás da barra de tarefas) e pode ser redimensionada
// ou maximizada, mas nunca passa desse limite.
public class JanelaJogo extends JFrame {
    private static final long serialVersionUID = 1L;
    public static final int LARGURA_MAXIMA = 1400;
    public static final int ALTURA_MAXIMA = 1050;
    private static final int LARGURA_MINIMA = 1000;
    private static final int ALTURA_MINIMA = 760;

    private final PainelTabuleiro painelTabuleiro;
    private final PainelJogador painelJogador;

    public JanelaJogo(HerdeirosDeKhanAPI modelo, AcoesDoJogador acoes) {
        super("Herdeiros de Khan");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // área da tela sem a barra de tarefas
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int largura = Math.min(LARGURA_MAXIMA, area.width);
        int altura = Math.min(ALTURA_MAXIMA, area.height);
        int x = area.x + (area.width - largura) / 2;
        int y = area.y + (area.height - altura) / 2;
        setBounds(x, y, largura, altura);
        setResizable(true);
        setMinimumSize(new Dimension(Math.min(LARGURA_MINIMA, largura), Math.min(ALTURA_MINIMA, altura)));
        // maximizar ocupa no máximo 1400 x 1050, centralizado no topo da área útil
        setMaximizedBounds(new Rectangle(x, area.y, largura, altura));
        addComponentListener(new LimiteDeTamanho());

        painelTabuleiro = new PainelTabuleiro(modelo, acoes);
        painelJogador = new PainelJogador(modelo, acoes);
        painelJogador.setPreferredSize(new Dimension(PainelJogador.LARGURA, ALTURA_MAXIMA));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(painelTabuleiro, BorderLayout.CENTER);
        getContentPane().add(painelJogador, BorderLayout.EAST);
        atualizar();
    }

    // redesenha tudo depois de uma jogada
    public void atualizar() {
        painelJogador.atualizar();
        painelTabuleiro.repaint();
    }

    // se o jogador arrastar a borda além de 1400 x 1050, a janela volta para o limite
    private class LimiteDeTamanho extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            int largura = Math.min(getWidth(), LARGURA_MAXIMA);
            int altura = Math.min(getHeight(), ALTURA_MAXIMA);
            if (largura != getWidth() || altura != getHeight()) {
                setSize(largura, altura);
            }
        }
    }
}
