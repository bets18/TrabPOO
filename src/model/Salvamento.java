package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// grava a partida num arquivo texto e monta a partida de volta a partir dele. Cada linha do
// arquivo é "chave=valor" (ex: "jogador.0.votos=3") e as listas são separadas por vírgula.
// Só se salva no começo da vez (Jogo.podeSalvar), então o que vale só durante o turno
// (cavalos, punhos, peças virtuais...) não vai pro arquivo
final class Salvamento {
    private static final String FORMATO = "HerdeirosDeKhan-1";
    private static final String NA_AREA_DE_MELHORIAS = "AREA_DE_MELHORIAS";

    private Salvamento() {
    }

    //  salvar

    static void salvar(Jogo jogo, File arquivo) throws IOException {
        List<String> linhas = new ArrayList<>();
        Tabuleiro tabuleiro = jogo.getTabuleiro();

        linhas.add("formato=" + FORMATO);
        linhas.add("jogadores=" + jogo.getNumeroDeJogadores());
        linhas.add("vez=" + jogo.getIndiceJogadorDaVez());
        linhas.add("votosNoConselho=" + jogo.getVotosNoConselho());
        linhas.add("partidaComecou=" + jogo.isPartidaComecou());
        linhas.add("fimDisparado=" + jogo.isFimDisparado());
        linhas.add("ultimaRodada=" + jogo.isUltimaRodada());
        String khan = jogo.isKhanNaAreaDeMelhorias() ? NA_AREA_DE_MELHORIAS : nome(jogo.getProvinciaDoKhan());
        linhas.add("khan=" + khan);
        for (TipoTributo tipo : TipoTributo.values()) {
            linhas.add("reserva." + tipo + "=" + jogo.getNaReserva(tipo));
        }

        linhas.add("cidades=" + juntar(jogo.getTodasAsCidades()));
        linhas.add("cidadesOcultas=" + juntar(jogo.getCidadesOcultas()));
        linhas.add("pilhaDeTesouros=" + juntar(jogo.getPilhaDeTesouros()));
        AreaDeMelhorias area = jogo.getAreaDeMelhorias();
        List<Melhoria> espacos = new ArrayList<>();
        for (int i = 0; i < AreaDeMelhorias.ESPACOS; i++) {
            espacos.add(area.espiar(i));
        }
        linhas.add("areaDeMelhorias=" + juntar(espacos));
        linhas.add("pilhaDeMelhorias=" + juntar(area.getPilha()));
        linhas.add("cartasAbertas=" + juntar(jogo.getCartasAbertas()));

        for (int i = 0; i < jogo.getNumeroDeJogadores(); i++) {
            salvarJogador(linhas, "jogador." + i + ".", jogo.getJogador(i));
        }
        for (Provincia provincia : tabuleiro.getLocais(Provincia.class)) {
            linhas.add("provincia." + provincia.getNome() + "=" + provincia.getQuantidade());
        }
        // o yurt de Karakorum é sempre o do Tolui e já vem com a preparação
        for (Parada parada : tabuleiro.getLocais(Parada.class)) {
            if (parada.getTipo() != TipoParada.KARAKORUM && parada.getQuantidadeYurts() > 0) {
                List<Herdeiro> donos = new ArrayList<>();
                for (Jogador dono : parada.getDonosDosYurts()) {
                    donos.add(dono.getHerdeiro());
                }
                linhas.add("yurts." + parada.getNome() + "=" + juntar(ordenar(donos)));
            }
        }
        for (Cidade cidade : tabuleiro.getLocais(Cidade.class)) {
            salvarCidade(linhas, "cidade." + cidade.getNome() + ".", cidade);
        }
        for (Conselheiro conselheiro : tabuleiro.getConselheiros()) {
            linhas.add("bonus." + conselheiro.getParada().getNome() + "=" + nome(conselheiro.getBonus()));
        }

        try (PrintWriter saida = new PrintWriter(new OutputStreamWriter(new FileOutputStream(arquivo),
                StandardCharsets.UTF_8))) {
            for (String linha : linhas) {
                saida.println(linha);
            }
            if (saida.checkError()) {
                throw new IOException("Não foi possível gravar " + arquivo);
            }
        }
    }

