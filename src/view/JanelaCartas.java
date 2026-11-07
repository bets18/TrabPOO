package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import model.HerdeirosDeKhanAPI;
import model.ObservadoIF;
import model.ObservadorIF;

// janela de peças e cartas do jogador da vez: tributos, tesouros, bônus guardados, votos e a
// carta de conselheiro secreto, todos desenhados com drawImage. Embaixo fica a escolha em
// andamento (tesouro do ataque, da entrega ou do bônus), que se faz clicando nas fichas.
// É um observador: se redesenha a cada mudança e abre sozinha quando começa uma escolha ou
// quando pedem pra ver as cartas. Fecha pelo botão "Fechar"
class JanelaCartas extends JDialog implements ObservadorIF {
    private static final long serialVersionUID = 1L;
    private static final int TAMANHO_FICHA = 40;
    private static final int TAMANHO_BONUS = 48;
    private static final int LARGURA_CARTA = 150;
    private static final int LARGURA = 470;
    private static final int ALTURA = 640;
    private static final Color FUNDO = new Color(245, 236, 215);

    // o que um clique numa região desenhada faz
    private static final int ESCOLHER_TESOURO = 0;
    private static final int PEGAR_COM_MOEDA = 1;
    private static final int USAR_BONUS = 2;

    private final HerdeirosDeKhanAPI modelo = HerdeirosDeKhanAPI.getInstancia();
    private final AcoesDoJogador acoes;
    private final Desenho desenho = new Desenho();
    private final JButton cancelar = new JButton("Cancelar escolha");
    // regiões clicáveis do último desenho; refeitas a cada paintComponent
    private final List<Regiao> regioes = new ArrayList<>();

