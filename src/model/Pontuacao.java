package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// faz as contas que as cartas de conselheiro e a pontuação final precisam sobre um jogador.
// É criada na hora de conferir, então sempre reflete o estado atual da partida
class Pontuacao {
    private final Jogo jogo;
    private final Jogador jogador;
    private final Tabuleiro tabuleiro;

    Pontuacao(Jogo jogo, Jogador jogador) {
        this.jogo = jogo;
        this.jogador = jogador;
        this.tabuleiro = jogo.getTabuleiro();
    }

    //  peças guardadas

    int getEspadas() {
        return jogador.getQuantidadeTributo(TipoTributo.ESPADA);
    }

    // os tesouros virtuais das melhorias também valem pras cartas abertas, no turno do dono
    int getTesouros() {
        int total = 0;
        for (TipoTesouro tipo : TipoTesouro.values()) {
            total += getTesouros(tipo);
        }
        return total;
    }

    int getTiposDeTesouro() {
        int tipos = 0;
        for (TipoTesouro tipo : TipoTesouro.values()) {
            if (getTesouros(tipo) > 0) {
                tipos++;
            }
        }
        return tipos;
    }

    private int getTesouros(TipoTesouro tipo) {
        int virtuais = jogador == jogo.getJogadorDaVez() ? jogo.getTesourosVirtuais(tipo) : 0;
        return jogador.getQuantidadeTesouro(tipo) + virtuais;
    }

    int getBonusObtidos() {
        return jogador.getBonusObtidos();
    }

    int getCidadesConquistadas() {
        return jogador.getCidadesConquistadas();
    }

    //  yurts

    // inclui os das cidades conquistadas e o do Tolui em Karakorum
    int getYurtsNoMapa() {
        return Jogador.YURTS_INICIAIS - jogador.getYurtsNoEstoque();
    }

    // yurts nas paradas da região mais os das cidades conquistadas nela.
    // Karakorum não tem região, então o yurt do Tolui não entra
    int getYurtsNaRegiao(Regiao regiao) {
        int total = 0;
        for (Parada parada : tabuleiro.getLocais(Parada.class)) {
            if (parada.getRegiao() == regiao) {
                total += parada.getQuantidadeYurtsDe(jogador);
            }
        }
        for (Cidade cidade : tabuleiro.getLocais(Cidade.class)) {
            if (cidade.getRegiao() == regiao && cidade.getConquistador() == jogador) {
                total++;
            }
        }
        return total;
    }

    int getMenorQuantidadeDeYurtsPorRegiao() {
        int menor = Integer.MAX_VALUE;
        for (Regiao regiao : Regiao.values()) {
            menor = Math.min(menor, getYurtsNaRegiao(regiao));
        }
        return menor;
    }

    // maior grupo de yurts ligados por rotas (busca em largura só pelas paradas com yurt dele)
    int getMaiorRedeDeYurts() {
        Set<Parada> visitadas = new HashSet<>();
        int maior = 0;
        for (Parada inicio : tabuleiro.getLocais(Parada.class)) {
            if (visitadas.contains(inicio) || !inicio.possuiYurtDe(jogador)) {
                continue;
            }
            int yurtsDaRede = 0;
            List<Parada> fila = new ArrayList<>();
            fila.add(inicio);
            visitadas.add(inicio);
            while (!fila.isEmpty()) {
                Parada atual = fila.remove(0);
                yurtsDaRede += atual.getQuantidadeYurtsDe(jogador);
                for (Parada vizinha : tabuleiro.getParadasVizinhas(atual)) {
                    if (!visitadas.contains(vizinha) && vizinha.possuiYurtDe(jogador)) {
                        visitadas.add(vizinha);
                        fila.add(vizinha);
                    }
                }
            }
            maior = Math.max(maior, yurtsDaRede);
        }
        return maior;
    }

    // yurts nas paradas em volta de cada província do Khan; conta as que têm pelo menos o mínimo
    int getProvinciasDoKhanComYurts(int minimo) {
        int total = 0;
        for (Provincia provincia : tabuleiro.getLocais(Provincia.class)) {
            if (provincia.isDoKhan() && getYurtsAdjacentes(provincia) >= minimo) {
                total++;
            }
        }
        return total;
    }

    // o yurt de dentro da cidade conquistada não conta como adjacente
    int getCidadesComYurts(int minimo) {
        int total = 0;
        for (Cidade cidade : tabuleiro.getLocais(Cidade.class)) {
            if (getYurtsAdjacentes(cidade) >= minimo) {
                total++;
            }
        }
        return total;
    }

    private int getYurtsAdjacentes(Local local) {
        int total = 0;
        for (Parada parada : tabuleiro.getParadasAdjacentes(local)) {
            total += parada.getQuantidadeYurtsDe(jogador);
        }
        return total;
    }

    // paradas duplas (pontos de entrega fora Karakorum) com pelo menos 1 yurt dele
    int getParadasDuplasComYurt() {
        int total = 0;
        for (Parada parada : tabuleiro.getLocais(Parada.class)) {
            if (parada.getTipo() == TipoParada.DUPLA && parada.possuiYurtDe(jogador)) {
                total++;
            }
        }
        return total;
    }

    //  melhorias

    int getTotalDeMelhorias() {
        return jogador.getTodasAsMelhorias().size();
    }

    int getSimbolosDiferentes() {
        return contarSimbolos().size();
    }

    // dois iguais formam um par; 4 iguais são 2 pares
    int getParesDeMelhorias() {
        int pares = 0;
        for (int quantidade : contarSimbolos().values()) {
            pares += quantidade / 2;
        }
        return pares;
    }

    int getMelhoriasDeCavaloOuPunho() {
        int total = 0;
        for (Melhoria melhoria : jogador.getTodasAsMelhorias()) {
            if (melhoria.getTipo().temCavaloOuPunho()) {
                total++;
            }
        }
        return total;
    }

    private Map<TipoMelhoria, Integer> contarSimbolos() {
        Map<TipoMelhoria, Integer> simbolos = new HashMap<>();
        for (Melhoria melhoria : jogador.getTodasAsMelhorias()) {
            simbolos.put(melhoria.getTipo(), simbolos.getOrDefault(melhoria.getTipo(), 0) + 1);
        }
        return simbolos;
    }

    // linhas em que as 3 colunas coloridas já têm melhoria
    int getLinhasCompletas() {
        int menor = Integer.MAX_VALUE;
        for (Coluna coluna : Coluna.values()) {
            if (coluna.isColorida()) {
                menor = Math.min(menor, jogador.getQuantidadeDeMelhorias(coluna));
            }
        }
        return menor;
    }

    int getColunasComPeloMenos(int quantidade) {
        int total = 0;
        for (Coluna coluna : Coluna.values()) {
            if (jogador.getQuantidadeDeMelhorias(coluna) >= quantidade) {
                total++;
            }
        }
        return total;
    }

    int getMaiorColuna() {
        int maior = 0;
        for (Coluna coluna : Coluna.values()) {
            maior = Math.max(maior, jogador.getQuantidadeDeMelhorias(coluna));
        }
        return maior;
    }

    int getRegioesComDoisYurtsEDuasMelhorias() {
        int total = 0;
        for (Regiao regiao : Regiao.values()) {
            if (getYurtsNaRegiao(regiao) >= 2 && jogador.getQuantidadeDeMelhorias(regiao.getColuna()) >= 2) {
                total++;
            }
        }
        return total;
    }
}
