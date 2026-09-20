# GameVault

O **GameVault** é uma aplicação web para descoberta, organização e avaliação de jogos.

O projeto utiliza a API pública da **RAWG** como fonte externa do catálogo de jogos e mantém no banco de dados apenas os dados próprios da aplicação, como usuários, favoritos, lista de desejos e avaliações.

> **Status:** em desenvolvimento
>
> **Etapa atual:** desenvolvimento Back-end — catálogo, Home e detalhes de jogos concluídos e validados.
>
> **Próximo foco:** revisão e fechamento dos requisitos restantes do back-end antes do início da camada de front-end.

---

## Funcionalidades da V1

* Cadastro e autenticação de usuários.
* Gerenciamento da própria conta.
* Perfil do usuário com imagem.
* Catálogo público de jogos.
* Busca de jogos por nome.
* Filtros e ordenações do catálogo.
* Listagem de jogos populares.
* Lançamentos recentes.
* Jogos mais bem avaliados.
* Detalhes completos dos jogos.
* Favoritos.
* Lista de Desejos.
* Avaliação de jogos com notas de 1 a 5.
* Integração com a API RAWG.
* Personalização dos detalhes do jogo para usuários autenticados.

---

# Tecnologias

## Back-end

* Java 21.
* Spring Boot 4.1.1.
* Spring Web MVC.
* Spring Data JPA.
* Bean Validation.
* Spring Security.
* Spring RestClient.
* Maven.

## Banco de dados e infraestrutura

* MySQL 8.4 LTS.
* Flyway.
* Docker.
* Docker Compose.
* DBeaver.

## Testes

* JUnit 5.
* Mockito.
* MockMvc.
* Spring Boot Test.
* Spring Security Test.
* MockRestServiceServer.

---

# Arquitetura

O projeto segue uma arquitetura de **monólito modular por funcionalidade**.

A estrutura é organizada principalmente em:

```text
com.gamevault
├── avaliacao
├── favorito
├── jogo
│   ├── controller
│   ├── dto
│   └── service
├── listadesejos
├── rawg
│   ├── client
│   ├── config
│   ├── dto
│   └── exception
├── shared
└── user
```

Cada funcionalidade utiliza, conforme necessário, uma estrutura semelhante a:

```text
Controller
   ↓
DTO
   ↓
Service
   ↓
Repository
   ↓
Entity
```

As regras de negócio permanecem concentradas na camada de serviço, enquanto os controllers são responsáveis pela comunicação HTTP.

A integração com a RAWG está isolada no módulo `rawg`. O módulo `jogo` consome essa integração e converte os contratos externos em DTOs próprios do GameVault.

Dessa forma, os consumidores da API não dependem diretamente da estrutura retornada pela RAWG.

---

# Persistência

O catálogo completo de jogos **não é persistido localmente**.

Os jogos são referenciados internamente pelo identificador fornecido pela RAWG:

```text
rawgGameId
```

O banco de dados do GameVault armazena apenas informações próprias da aplicação.

As principais tabelas são:

```text
users
favorites
wishlist
reviews
```

O Flyway controla a evolução do schema através de migrations versionadas.

A migration inicial está localizada em:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

O Hibernate utiliza:

```yaml
ddl-auto: validate
```

Assim:

* o Flyway controla o schema;
* o Hibernate valida a compatibilidade das entidades com o banco;
* alterações estruturais não são geradas automaticamente pelo Hibernate.

---

## Regras de integridade

* `username` deve ser único.
* `email` deve ser único.
* Um jogo não pode aparecer duas vezes nos Favoritos do mesmo usuário.
* Um jogo não pode aparecer duas vezes na Lista de Desejos do mesmo usuário.
* Um usuário pode possuir apenas uma avaliação por jogo.
* Avaliações aceitam notas inteiras de 1 a 5.
* Um mesmo jogo pode estar simultaneamente nos Favoritos e na Lista de Desejos.
* A exclusão de um usuário remove automaticamente seus Favoritos, itens da Lista de Desejos e Avaliações através de `ON DELETE CASCADE`.

---

# Autenticação e segurança

O GameVault utiliza **autenticação baseada em sessão HTTP com Spring Security**.

Após o login bem-sucedido, o servidor mantém a autenticação através da sessão e do cookie:

```text
JSESSIONID
```

O usuário autenticado é representado internamente por:

```java
UsuarioPrincipal
```

Controllers que precisam identificar o usuário da sessão utilizam:

