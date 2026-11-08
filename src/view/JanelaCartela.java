package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import model.HerdeirosDeKhanAPI;
import model.ObservadoIF;
import model.ObservadorIF;

// janela da cartela (tabuleiro do herdeiro) do jogador da vez, com as 4 colunas, as fichas
// de ativação e as melhorias. É um observador: abre sozinha quando começa um turno, quando
// pedem pra ver a cartela ou quando chega uma melhoria nova, e fecha depois de ativar a coluna
class JanelaCartela extends JDialog implements ObservadorIF {
    private static final long serialVersionUID = 1L;
    private static final int LARGURA_CARTELA = 860;
    private static final int TAMANHO_FICHA = 34;

    // nome de cada coluna no Model e onde ela fica na cartela (em fração da largura e da altura)
    private static final List<String> COLUNAS = Arrays.asList("VERMELHA", "VERDE", "AMARELA", "BRANCA");
    private static final double[][] FAIXAS_X = {{0.262, 0.412}, {0.435, 0.587}, {0.609, 0.761}, {0.806, 0.956}};
    private static final double Y_INICIO = 0.09, Y_FIM = 0.92;
    // topo de cada espaço de melhoria, de baixo pra cima, e a altura dele
    private static final double[] TOPO_DOS_ESPACOS = {0.586, 0.422, 0.258, 0.095};
    private static final double ALTURA_DO_ESPACO = 0.145;

    private final HerdeirosDeKhanAPI modelo = HerdeirosDeKhanAPI.getInstancia();
    private final AcoesDoJogador acoes;
    private final Desenho desenho = new Desenho();
    private final JLabel aviso = new JLabel(" ");

    JanelaCartela(Frame dona, AcoesDoJogador acoes) {
        super(dona, "Cartela", false);
        this.acoes = acoes;
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        // as cartelas são enormes (uns 6000 px). Carregando aqui, a janela não trava
        // na primeira vez que aparece cada herdeiro
        for (int i = 0; i < modelo.getNumeroDeJogadores(); i++) {
            Imagens.carregar(EstiloJogador.getCartela(modelo.getHerdeiro(i)), LARGURA_CARTELA);
            Imagens.carregar(EstiloJogador.getFicha(modelo.getHerdeiro(i)), TAMANHO_FICHA);
        }
        BufferedImage exemplo = Imagens.carregar(EstiloJogador.getCartela(modelo.getHerdeiro(0)), LARGURA_CARTELA);
        desenho.setPreferredSize(new Dimension(exemplo.getWidth(), exemplo.getHeight()));
        desenho.addMouseListener(new CliqueNaCartela());

        aviso.setFont(new Font("SansSerif", Font.BOLD, 15));
        aviso.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        // a janela abre pelo botão "Ver cartela" do painel e fecha por este botão
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(aviso, BorderLayout.CENTER);
        rodape.add(fechar, BorderLayout.EAST);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(desenho, BorderLayout.CENTER);
        getContentPane().add(rodape, BorderLayout.SOUTH);
        pack();
        setResizable(false);
        // em cima, à esquerda: assim não cobre a área de melhorias, que fica embaixo do mapa
        setLocation(dona.getX() + 10, dona.getY() + 30);
        modelo.adicionarObservador(this);
    }

    @Override
    public void notificar(ObservadoIF observado, String evento) {
        atualizarAviso();
        if (evento.equals(HerdeirosDeKhanAPI.EVENTO_JOGO_TERMINADO)) {
            setVisible(false);
        } else if (evento.equals(HerdeirosDeKhanAPI.EVENTO_TURNO_INICIADO)
                || evento.equals(HerdeirosDeKhanAPI.EVENTO_JANELA_DA_CARTELA)
                || evento.equals(HerdeirosDeKhanAPI.EVENTO_MELHORIA_COMPRADA)
                || evento.equals(HerdeirosDeKhanAPI.EVENTO_MELHORIA_POSICIONADA)) {
            setVisible(true);
            toFront();
        } else if (evento.equals(HerdeirosDeKhanAPI.EVENTO_COLUNA_ATIVADA) && modelo.getMelhoriaParaPosicionar() == null) {
            // a coluna já foi escolhida: libera a vista do mapa
            setVisible(false);
        }
        desenho.repaint();
    }

