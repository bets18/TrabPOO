package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// o mapa como um grafo: rotas entre as paradas e o que fica do lado de cada parada
// (cidades e províncias). Também guarda os conselheiros das paradas de entrega
class Tabuleiro {
    private final Map<String, Local> locais = new HashMap<>();
    private final Map<Parada, List<Parada>> rotas = new HashMap<>();
    private final Map<Parada, List<Local>> vizinhancas = new HashMap<>();
    private final Map<Provincia, List<Provincia>> afetadasPeloKhan = new HashMap<>();
    // LinkedHashMap (um HashMap que guarda a ordem): o bônus de cada conselheiro é sorteado nessa ordem
    private final Map<Parada, Conselheiro> conselheiros = new LinkedHashMap<>();
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

    // todos os locais de uma classe, ex: getLocais(Cidade.class)
    <T extends Local> List<T> getLocais(Class<T> tipo) {
        List<T> encontrados = new ArrayList<>();
        for (Local local : locais.values()) {
            if (tipo.isInstance(local)) {
                encontrados.add(tipo.cast(local));
            }
        }
        return encontrados;
    }

    // a rota vale nos dois sentidos
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

    // paradas em volta de uma cidade ou província
    List<Parada> getParadasAdjacentes(Local local) {
        List<Parada> adjacentes = new ArrayList<>();
        for (Map.Entry<Parada, List<Local>> entrada : vizinhancas.entrySet()) {
            if (entrada.getValue().contains(local)) {
                adjacentes.add(entrada.getKey());
            }
        }
        return adjacentes;
    }

    // as províncias para onde apontam as setas que saem do Khan
    void definirAfetadasPeloKhan(Provincia provinciaDoKhan, Provincia... afetadas) {
        afetadasPeloKhan.put(provinciaDoKhan, new ArrayList<>(Arrays.asList(afetadas)));
    }

    List<Provincia> getAfetadasPeloKhan(Provincia provinciaDoKhan) {
        List<Provincia> afetadas = afetadasPeloKhan.get(provinciaDoKhan);
        return afetadas == null ? new ArrayList<Provincia>() : afetadas;
    }

    void adicionarConselheiro(Conselheiro conselheiro) {
        conselheiros.put(conselheiro.getParada(), conselheiro);
    }

    // null se a parada não for de entrega
    Conselheiro getConselheiro(Parada parada) {
        return conselheiros.get(parada);
    }

    List<Conselheiro> getConselheiros() {
        return new ArrayList<>(conselheiros.values());
    }

    // menor número de rotas entre duas paradas (busca em largura); -1 se não tem caminho
    int distancia(Parada origem, Parada destino) {
        Map<Parada, Integer> passos = new HashMap<>();
        List<Parada> fila = new ArrayList<>();
        passos.put(origem, 0);
        fila.add(origem);
        while (!fila.isEmpty()) {
            Parada atual = fila.remove(0);
            if (atual == destino) {
                return passos.get(atual);
            }
            for (Parada vizinha : getParadasVizinhas(atual)) {
                if (!passos.containsKey(vizinha)) {
                    passos.put(vizinha, passos.get(atual) + 1);
                    fila.add(vizinha);
                }
            }
        }
        return -1;
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
