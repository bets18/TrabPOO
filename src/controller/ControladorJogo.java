package controller;

import java.util.List;

import model.HerdeirosDeKhanAPI;
import view.AcoesDoJogador;
import view.JanelaCartaSecreta;
import view.JanelaInicial;
import view.JanelaJogo;
import view.SeletorDeArquivo;

// organiza a sequência da partida: nomes e herdeiros dos jogadores (ou uma partida salva),
// escolha das cartas secretas e depois os turnos. Recebe os cliques da View, decide qual jogada do Model eles significam no momento
// e pede ao Model. Não redesenha nada: quem atualiza a tela são as janelas, que observam o
// Model. Jogada inválida não faz nada (sem mensagem), como o enunciado pede
public class ControladorJogo implements AcoesDoJogador {
    private final HerdeirosDeKhanAPI modelo = HerdeirosDeKhanAPI.getInstancia();
    private JanelaJogo janela = null;

    // sem herdeiros pelos argumentos, abre a janela inicial pra escolher
    public void iniciar(List<String> herdeirosPelosArgumentos) {
        if (herdeirosPelosArgumentos == null || herdeirosPelosArgumentos.isEmpty()) {
            prepararPelaJanelaInicial();
        } else {
            modelo.novaPartida(herdeirosPelosArgumentos);
            escolherCartasSecretas();
        }

        janela = new JanelaJogo(this);
        janela.setVisible(true);
        // 1º passo do turno: a cartela aparece pra escolher a coluna
        modelo.exibirCartela();
    }

    // a janela inicial volta enquanto o arquivo escolhido não puder ser carregado
    private void prepararPelaJanelaInicial() {
        String aviso = null;
        while (!modelo.temPartida()) {
            JanelaInicial inicial = JanelaInicial.abrir(aviso);
            if (inicial.getArquivoParaCarregar() != null) {
                // a partida salva já tem as cartas secretas escolhidas
                if (!modelo.carregarPartida(inicial.getArquivoParaCarregar())) {
                    aviso = "Esse arquivo não é uma partida salva válida";
                }
            } else if (inicial.isConfirmada()) {
                modelo.novaPartida(inicial.getHerdeiros(), inicial.getNomes());
                escolherCartasSecretas();
            } else {
                System.exit(0); // fecharam a janela inicial
            }
        }
    }

    // antes da 1ª rodada cada um fica com 1 das 2 cartas de conselheiro secreto
    private void escolherCartasSecretas() {
        for (int i = 0; i < modelo.getNumeroDeJogadores(); i++) {
            String carta = JanelaCartaSecreta.escolher(modelo.getNomeDoJogador(i), modelo.getHerdeiro(i),
                    modelo.getCartasSecretasOferecidas(i));
            modelo.escolherCartaSecreta(i, carta);
        }
    }

    //  cartela

    // se o Jochi tem melhoria esperando, o clique escolhe a coluna dela; senão ativa a coluna
    @Override
    public void colunaClicada(String coluna) {
        if (modelo.getMelhoriaParaPosicionar() != null) {
            modelo.posicionarMelhoria(coluna);
        } else {
            modelo.ativarColuna(coluna);
        }
    }

    //  mapa

    // a parada clicada é o destino; quem decide se dá pra ir é o Model
    @Override
    public void paradaClicada(String parada) {
        modelo.moverPara(parada);
    }

    @Override
    public void yurtClicado(String parada) {
        modelo.construirYurt(parada);
    }

    // clicou no outro peão da Altani: ela passa a jogar com ele
    @Override
    public void peaoClicado(int peao) {
        modelo.selecionarPeao(peao);
    }

    // cabeça do Khan move o Khan; ícone de tributo usa um punho
    @Override
    public void provinciaClicada(String provincia) {
        if (modelo.isProvinciaDoKhan(provincia)) {
            modelo.moverKhan(provincia);
        } else {
            modelo.pegarTributo(provincia);
        }
    }

    @Override
    public void areaDoKhanClicada() {
        modelo.moverKhanParaAreaDeMelhorias();
    }

    // a cidade vira o alvo e a janela de cartas mostra os tesouros dela
    @Override
    public void cidadeClicada(String cidade) {
        modelo.selecionarCidade(cidade);
    }

    // conselheiro de pedido fixo: entrega na hora. O de 3 quaisquer: escolhe na janela de cartas
    @Override
    public void conselheiroClicado(String parada) {
        if (modelo.getPedidoDoConselheiro(parada).contains(HerdeirosDeKhanAPI.QUALQUER_TESOURO)) {
            modelo.selecionarConselheiro(parada);
        } else {
            modelo.entregarTesouros(parada);
        }
    }

    //  melhorias

    @Override
    public void melhoriaDaAreaClicada(int espaco) {
        pegarMelhoria(espaco);
    }

    @Override
    public void pilhaDeMelhoriasClicada() {
        pegarMelhoria(HerdeirosDeKhanAPI.TOPO_DA_PILHA);
    }

    // depois de completar uma linha a próxima melhoria é grátis; senão é compra
    private void pegarMelhoria(int espaco) {
        if (modelo.isMelhoriaGratisPendente()) {
            modelo.escolherMelhoriaGratis(espaco);
        } else {
            modelo.comprarMelhoria(espaco);
        }
    }

    //  janela de cartas

    // o mesmo botão de tesouro serve pra 3 escolhas; a que estiver em aberto decide
    @Override
    public void tesouroEscolhido(String tesouro) {
        if (!modelo.getTesourosParaEscolher().isEmpty()) {
            modelo.escolherTesouroDoBonus(tesouro);
        } else if (modelo.getConselheiroSelecionado() != null) {
            modelo.adicionarTesouroAEntrega(tesouro);
        } else if (modelo.getCidadeSelecionada() != null) {
            modelo.atacarCidade(modelo.getCidadeSelecionada(), tesouro);
        }
    }

    @Override
    public void tesouroComMoedaEscolhido(String tesouro) {
        if (modelo.getCidadeSelecionada() != null) {
            modelo.pegarTesouroComMoeda(modelo.getCidadeSelecionada(), tesouro);
        }
    }

    @Override
    public void bonusClicado(String bonus) {
        modelo.usarBonus(bonus);
    }

    @Override
    public void cancelarSelecaoClicado() {
        modelo.cancelarSelecao();
    }

    //  painel do jogador

    @Override
    public void habilidadeClicada() {
        modelo.trocarCavaloPorEspada();
    }

    @Override
    public void verCartelaClicado() {
        modelo.exibirCartela();
    }

    @Override
    public void verCartasClicado() {
        modelo.exibirCartas();
    }

    @Override
    public void passarVezClicado() {
        modelo.passarVez();
    }

    //  salvar e carregar

    // o Model só deixa salvar no começo da vez; a View escolhe o arquivo e o Model grava
    @Override
    public void salvarPartidaClicado() {
        if (!modelo.podeSalvar()) {
            return;
        }
        String caminho = SeletorDeArquivo.escolherParaSalvar(janela);
        if (caminho != null && !modelo.salvarPartida(caminho)) {
            janela.avisar("Não foi possível gravar o arquivo");
        }
    }

    // a partida do arquivo entra no lugar da atual e continua do começo da vez em que foi salva
    @Override
    public void carregarPartidaClicado() {
        String caminho = SeletorDeArquivo.escolherParaAbrir(janela);
        if (caminho == null) {
            return;
        }
        if (modelo.carregarPartida(caminho)) {
            modelo.exibirCartela();
        } else {
            janela.avisar("Esse arquivo não é uma partida salva válida");
        }
    }
}
