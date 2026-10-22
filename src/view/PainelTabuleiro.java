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

// desenha o tabuleiro (drawImage do Graphics2D) e os peões (formas do Java2D), e transforma
// um clique na imagem no nome da parada clicada
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

    // mesma escala nos dois eixos, para caber no painel sem distorcer
    private double escala() {
        return Math.min(getWidth() / (double) CoordenadasMapa.LARGURA_IMAGEM,
                getHeight() / (double) CoordenadasMapa.ALTURA_IMAGEM);
    }

    // o tabuleiro fica centralizado na horizontal quando sobra largura
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

    // yurts dos jogadores: uma tenda na cor do dono, acima da parada (Java2D)
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
        // agrupa os peões pela parada onde estão, para não desenhar um por cima do outro;
        // cada peão é o par {jogador, índice do peão} (Altani tem 2)
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
                    // espalha os peões lado a lado em volta do ponto
                    double deslocamento = (k - (jogadores.size() - 1) / 2.0) * (RAIO_PEAO * 2 + 2);
                    x = pontos[0][0] * s + deslocamento;
                    y = pontos[0][1] * s;
                }
                desenharPeao(g2, jogadores.get(k)[0], jogadores.get(k)[1], x, y);
            }
        }
    }

    private void desenharPeao(Graphics2D g2, int jogador, int peao, double x, double y) {
        String herdeiro = modelo.getHerdeiro(jogador);
        Ellipse2D corpo = new Ellipse2D.Double(x - RAIO_PEAO, y - RAIO_PEAO, RAIO_PEAO * 2, RAIO_PEAO * 2);
        // destaque só no peão que o jogador da vez vai mover
        if (jogador == modelo.getIndiceJogadorDaVez() && peao == modelo.getPeaoAtivo(jogador)) {
            g2.setColor(DESTAQUE);
            g2.fill(new Ellipse2D.Double(x - RAIO_PEAO - 5, y - RAIO_PEAO - 5, RAIO_PEAO * 2 + 10, RAIO_PEAO * 2 + 10));
        }
        g2.setColor(EstiloJogador.getCor(herdeiro));
        g2.fill(corpo);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(corpo);

        // com 2 peões, cada um leva o número: A1, A2
        String inicial = herdeiro.substring(0, 1) + (modelo.getNumeroDePeoes(jogador) > 1 ? String.valueOf(peao + 1) : "");
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(inicial, (float) (x - fm.stringWidth(inicial) / 2.0), (float) (y + fm.getAscent() / 2.0 - 2));
    }

    // tratamento do clique: converte o ponto da tela para a imagem original e pergunta qual parada é
    private class CliqueNoTabuleiro extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            double s = escala();
            String parada = CoordenadasMapa.paradaEm((e.getX() - margemEsquerda()) / s, e.getY() / s);
            if (parada != null) {
                acoes.paradaClicada(parada);
            }
        }
    }
}
