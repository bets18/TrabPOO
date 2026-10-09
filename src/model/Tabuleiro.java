package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Tabuleiro {
    Map<Local, List<Local>> conexoes = new HashMap<>();

    void adicionarConexao(Local a, Local b) {
        List<Local> vizinhosA = conexoes.get(a); // tenta buscar a lista com get()
        if (vizinhosA == null) { // verifica se retornou nulo (olhar slide 25 cap15)
            vizinhosA = new ArrayList<>();
            conexoes.put(a, vizinhosA); // insere no mapa com put() (olhar slide 22 cap15)
        }
        vizinhosA.add(b);

        List<Local> vizinhosB = conexoes.get(b);
        if (vizinhosB == null) {
            vizinhosB = new ArrayList<>();
            conexoes.put(b, vizinhosB);
        }
        vizinhosB.add(a);
    }

    boolean validarMovimento(Local origem, Local destino) {
        List<Local> vizinhos = conexoes.get(origem);
        return vizinhos != null && vizinhos.contains(destino);
    }
}
