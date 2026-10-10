package model;

// Qualquer lugar nomeado do tabuleiro: paradas, cidades e províncias
abstract class Local {
    private final String nome;
    private final Regiao regiao; // null para Karakorum, que não pertence a nenhuma região

    Local(String nome, Regiao regiao) {
        this.nome = nome;
        this.regiao = regiao;
    }

    String getNome() {
        return nome;
    }

    Regiao getRegiao() {
        return regiao;
    }

    @Override
    public String toString() {
        return nome;
    }
}
