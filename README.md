# Herdeiros de Khan

Trabalho desenvolvido para a disciplina de Programação Orientada a Objetos (POO) no semestre 2026.2.

Esse projeto é uma implementação em Java do jogo de tabuleiro Herdeiros de Khan, organizada segundo a arquitetura MVC. Aqui colocamos em prática o que aprendemos sobre orientação a objetos, como encapsulamento, ocultação da informação e testes unitários usando JUnit 4.

## 1ª iteração — componente Model

O pacote `model` contém as regras do jogo. Só a classe `HerdeirosDeKhanAPI` é pública; as demais (tabuleiro, jogador, cidades etc.) são de pacote. A API recebe e devolve apenas `String`s e tipos primitivos. Os padrões Singleton e Façade serão aplicados a ela na 3ª iteração.

Regras implementadas (classe `Jogo`):
* Preparação: 2 a 5 jogadores, peões em Karakorum, 1 moeda para os dois primeiros e 2 para os demais, 1 tributo por província, 3 cidades reveladas com 4 tesouros cada.
* Movimento: só por rotas, sem terminar em parada cheia (simples = 1 peão, dupla = 2, Karakorum = todos). O jogador pode, se quiser, pular os próprios yurts sem gastar movimento; paradas puladas não contam para ações.
* Tributos: só de províncias adjacentes às paradas feitas no turno.
* Khan: só em províncias do Khan, sem permanecer no mesmo lugar, adicionando tributo na província e nas 2 indicadas (máximo 3).
* Ataque a cidades: 1 espada pelo 1º tesouro e 2 pelos seguintes no mesmo turno; quem pega o último conquista a cidade e revela a próxima.
* Yurts: só em paradas feitas durante o movimento e com espaço (Karakorum não aceita).
* Fim de jogo: disparado com 10/14/16/17 votos (2/3/4/5 jogadores) ou com todas as cidades conquistadas.

Os jogadores são identificados pelo herdeiro do seu tabuleiro (Altani, Chagatai, Jochi, Ogedei, Tolui) e os tesouros são de 5 tipos: pele, ferro, carne, grãos e lã.

## 2ª iteração — interface gráfica (View e Controller)

* `model`: o mapa real do tabuleiro (`MapaPadrao`: 33 paradas, 6 delas duplas, mais Karakorum, 54 rotas e 9 cidades), as colunas de ativação de cada herdeiro e o movimento por clique (`moverPara`, que acha o menor caminho dentro dos cavalos disponíveis).
* `view`: janela Swing de até 1400 x 1050. O tabuleiro e o tabuleiro do herdeiro são desenhados com `drawImage()` do `Graphics2D`; os peões, com formas do Java2D. A faixa do painel lateral fica na cor do jogador da vez.
* `controller`: recebe os cliques (pela interface `view.AcoesDoJogador`), pede a jogada ao Model e manda a View se redesenhar. Jogada inválida não faz nada. Nenhum evento usa expressão lambda.

Como jogar: clique numa coluna do tabuleiro do herdeiro (os cavalos dela são os movimentos), depois clique na parada de destino; ao terminar, clique em "Passar a vez".

Habilidades de herdeiro que afetam o movimento: Altani tem 2 peões e, antes de mover, escolhe qual usar clicando nele; Tolui começa com um yurt em Karakorum e pode pulá-la como pula os próprios yurts. Os yurts aparecem no tabuleiro na cor do dono.

## 3ª iteração — integração View/Model com Singleton, Façade e Observer

Todas as regras do manual podem ser acionadas pela interface gráfica.

Padrões:
* **Singleton**: `HerdeirosDeKhanAPI.getInstancia()` (uma partida por vez, criada com `novaPartida`).
* **Façade**: a `HerdeirosDeKhanAPI` continua sendo a única porta do Model. Ela esconde `Jogo`, `Tabuleiro`, `Jogador` etc. e só recebe e devolve `String`, `int` e `boolean`.
* **Observer**: interfaces `model.ObservadoIF` (adicionar/remover observador) e `model.ObservadorIF` (`notificar(observado, evento)`). A `HerdeirosDeKhanAPI` é a observada: cada jogada que dá certo avisa os observadores com um evento (`EVENTO_PEAO_MOVIDO`, `EVENTO_COLUNA_ATIVADA`, `EVENTO_JANELA_DA_CARTELA`, `EVENTO_JANELA_DE_CARTAS`, `EVENTO_JOGO_TERMINADO`...). As observadoras são `PainelTabuleiro`, `PainelJogador`, `JanelaCartela`, `JanelaCartas` e `JanelaJogo`, e cada uma se atualiza sozinha. O Controller não redesenha nada.
* **Controller**: o `ControladorJogo` organiza a sequência da partida: janela inicial (escolha dos herdeiros), escolha da carta de conselheiro secreto de cada jogador e depois os turnos. Ele traduz cada clique na jogada certa para o momento (ex.: clicar numa coluna ativa a coluna ou, se o Jochi tem melhoria esperando, posiciona a melhoria).