    private static void salvarJogador(List<String> linhas, String prefixo, Jogador jogador) {
        linhas.add(prefixo + "herdeiro=" + jogador.getHerdeiro());
        linhas.add(prefixo + "nome=" + jogador.getNome());
        for (TipoTributo tipo : TipoTributo.values()) {
            linhas.add(prefixo + "tributo." + tipo + "=" + jogador.getQuantidadeTributo(tipo));
        }
        for (TipoTesouro tipo : TipoTesouro.values()) {
            linhas.add(prefixo + "tesouro." + tipo + "=" + jogador.getQuantidadeTesouro(tipo));
        }
        linhas.add(prefixo + "yurtsNoEstoque=" + jogador.getYurtsNoEstoque());
        linhas.add(prefixo + "votos=" + jogador.getVotos());
        List<Parada> peoes = new ArrayList<>();
        for (int peao = 0; peao < jogador.getNumeroDePeoes(); peao++) {
            peoes.add(jogador.getPosicaoDoPeao(peao));
        }
        linhas.add(prefixo + "peoes=" + juntar(peoes));
        linhas.add(prefixo + "peaoAtivo=" + jogador.getPeaoAtivo());
        linhas.add(prefixo + "fichas=" + juntar(ordenar(jogador.getColunasComFicha())));
        for (Coluna coluna : Coluna.values()) {
            linhas.add(prefixo + "melhorias." + coluna + "=" + juntar(jogador.getMelhorias(coluna)));
        }
        linhas.add(prefixo + "melhoriaParaPosicionar=" + nome(jogador.getMelhoriaParaPosicionar()));
        linhas.add(prefixo + "outraMelhoriaInicial=" + nome(jogador.getOutraMelhoriaInicial()));
        linhas.add(prefixo + "bonus=" + juntar(ordenar(jogador.getBonusGuardados())));
        linhas.add(prefixo + "bonusObtidos=" + jogador.getBonusObtidos());
        linhas.add(prefixo + "cidadesConquistadas=" + jogador.getCidadesConquistadas());
        linhas.add(prefixo + "cartasOferecidas=" + juntar(jogador.getCartasOferecidas()));
        linhas.add(prefixo + "cartaSecreta=" + nome(jogador.getCartaSecreta()));
    }

    private static void salvarCidade(List<String> linhas, String prefixo, Cidade cidade) {
        List<TipoTesouro> tesouros = new ArrayList<>();
        for (TipoTesouro tipo : TipoTesouro.values()) {
            for (int i = 0; i < cidade.getQuantidadeDe(tipo); i++) {
                tesouros.add(tipo);
            }
        }
        Jogador conquistador = cidade.getConquistador();
        linhas.add(prefixo + "revelada=" + cidade.isRevelada());
        linhas.add(prefixo + "tesouros=" + juntar(tesouros));
        linhas.add(prefixo + "conquistador=" + nome(conquistador == null ? null : conquistador.getHerdeiro()));
        linhas.add(prefixo + "yurt=" + cidade.temYurtDoConquistador());
    }

    // o que no jogo fica em HashSet/HashMap sai sempre na mesma ordem no arquivo
    private static <E extends Enum<E>> List<E> ordenar(List<E> itens) {
        Collections.sort(itens);
        return itens;
    }

    // null vira texto vazio
    private static String nome(Object objeto) {
        return objeto == null ? "" : objeto.toString();
    }

