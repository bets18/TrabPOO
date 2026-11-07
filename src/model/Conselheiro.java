package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ponto de entrega numa parada (as 6 duplas e Karakorum). Cada entrega do conjunto pedido
// dá votos, e a 1ª também dá a peça de bônus que está em cima dele
class Conselheiro {
    private final Parada parada;
    private final List<TipoTesouro> pedido; // null na lista = tesouro de qualquer tipo
    private final int votos;
    private TipoBonus bonus = null;

    Conselheiro(Parada parada, int votos, TipoTesouro... pedido) {
        this.parada = parada;
        this.votos = votos;
        this.pedido = new ArrayList<>(Arrays.asList(pedido));
    }

    Parada getParada() {
        return parada;
    }

    int getVotos() {
        return votos;
    }

    List<TipoTesouro> getPedido() {
        return new ArrayList<>(pedido);
    }

    // o de 3 tesouros quaisquer: o jogador escolhe quais entrega
    boolean aceitaQualquer() {
        return pedido.contains(null);
    }

    TipoBonus getBonus() {
        return bonus;
    }

    void colocarBonus(TipoBonus bonus) {
        this.bonus = bonus;
    }

    // devolve o bônus (ou null se alguém já levou) e deixa o lugar vazio
    TipoBonus retirarBonus() {
        TipoBonus retirado = bonus;
        bonus = null;
        return retirado;
    }

    // os tesouros oferecidos são exatamente o que ele pede?
    boolean atende(List<TipoTesouro> tesouros) {
        if (tesouros.size() != pedido.size() || tesouros.contains(null)) {
            return false;
        }
        if (aceitaQualquer()) {
            return true;
        }
        return contar(tesouros).equals(contar(pedido));
    }

    static Map<TipoTesouro, Integer> contar(List<TipoTesouro> tesouros) {
        Map<TipoTesouro, Integer> contagem = new HashMap<>();
        for (TipoTesouro tipo : tesouros) {
            contagem.put(tipo, contagem.getOrDefault(tipo, 0) + 1);
        }
        return contagem;
    }
}
