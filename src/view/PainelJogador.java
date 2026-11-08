package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import model.HerdeirosDeKhanAPI;
import model.ObservadoIF;
import model.ObservadorIF;

// painel da direita: faixa com a cor do jogador da vez e o que ele pode fazer agora,
// os jogadores e seus votos, as cartas de conselheiro aberto e os botões do turno.
// Observa o Model e se atualiza a cada mudança
class PainelJogador extends JPanel implements ObservadorIF {
    private static final long serialVersionUID = 1L;
    static final int LARGURA = 384;

    private static final Color FUNDO = new Color(245, 236, 215);
    private static final int ALTURA_DA_FAIXA = 196;
    private static final int LARGURA_MAXIMA_DA_CARTA = 84;
    private static final double PROPORCAO_DA_CARTA = 1.6; // altura / largura

    private final HerdeirosDeKhanAPI modelo = HerdeirosDeKhanAPI.getInstancia();
    private final JButton botaoCartela = new JButton("Ver cartela (colunas)");
    private final JButton botaoCartas = new JButton("Ver peças e cartas");
    private final JButton botaoPilha = new JButton("Melhoria do topo da pilha");
    private final JButton botaoHabilidade = new JButton("Trocar 1 cavalo por 1 espada");
    private final JButton botaoPassarVez = new JButton("Passar a vez");
    private final Faixa faixa = new Faixa();
    private final Informacoes informacoes = new Informacoes();
    private final JPanel botoes = new JPanel(new GridLayout(0, 1, 0, 6));