    JanelaCartas(Frame dona, AcoesDoJogador acoes) {
        super(dona, "Peças e cartas", false);
        this.acoes = acoes;
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        desenho.setPreferredSize(new Dimension(LARGURA, ALTURA));
        desenho.setBackground(FUNDO);
        desenho.addMouseListener(new CliqueNaJanela());

        cancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JanelaCartas.this.acoes.cancelarSelecaoClicado();
            }
        });
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(cancelar);
        rodape.add(fechar);

        JScrollPane rolagem = new JScrollPane(desenho);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        rolagem.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(rolagem, BorderLayout.CENTER);
        getContentPane().add(rodape, BorderLayout.SOUTH);
        setSize(new Dimension(LARGURA + 30, Math.min(ALTURA + 80, dona.getHeight() - 60)));
        // à direita da janela do jogo, por cima do painel do jogador
        setLocation(dona.getX() + dona.getWidth() - getWidth() - 10, dona.getY() + 40);
        modelo.adicionarObservador(this);
        atualizar();
    }

    @Override
    public void notificar(ObservadoIF observado, String evento) {
        atualizar();
        // abre quando começa uma escolha (cidade, entrega de 3 quaisquer, tesouro do bônus)
        // ou quando pedem pra ver as cartas; nas outras mudanças só redesenha
        boolean comecouEscolha = evento.equals(HerdeirosDeKhanAPI.EVENTO_CIDADE_SELECIONADA)
                || evento.equals(HerdeirosDeKhanAPI.EVENTO_CONSELHEIRO_SELECIONADO)
                || (evento.equals(HerdeirosDeKhanAPI.EVENTO_BONUS_USADO) && !modelo.getTesourosParaEscolher().isEmpty());
        if (evento.equals(HerdeirosDeKhanAPI.EVENTO_JOGO_TERMINADO)) {
            setVisible(false);
        } else if (evento.equals(HerdeirosDeKhanAPI.EVENTO_JANELA_DE_CARTAS) || comecouEscolha) {
            setVisible(true);
            toFront();
        }
    }

    private void atualizar() {
        setTitle("Peças e cartas de " + EstiloJogador.getNome(modelo.getHerdeiroDoJogadorDaVez()));
        cancelar.setVisible(modelo.getCidadeSelecionada() != null || modelo.getConselheiroSelecionado() != null);
        desenho.repaint();
    }

    // retângulo clicável e o que ele faz
    private static class Regiao {
        private final Rectangle retangulo;
        private final int acao;
        private final String valor;

        Regiao(Rectangle retangulo, int acao, String valor) {
            this.retangulo = retangulo;
            this.acao = acao;
            this.valor = valor;
        }
    }

    private class Desenho extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            regioes.clear();
            int jogador = modelo.getIndiceJogadorDaVez();
            String herdeiro = modelo.getHerdeiro(jogador);

            g2.setColor(EstiloJogador.getCor(herdeiro));
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            g2.drawString(EstiloJogador.getNome(herdeiro) + " - " + modelo.getVotos(jogador) + " votos", 12, 28);

            // tributos (as virtuais só valem neste turno)
            titulo(g2, "Tributos", 56);
            for (int i = 0; i < Pecas.TRIBUTOS.size(); i++) {
                String tipo = Pecas.TRIBUTOS.get(i);
                int virtuais = modelo.getTributosVirtuais(tipo);
                ficha(g2, Pecas.tributo(tipo), 12 + i * 90, 64, TAMANHO_FICHA,
                        "x" + modelo.getQuantidadeTributo(tipo) + (virtuais > 0 ? " +" + virtuais + "v" : ""));
            }
            texto(g2, "Yurts no estoque: " + modelo.getYurtsNoEstoque(jogador), 12, 138, 13, false);
            texto(g2, "Cidades conquistadas: " + modelo.getCidadesConquistadas(jogador), 12, 156, 13, false);

            titulo(g2, "Tesouros", 184);
            for (int i = 0; i < Pecas.TESOUROS.size(); i++) {
                String tipo = Pecas.TESOUROS.get(i);
                int virtuais = modelo.getTesourosVirtuais(tipo);
                ficha(g2, Pecas.tesouro(tipo), 12 + i * 58, 192, TAMANHO_FICHA,
                        "x" + modelo.getQuantidadeTesouro(jogador, tipo) + (virtuais > 0 ? "+" + virtuais + "v" : ""));
            }
            texto(g2, "(v = virtual, só neste turno)", 12, 262, 11, false);

            // carta de conselheiro secreto, à direita
            String secreta = modelo.getCartaSecreta(jogador);
            if (secreta != null) {
                texto(g2, "Carta secreta: " + Pecas.nomeDaCartaSecreta(secreta), 305, 56, 13, true);
                g2.drawImage(Imagens.carregar(Pecas.cartaSecreta(secreta), LARGURA_CARTA), 305, 64, null);
            }

            // bônus guardados: clicar usa
            titulo(g2, "Bônus guardados (clique para usar)", 312);
            List<String> bonus = modelo.getBonusGuardados(jogador);
            if (bonus.isEmpty()) {
                texto(g2, "Nenhum", 12, 336, 13, false);
            }
            for (int i = 0; i < bonus.size(); i++) {
                Rectangle r = new Rectangle(12 + i * (TAMANHO_BONUS + 8), 320, TAMANHO_BONUS, TAMANHO_BONUS);
                g2.drawImage(Imagens.carregar(Pecas.bonus(bonus.get(i)), TAMANHO_BONUS * 2), r.x, r.y, r.width, r.height, null);
                regioes.add(new Regiao(r, USAR_BONUS, bonus.get(i)));
            }

            desenharEscolha(g2, herdeiro, 400);
        }

        // o que está sendo escolhido agora: tesouro do bônus, entrega de 3 quaisquer ou ataque
        private void desenharEscolha(Graphics2D g2, String herdeiro, int y) {
            List<String> doBonus = modelo.getTesourosParaEscolher();
            String conselheiro = modelo.getConselheiroSelecionado();
            String cidade = modelo.getCidadeSelecionada();
            if (!doBonus.isEmpty()) {
                titulo(g2, "Bônus: clique no tesouro que vai ficar com você", y);
                opcoes(g2, doBonus, null, ESCOLHER_TESOURO, y + 10);
            } else if (conselheiro != null) {
                List<String> escolhidos = modelo.getTesourosEmEntrega();
                int faltam = modelo.getPedidoDoConselheiro(conselheiro).size() - escolhidos.size();
                titulo(g2, "Entrega em " + conselheiro + ": clique em mais " + faltam + " tesouro(s)", y);
                if (!escolhidos.isEmpty()) {
                    texto(g2, "Escolhidos: " + nomes(escolhidos), 12, y + 18, 12, false);
                }
                List<String> disponiveis = new ArrayList<>();
                for (String tipo : Pecas.TESOUROS) {
                    if (modelo.getQuantidadeTesouro(modelo.getIndiceJogadorDaVez(), tipo) + modelo.getTesourosVirtuais(tipo) > 0) {
                        disponiveis.add(tipo);
                    }
                }
                opcoes(g2, disponiveis, null, ESCOLHER_TESOURO, y + 26);
            } else if (cidade != null) {
                int custo = modelo.getCustoDoProximoAtaque(cidade);
                titulo(g2, "Atacar " + Pecas.nomeDaCidade(cidade) + ": o próximo tesouro custa " + custo
                        + (custo == 1 ? " espada" : " espadas"), y);
                texto(g2, "É preciso ter feito uma parada ao lado da cidade neste turno", 12, y + 18, 12, false);
                List<String> tipos = new ArrayList<>();
                int total = 0;
                for (String tipo : Pecas.TESOUROS) {
                    int quantidade = modelo.getTesourosNaCidade(cidade, tipo);
                    total += quantidade;
                    if (quantidade > 0) {
                        tipos.add(tipo);
                    }
                }
                opcoes(g2, tipos, cidade, ESCOLHER_TESOURO, y + 26);
                // habilidade do Ogedei: 1 moeda por um tesouro de qualquer cidade, menos o último
                if (herdeiro.equals("OGEDEI") && modelo.isHabilidadeDisponivel() && total >= 2) {
                    titulo(g2, "Ou pague 1 moeda (habilidade do Ogedei, mesmo longe):", y + 110);
                    opcoes(g2, tipos, cidade, PEGAR_COM_MOEDA, y + 120);
                }
            } else {
                texto(g2, "Clique numa cidade do mapa para atacá-la", 12, y, 13, false);
            }
        }

        // fichas de tesouro clicáveis; com cidade, mostra quantas ela tem de cada
        private void opcoes(Graphics2D g2, List<String> tipos, String cidade, int acao, int y) {
            for (int i = 0; i < tipos.size(); i++) {
                String tipo = tipos.get(i);
                String legenda = Pecas.nomeDoTesouro(tipo);
                if (cidade != null) {
                    legenda += " x" + modelo.getTesourosNaCidade(cidade, tipo);
                }
                Rectangle r = ficha(g2, Pecas.tesouro(tipo), 12 + i * 80, y, 48, legenda);
                regioes.add(new Regiao(r, acao, tipo));
            }
        }

        // desenha a ficha e a legenda embaixo; devolve o retângulo da imagem
        private Rectangle ficha(Graphics2D g2, String imagem, int x, int y, int tamanho, String legenda) {
            BufferedImage peca = Imagens.carregar(imagem, tamanho * 2);
            g2.drawImage(peca, x, y, tamanho, tamanho, null);
            texto(g2, legenda, x, y + tamanho + 16, 13, true);
            return new Rectangle(x, y, tamanho, tamanho);
        }

        private void titulo(Graphics2D g2, String texto, int y) {
            texto(g2, texto, 12, y, 15, true);
        }

        private void texto(Graphics2D g2, String texto, int x, int y, int tamanho, boolean negrito) {
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", negrito ? Font.BOLD : Font.PLAIN, tamanho));
            g2.drawString(texto, x, y);
        }
    }

    private static String nomes(List<String> tesouros) {
        StringBuilder texto = new StringBuilder();
        for (String tipo : tesouros) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(Pecas.nomeDoTesouro(tipo));
        }
        return texto.toString();
    }

    // vê em qual ficha ou bônus o clique caiu
    private class CliqueNaJanela extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            for (Regiao regiao : new ArrayList<>(regioes)) {
                if (!regiao.retangulo.contains(e.getPoint())) {
                    continue;
                }
                if (regiao.acao == ESCOLHER_TESOURO) {
                    acoes.tesouroEscolhido(regiao.valor);
                } else if (regiao.acao == PEGAR_COM_MOEDA) {
                    acoes.tesouroComMoedaEscolhido(regiao.valor);
                } else {
                    acoes.bonusClicado(regiao.valor);
                }
                return;
            }
        }
    }
}
