package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// grafo do mapa: rotas entre paradas, e quais cidades/províncias ficam adjacentes a cada parada
class Tabuleiro {
    private final Map<String, Local> locais = new HashMap<>();
    private final Map<Parada, List<Parada>> rotas = new HashMap<>();
    private final Map<Parada, List<Local>> vizinhancas = new HashMap<>();
    private final Map<Provincia, List<Provincia>> afetadasPeloKhan = new HashMap<>();
    private Parada karakorum;

    void adicionarLocal(Local local) {
        if (locais.containsKey(local.getNome())) {
            throw new IllegalArgumentException("Já existe um local chamado " + local.getNome() + ".");
        }
        locais.put(local.getNome(), local);
        if (local instanceof Parada && ((Parada) local).getTipo() == TipoParada.KARAKORUM) {
            karakorum = (Parada) local;
        }
    }

    Local getLocal(String nome) {
        return locais.get(nome);
    }

    Parada getKarakorum() {
        return karakorum;
    }

    List<Provincia> getProvincias() {
        List<Provincia> provincias = new ArrayList<>();
        for (Local local : locais.values()) {
            if (local instanceof Provincia) {
                provincias.add((Provincia) local);
            }
        }
        return provincias;
    }

    // rotas são de mão dupla
    void adicionarRota(Parada a, Parada b) {
        adicionarNaLista(rotas, a, b);
        adicionarNaLista(rotas, b, a);
    }

    boolean existeRota(Parada origem, Parada destino) {
        return getParadasVizinhas(origem).contains(destino);
    }

    List<Parada> getParadasVizinhas(Parada parada) {
        List<Parada> vizinhas = rotas.get(parada);
        return vizinhas == null ? new ArrayList<Parada>() : vizinhas;
    }

    void adicionarVizinhanca(Parada parada, Local local) {
        adicionarNaLista(vizinhancas, parada, local);
    }

    boolean saoVizinhos(Parada parada, Local local) {
        List<Local> vizinhos = vizinhancas.get(parada);
        return vizinhos != null && vizinhos.contains(local);
    }

    // setas que partem do ícone do Khan: as 2 províncias que também recebem tributo
    void definirAfetadasPeloKhan(Provincia provinciaDoKhan, Provincia a, Provincia b) {
        List<Provincia> afetadas = new ArrayList<>();
        afetadas.add(a);
        afetadas.add(b);
        afetadasPeloKhan.put(provinciaDoKhan, afetadas);
    }

    List<Provincia> getAfetadasPeloKhan(Provincia provinciaDoKhan) {
        List<Provincia> afetadas = afetadasPeloKhan.get(provinciaDoKhan);
        return afetadas == null ? new ArrayList<Provincia>() : afetadas;
    }

    private static <K, V> void adicionarNaLista(Map<K, List<V>> mapa, K chave, V valor) {
        List<V> lista = mapa.get(chave);
        if (lista == null) {
            lista = new ArrayList<>();
            mapa.put(chave, lista);
        }
        lista.add(valor);
    }
}
