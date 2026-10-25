package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import model.HerdeirosDeKhanAPI;

// desenha o tabuleiro com drawImage e os peões e yurts com formas do Java2D.
// Também descobre em qual parada (ou peão) o jogador clicou
class PainelTabuleiro extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int LARGURA_EXIBIDA = 1000;
    private static final int RAIO_PEAO = 12;
    private static final Color DESTAQUE = new Color(255, 215, 0);

    private final HerdeirosDeKhanAPI modelo;
    private final AcoesDoJogador acoes;
    private final BufferedImage tabuleiro;

    PainelTabuleiro(HerdeirosDeKhanAPI modelo, AcoesDoJogador acoes) {
        this.modelo = modelo;
        this.acoes = acoes;
        this.tabuleiro = Imagens.carregar("tabuleiro.jpg", LARGURA_EXIBIDA);
        setBackground(Color.DARK_GRAY);
        addMouseListener(new CliqueNoTabuleiro());
    }

    // mesma escala nos dois eixos pra imagem não ficar distorcida
    private double escala() {
        return Math.min(getWidth() / (double) CoordenadasMapa.LARGURA_IMAGEM,
                getHeight() / (double) CoordenadasMapa.ALTURA_IMAGEM);
    }

    // se sobrar largura, o tabuleiro fica centralizado
    private int margemEsquerda() {
        return (getWidth() - (int) (CoordenadasMapa.LARGURA_IMAGEM * escala())) / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double s = escala();
        g2.translate(margemEsquerda(), 0);
        g2.drawImage(tabuleiro, 0, 0, (int) (CoordenadasMapa.LARGURA_IMAGEM * s),
                (int) (CoordenadasMapa.ALTURA_IMAGEM * s), null);
        desenharYurts(g2, s);
        desenharPeoes(g2, s);
    }

    // cada yurt é uma tendinha na cor do dono, um pouco acima da parada
    private void desenharYurts(Graphics2D g2, double s) {
        for (String parada : CoordenadasMapa.getNomes()) {
            List<String> donos = modelo.getDonosDeYurts(parada);
            int[][] pontos = CoordenadasMapa.getPontos(parada);
            for (int k = 0; k < donos.size(); k++) {
                double x = pontos[0][0] * s + (k - (donos.size() - 1) / 2.0) * 22;
                double y = pontos[0][1] * s - RAIO_PEAO - 16;
                desenharYurt(g2, EstiloJogador.getCor(donos.get(k)), x, y);
            }
        }
    }

    private void desenharYurt(Graphics2D g2, Color cor, double x, double y) {
        Path2D tenda = new Path2D.Double();
        tenda.moveTo(x - 10, y + 8);
        tenda.lineTo(x - 10, y);
        tenda.lineTo(x, y - 9);
        tenda.lineTo(x + 10, y);
        tenda.lineTo(x + 10, y + 8);
        tenda.closePath();
        g2.setColor(cor);
        g2.fill(tenda);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(tenda);
    }

    private void desenharPeoes(Graphics2D g2, double s) {
        for (double[] p : posicoesDosPeoes(s)) {
            desenharPeao(g2, (int) p[0], (int) p[1], p[2], p[3]);
        }
    }

    // posição de cada peão na tela (sem a margem), no formato {jogador, peão, x, y}.
    // O desenho e o clique usam essa mesma conta, assim o clique cai onde o peão aparece
    private List<double[]> posicoesDosPeoes(double s) {
        // junta os peões por parada pra não desenhar um em cima do outro
        Map<String, List<int[]>> porParada = new HashMap<>();
        for (int i = 0; i < modelo.getNumeroDeJogadores(); i++) {
            for (int peao = 0; peao < modelo.getNumeroDePeoes(i); peao++) {
                String parada = modelo.getPosicaoDoPeao(i, peao);
                List<int[]> lista = porParada.get(parada);
                if (lista == null) {
                    lista = new ArrayList<>();
                    porParada.put(parada, lista);
                }
                lista.add(new int[] {i, peao});
            }
        }
        List<double[]> posicoes = new ArrayList<>();
        for (Map.Entry<String, List<int[]>> entrada : porParada.entrySet()) {
            int[][] pontos = CoordenadasMapa.getPontos(entrada.getKey());
            List<int[]> jogadores = entrada.getValue();
            for (int k = 0; k < jogadores.size(); k++) {
                double x, y;
                if (pontos.length == 2) {
                    // parada dupla: um peão em cada círculo
                    x = pontos[k % 2][0] * s;
                    y = pontos[k % 2][1] * s;
                } else {
                    // senão espalha os peões lado a lado
                    double deslocamento = (k - (jogadores.size() - 1) / 2.0) * (RAIO_PEAO * 2 + 2);
                    x = pontos[0][0] * s + deslocamento;
                    y = pontos[0][1] * s;
                }
                posicoes.add(new double[] {jogadores.get(k)[0], jogadores.get(k)[1], x, y});
            }
        }
        return posicoes;
    }

    // peão do jogador da vez que não está ativo e foi clicado (-1 se não clicou em nenhum)
    private int peaoInativoEm(double x, double y, double s) {
        int jogador = modelo.getIndiceJogadorDaVez();
        for (double[] p : posicoesDosPeoes(s)) {
            double dx = x - p[2], dy = y - p[3];
            if ((int) p[0] == jogador && (int) p[1] != modelo.getPeaoAtivo(jogador)
                    && dx * dx + dy * dy <= RAIO_PEAO * RAIO_PEAO) {
                return (int) p[1];
            }
        }
        return -1;
    }

    private void desenharPeao(Graphics2D g2, int jogador, int peao, double x, double y) {
        String herdeiro = modelo.getHerdeiro(jogador);
        Ellipse2D corpo = new Ellipse2D.Double(x - RAIO_PEAO, y - RAIO_PEAO, RAIO_PEAO * 2, RAIO_PEAO * 2);
        // contorno amarelo só no peão que vai jogar
        if (jogador == modelo.getIndiceJogadorDaVez() && peao == modelo.getPeaoAtivo(jogador)) {
            g2.setColor(DESTAQUE);
            g2.fill(new Ellipse2D.Double(x - RAIO_PEAO - 5, y - RAIO_PEAO - 5, RAIO_PEAO * 2 + 10, RAIO_PEAO * 2 + 10));
        }
        g2.setColor(EstiloJogador.getCor(herdeiro));
        g2.fill(corpo);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(corpo);

        // a Altani tem 2 peões, então eles ficam A1 e A2
        String inicial = herdeiro.substring(0, 1) + (modelo.getNumeroDePeoes(jogador) > 1 ? String.valueOf(peao + 1) : "");
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(inicial, (float) (x - fm.stringWidth(inicial) / 2.0), (float) (y + fm.getAscent() / 2.0 - 2));
    }

    // se clicou no outro peão da Altani, troca de peão. Senão converte o ponto pra
    // coordenada da imagem e vê qual parada é
    private class CliqueNoTabuleiro extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            double s = escala();
            int x = e.getX() - margemEsquerda();
            int peao = peaoInativoEm(x, e.getY(), s);
            if (peao >= 0) {
                acoes.peaoClicado(peao);
                return;
            }
            String parada = CoordenadasMapa.paradaEm(x / s, e.getY() / s);
            if (parada != null) {
                acoes.paradaClicada(parada);
            }
        }
    }
}