```java
@AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
```

Os endpoints privados não recebem um `usuarioId` arbitrário fornecido pelo cliente.

Exemplo:

```text
/api/usuarios/me/favoritos
```

em vez de:

```text
/api/usuarios/{usuarioId}/favoritos
```

Isso impede que um usuário tente acessar dados de outro usuário apenas alterando um identificador na URL.

---

## CSRF

A proteção contra **CSRF** permanece habilitada.

O cliente pode obter as informações do token através de:

```text
GET /api/csrf
```

Requisições que alteram estado, como `POST`, `PUT` e `DELETE`, devem utilizar um token CSRF válido quando aplicável.

---

## Endpoints de autenticação

| Método | Endpoint           | Descrição                          | Resposta         |
| ------ | ------------------ | ---------------------------------- | ---------------- |
| `GET`  | `/api/csrf`        | Obtém as informações do token CSRF | `200 OK`         |
| `POST` | `/api/auth/login`  | Autentica o usuário                | `204 No Content` |
| `POST` | `/api/auth/logout` | Encerra a sessão autenticada       | `204 No Content` |

O login utiliza:

```text
email
senha
```

Credenciais inválidas resultam em:

```text
401 Unauthorized
```

O logout:

* invalida a sessão;
* limpa a autenticação;
* remove o cookie `JSESSIONID`.

---

# Usuários e gerenciamento de conta

O módulo `user` implementa:

```text
user
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
└── service
```

## Regras implementadas

* Cadastro de usuário.
* Validação de username único.
* Validação de e-mail único.
* Validação e codificação da senha.
* Consulta do próprio perfil.
* Alteração de nome.
* Alteração de username.
* Alteração de e-mail.
* Alteração da URL da imagem de perfil.
* Alteração de senha.
* Validação da senha atual antes da troca.
* Exclusão da própria conta.
* Encerramento da sessão após exclusão.
* Remoção automática dos dados relacionados.

## Endpoints

| Método   | Endpoint                 | Descrição                 | Resposta         |
| -------- | ------------------------ | ------------------------- | ---------------- |
| `POST`   | `/api/usuarios`          | Cadastra um usuário       | `201 Created`    |
| `GET`    | `/api/usuarios/me`       | Consulta o próprio perfil | `200 OK`         |
| `PUT`    | `/api/usuarios/me`       | Atualiza o próprio perfil | `200 OK`         |
| `PUT`    | `/api/usuarios/me/senha` | Altera a própria senha    | `204 No Content` |
| `DELETE` | `/api/usuarios/me`       | Exclui a própria conta    | `204 No Content` |

O cadastro é público.

Os demais endpoints de gerenciamento exigem autenticação.

---

# Favoritos

A funcionalidade de Favoritos está concluída no back-end.

## Regras implementadas

* Adicionar um jogo aos Favoritos.
* Listar os Favoritos do usuário autenticado.
* Remover um jogo.
* Impedir duplicidade.
* Retornar erro controlado quando o Favorito não existe.
* Garantir a unicidade no banco.
* Associar as operações ao usuário autenticado.
* Consultar se determinado jogo está favoritado para composição do detalhe personalizado.

## Endpoints

| Método   | Endpoint                                  | Descrição                      | Resposta         |
| -------- | ----------------------------------------- | ------------------------------ | ---------------- |
| `POST`   | `/api/usuarios/me/favoritos`              | Adiciona um jogo aos Favoritos | `201 Created`    |
| `GET`    | `/api/usuarios/me/favoritos`              | Lista os Favoritos             | `200 OK`         |
| `DELETE` | `/api/usuarios/me/favoritos/{rawgGameId}` | Remove um jogo dos Favoritos   | `204 No Content` |

---

# Lista de Desejos

A funcionalidade de Lista de Desejos está concluída no back-end.

## Regras implementadas

* Adicionar um jogo à Lista de Desejos.
* Listar os jogos da Lista de Desejos.
* Remover um jogo.
* Impedir duplicidade.
* Retornar erro controlado quando o item não existe.
* Garantir unicidade no banco.
* Associar as operações ao usuário autenticado.
* Permitir que um jogo esteja simultaneamente nos Favoritos e na Lista de Desejos.
* Consultar se determinado jogo está na Lista de Desejos para composição do detalhe personalizado.

## Endpoints

