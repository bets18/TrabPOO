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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.HerdeirosDeKhanAPI;
import model.ObservadoIF;
import model.ObservadorIF;

// desenha o tabuleiro com drawImage e, por cima, o estado da partida: peões, yurts, khan,
// tributos das províncias, tesouros das cidades, melhorias e bônus. É um observador do
// Model: a cada aviso de mudança ele se redesenha. Também descobre onde o jogador clicou
class PainelTabuleiro extends JPanel implements ObservadorIF {
    private static final long serialVersionUID = 1L;
    private static final int LARGURA_EXIBIDA = 1000;
    private static final int RAIO_PEAO = 12;
    private static final Color DESTAQUE = new Color(255, 215, 0);
    private static final int LARGURA_DAS_PECAS = 160; // as imagens pequenas são lidas nesse tamanho

    private final HerdeirosDeKhanAPI modelo = HerdeirosDeKhanAPI.getInstancia();
    private final AcoesDoJogador acoes;
    private final BufferedImage tabuleiro;

    PainelTabuleiro(AcoesDoJogador acoes) {
        this.acoes = acoes;
        this.tabuleiro = Imagens.carregar("tabuleiro.jpg", LARGURA_EXIBIDA);
        setBackground(Color.DARK_GRAY);
        addMouseListener(new CliqueNoTabuleiro());
        modelo.adicionarObservador(this);
    }

    // qualquer mudança no estado da partida aparece no tabuleiro
    @Override
    public void notificar(ObservadoIF observado, String evento) {
        repaint();
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
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double s = escala();
        g2.translate(margemEsquerda(), 0);
        g2.drawImage(tabuleiro, 0, 0, (int) (CoordenadasMapa.LARGURA_IMAGEM * s),
                (int) (CoordenadasMapa.ALTURA_IMAGEM * s), null);
        desenharAreaDeMelhorias(g2, s);
        desenharConselheiros(g2, s);
        desenharCidades(g2, s);
        desenharProvincias(g2, s);
        desenharYurts(g2, s);
        desenharKhan(g2, s);
        desenharPeoes(g2, s);
    }

    // desenha a imagem dentro do retângulo {x1, y1, x2, y2} da imagem original
    private void desenharNoRetangulo(Graphics2D g2, String imagem, int[] r, double s) {
        BufferedImage peca = Imagens.carregar(imagem, LARGURA_DAS_PECAS);
        g2.drawImage(peca, (int) (r[0] * s), (int) (r[1] * s), (int) ((r[2] - r[0]) * s), (int) ((r[3] - r[1]) * s), null);
    }

    private void contornar(Graphics2D g2, int[] r, double s, float espessura) {
        g2.setColor(DESTAQUE);
        g2.setStroke(new BasicStroke(espessura));
        g2.draw(new RoundRectangle2D.Double(r[0] * s, r[1] * s, (r[2] - r[0]) * s, (r[3] - r[1]) * s, 8, 8));
    }

    //  melhorias e conselheiros