    PainelJogador(final AcoesDoJogador acoes) {
        setLayout(new BorderLayout());
        setBackground(FUNDO);
        faixa.setPreferredSize(new Dimension(LARGURA, ALTURA_DA_FAIXA));
        add(faixa, BorderLayout.NORTH);
        add(informacoes, BorderLayout.CENTER);

        // nenhum evento usa lambda: cada botão tem o seu ActionListener
        botaoCartela.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acoes.verCartelaClicado();
            }
        });
        botaoCartas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acoes.verCartasClicado();
            }
        });
        botaoPilha.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acoes.pilhaDeMelhoriasClicada();
            }
        });
        botaoHabilidade.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acoes.habilidadeClicada();
            }
        });
        botaoPassarVez.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acoes.passarVezClicado();
            }
        });
        botaoPassarVez.setFont(new Font("SansSerif", Font.BOLD, 16));

        botoes.setOpaque(false);
        botoes.setBorder(BorderFactory.createEmptyBorder(6, 20, 12, 20));
        botoes.add(botaoCartela);
        botoes.add(botaoCartas);
        botoes.add(botaoPilha);
        botoes.add(botaoPassarVez);
        add(botoes, BorderLayout.SOUTH);

        modelo.adicionarObservador(this);
        atualizar();
    }

    @Override
    public void notificar(ObservadoIF observado) {
        atualizar();
    }

    private void atualizar() {
        boolean emJogo = !modelo.isJogoTerminado();
        botaoPassarVez.setEnabled(emJogo && modelo.podePassarVez());
        botaoPilha.setText(modelo.isMelhoriaGratisPendente() ? "Melhoria grátis do topo da pilha"
                : "Comprar melhoria do topo da pilha (2 moedas)");
        botaoPilha.setEnabled(emJogo && modelo.getTamanhoDaPilhaDeMelhorias() > 0);
        // só o Chagatai tem essa habilidade de botão (a do Ogedei fica na janela de cartas);
        // o botão entra antes do "Passar a vez" só na vez dele, pra não deixar buraco
        boolean chagatai = modelo.getHerdeiroDoJogadorDaVez().equals("CHAGATAI");
        if (chagatai && botaoHabilidade.getParent() == null) {
            botoes.add(botaoHabilidade, botoes.getComponentCount() - 1);
        } else if (!chagatai && botaoHabilidade.getParent() != null) {
            botoes.remove(botaoHabilidade);
        }
        botoes.revalidate();
        botaoHabilidade.setEnabled(emJogo && modelo.isHabilidadeDisponivel() && modelo.getMovimentosRestantes() > 0);
        repaint();
    }

    // o que o jogador da vez precisa fazer, na ordem em que as coisas travam o turno
    private String instrucao() {
        if (modelo.isJogoTerminado()) {
            return "Fim de jogo!";
        }
        if (!modelo.getTesourosParaEscolher().isEmpty()) {
            return "Escolha o tesouro do bônus na janela de peças e cartas";
        }
        if (modelo.getMelhoriaParaPosicionar() != null) {
            return "Escolha na cartela a coluna da sua nova melhoria";
        }
        if (modelo.isMelhoriaGratisPendente()) {
            return "Linha completa! Escolha uma melhoria grátis na área de melhorias";
        }
        if (modelo.getColunaDoTurno() == null) {
            return "Clique numa coluna da sua cartela";
        }
        if (modelo.isKhanPendente()) {
            return "Mova o Khan: clique numa cabeça do Khan no mapa";
        }
        if (modelo.getMovimentosRestantes() > 0) {
            return "Clique na parada de destino ou faça suas ações";
        }
        return "Faça suas ações ou passe a vez";
    }

    // quebra o texto em linhas que caibam na largura
    private static List<String> quebrar(String texto, FontMetrics fm, int largura) {
        List<String> linhas = new ArrayList<>();
        StringBuilder linha = new StringBuilder();
        for (String palavra : texto.split(" ")) {
            String tentativa = linha.length() == 0 ? palavra : linha + " " + palavra;
            if (fm.stringWidth(tentativa) > largura && linha.length() > 0) {
                linhas.add(linha.toString());
                linha = new StringBuilder(palavra);
            } else {
                linha = new StringBuilder(tentativa);
            }
        }
        linhas.add(linha.toString());
        return linhas;
    }

    private static void suavizar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    // faixa na cor do jogador da vez
    private class Faixa extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            suavizar(g2);
            String herdeiro = modelo.getHerdeiroDoJogadorDaVez();
            int jogador = modelo.getIndiceJogadorDaVez();
            g2.setColor(EstiloJogador.getCor(herdeiro));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 26));
            g2.drawString("Vez de " + EstiloJogador.getNome(herdeiro), 14, 36);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            String rodada = modelo.isUltimaRodada() ? "  -  última rodada!" : "";
            g2.drawString("Jogador " + (jogador + 1) + " de " + modelo.getNumeroDeJogadores() + rodada, 14, 60);

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            int y = 88;
            for (String linha : quebrar(instrucao(), g2.getFontMetrics(), getWidth() - 28)) {
                g2.drawString(linha, 14, y);
                y += 20;
            }
            g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
            String coluna = modelo.getColunaDoTurno() == null ? "-" : modelo.getColunaDoTurno().toLowerCase();
            g2.drawString("Coluna: " + coluna + "   Movimentos: " + modelo.getMovimentosRestantes()
                    + "   Punhos: " + modelo.getPunhosRestantes(), 14, y + 8);
            if (modelo.getNumeroDePeoes(jogador) > 1) {
                g2.drawString("Peão " + (modelo.getPeaoAtivo(jogador) + 1) + " ativo (clique no outro para trocar)", 14, y + 30);
            }
            g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
            g2.drawString("Botão direito numa parada: construir yurt", 14, getHeight() - 8);
        }
    }

    // jogadores, votos no conselho e as cartas abertas
    private class Informacoes extends JPanel {
        private static final long serialVersionUID = 1L;

        Informacoes() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            suavizar(g2);
            int y = 24;
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("Jogadores", 14, y);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            for (int i = 0; i < modelo.getNumeroDeJogadores(); i++) {
                y += 24;
                String herdeiro = modelo.getHerdeiro(i);
                g2.setColor(EstiloJogador.getCor(herdeiro));
                g2.fillOval(14, y - 14, 18, 18);
                g2.setColor(Color.BLACK);
                g2.drawOval(14, y - 14, 18, 18);
                StringBuilder posicoes = new StringBuilder(modelo.getPosicaoDoPeao(i, 0));
                for (int peao = 1; peao < modelo.getNumeroDePeoes(i); peao++) {
                    posicoes.append(" e ").append(modelo.getPosicaoDoPeao(i, peao));
                }
                String marca = i == modelo.getIndiceJogadorDaVez() ? " (da vez)" : "";
                g2.drawString(EstiloJogador.getNome(herdeiro) + ": " + modelo.getVotos(i) + " votos, em "
                        + posicoes + marca, 40, y);
            }

            y += 30;
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("Conselho Kurultai: " + modelo.getVotosNoConselho() + " de "
                    + modelo.getVotosParaFimDeJogo() + " votos", 14, y);

            y += 26;
            g2.drawString("Cartas de conselheiro aberto", 14, y);
            y += 6;
            List<String> abertas = modelo.getCartasAbertas();
            if (abertas.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.drawString("Todas já foram cumpridas", 14, y + 20);
            }
            // em tela baixa as cartas encolhem pra caber no espaço que sobra
            int largura = (int) Math.min(LARGURA_MAXIMA_DA_CARTA, (getHeight() - y - 6) / PROPORCAO_DA_CARTA);
            if (largura < 20) {
                return;
            }
            for (int i = 0; i < abertas.size(); i++) {
                BufferedImage carta = Imagens.carregar(Pecas.cartaAberta(abertas.get(i)), largura);
                g2.drawImage(carta, 14 + i * (largura + 6), y, null);
            }
        }
    }
}
