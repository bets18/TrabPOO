package view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// liga os nomes que o Model passa (TIPO:COR, PELE, VOTO, VIAJANTE...) às imagens da pasta
// assets e aos nomes que aparecem na tela
final class Pecas {
    static final List<String> TRIBUTOS = Arrays.asList("MOEDA", "ESPADA", "YURT");
    static final List<String> TESOUROS = Arrays.asList("PELE", "FERRO", "CARNE", "GRAOS", "LA");

    private static final Map<String, String> MELHORIAS = new HashMap<>();
    private static final Map<String, String> CORES = new HashMap<>();
    private static final Map<String, String> IMAGENS = new HashMap<>();
    private static final Map<String, String> NOMES = new HashMap<>();

    static {
        // prefixo dos arquivos de cartoes1 (ex: CAV-V.jpg = 2 cavalos de fundo verde)
        MELHORIAS.put("MOEDA", "MOE");
        MELHORIAS.put("ESPADA", "ESP");
        MELHORIAS.put("YURT", "CAS");
        MELHORIAS.put("PUNHO", "MAO");
        MELHORIAS.put("CAVALOS", "CAV");
        MELHORIAS.put("PELE", "PER");
        MELHORIAS.put("FERRO", "MOB");
        MELHORIAS.put("CARNE", "BOI");
        MELHORIAS.put("GRAOS", "CUI");
        MELHORIAS.put("LA", "MOP");
        CORES.put("AMARELA", "A");
        CORES.put("VERMELHA", "R");
        CORES.put("VERDE", "V");

        imagem("tributo:YURT", "cartoes/cartoes6/cartoes6-1.jpg", "Yurt");
        imagem("tributo:ESPADA", "cartoes/cartoes6/cartoes6-2.jpg", "Espada");
        imagem("tributo:MOEDA", "cartoes/cartoes6/cartoes6-3.jpg", "Moeda");

        imagem("tesouro:CARNE", "cartoes/cartoes7/cartoes7-1.jpg", "Carne");
        imagem("tesouro:LA", "cartoes/cartoes7/cartoes7-2.jpg", "Lã");
        imagem("tesouro:FERRO", "cartoes/cartoes7/cartoes7-3.jpg", "Ferro");
        imagem("tesouro:PELE", "cartoes/cartoes7/cartoes7-4.jpg", "Pele");
        imagem("tesouro:GRAOS", "cartoes/cartoes7/cartoes7-5.jpg", "Grãos");

        imagem("bonus:MOVIMENTOS", "cartoes/cartoes3/cartoes3-1.jpg", "3 movimentos e 1 punho");
        imagem("bonus:VOTO", "cartoes/cartoes3/cartoes3-2.jpg", "1 voto");
        imagem("bonus:DOIS_YURTS", "cartoes/cartoes3/cartoes3-3.jpg", "2 yurts");
        imagem("bonus:DUAS_ESPADAS", "cartoes/cartoes3/cartoes3-4.jpg", "2 espadas");
        imagem("bonus:TESOUROS", "cartoes/cartoes3/cartoes3-5.jpg", "3 tesouros, fique com 1");
        imagem("bonus:DOIS_PUNHOS", "cartoes/cartoes3/cartoes3-6.jpg", "2 punhos");
        imagem("bonus:DUAS_MOEDAS", "cartoes/cartoes3/cartoes3-7.jpg", "2 moedas");

        imagem("aberta:QUATRO_ESPADAS", "cartas/cartas1/cartas1-1.jpg", "Ter 4 espadas");
        imagem("aberta:SEIS_YURTS", "cartas/cartas1/cartas1-2.jpg", "6 yurts no mapa");
        imagem("aberta:DEZ_YURTS", "cartas/cartas1/cartas1-3.jpg", "10 yurts no mapa");
        imagem("aberta:QUATRO_TESOUROS", "cartas/cartas1/cartas1-4.jpg", "Ter 4 tesouros");
        imagem("aberta:DOIS_YURTS_POR_REGIAO", "cartas/cartas1/cartas1-5.jpg", "2 yurts em cada região");
        imagem("aberta:UM_YURT_POR_REGIAO", "cartas/cartas1/cartas1-6.jpg", "1 yurt em cada região");
        imagem("aberta:DOIS_BONUS", "cartas/cartas1/cartas1-7.jpg", "2 peças de bônus");
        imagem("aberta:TRES_BONUS", "cartas/cartas1/cartas1-8.jpg", "3 peças de bônus");
        imagem("aberta:CINCO_TESOUROS_DIFERENTES", "cartas/cartas1/cartas1-9.jpg", "5 tesouros diferentes");
        imagem("aberta:TRES_CIDADES", "cartas/cartas2/cartas2-1.jpg", "Conquistar 3 cidades");
        imagem("aberta:DUAS_CIDADES", "cartas/cartas2/cartas2-2.jpg", "Conquistar 2 cidades");
        imagem("aberta:LINHA_DE_MELHORIAS", "cartas/cartas2/cartas2-3.jpg", "Completar uma linha de melhorias");
        imagem("aberta:QUATRO_NUMA_COLUNA", "cartas/cartas3/cartas3-1.jpg", "4 melhorias numa coluna");
        imagem("aberta:DUAS_COLUNAS_COM_DUAS", "cartas/cartas3/cartas3-2.jpg", "2 colunas com 2 melhorias");

        imagem("secreta:SACERDOTE", "cartas/cartas4/cartas4-1.jpg", "Sacerdote");
        imagem("secreta:COLECIONADOR", "cartas/cartas4/cartas4-2.jpg", "Colecionador");
        imagem("secreta:VIAJANTE", "cartas/cartas5/cartas5-1.jpg", "Viajante");
        imagem("secreta:SOLDADO", "cartas/cartas5/cartas5-2.jpg", "Soldado");
        imagem("secreta:CIDADAO", "cartas/cartas6/cartas6-1.jpg", "Cidadão");
        imagem("secreta:CAVALEIRO", "cartas/cartas6/cartas6-2.jpg", "Cavaleiro");
        imagem("secreta:MENSAGEIRO", "cartas/cartas7/cartas7-1.jpg", "Mensageiro");
        imagem("secreta:GOVERNADOR", "cartas/cartas7/cartas7-2.jpg", "Governador");
        imagem("secreta:PREFEITO", "cartas/cartas8/cartas8-1.jpg", "Prefeito");
        imagem("secreta:BAJULADOR", "cartas/cartas8/cartas8-2.jpg", "Bajulador");

        NOMES.put("cidade:Bagda", "Bagdá");
        NOMES.put("cidade:Cantao", "Cantão");
    }

