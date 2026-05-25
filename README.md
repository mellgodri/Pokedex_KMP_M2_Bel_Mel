# Pokédex Multiplatform - Trabalho M2

Projeto atualizado para a M2 da disciplina **Programação para Dispositivos Móveis II**.

## Principais alterações feitas

- Nova interface baseada nos prints enviados:
  - onboarding com duas telas;
  - tela principal da Pokédex com busca, filtro por tipo e grid em 2 colunas;
  - bottom sheet para seleção de tipos;
  - tela de detalhes com cor baseada no tipo do Pokémon;
  - tela de favoritos com cards e local de captura.

- Alterações funcionais:
  - consumo da **PokeAPI** com Ktor;
  - sincronização inicial dos Pokémons para cache local;
  - paginação com `LIMIT` e `OFFSET` no banco local;
  - busca por nome e filtro por tipo consultando o cache local;
  - tela de detalhes carregando dados em tempo real pela PokeAPI;
  - favoritos persistidos em SQLite no Android;
  - campo obrigatório **Onde foi capturado?** ao salvar favorito;
  - ViewModels com `StateFlow` e estados explícitos de carregamento, sucesso e erro.

## Como executar no Android Studio

1. Abra a pasta do projeto no Android Studio.
2. Aguarde o Gradle sincronizar.
3. Selecione a configuração `composeApp`.
4. Execute em um emulador ou dispositivo Android.

Também é possível executar pelo terminal:

```bash
./gradlew :composeApp:assembleDebug
```

## Observações importantes

- Na primeira abertura, o app tenta buscar os dados na PokeAPI e salvar o cache local.
- Se estiver sem internet na primeira execução, o app carrega uma base mínima local para não ficar vazio.
- No Android, a persistência local foi feita usando SQLite nativo via `SQLiteOpenHelper`, mantendo a lógica de banco relacional exigida pela atividade.
- Para iOS, foi mantida uma implementação em memória apenas para permitir compilação do alvo multiplatform. O foco da entrega está no Android Studio.


## Alteracao adicional

- A lista de Pokemons salvos/favoritos agora respeita o limite maximo de 6 Pokemons.
- Ao tentar salvar o setimo Pokemon, o aplicativo exibe uma tela de erro informando que o limite foi atingido.
- Para adicionar outro Pokemon, o usuario precisa remover um dos Pokemons ja salvos.