| Método   | Endpoint                                      | Descrição        | Resposta         |
| -------- | --------------------------------------------- | ---------------- | ---------------- |
| `POST`   | `/api/usuarios/me/lista-desejos`              | Adiciona um jogo | `201 Created`    |
| `GET`    | `/api/usuarios/me/lista-desejos`              | Lista os jogos   | `200 OK`         |
| `DELETE` | `/api/usuarios/me/lista-desejos/{rawgGameId}` | Remove um jogo   | `204 No Content` |

---

# Avaliações

A funcionalidade de Avaliações está concluída no back-end.

## Regras implementadas

* Notas inteiras entre 1 e 5.
* Uma avaliação por usuário e jogo.
* Atualização da avaliação existente quando a nota é alterada.
* Manutenção do mesmo registro durante a atualização.
* Consulta da avaliação do usuário autenticado.
* Remoção da avaliação.
* Cálculo da média das avaliações de cada jogo.
* Contagem da quantidade de avaliações.
* Jogos sem avaliações possuem média ausente.
* Rejeição de notas inválidas.
* Associação das operações privadas ao usuário autenticado.
* Integração da média, quantidade e avaliação individual aos detalhes do jogo.

## Endpoints

| Método   | Endpoint                                    | Descrição                      | Resposta         |
| -------- | ------------------------------------------- | ------------------------------ | ---------------- |
| `PUT`    | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Cria ou atualiza uma avaliação | `200 OK`         |
| `GET`    | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Consulta a própria avaliação   | `200 OK`         |
| `DELETE` | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Remove a própria avaliação     | `204 No Content` |
| `GET`    | `/api/jogos/{rawgGameId}/avaliacoes/resumo` | Consulta média e quantidade    | `200 OK`         |

O resumo das avaliações é público.

Um jogo sem avaliações locais é representado conceitualmente por:

```json
{
  "rawgGameId": 3498,
  "media": null,
  "quantidade": 0
}
```

A ausência de avaliações não é tratada como nota zero.

---

# Catálogo e busca de jogos

O módulo `jogo` representa a camada pública do catálogo.

Sua estrutura principal é:

```text
jogo
├── controller
│   └── JogoController
├── dto
│   ├── BuscaJogosResposta
│   ├── FiltroCatalogoJogos
│   ├── JogoDetalhesResposta
│   ├── JogoResumoResposta
│   ├── OrdenacaoJogo
│   └── PlataformaJogoDetalhesResposta
└── service
    └── JogoService
```

O catálogo não retorna os DTOs externos da RAWG diretamente.

O fluxo é:

```text
RAWG
  ↓
RawgClient
  ↓
JogoService
  ↓
DTO do GameVault
  ↓
JogoController
```

---

## Endpoint do catálogo

```text
GET /api/jogos
```

O endpoint é público.

Todos os filtros são opcionais, com exceção das validações aplicadas quando um parâmetro é efetivamente informado.

Parâmetros disponíveis:

| Parâmetro          | Tipo    | Descrição                               |
| ------------------ | ------- | --------------------------------------- |
| `nome`             | texto   | Pesquisa pelo nome do jogo              |
| `genero`           | texto   | Filtra pelo gênero                      |
| `plataforma`       | inteiro | Filtra pelo identificador da plataforma |
| `desenvolvedora`   | texto   | Filtra pela desenvolvedora              |
| `publicadora`      | texto   | Filtra pela publicadora                 |
| `lancamentoInicio` | data    | Início do período de lançamento         |
| `lancamentoFim`    | data    | Fim do período de lançamento            |
| `ordenacao`        | enum    | Define a ordenação                      |
| `pagina`           | inteiro | Página solicitada, padrão `1`           |

Exemplo:

```text
GET /api/jogos?nome=portal&genero=action&plataforma=4&ordenacao=METACRITIC&pagina=1
```

---

## Ordenações

As ordenações disponíveis são:

```text
POPULARIDADE
AVALIACAO_RAWG
METACRITIC
LANCAMENTO
NOME
```

O GameVault converte internamente essas opções para os parâmetros correspondentes da RAWG.

A popularidade não utiliza divisão semanal ou mensal.

---

## Regras do catálogo

* `nome` é opcional.
* Quando `nome` é informado, não pode conter apenas espaços.
* A página padrão é `1`.
* A página deve ser maior ou igual a `1`.
* Cada consulta solicita até 20 resultados.
* Uma consulta sem resultados retorna uma lista vazia.
* O gênero é opcional.
* A plataforma é opcional e deve possuir identificador maior ou igual a `1`.
* Desenvolvedora e publicadora são opcionais.
* O período de lançamento é opcional.
* Quando as duas datas são informadas, o início não pode ser posterior ao fim.
* A ordenação é opcional.
* Os DTOs externos da RAWG não são expostos diretamente.