    private Pecas() {
    }

    private static void imagem(String chave, String caminho, String nome) {
        IMAGENS.put(chave, caminho);
        NOMES.put(chave, nome);
    }

    // "CAVALOS:VERDE" -> cartoes/cartoes1/CAV-V.jpg
    static String melhoria(String nome) {
        String[] partes = nome.split(":");
        return "cartoes/cartoes1/" + MELHORIAS.get(partes[0]) + "-" + CORES.get(partes[1]) + ".jpg";
    }

    static String tributo(String tipo) {
        return IMAGENS.get("tributo:" + tipo);
    }

    static String tesouro(String tipo) {
        return IMAGENS.get("tesouro:" + tipo);
    }

    static String bonus(String tipo) {
        return IMAGENS.get("bonus:" + tipo);
    }

    static String cartaAberta(String nome) {
        return IMAGENS.get("aberta:" + nome);
    }

    static String cartaSecreta(String nome) {
        return IMAGENS.get("secreta:" + nome);
    }

    static String nomeDoTributo(String tipo) {
        return NOMES.get("tributo:" + tipo);
    }

    static String nomeDoTesouro(String tipo) {
        return NOMES.get("tesouro:" + tipo);
    }

    static String nomeDoBonus(String tipo) {
        return NOMES.get("bonus:" + tipo);
    }

    static String nomeDaCartaAberta(String nome) {
        return NOMES.get("aberta:" + nome);
    }

    static String nomeDaCartaSecreta(String nome) {
        return NOMES.get("secreta:" + nome);
    }

    // "Bagda" -> "Bagdá"; as outras já vêm com o nome certo
    static String nomeDaCidade(String nome) {
        String acentuado = NOMES.get("cidade:" + nome);
        return acentuado == null ? nome : acentuado;
    }
}