    private static String juntar(List<?> itens) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < itens.size(); i++) {
            if (i > 0) {
                texto.append(',');
            }
            texto.append(nome(itens.get(i)));
        }
        return texto.toString();
    }

    //  carregar

    // monta a partida num tabuleiro novo. Qualquer coisa errada no arquivo (chave faltando,
    // nome que não existe, número inválido) vira IOException e a partida atual não é tocada
    static Jogo carregar(File arquivo) throws IOException {
        Map<String, String> valores = lerArquivo(arquivo);
        try {
            return montar(new Leitor(valores));
        } catch (RuntimeException e) {
            throw new IOException("O arquivo " + arquivo.getName() + " não é uma partida salva válida.", e);
        }
    }

    private static Map<String, String> lerArquivo(File arquivo) throws IOException {
        Map<String, String> valores = new HashMap<>();
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(new FileInputStream(arquivo),
                StandardCharsets.UTF_8))) {
            String linha = entrada.readLine();
            while (linha != null) {
                int igual = linha.indexOf('=');
                if (igual > 0) {
                    valores.put(linha.substring(0, igual), linha.substring(igual + 1));
                }
                linha = entrada.readLine();
            }
        }
        return valores;
    }

    private static Jogo montar(Leitor leitor) {
        if (!FORMATO.equals(leitor.texto("formato"))) {
            throw new IllegalArgumentException("Formato desconhecido.");
        }
        int quantidade = leitor.inteiro("jogadores");
        List<Herdeiro> herdeiros = new ArrayList<>();
        List<String> nomes = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            herdeiros.add(Herdeiro.valueOf(leitor.texto("jogador." + i + ".herdeiro")));
            nomes.add(leitor.texto("jogador." + i + ".nome"));
        }
        Tabuleiro tabuleiro = MapaPadrao.criar();
        Jogo jogo = new Jogo(tabuleiro, herdeiros, nomes);
        leitor.usar(jogo);

        for (TipoTributo tipo : TipoTributo.values()) {
            jogo.definirReserva(tipo, leitor.inteiro("reserva." + tipo));
        }
        jogo.definirAndamento(leitor.inteiro("votosNoConselho"), leitor.logico("partidaComecou"),
                leitor.logico("fimDisparado"), leitor.logico("ultimaRodada"));
        String khan = leitor.texto("khan");
        boolean naArea = khan.equals(NA_AREA_DE_MELHORIAS);
        jogo.definirKhan(naArea || khan.isEmpty() ? null : leitor.local(Provincia.class, khan), naArea);

        jogo.definirCidades(leitor.locais(Cidade.class, "cidades"), leitor.locais(Cidade.class, "cidadesOcultas"),
                leitor.lista(TipoTesouro.class, "pilhaDeTesouros"));
        jogo.definirAreaDeMelhorias(new AreaDeMelhorias(leitor.melhorias("areaDeMelhorias"),
                leitor.melhorias("pilhaDeMelhorias")));
        jogo.definirCartasAbertas(leitor.lista(CartaAberta.class, "cartasAbertas"));

        for (int i = 0; i < quantidade; i++) {
            carregarJogador(leitor, "jogador." + i + ".", jogo.getJogador(i));
        }
        for (Provincia provincia : tabuleiro.getLocais(Provincia.class)) {
            provincia.definirQuantidade(leitor.inteiro("provincia." + provincia.getNome()));
        }
        for (Parada parada : tabuleiro.getLocais(Parada.class)) {
            if (parada.getTipo() != TipoParada.KARAKORUM && leitor.tem("yurts." + parada.getNome())) {
                for (Herdeiro dono : leitor.lista(Herdeiro.class, "yurts." + parada.getNome())) {
                    parada.adicionarYurt(leitor.jogador(dono));
                }
            }
        }
        for (Cidade cidade : tabuleiro.getLocais(Cidade.class)) {
            carregarCidade(leitor, "cidade." + cidade.getNome() + ".", cidade);
        }
        for (Conselheiro conselheiro : tabuleiro.getConselheiros()) {
            String bonus = leitor.texto("bonus." + conselheiro.getParada().getNome());
            conselheiro.colocarBonus(bonus.isEmpty() ? null : TipoBonus.valueOf(bonus));
        }

        jogo.retomarNaVezDe(leitor.inteiro("vez"));
        return jogo;
    }

    private static void carregarJogador(Leitor leitor, String prefixo, Jogador jogador) {
        for (TipoTributo tipo : TipoTributo.values()) {
            jogador.definirTributos(tipo, leitor.inteiro(prefixo + "tributo." + tipo));
        }
        for (TipoTesouro tipo : TipoTesouro.values()) {
            for (int i = leitor.inteiro(prefixo + "tesouro." + tipo); i > 0; i--) {
                jogador.adicionarTesouro(tipo);
            }
        }
        jogador.definirYurtsNoEstoque(leitor.inteiro(prefixo + "yurtsNoEstoque"));
        jogador.adicionarVotos(leitor.inteiro(prefixo + "votos"));

        // a preparação deixou todos os peões em Karakorum
        List<Parada> peoes = leitor.locais(Parada.class, prefixo + "peoes");
        if (peoes.size() != jogador.getNumeroDePeoes()) {
            throw new IllegalArgumentException("Número de peões errado.");
        }
        for (int peao = 0; peao < peoes.size(); peao++) {
            jogador.getPosicaoDoPeao(peao).removerPeao(jogador);
            peoes.get(peao).adicionarPeao(jogador);
            jogador.setPosicaoDoPeao(peao, peoes.get(peao));
        }
        int peaoAtivo = leitor.inteiro(prefixo + "peaoAtivo");
        if (peaoAtivo < 0 || peaoAtivo >= peoes.size()) {
            throw new IllegalArgumentException("Peão ativo inválido.");
        }
        jogador.setPeaoAtivo(peaoAtivo);

        for (Coluna coluna : leitor.lista(Coluna.class, prefixo + "fichas")) {
            jogador.colocarFicha(coluna);
        }
        for (Coluna coluna : Coluna.values()) {
            for (Melhoria melhoria : leitor.melhorias(prefixo + "melhorias." + coluna)) {
                if (melhoria == null || !jogador.adicionarMelhoria(coluna, melhoria)) {
                    throw new IllegalArgumentException("Melhorias inválidas na coluna " + coluna);
                }
            }
        }
        jogador.setMelhoriaParaPosicionar(leitor.melhoria(leitor.texto(prefixo + "melhoriaParaPosicionar")));
        jogador.setOutraMelhoriaInicial(leitor.melhoria(leitor.texto(prefixo + "outraMelhoriaInicial")));

        for (TipoBonus bonus : leitor.lista(TipoBonus.class, prefixo + "bonus")) {
            jogador.guardarBonus(bonus);
        }
        for (int i = leitor.inteiro(prefixo + "bonusObtidos"); i > 0; i--) {
            jogador.contarBonusObtido();
        }
        for (int i = leitor.inteiro(prefixo + "cidadesConquistadas"); i > 0; i--) {
            jogador.conquistarCidade();
        }
        jogador.oferecerCartasSecretas(leitor.lista(CartaSecreta.class, prefixo + "cartasOferecidas"));
        String carta = leitor.texto(prefixo + "cartaSecreta");
        if (!carta.isEmpty() && !jogador.escolherCartaSecreta(CartaSecreta.valueOf(carta))) {
            throw new IllegalArgumentException("Carta secreta que o jogador não recebeu.");
        }
    }

    private static void carregarCidade(Leitor leitor, String prefixo, Cidade cidade) {
        if (!leitor.logico(prefixo + "revelada")) {
            return;
        }
        List<TipoTesouro> tesouros = leitor.lista(TipoTesouro.class, prefixo + "tesouros");
        if (tesouros.size() > Cidade.TESOUROS_AO_REVELAR) {
            throw new IllegalArgumentException("Tesouros demais em " + cidade.getNome());
        }
        cidade.revelar(tesouros);
        String conquistador = leitor.texto(prefixo + "conquistador");
        if (!conquistador.isEmpty()) {
            cidade.conquistar(leitor.jogador(Herdeiro.valueOf(conquistador)));
            if (leitor.logico(prefixo + "yurt")) {
                cidade.colocarYurtDoConquistador();
            }
        }
    }

    // converte os textos do arquivo. Tudo que estiver errado estoura uma exceção, que o
    // carregar() transforma em IOException
    private static final class Leitor {
        private final Map<String, String> valores;
        private Jogo jogo = null;

        Leitor(Map<String, String> valores) {
            this.valores = valores;
        }

        // a partir daqui os nomes de locais e de herdeiros são procurados nessa partida
        void usar(Jogo jogo) {
            this.jogo = jogo;
        }

        boolean tem(String chave) {
            return valores.containsKey(chave);
        }

        String texto(String chave) {
            String valor = valores.get(chave);
            if (valor == null) {
                throw new IllegalArgumentException("Falta a chave " + chave);
            }
            return valor;
        }

        int inteiro(String chave) {
            return Integer.parseInt(texto(chave));
        }

        boolean logico(String chave) {
            String valor = texto(chave);
            if (!valor.equals("true") && !valor.equals("false")) {
                throw new IllegalArgumentException(chave + " tem que ser true ou false.");
            }
            return valor.equals("true");
        }

        // lista vazia se o valor estiver em branco
        private String[] partes(String chave) {
            String valor = texto(chave);
            return valor.isEmpty() ? new String[0] : valor.split(",", -1);
        }

        <E extends Enum<E>> List<E> lista(Class<E> classe, String chave) {
            List<E> itens = new ArrayList<>();
            for (String parte : partes(chave)) {
                itens.add(Enum.valueOf(classe, parte));
            }
            return itens;
        }

        <T extends Local> T local(Class<T> classe, String nome) {
            Local local = jogo.getTabuleiro().getLocal(nome);
            if (!classe.isInstance(local)) {
                throw new IllegalArgumentException("Local desconhecido: " + nome);
            }
            return classe.cast(local);
        }

        <T extends Local> List<T> locais(Class<T> classe, String chave) {
            List<T> itens = new ArrayList<>();
            for (String parte : partes(chave)) {
                itens.add(local(classe, parte));
            }
            return itens;
        }

        Jogador jogador(Herdeiro herdeiro) {
            for (int i = 0; i < jogo.getNumeroDeJogadores(); i++) {
                if (jogo.getJogador(i).getHerdeiro() == herdeiro) {
                    return jogo.getJogador(i);
                }
            }
            throw new IllegalArgumentException(herdeiro + " não está na partida.");
        }

        // "TIPO:COR"; texto vazio = null (espaço vazio)
        Melhoria melhoria(String texto) {
            if (texto.isEmpty()) {
                return null;
            }
            String[] partes = texto.split(":");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Melhoria inválida: " + texto);
            }
            return new Melhoria(TipoMelhoria.valueOf(partes[0]), Coluna.valueOf(partes[1]));
        }

        List<Melhoria> melhorias(String chave) {
            List<Melhoria> itens = new ArrayList<>();
            for (String parte : partes(chave)) {
                itens.add(melhoria(parte));
            }
            return itens;
        }
    }
}