Exemplo conceitual de resposta:

```json
{
  "pagina": 1,
  "totalResultados": 1,
  "jogos": [
    {
      "rawgGameId": 4200,
      "nome": "Portal 2",
      "dataLancamento": "2011-04-18",
      "imagemFundo": "https://exemplo.com/portal-2.jpg",
      "notaRawg": 4.61,
      "quantidadeAvaliacoesRawg": 6900,
      "metacritic": 95
    }
  ]
}
```

---

# Home

O back-end disponibiliza três seções próprias para composição da Home.

## Jogos populares

```text
GET /api/jogos/populares
```

A popularidade utiliza a ordenação de popularidade fornecida pela RAWG, sem divisão em período semanal ou mensal.

---

## Lançamentos recentes

```text
GET /api/jogos/lancamentos-recentes
```

São considerados os lançamentos dos últimos **30 dias corridos**, incluindo o dia atual.

A data atual é obtida através de um `Clock` injetável, permitindo testes determinísticos.

---

## Jogos mais bem avaliados

```text
GET /api/jogos/mais-bem-avaliados
```

A seção utiliza a avaliação da RAWG como critério de ordenação.

---

## Paginação da Home

As três seções aceitam:

```text
pagina
```

com valor padrão:

```text
1
```

---

# Detalhes do jogo

O endpoint:

```text
GET /api/jogos/{rawgGameId}
```

retorna os detalhes completos utilizados pela aplicação.

Ele permanece público.

A resposta combina informações provenientes da RAWG com informações próprias do GameVault.

---

## Dados provenientes da RAWG

Entre os campos retornados estão:

* identificador RAWG;
* nome;
* descrição;
* data de lançamento;
* imagem de fundo;
* avaliação RAWG;
* quantidade de avaliações RAWG;
* Metacritic;
* tempo médio de jogo;
* classificação etária;
* gêneros;
* plataformas;
* requisitos mínimos por plataforma;
* requisitos recomendados por plataforma;
* desenvolvedoras;
* publicadoras;
* screenshots.

Screenshots marcadas pela RAWG como ocultas não são disponibilizadas no contrato público.

---

## Dados provenientes do GameVault

O detalhe também retorna:

```text
mediaAvaliacoesGameVault
quantidadeAvaliacoesGameVault
```

Esses dados são públicos.

Quando não existem avaliações:

```text
mediaAvaliacoesGameVault = null
quantidadeAvaliacoesGameVault = 0
```

---

## Dados personalizados

Quando existe um usuário autenticado, o mesmo endpoint também retorna:

```text
minhaAvaliacao
favoritado
naListaDesejos
```

Exemplo conceitual:

```json
{
  "minhaAvaliacao": 5,
  "favoritado": true,
  "naListaDesejos": false
}
```

Caso o usuário autenticado ainda não tenha avaliado o jogo:

```text
minhaAvaliacao = null
```

Para visitantes:

```text
minhaAvaliacao = null
favoritado = null
naListaDesejos = null
```

Assim, `false` mantém o significado de que um usuário autenticado realmente não marcou o jogo, enquanto `null` representa ausência de contexto de usuário.

---

# Integração com a API RAWG

A integração utiliza o cliente HTTP síncrono:

```text
RestClient
```

Essa escolha acompanha a arquitetura imperativa do projeto baseada em Spring MVC e Spring Data JPA.

A estrutura é:

```text
rawg
├── client
│   └── RawgClient
├── config
│   ├── RawgConfig
│   └── RawgProperties
├── dto
│   ├── RawgBuscaJogosResposta
│   ├── RawgJogoDetalhesResposta
│   ├── RawgJogoResumoResposta
│   ├── RawgPlataformaJogoResposta
│   ├── RawgReferenciaResposta
│   ├── RawgRequisitosResposta
│   ├── RawgScreenshotResposta
│   └── RawgScreenshotsResposta
└── exception
    ├── JogoRawgNaoEncontradoException
    ├── RawgApiKeyNaoConfiguradaException
    └── RawgIntegracaoException
```

---

## Operações utilizadas

O `RawgClient` realiza consultas equivalentes a:

```text
GET /games
GET /games/{id}
GET /games/{id}/screenshots
```

A consulta de catálogo pode enviar dinamicamente parâmetros como:

```text
search
page
page_size
genres
platforms
developers
publishers
dates
ordering
```

Somente filtros efetivamente informados são adicionados à requisição.

---

## Configuração

A integração possui:

* URL base configurável;
* chave obtida através de `RAWG_API_KEY`;
* timeout de conexão de 3 segundos;
* timeout de leitura de 10 segundos;
* validação da presença da chave antes da chamada externa.

Configuração atual:

```yaml
rawg:
  base-url: https://api.rawg.io/api
  api-key: ${RAWG_API_KEY:}
```

---

## Tratamento de falhas

O cliente trata explicitamente:

* jogo não encontrado;
* chave da API ausente;
* erros HTTP;
* resposta sem corpo;
* resposta estruturalmente inválida;
* falhas de comunicação;
* parâmetros inválidos de catálogo.

Os principais erros externos são convertidos pela API em:

```text
404 Not Found
502 Bad Gateway
503 Service Unavailable
```

---

# Tratamento global de erros

A aplicação utiliza:

```java
@RestControllerAdvice
```

e respostas baseadas em:

```text
ProblemDetail
```

Entre os cenários tratados estão:

* recurso inexistente — `404 Not Found`;
* dados de usuário duplicados — `409 Conflict`;
* Favorito duplicado — `409 Conflict`;
* item duplicado na Lista de Desejos — `409 Conflict`;
* senha inválida — `400 Bad Request`;
* avaliação inválida — `400 Bad Request`;
* parâmetros inválidos — `400 Bad Request`;
* falha de comunicação com a RAWG — `502 Bad Gateway`;
* chave RAWG ausente — `503 Service Unavailable`;
* acesso sem autenticação — `401 Unauthorized`.

---

# Estratégia de testes

O projeto utiliza testes de diferentes níveis.

## Testes de persistência

Validam, entre outros pontos:

* persistência de usuários;
* unicidade;
* relacionamentos;
* Favoritos duplicados;
* itens duplicados na Lista de Desejos;
* avaliações duplicadas;
* intervalo permitido das notas;
* agregações das avaliações.

---

## Testes unitários

Os serviços são testados com JUnit 5 e Mockito.

São cobertos cenários relacionados a:

* usuários;
* senhas;
* perfil;
* Favoritos;
* Lista de Desejos;
* Avaliações;
* catálogo;
* filtros;
* ordenação;
* Home;
* detalhes dos jogos;
* dados personalizados por usuário;
* conversão dos contratos da RAWG.

---

## Testes MVC

Os controllers são testados com MockMvc.

São validados:

* status HTTP;
* contratos JSON;
* validação de parâmetros;
* tratamento de exceções;
* autenticação;
* autorização;
* CSRF;
* utilização de `UsuarioPrincipal`;
* endpoints públicos;
* endpoints privados;
* Home;
* catálogo;
* detalhes públicos;
* detalhes personalizados para usuário autenticado;
* respostas `404`, `502` e `503`.

---

## Testes do cliente RAWG

O `RawgClient` é testado com:

```text
MockRestServiceServer
```

Nenhuma chamada real à RAWG é necessária durante a suíte automatizada.

São cobertos:

* consulta por ID;
* busca paginada;
* filtros;
* ordenações;
* envio da chave;
* desserialização;
* campos desconhecidos;
* detalhes expandidos;
* plataformas e requisitos;
* screenshots;
* jogo inexistente;
* erros HTTP;
* respostas inválidas;
* respostas sem corpo;
* chave ausente;
* falhas de comunicação.

---

## Testes de integração

Os testes de integração carregam o contexto completo do Spring e utilizam o MySQL real do ambiente de desenvolvimento.

O fluxo validado percorre:

```text
HTTP
 ↓
Spring Security
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
MySQL
```

Os fluxos já cobertos incluem:

### Autenticação

* cadastro;
* login;
* criação da sessão;
* credenciais inválidas;
* endpoints protegidos;
* logout;
* invalidação da sessão;
* CSRF.

### Perfil

* consulta;
* atualização;
* persistência;
* proteção contra acesso não autenticado.

### Alteração de senha

* validação da senha atual;
* geração do novo hash;
* rejeição da senha anterior;
* autenticação com a nova senha.

### Exclusão de conta

* remoção do usuário;
* remoção de Favoritos;
* remoção da Lista de Desejos;
* remoção das Avaliações;
* `ON DELETE CASCADE`;
* invalidação da sessão.