Janelas: todas as imagens de elementos do jogo (tabuleiro, peões, cartela, melhorias, fichas, bônus e cartas) são desenhadas com `drawImage()`; os componentes Swing só aparecem em botões e textos. A janela da cartela e a de peças e cartas abrem pelos botões do painel e fecham pelo botão "Fechar" delas.

Regras novas no Model:
* Mapa: 12 províncias de tributo e 4 do Khan (cada Khan com 3 setas) e 7 conselheiros (as 6 paradas duplas e Karakorum).
* Colunas: os punhos limitam quantos tributos se pega; o Khan da coluna é obrigatório (não passa a vez sem movê-lo) e vai para uma província do Khan ou para a área de melhorias; a moeda virtual vale só no turno.
* Melhorias: área com 5 peças e pilha; compra por 2 moedas (as virtuais primeiro), na coluna da cor da peça, sem valer no turno da compra; as restantes andam para a direita; completar uma linha dá uma melhoria grátis na coluna branca; o Khan na área de melhorias repõe as peças e dá moeda/espada se esses espaços estiverem vazios.
* Conselheiros: entrega do conjunto pedido numa parada feita no turno (1 voto, 2 em Karakorum); a 1ª entrega leva a peça de bônus (voto, 2 espadas, 2 moedas, 2 yurts, 3 movimentos + 1 punho, 2 punhos, 3 tesouros para escolher 1).
* Cartas: 4 de conselheiro aberto na mesa (1 voto para quem cumprir primeiro) e 2 secretas por jogador, que fica com 1.
* Habilidades: Altani (só o peão ativo age), Chagatai (1 cavalo vira 1 espada), Jochi (começa com 1 melhoria na coluna branca e escolhe a coluna das melhorias), Ogedei (1 moeda por um tesouro de qualquer cidade, menos o último), Tolui (yurt em Karakorum e construção nas paradas vizinhas).
* Fim de jogo: depois do disparo termina a rodada e joga-se mais uma completa. Contam a influência nas 3 regiões (tabela do tabuleiro: votos pelas melhorias da coluna da região para quem tem mais yurts e para o 2º) e a carta secreta. Desempate: tesouros não entregues e depois a distância até Karakorum.

Decisões nossas onde o manual não diz:
* A divisão das melhorias entre as 3 cores de fundo (as de tesouro seguem as imagens).
* Das 2 melhorias iniciais do Jochi, a 1ª da pilha vai para a coluna branca e ele escolhe a coluna da outra no 1º turno.
* As peças de tributo da reserva não têm limite.
* A peça opcional de Karakorum não é usada.

## Como jogar

* **Cartela** (abre sozinha no começo do turno): clique numa coluna sem ficha. Os cavalos dela são os movimentos e os punhos são os tributos que se pode pegar.
* **Mapa**:
  * Mover: clique na parada de destino.
  * Construir yurt: botão direito numa parada.
  * Pegar tributo: clique no ícone de uma província (o número mostra quantas peças ela tem).
  * Mover o Khan: clique numa cabeça do Khan ou na cabeça ao lado da área de melhorias.
  * Atacar: clique numa cidade e escolha o tesouro na janela de peças e cartas.
  * Entregar tesouros: clique no pergaminho de um conselheiro. Os que têm contorno amarelo são aqueles em que você parou no turno.
  * Comprar melhoria: clique numa peça da área de melhorias.
* **Painel da direita**: ver a cartela, ver peças e cartas, comprar do topo da pilha, a habilidade do Chagatai e "Passar a vez".
* **Janela de peças e cartas**: tributos, tesouros, bônus (clique para usar), votos e a carta secreta, além das escolhas de tesouro.

## Como executar

No Eclipse, rode `controller.Principal` (Run As > Java Application) com o diretório de trabalho na pasta do projeto, para que a pasta `assets` seja encontrada. Sem argumentos, abre a janela para escolher os herdeiros na ordem de jogo; também dá para passar os nomes como argumentos (ex.: `ALTANI OGEDEI TOLUI`).

## Como rodar os testes

Importe a pasta no Eclipse (File > Import > Existing Projects into Workspace). As pastas `src` e `teste` já estão configuradas como source folders e o JUnit 4 está no build path. Clique com o botão direito em `teste` > Run As > JUnit Test.

## Grupo
* Lis Almeida
* Rafaela Bessa
* Davi Rangel
