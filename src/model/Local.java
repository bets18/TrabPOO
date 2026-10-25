package model;

// parada, cidade ou província. O nome é o que a API usa pra achar o local
abstract class Local {
    private final String nome;
    private final Regiao regiao; // Karakorum não tem região (null)

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