### Favoritos

* adição;
* listagem;
* remoção;
* duplicidade;
* associação ao usuário autenticado.

### Lista de Desejos

* adição;
* listagem;
* remoção;
* duplicidade;
* associação ao usuário autenticado;
* coexistência com Favoritos.

### Avaliações

* criação;
* atualização;
* consulta;
* média;
* quantidade;
* remoção;
* associação ao usuário autenticado.

---

# Validação atual

A suíte completa pode ser executada com:

```powershell
mvn clean test
```

Última validação completa:

```text
Tests run: 193
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

O repositório também foi validado sem alterações pendentes após a conclusão do bloco de detalhes de jogos.

---

# Executando o banco com Docker

Crie um arquivo `.env` na raiz do projeto:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

O arquivo `.env` não deve ser versionado.

Suba o banco:

```bash
docker compose up -d
```

Verifique:

```bash
docker ps
```

Para interromper:

```bash
docker compose stop
```

Para iniciar novamente:

```bash
docker compose start
```

Para remover os containers sem apagar os volumes:

```bash
docker compose down
```

O comando:

```bash
docker compose down -v
```

também remove os volumes e apaga os dados armazenados.

---

# Configuração da aplicação

A conexão com o MySQL utiliza:

```text
host: localhost
porta: 3306
database: gamevault
```

O host e a porta podem ser alterados por:

```env
DB_HOST=localhost
DB_PORT=3306
```

As credenciais são fornecidas por:

```env
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

A integração com a RAWG utiliza:

```env
RAWG_API_KEY=sua_chave
```

Nunca versione credenciais reais nem o arquivo `.env`.

---

# Estado atual do desenvolvimento

## Infraestrutura

* Java 21.
* Spring Boot.
* Maven.
* MySQL 8.4.
* Docker.
* Docker Compose.
* Flyway.
* Migration inicial.
* JPA.
* Tratamento global de exceções.

## Segurança

* Spring Security.
* Autenticação baseada em sessão.
* Login.
* Logout.
* `UsuarioPrincipal`.
* Proteção dos endpoints privados.
* CSRF.

## Usuários

* Cadastro.
* Perfil.
* Atualização dos dados.
* Alteração de senha.
* Exclusão da conta.
* Remoção em cascata dos dados relacionados.

## Favoritos

* Adição.
* Listagem.
* Remoção.
* Prevenção de duplicidade.
* Integração com os detalhes do jogo.

## Lista de Desejos

* Adição.
* Listagem.
* Remoção.
* Prevenção de duplicidade.
* Coexistência com Favoritos.
* Integração com os detalhes do jogo.

## Avaliações

* Criação e atualização.
* Consulta.
* Remoção.
* Média.
* Quantidade.
* Notas de 1 a 5.
* Uma avaliação por usuário e jogo.
* Resumo público.
* Integração com os detalhes do jogo.

## Catálogo de jogos

* Busca opcional por nome.
* Paginação.
* Filtro por gênero.
* Filtro por plataforma.
* Filtro por desenvolvedora.
* Filtro por publicadora.
* Filtro por período de lançamento.
* Ordenação por popularidade.
* Ordenação por avaliação RAWG.
* Ordenação por Metacritic.
* Ordenação por lançamento.
* Ordenação por nome.

## Home

* Jogos populares.
* Lançamentos recentes.
* Jogos mais bem avaliados.

## Detalhes de jogos

* Informações expandidas da RAWG.
* Classificação etária.
* Gêneros.
* Plataformas.
* Requisitos.
* Desenvolvedoras.
* Publicadoras.
* Screenshots.
* Avaliações do GameVault.
* Favoritos.
* Lista de Desejos.
* Personalização para usuário autenticado.
* Acesso público para visitantes.

## Integração RAWG

* `RestClient`.
* Consulta de catálogo.
* Consulta por ID.
* Consulta de screenshots.
* Filtros.
* Ordenação.
* Paginação.
* Timeouts.
* Tratamento de falhas.
* Proteção da API key.

## Validação

* 193 testes automatizados.
* 0 falhas.
* 0 erros.
* `BUILD SUCCESS`.

---

# Próximos passos

* Revisar os requisitos restantes do back-end.
* Identificar eventuais funcionalidades ainda não implementadas na V1.
* Atualizar a documentação principal após o fechamento do back-end.
* Iniciar a camada de front-end somente após a conclusão e validação da etapa atual.

---

# Autor

**Isaque Costa da Cunha**
