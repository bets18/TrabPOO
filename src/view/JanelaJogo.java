package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

import model.HerdeirosDeKhanAPI;
import model.ObservadoIF;
import model.ObservadorIF;

// janela da partida: tabuleiro na esquerda e painel do jogador da vez na direita. Ela cria
// as janelas da cartela e de cartas, e observa o Model pra mostrar o resultado no fim.
// O enunciado limita a janela a 1400 x 1050, então ela abre no maior tamanho que cabe
// na tela (sem ficar atrás da barra de tarefas) e nunca passa desse limite, nem maximizada
public class JanelaJogo extends JFrame implements ObservadorIF {
    private static final long serialVersionUID = 1L;
    public static final int LARGURA_MAXIMA = 1400;
    public static final int ALTURA_MAXIMA = 1050;
    private static final int LARGURA_MINIMA = 1000;
    private static final int ALTURA_MINIMA = 760;

    public JanelaJogo(AcoesDoJogador acoes) {
        super("Herdeiros de Khan");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // área da tela tirando a barra de tarefas
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int largura = Math.min(LARGURA_MAXIMA, area.width);
        int altura = Math.min(ALTURA_MAXIMA, area.height);
        int x = area.x + (area.width - largura) / 2;
        int y = area.y + (area.height - altura) / 2;
        setBounds(x, y, largura, altura);
        setResizable(true);
        setMinimumSize(new Dimension(Math.min(LARGURA_MINIMA, largura), Math.min(ALTURA_MINIMA, altura)));
        // maximizada também fica no limite de 1400 x 1050
        setMaximizedBounds(new Rectangle(x, area.y, largura, altura));
        addComponentListener(new LimiteDeTamanho());

        PainelTabuleiro painelTabuleiro = new PainelTabuleiro(acoes);
        PainelJogador painelJogador = new PainelJogador(acoes);
        painelJogador.setPreferredSize(new Dimension(PainelJogador.LARGURA, ALTURA_MAXIMA));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(painelTabuleiro, BorderLayout.CENTER);
        getContentPane().add(painelJogador, BorderLayout.EAST);

        // as duas janelas se registram como observadoras e aparecem quando o Model avisa
        new JanelaCartela(this, acoes);
        new JanelaCartas(this, acoes);
        HerdeirosDeKhanAPI.getInstancia().adicionarObservador(this);
    }

    @Override
    public void notificar(ObservadoIF observado, String evento) {
        if (evento.equals(HerdeirosDeKhanAPI.EVENTO_JOGO_TERMINADO)) {
            new JanelaResultado(this).setVisible(true);
        }
    }

    // se arrastarem a borda além do limite, a janela volta pro tamanho máximo
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
