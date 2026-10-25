package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import model.HerdeirosDeKhanAPI;

// painel da direita: faixa com a cor do jogador da vez, a cartela do herdeiro (onde ele
// clica pra ativar a coluna), os movimentos que sobram e o botão de passar a vez
class PainelJogador extends JPanel {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 384;

    private static final int MARGEM = 2;
    private static final int LARGURA_CARTELA = LARGURA - 2 * MARGEM;
    private static final int TOPO_CARTELA = 170;
    private static final int TAMANHO_FICHA = 34;

    // nome de cada coluna no Model e onde ela fica na cartela (em fração da largura e da altura)
    private static final List<String> COLUNAS = Arrays.asList("VERMELHA", "VERDE", "AMARELA", "BRANCA");
    private static final double[][] FAIXAS_X = {{0.262, 0.412}, {0.435, 0.587}, {0.609, 0.761}, {0.806, 0.956}};
    private static final double Y_INICIO = 0.11, Y_FIM = 0.92;

    private final HerdeirosDeKhanAPI modelo;
    private final AcoesDoJogador acoes;
    private final JButton botaoPassarVez = new JButton("Passar a vez");

    PainelJogador(HerdeirosDeKhanAPI modelo, AcoesDoJogador acoes) {
        this.modelo = modelo;
        this.acoes = acoes;
        // BorderLayout pra deixar o botão sempre embaixo, qualquer que seja a altura
        setLayout(new BorderLayout());
        setBackground(new Color(245, 236, 215));

        JPanel base = new JPanel(new BorderLayout());
        base.setOpaque(false);
        base.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        botaoPassarVez.setFont(new Font("SansSerif", Font.BOLD, 16));
        botaoPassarVez.addActionListener(new PassarVez());
        base.add(botaoPassarVez, BorderLayout.CENTER);
        add(base, BorderLayout.SOUTH);

        addMouseListener(new CliqueNaCartela());

        // as cartelas são enormes (uns 6000 px). Carregando aqui, a janela não trava
        // na primeira vez que aparece cada herdeiro
        for (int i = 0; i < modelo.getNumeroDeJogadores(); i++) {
            Imagens.carregar(EstiloJogador.getCartela(modelo.getHerdeiro(i)), LARGURA_CARTELA);
            Imagens.carregar(EstiloJogador.getFicha(modelo.getHerdeiro(i)), TAMANHO_FICHA);
        }
    }

    void atualizar() {
        botaoPassarVez.setEnabled(modelo.getColunaDoTurno() != null);
        repaint();
    }

    private String herdeiroDaVez() {
        return modelo.getHerdeiroDoJogadorDaVez();
    }

    private Rectangle areaDaCartela(BufferedImage cartela) {
        return new Rectangle(MARGEM, TOPO_CARTELA, cartela.getWidth(), cartela.getHeight());
    }

    // retângulo da coluna i em cima da cartela, em coordenadas do painel
    private Rectangle areaDaColuna(Rectangle cartela, int i) {
        int x1 = cartela.x + (int) (FAIXAS_X[i][0] * cartela.width);
        int x2 = cartela.x + (int) (FAIXAS_X[i][1] * cartela.width);
        int y1 = cartela.y + (int) (Y_INICIO * cartela.height);
        int y2 = cartela.y + (int) (Y_FIM * cartela.height);
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String herdeiro = herdeiroDaVez();

        // faixa na cor do jogador da vez
        g2.setColor(EstiloJogador.getCor(herdeiro));
        g2.fillRect(0, 0, getWidth(), 150);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        g2.drawString("Vez de " + EstiloJogador.getNome(herdeiro), 14, 40);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.drawString("Jogador " + (modelo.getIndiceJogadorDaVez() + 1) + " de " + modelo.getNumeroDeJogadores(), 14, 66);
        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        g2.drawString(instrucao(), 14, 98);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.drawString("Movimentos restantes: " + modelo.getMovimentosRestantes(), 14, 120);
        int jogador = modelo.getIndiceJogadorDaVez();
        if (modelo.getNumeroDePeoes(jogador) > 1) {
            g2.drawString("Peão " + (modelo.getPeaoAtivo(jogador) + 1) + " ativo (clique no outro para trocar)", 14, 142);
        }

        desenharCartela(g2, herdeiro);
        desenharJogadores(g2);
    }

    private String instrucao() {
        if (modelo.getColunaDoTurno() == null) {
            return "Clique numa coluna do seu tabuleiro";
        }
        if (modelo.getMovimentosRestantes() > 0) {
            return "Clique na parada de destino";
        }
        return "Sem movimentos: passe a vez";
    }

    private void desenharCartela(Graphics2D g2, String herdeiro) {
        BufferedImage cartela = Imagens.carregar(EstiloJogador.getCartela(herdeiro), LARGURA_CARTELA);
        Rectangle area = areaDaCartela(cartela);
        g2.drawImage(cartela, area.x, area.y, null);

        BufferedImage ficha = Imagens.carregar(EstiloJogador.getFicha(herdeiro), TAMANHO_FICHA);
        for (int i = 0; i < COLUNAS.size(); i++) {
            Rectangle coluna = areaDaColuna(area, i);
            boolean ativada = COLUNAS.get(i).equals(modelo.getColunaDoTurno());
            if (ativada) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(4f));
                g2.draw(coluna);
            }
            // ficha nas colunas já usadas e na que foi ativada agora
            if (ativada || !modelo.colunaLivre(COLUNAS.get(i))) {
                g2.drawImage(ficha, coluna.x + (coluna.width - TAMANHO_FICHA) / 2, coluna.y + 6, null);
            }
        }
    }

    // lista dos jogadores com a cor e onde está cada peão
    private void desenharJogadores(Graphics2D g2) {
        int y = TOPO_CARTELA + 240;
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Jogadores", 14, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        for (int i = 0; i < modelo.getNumeroDeJogadores(); i++) {
            y += 30;
            String herdeiro = modelo.getHerdeiro(i);
            g2.setColor(EstiloJogador.getCor(herdeiro));
            g2.fillOval(14, y - 15, 20, 20);
            g2.setColor(Color.BLACK);
            g2.drawOval(14, y - 15, 20, 20);
            StringBuilder posicoes = new StringBuilder(modelo.getPosicaoDoPeao(i, 0));
            for (int peao = 1; peao < modelo.getNumeroDePeoes(i); peao++) {
                posicoes.append(" e ").append(modelo.getPosicaoDoPeao(i, peao));
            }
            String marca = i == modelo.getIndiceJogadorDaVez() ? "  (da vez)" : "";
            g2.drawString(EstiloJogador.getNome(herdeiro) + " em " + posicoes + marca, 44, y);
        }
    }

    // vê se o clique caiu em alguma das 4 colunas da cartela
    private class CliqueNaCartela extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            BufferedImage cartela = Imagens.carregar(EstiloJogador.getCartela(herdeiroDaVez()), LARGURA_CARTELA);
            Rectangle area = areaDaCartela(cartela);
            for (int i = 0; i < COLUNAS.size(); i++) {
                if (areaDaColuna(area, i).contains(e.getPoint())) {
                    acoes.colunaClicada(COLUNAS.get(i));
                    return;
                }
            }
        }
    }

    private class PassarVez implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            acoes.passarVezClicado();
        }
    }
}