    private void atualizarAviso() {
        String nome = EstiloJogador.getNome(modelo.getHerdeiroDoJogadorDaVez());
        setTitle("Cartela de " + nome);
        if (modelo.isEscolhaInicialDoJochi()) {
            aviso.setText("Jochi: clique na coluna branca para pôr esta melhoria lá, ou numa colorida (a outra vai para a branca)");
        } else if (modelo.getMelhoriaParaPosicionar() != null) {
            aviso.setText("Clique na coluna colorida onde vai a melhoria nova (as livres estão destacadas)");
        } else if (modelo.getColunaDoTurno() == null) {
            aviso.setText("Clique numa coluna sem ficha (a branca devolve todas as fichas)");
        } else {
            aviso.setText("Coluna " + modelo.getColunaDoTurno().toLowerCase() + " ativada neste turno");
        }
    }

    private Rectangle areaDaCartela(BufferedImage cartela) {
        return new Rectangle(0, 0, cartela.getWidth(), cartela.getHeight());
    }

    // retângulo da coluna i em cima da cartela
    private Rectangle areaDaColuna(Rectangle cartela, int i) {
        int x1 = cartela.x + (int) (FAIXAS_X[i][0] * cartela.width);
        int x2 = cartela.x + (int) (FAIXAS_X[i][1] * cartela.width);
        int y1 = cartela.y + (int) (Y_INICIO * cartela.height);
        int y2 = cartela.y + (int) (Y_FIM * cartela.height);
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    // espaço de melhoria da linha (0 = de baixo) na coluna i
    private Rectangle espacoDeMelhoria(Rectangle cartela, int i, int linha) {
        int x1 = cartela.x + (int) (FAIXAS_X[i][0] * cartela.width);
        int x2 = cartela.x + (int) (FAIXAS_X[i][1] * cartela.width);
        int y = cartela.y + (int) (TOPO_DOS_ESPACOS[linha] * cartela.height);
        return new Rectangle(x1, y, x2 - x1, (int) (ALTURA_DO_ESPACO * cartela.height));
    }

    private BufferedImage cartelaDaVez() {
        return Imagens.carregar(EstiloJogador.getCartela(modelo.getHerdeiroDoJogadorDaVez()), LARGURA_CARTELA);
    }

    private class Desenho extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            String herdeiro = modelo.getHerdeiroDoJogadorDaVez();
            int jogador = modelo.getIndiceJogadorDaVez();
            BufferedImage cartela = cartelaDaVez();
            Rectangle area = areaDaCartela(cartela);
            g2.drawImage(cartela, area.x, area.y, null);

            BufferedImage ficha = Imagens.carregar(EstiloJogador.getFicha(herdeiro), TAMANHO_FICHA);
            boolean posicionando = modelo.getMelhoriaParaPosicionar() != null;
            for (int i = 0; i < COLUNAS.size(); i++) {
                String coluna = COLUNAS.get(i);
                List<String> melhorias = modelo.getMelhorias(jogador, coluna);
                for (int linha = 0; linha < melhorias.size() && linha < TOPO_DOS_ESPACOS.length; linha++) {
                    Rectangle espaco = espacoDeMelhoria(area, i, linha);
                    BufferedImage peca = Imagens.carregar(Pecas.melhoria(melhorias.get(linha)), espaco.width);
                    g2.drawImage(peca, espaco.x, espaco.y, espaco.width, espaco.height, null);
                }
                Rectangle retangulo = areaDaColuna(area, i);
                boolean ativada = coluna.equals(modelo.getColunaDoTurno());
                boolean livreParaMelhoria = posicionando && (i < 3 || modelo.isEscolhaInicialDoJochi())
                        && melhorias.size() < TOPO_DOS_ESPACOS.length;
                if (ativada || livreParaMelhoria) {
                    g2.setColor(livreParaMelhoria ? new Color(255, 215, 0) : Color.WHITE);
                    g2.setStroke(new BasicStroke(4f));
                    g2.draw(retangulo);
                }
                // ficha nas colunas já usadas e na que foi ativada agora
                if (ativada || !modelo.colunaLivre(coluna)) {
                    g2.drawImage(ficha, retangulo.x + (retangulo.width - TAMANHO_FICHA) / 2,
                            area.y + (int) (0.015 * area.height), null);
                }
            }
        }
    }

    // vê se o clique caiu em alguma das 4 colunas da cartela
    private class CliqueNaCartela extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            Rectangle area = areaDaCartela(cartelaDaVez());
            for (int i = 0; i < COLUNAS.size(); i++) {
                if (areaDaColuna(area, i).contains(e.getPoint())) {
                    acoes.colunaClicada(COLUNAS.get(i));
                    return;
                }
            }
        }
    }
}