    private void desenharAreaDeMelhorias(Graphics2D g2, double s) {
        boolean gratis = modelo.isMelhoriaGratisPendente();
        for (int i = 0; i < HerdeirosDeKhanAPI.ESPACOS_DA_AREA_DE_MELHORIAS; i++) {
            int[] espaco = CoordenadasMapa.getEspacoDeMelhoria(i);
            String melhoria = modelo.getMelhoriaNaArea(i);
            if (melhoria != null) {
                desenharNoRetangulo(g2, Pecas.melhoria(melhoria), espaco, s);
                // contorno amarelo: dá pra pegar a melhoria grátis desta área
                if (gratis) {
                    contornar(g2, espaco, s, 2.5f);
                }
            }
        }
        int[] primeiro = CoordenadasMapa.getEspacoDeMelhoria(0);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Pilha: " + modelo.getTamanhoDaPilhaDeMelhorias(), (float) (primeiro[0] * s),
                (float) ((primeiro[1] - 12) * s));
    }

    // a peça de bônus fica no quadrado do pergaminho até alguém fazer a 1ª entrega.
    // Contorno amarelo nos conselheiros em que o jogador da vez parou neste turno
    private void desenharConselheiros(Graphics2D g2, double s) {
        for (String parada : CoordenadasMapa.getConselheiros()) {
            String bonus = modelo.getBonusDoConselheiro(parada);
            if (bonus != null) {
                desenharNoRetangulo(g2, Pecas.bonus(bonus), CoordenadasMapa.getQuadradoDoBonus(parada), s);
            }
            if (modelo.podeEntregarEm(parada)) {
                boolean selecionado = parada.equals(modelo.getConselheiroSelecionado());
                contornar(g2, CoordenadasMapa.getPergaminho(parada), s, selecionado ? 5f : 3f);
            }
        }
    }

    //  cidades e províncias

    private void desenharCidades(Graphics2D g2, double s) {
        for (String cidade : CoordenadasMapa.getCidades()) {
            int[] c = CoordenadasMapa.getCentroDaCidade(cidade);
            double x = c[0] * s, y = c[1] * s;
            String conquistador = modelo.getConquistadorDaCidade(cidade);
            if (!modelo.isCidadeRevelada(cidade)) {
                // ainda virada pra baixo na pilha de cidades
                double r = 120 * s;
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                centralizar(g2, "?", x, y);
            } else if (conquistador != null) {
                desenharYurt(g2, EstiloJogador.getCor(conquistador), x, y, 2.0);
            } else {
                desenharTesourosDaCidade(g2, cidade, x, y + 115 * s);
            }
            if (cidade.equals(modelo.getCidadeSelecionada())) {
                double r = 150 * s;
                g2.setColor(DESTAQUE);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
            }
        }
    }

    // fichas de tesouro lado a lado embaixo do nome da cidade
    private void desenharTesourosDaCidade(Graphics2D g2, String cidade, double x, double y) {
        List<String> pecas = new ArrayList<>();
        for (String tipo : Pecas.TESOUROS) {
            for (int i = 0; i < modelo.getTesourosNaCidade(cidade, tipo); i++) {
                pecas.add(tipo);
            }
        }
        int tamanho = 22;
        double inicio = x - pecas.size() * tamanho / 2.0;
        for (int i = 0; i < pecas.size(); i++) {
            BufferedImage ficha = Imagens.carregar(Pecas.tesouro(pecas.get(i)), 48);
            g2.drawImage(ficha, (int) (inicio + i * tamanho), (int) y, tamanho, tamanho, null);
        }
    }

    // número de peças de tributo ao lado do ícone (as do Khan não têm tributo)
    private void desenharProvincias(Graphics2D g2, double s) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (String provincia : CoordenadasMapa.getProvincias()) {
            if (modelo.isProvinciaDoKhan(provincia)) {
                continue;
            }
            int[] c = CoordenadasMapa.getCentroDaProvincia(provincia);
            int quantidade = modelo.getTributosNaProvincia(provincia);
            double x = (c[0] + 60) * s, y = (c[1] + 55) * s, r = 10;
            g2.setColor(quantidade > 0 ? new Color(40, 25, 10, 230) : new Color(110, 110, 110, 180));
            g2.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
            centralizar(g2, String.valueOf(quantidade), x, y);
        }
    }

    // peão branco do Genghis Khan, ao lado da cabeça onde ele está
    private void desenharKhan(Graphics2D g2, double s) {
        String posicao = modelo.getPosicaoDoKhan();
        if (posicao == null) {
            return; // ainda fora do tabuleiro
        }
        double x, y;
        if (posicao.equals(HerdeirosDeKhanAPI.AREA_DE_MELHORIAS)) {
            int[] r = CoordenadasMapa.getAreaDoKhan();
            x = (r[0] + r[2]) / 2.0 * s + 22;
            y = (r[1] + r[3]) / 2.0 * s;
        } else {
            int[] c = CoordenadasMapa.getCentroDaProvincia(posicao);
            x = (c[0] + 70) * s;
            y = (c[1] - 20) * s;
        }
        Path2D corpo = new Path2D.Double();
        corpo.moveTo(x - 9, y + 13);
        corpo.lineTo(x - 4, y - 2);
        corpo.lineTo(x + 4, y - 2);
        corpo.lineTo(x + 9, y + 13);
        corpo.closePath();
        Ellipse2D cabeca = new Ellipse2D.Double(x - 6, y - 13, 12, 12);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(Color.WHITE);
        g2.fill(corpo);
        g2.fill(cabeca);
        g2.setColor(Color.BLACK);
        g2.draw(corpo);
        g2.draw(cabeca);
    }

    //  yurts e peões

    // cada yurt é uma tendinha na cor do dono, um pouco acima da parada
    private void desenharYurts(Graphics2D g2, double s) {
        for (String parada : CoordenadasMapa.getNomes()) {
            List<String> donos = modelo.getDonosDeYurts(parada);
            int[][] pontos = CoordenadasMapa.getPontos(parada);
            for (int k = 0; k < donos.size(); k++) {
                double x = pontos[0][0] * s + (k - (donos.size() - 1) / 2.0) * 22;
                double y = pontos[0][1] * s - RAIO_PEAO - 16;
                desenharYurt(g2, EstiloJogador.getCor(donos.get(k)), x, y, 1.0);
            }
        }
    }

    private void desenharYurt(Graphics2D g2, Color cor, double x, double y, double tamanho) {
        Path2D tenda = new Path2D.Double();
        tenda.moveTo(x - 10 * tamanho, y + 8 * tamanho);
        tenda.lineTo(x - 10 * tamanho, y);
        tenda.lineTo(x, y - 9 * tamanho);
        tenda.lineTo(x + 10 * tamanho, y);
        tenda.lineTo(x + 10 * tamanho, y + 8 * tamanho);
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
        g2.setColor(Color.WHITE);
        centralizar(g2, inicial, x, y);
    }

    private static void centralizar(Graphics2D g2, String texto, double x, double y) {
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D caixa = fm.getStringBounds(texto, g2);
        g2.drawString(texto, (float) (x - caixa.getWidth() / 2), (float) (y + fm.getAscent() / 2.0 - 2));
    }

    // ordem: peão da Altani, pergaminho de conselheiro, área de melhorias, Khan da área,
    // parada, província e cidade. O botão direito numa parada constrói yurt
    private class CliqueNoTabuleiro extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            double s = escala();
            int x = e.getX() - margemEsquerda();
            double ox = x / s, oy = e.getY() / s; // coordenada na imagem original
            if (SwingUtilities.isRightMouseButton(e)) {
                String parada = CoordenadasMapa.paradaEm(ox, oy);
                if (parada != null) {
                    acoes.yurtClicado(parada);
                }
                return;
            }
            int peao = peaoInativoEm(x, e.getY(), s);
            if (peao >= 0) {
                acoes.peaoClicado(peao);
                return;
            }
            String conselheiro = CoordenadasMapa.conselheiroEm(ox, oy);
            if (conselheiro != null) {
                acoes.conselheiroClicado(conselheiro);
                return;
            }
            int espaco = CoordenadasMapa.espacoDeMelhoriaEm(ox, oy);
            if (espaco >= 0) {
                acoes.melhoriaDaAreaClicada(espaco);
                return;
            }
            if (CoordenadasMapa.naAreaDoKhan(ox, oy)) {
                acoes.areaDoKhanClicada();
                return;
            }
            String parada = CoordenadasMapa.paradaEm(ox, oy);
            if (parada != null) {
                acoes.paradaClicada(parada);
                return;
            }
            String provincia = CoordenadasMapa.provinciaEm(ox, oy);
            if (provincia != null) {
                acoes.provinciaClicada(provincia);
                return;
            }
            String cidade = CoordenadasMapa.cidadeEm(ox, oy);
            if (cidade != null) {
                acoes.cidadeClicada(cidade);
            }
        }
    }
}
