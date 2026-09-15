# GameVault

O **GameVault** é uma aplicação web para descoberta, organização e avaliação de jogos.

O projeto utiliza a API pública da **RAWG** como fonte externa do catálogo de jogos e mantém no banco de dados apenas os dados próprios da aplicação, como usuários, favoritos, lista de desejos e avaliações.

> **Status:** em desenvolvimento
>
> **Etapa atual:** desenvolvimento Back-end — filtro opcional por gênero na busca pública de jogos concluído e validado.
>
> **Próximo foco:** implementação do filtro por plataforma na busca de jogos.

---

## Funcionalidades planejadas para a V1

* Cadastro e autenticação de usuários.
* Gerenciamento da própria conta.
* Perfil do usuário com imagem.
* Busca de jogos.
* Listagem de jogos populares.
* Lançamentos recentes.
* Jogos mais bem avaliados.
* Favoritos.
* Lista de Desejos.
* Avaliação de jogos com notas de 1 a 5.
* Integração com a API RAWG.

---

# Tecnologias

## Back-end

* Java 21
* Spring Boot 4.1.1
* Spring Web
* Spring Data JPA
* Bean Validation
* Spring Security
* Maven
* Spring RestClient

## Banco de dados e infraestrutura

* MySQL 8.4 LTS
* Docker
* Docker Compose
* Flyway
* DBeaver

## Testes

* JUnit 5
* Mockito
* MockMvc
* Spring Boot Test
* Spring Security Test

---

# Arquitetura

O projeto segue uma arquitetura de **monólito modular por funcionalidade**.

A estrutura atual é organizada em módulos como:

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

A integração com a RAWG está isolada no módulo `rawg`, evitando acoplamento direto entre a API externa e as demais funcionalidades do sistema.

---

# Persistência

O catálogo completo de jogos **não é persistido localmente**.

Os jogos são identificados internamente pelo ID fornecido pela RAWG.

O banco de dados do GameVault possui atualmente:

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

Dessa forma:

* o Flyway controla o schema;
* o Hibernate apenas valida a compatibilidade entre entidades e banco.

---

## Regras de integridade implementadas

* `username` deve ser único.
* `email` deve ser único.
* Um jogo não pode aparecer duas vezes nos Favoritos do mesmo usuário.
* Um jogo não pode aparecer duas vezes na Lista de Desejos do mesmo usuário.
* Um usuário pode possuir apenas uma avaliação por jogo.
* Avaliações aceitam notas inteiras de 1 a 5.
* Um jogo pode estar simultaneamente nos Favoritos e na Lista de Desejos.
* A exclusão de um usuário remove automaticamente seus Favoritos, itens da Lista de Desejos e Avaliações através de `ON DELETE CASCADE`.

---

# Autenticação e segurança

O GameVault utiliza **autenticação baseada em sessão HTTP com Spring Security**.

Após o login bem-sucedido, o servidor mantém a autenticação através da sessão e do cookie `JSESSIONID`.

O usuário autenticado é representado internamente por `UsuarioPrincipal`.

As funcionalidades privadas utilizam:

```java
@AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
```

para obter o usuário da sessão.

Dessa forma, os endpoints privados não recebem mais um `usuarioId` fornecido pelo cliente.

Exemplo:

```text
/api/usuarios/me/favoritos
```

em vez de:

```text
/api/usuarios/{usuarioId}/favoritos
```

Isso evita que um usuário tente acessar dados pertencentes a outro usuário simplesmente alterando um ID na URL.

---

## CSRF

O Spring Security mantém proteção contra **CSRF** habilitada.

O endpoint:

```text
GET /api/csrf
```

pode ser utilizado pelo cliente para obter as informações necessárias para requisições protegidas por CSRF.

Requisições que alteram estado, como `POST`, `PUT` e `DELETE`, devem utilizar um token CSRF válido quando aplicável.

---

## Endpoints de autenticação

| Método | Endpoint           | Descrição                       | Resposta esperada |
| ------ | ------------------ | ------------------------------- | ----------------- |
| `GET`  | `/api/csrf`        | Obtém informações do token CSRF | `200 OK`          |
| `POST` | `/api/auth/login`  | Autentica o usuário             | `204 No Content`  |
| `POST` | `/api/auth/logout` | Encerra a sessão autenticada    | `204 No Content`  |

O login utiliza os parâmetros:

```text
email
senha
```

Em caso de credenciais inválidas:

```text
401 Unauthorized
```

O logout:

* invalida a sessão;
* limpa a autenticação;
* remove o cookie `JSESSIONID`.

---

# Usuários e gerenciamento de conta

O módulo `user` implementa atualmente:

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
* Encerramento da sessão após a exclusão.
* Remoção automática dos dados relacionados através de `ON DELETE CASCADE`.

---

## Endpoints de usuário

| Método   | Endpoint                 | Descrição                 | Resposta esperada |
| -------- | ------------------------ | ------------------------- | ----------------- |
| `POST`   | `/api/usuarios`          | Cadastra um usuário       | `201 Created`     |
| `GET`    | `/api/usuarios/me`       | Consulta o próprio perfil | `200 OK`          |
| `PUT`    | `/api/usuarios/me`       | Atualiza o próprio perfil | `200 OK`          |
| `PUT`    | `/api/usuarios/me/senha` | Altera a própria senha    | `204 No Content`  |
| `DELETE` | `/api/usuarios/me`       | Exclui a própria conta    | `204 No Content`  |

O cadastro é público.

Os demais endpoints de gerenciamento exigem uma sessão autenticada.

---

# Favoritos

A funcionalidade de Favoritos está concluída no back-end.

Estrutura:

```text
favorito
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Regras implementadas

* Adicionar um jogo aos Favoritos.
* Listar os Favoritos do usuário autenticado.
* Remover um jogo dos Favoritos.
* Impedir duplicidade.
* Retornar erro controlado quando um Favorito não existe.
* Garantir a unicidade também no banco de dados.
* Associar todas as operações exclusivamente ao usuário autenticado.

## Endpoints

| Método   | Endpoint                                  | Descrição                      | Resposta esperada |
| -------- | ----------------------------------------- | ------------------------------ | ----------------- |
| `POST`   | `/api/usuarios/me/favoritos`              | Adiciona um jogo aos Favoritos | `201 Created`     |
| `GET`    | `/api/usuarios/me/favoritos`              | Lista os Favoritos             | `200 OK`          |
| `DELETE` | `/api/usuarios/me/favoritos/{rawgGameId}` | Remove um jogo dos Favoritos   | `204 No Content`  |

---

# Lista de Desejos

A funcionalidade de Lista de Desejos está concluída no back-end.

Estrutura:

```text
listadesejos
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Regras implementadas

* Adicionar um jogo à Lista de Desejos.
* Listar os jogos da Lista de Desejos.
* Remover um jogo.
* Impedir duplicidade.
* Retornar erro controlado quando um item não existe.
* Garantir unicidade também no banco.
* Associar todas as operações ao usuário autenticado.
* Permitir que o mesmo jogo esteja simultaneamente nos Favoritos e na Lista de Desejos.

## Endpoints

| Método   | Endpoint                                      | Descrição                           | Resposta esperada |
| -------- | --------------------------------------------- | ----------------------------------- | ----------------- |
| `POST`   | `/api/usuarios/me/lista-desejos`              | Adiciona um jogo à Lista de Desejos | `201 Created`     |
| `GET`    | `/api/usuarios/me/lista-desejos`              | Lista os jogos da Lista de Desejos  | `200 OK`          |
| `DELETE` | `/api/usuarios/me/lista-desejos/{rawgGameId}` | Remove um jogo da Lista de Desejos  | `204 No Content`  |

---

# Avaliações

A funcionalidade de Avaliações está concluída no back-end.

Estrutura:

```text
avaliacao
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Regras implementadas

* Permitir notas entre 1 e 5.
* Garantir apenas uma avaliação por usuário e jogo.
* Atualizar a avaliação existente quando o usuário muda sua nota.
* Manter o mesmo registro durante a atualização.
* Consultar a avaliação do usuário autenticado.
* Remover uma avaliação.
* Calcular a média das avaliações de um jogo.
* Contar a quantidade de avaliações.
* Representar jogos sem avaliações com média ausente.
* Rejeitar notas inválidas.
* Associar avaliações privadas exclusivamente ao usuário autenticado.

## Endpoints

| Método   | Endpoint                                    | Descrição                                 | Resposta esperada |
| -------- | ------------------------------------------- | ----------------------------------------- | ----------------- |
| `PUT`    | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Cria ou atualiza uma avaliação            | `200 OK`          |
| `GET`    | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Consulta a própria avaliação              | `200 OK`          |
| `DELETE` | `/api/usuarios/me/avaliacoes/{rawgGameId}`  | Remove a própria avaliação                | `204 No Content`  |
| `GET`    | `/api/jogos/{rawgGameId}/avaliacoes/resumo` | Consulta média e quantidade de avaliações | `200 OK`          |

O resumo das avaliações de um jogo é público.

Um jogo sem avaliações é representado conceitualmente como:

```json
{
  "rawgGameId": 3498,
  "media": null,
  "quantidade": 0
}
```

A ausência de avaliações não é confundida com uma avaliação de nota zero.

---

# Busca de jogos

O módulo `jogo` representa a camada pública de consulta ao catálogo externo.

Sua estrutura atual é:

```text
jogo
├── controller
│   └── JogoController
├── dto
│   ├── BuscaJogosResposta
│   └── JogoResumoResposta
└── service
    └── JogoService
```

O `JogoService` utiliza o `RawgClient`, mas converte os DTOs externos em DTOs próprios do GameVault. Dessa forma, o contrato público da aplicação não fica diretamente acoplado à estrutura retornada pela RAWG.

## Endpoint de busca

| Método | Endpoint                                  | Descrição                         | Resposta esperada |
| ------ | ----------------------------------------- | --------------------------------- | ----------------- |
| `GET` | `/api/jogos?nome={nome}&pagina={pagina}[&genero={genero}]` | Busca jogos por nome, com paginação e filtro opcional por gênero | `200 OK` |

O endpoint é público e pode ser utilizado sem autenticação.

Regras implementadas:

* o nome do jogo é obrigatório e não pode estar em branco;
* a primeira página é utilizada quando `pagina` não é informada;
* a página deve ser maior ou igual a 1;
* cada página solicita até 20 resultados da RAWG;
* uma busca sem resultados retorna uma lista vazia;
* os DTOs externos não são retornados diretamente ao consumidor;
* o filtro `genero` é opcional;
* o gênero deve ser informado pelo slug reconhecido pela RAWG, como `action` ou `indie`;
* espaços no início e no fim do gênero são removidos;
* quando o gênero estiver ausente ou em branco, a busca não aplica esse filtro.

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

Falhas de comunicação com a RAWG retornam `502 Bad Gateway`. A ausência da chave de integração retorna `503 Service Unavailable`.

---

# Integração com a API RAWG

A integração inicial com a RAWG utiliza o cliente HTTP síncrono `RestClient`, compatível com a arquitetura imperativa do projeto baseada em Spring MVC e Spring Data JPA.

A estrutura implementada é:

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
│   └── RawgJogoResumoResposta
└── exception
    ├── JogoRawgNaoEncontradoException
    ├── RawgApiKeyNaoConfiguradaException
    └── RawgIntegracaoException
```
O `RawgClient` realiza atualmente duas operações na RAWG:

```text
GET /games/{id}
GET /games?search={nome}&page={pagina}&page_size=20[&genres={genero}]
```

O parâmetro `genres` somente é enviado quando `genero` não é nulo nem está em branco.

A primeira consulta obtém os detalhes de um jogo pelo ID da RAWG. A segunda realiza uma busca paginada por nome, utilizando páginas com 20 resultados.

A busca valida o nome informado e o número da página antes da comunicação externa. Espaços no início e no fim do nome são removidos.

A resposta paginada mapeia:

* a quantidade total de resultados;
* a lista resumida de jogos da página atual.

Os campos `next` e `previous` retornados pela RAWG não são mantidos no DTO do GameVault, pois essas URLs externas podem incluir a chave da API. A navegação entre páginas será controlada posteriormente pela própria API do GameVault.

A resposta externa é mapeada para os seguintes dados:

| Campo da RAWG      | Campo no GameVault |
| ------------------ | ------------------ |
| `id`               | `id`               |
| `name`             | `nome`             |
| `released`         | `dataLancamento`   |
| `background_image` | `imagemFundo`      |
| `rating`           | `notaRawg`         |
| `ratings_count`    | `totalAvaliacoes`  |
| `metacritic`       | `metacritic`       |

Campos adicionais retornados pela RAWG são ignorados durante a desserialização.

A configuração utiliza:

* URL base externa configurável;
* chave da RAWG obtida pela variável `RAWG_API_KEY`;
* timeout de conexão de 3 segundos;
* timeout de leitura de 10 segundos;
* validação da presença da chave antes da requisição.

O cliente trata explicitamente:

* jogo não encontrado na RAWG;
* chave da API ausente;
* erros HTTP retornados pela RAWG;
* resposta sem corpo;
* falhas de comunicação com o serviço externo;
* parâmetros de busca inválidos;
* resposta com estrutura inválida.

O `RawgClient` é consumido pelo `JogoService`, que converte as respostas externas em DTOs próprios do GameVault. A busca é exposta publicamente pelo `JogoController`, sem revelar a API key nem retornar diretamente os contratos da RAWG.

---

# Tratamento de erros

A API utiliza tratamento global de exceções com:

```java
@RestControllerAdvice
```

e respostas no padrão:

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
* validação de requisição — `400 Bad Request`;
* falha de comunicação com a RAWG — `502 Bad Gateway`;
* integração RAWG sem chave configurada — `503 Service Unavailable`;
* acesso sem autenticação — `401 Unauthorized`.

---

# Estratégia de testes

O projeto utiliza diferentes níveis de testes.

## Testes de persistência

Validam:

* persistência de usuários;
* restrições de unicidade;
* relacionamentos entre usuários e demais entidades;
* Favoritos duplicados;
* itens duplicados na Lista de Desejos;
* avaliações duplicadas;
* intervalo permitido para notas;
* consultas e agregações de avaliações.

---

## Testes unitários

Os serviços são testados com JUnit e Mockito.

Entre as regras validadas estão:

* cadastro de usuário;
* validação e codificação de senha;
* atualização de perfil;
* alteração de senha;
* exclusão de conta;
* Favoritos;
* Lista de Desejos;
* Avaliações;
* cenários de erro;
* conversão dos DTOs da RAWG para os DTOs públicos de jogos;
* busca de jogos com e sem resultados.

---

## Testes MVC

Os controllers são testados com `MockMvc`.

São validados:

* status HTTP;
* DTOs;
* validação de requisições;
* tratamento de exceções;
* autenticação;
* autorização;
* CSRF;
* utilização do usuário autenticado;
* acesso público à busca de jogos;
* página padrão da busca;
* nome obrigatório e página válida;
* estrutura JSON da resposta;
* falhas externas representadas por `502` e `503`.

---

## Testes do cliente RAWG

O cliente HTTP é validado com `MockRestServiceServer`, sem realizar chamadas reais à API externa durante os testes.

Os cenários cobertos incluem:

* requisição correta pelo ID do jogo;
* envio da chave como parâmetro;
* mapeamento da resposta JSON;
* campos externos desconhecidos;
* jogo não encontrado;
* erros HTTP;
* resposta sem corpo;
* chave da API não configurada;
* falha de comunicação.
* busca paginada por nome;
* remoção de espaços desnecessários do termo pesquisado;
* envio dos parâmetros `search`, `page` e `page_size`;
* mapeamento do total e dos jogos encontrados;
* busca sem resultados;
* rejeição de nome vazio e página inválida;
* resposta paginada com estrutura inválida;

---

## Testes de integração

Os fluxos de integração carregam o contexto completo do Spring e utilizam o MySQL real do ambiente de desenvolvimento.

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

Entre os fluxos validados estão:

### Autenticação

* cadastro público;
* login;
* criação da sessão;
* rejeição de credenciais inválidas;
* proteção de endpoints privados;
* logout;
* invalidação da sessão;
* CSRF.

### Perfil

* consulta do próprio perfil;
* atualização do perfil;
* persistência das alterações;
* proteção contra acesso não autenticado.

### Alteração de senha

* validação da senha atual;
* geração de novo hash;
* rejeição da senha antiga após a alteração;
* autenticação com a nova senha.

### Exclusão de conta

* remoção do usuário;
* remoção dos Favoritos;
* remoção da Lista de Desejos;
* remoção das Avaliações;
* validação real do `ON DELETE CASCADE`;
* invalidação da sessão após a exclusão.

### Favoritos

* adição;
* persistência;
* listagem;
* remoção;
* prevenção de duplicidade;
* associação ao usuário autenticado.

### Lista de Desejos

* adição;
* persistência;
* listagem;
* remoção;
* prevenção de duplicidade;
* coexistência do mesmo jogo nos Favoritos e na Lista de Desejos;
* associação ao usuário autenticado.

### Avaliações

* criação;
* persistência;
* atualização da nota;
* manutenção do mesmo registro;
* consulta;
* média;
* quantidade;
* remoção;
* associação ao usuário autenticado.

A suíte completa é executada com:

```powershell
mvn test
```

e atualmente deve finalizar com:

```text
BUILD SUCCESS
```

---

# Executando o banco com Docker

Crie um arquivo `.env` na raiz do projeto:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

> O arquivo `.env` não deve ser versionado.

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

> `docker compose down -v` também remove os volumes e apaga os dados armazenados.

---

# Configuração da aplicação

A aplicação utiliza as credenciais definidas no `.env`.

Exemplo:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

Por padrão:

```text
host: localhost
porta: 3306
database: gamevault
```

Também podem ser configuradas:

```env
DB_HOST=localhost
DB_PORT=3306
```

A integração com a RAWG utiliza:

```env
RAWG_API_KEY=sua_chave
```

> Nunca versione o arquivo `.env` nem credenciais reais.

---

# Estado atual do desenvolvimento

## Concluído

### Infraestrutura

* Configuração inicial do Spring Boot.
* Java 21.
* Maven.
* MySQL 8.4.
* Docker.
* Docker Compose.
* Flyway.
* Migration inicial.
* Entidades JPA.
* Repositories Spring Data JPA.
* Tratamento global de exceções.

### Segurança

* Spring Security.
* Autenticação por sessão.
* Login por e-mail e senha.
* Logout.
* `UsuarioPrincipal`.
* Proteção dos endpoints privados.
* Utilização do usuário autenticado nas regras privadas.
* Proteção CSRF.
* Endpoint para obtenção do token CSRF.

### Usuários

* Cadastro.
* Validação de username e e-mail únicos.
* Codificação de senha.
* Consulta do próprio perfil.
* Atualização do perfil.
* Alteração de senha.
* Validação da senha atual.
* Exclusão da conta.
* Invalidação da sessão após exclusão.
* Remoção em cascata dos dados relacionados.

### Favoritos

* Adição.
* Listagem.
* Remoção.
* Prevenção de duplicidade.
* Associação ao usuário autenticado.
* Testes unitários, MVC e integração.

### Lista de Desejos

* Adição.
* Listagem.
* Remoção.
* Prevenção de duplicidade.
* Associação ao usuário autenticado.
* Coexistência com Favoritos.
* Testes unitários, MVC e integração.

### Avaliações

* Criação e atualização.
* Consulta.
* Remoção.
* Média.
* Quantidade.
* Notas entre 1 e 5.
* Uma avaliação por usuário e jogo.
* Associação ao usuário autenticado.
* Endpoint público de resumo.
* Testes unitários, MVC e integração.

### Busca de jogos

* Módulo público `jogo`.
* Serviço de consulta ao catálogo externo.
* DTOs próprios para a resposta pública.
* Endpoint `GET /api/jogos`.
* Busca por nome.
* Paginação com primeira página padrão.
* Validação de nome e página.
* Acesso permitido para visitantes.
* Tratamento de falhas externas com `502` e `503`.
* Testes unitários e MVC.
* Filtro opcional por gênero no cliente RAWG.
* Propagação do filtro pelas camadas controller e service.
* Busca com gênero informado.
* Busca sem gênero e com gênero em branco.

### Integração RAWG

* Cliente HTTP baseado em `RestClient`.
* Configuração externa da URL base e da API key.
* Timeouts de conexão e leitura.
* Consulta de jogo pelo ID da RAWG.
* Mapeamento da resposta externa.
* Tratamento de falhas HTTP e de comunicação.
* Validação de chave ausente e resposta sem corpo.
* Testes isolados com `MockRestServiceServer`.
* Busca paginada de jogos por nome.
* Página fixa com 20 resultados.
* DTO específico para a resposta paginada.
* DTO resumido para os jogos da listagem.
* Validação do nome pesquisado e da página solicitada.
* Tratamento de busca vazia e resposta estruturalmente inválida.

### Validação

* Fluxos principais validados ponta a ponta.
* Suíte completa com 166 testes.
* Build finalizado com `BUILD SUCCESS`.

---

# Próximos passos

* Implementar filtro por plataforma na busca de jogos.
* Implementar ordenações da busca em blocos posteriores.
* Implementar listagem de jogos populares.
* Implementar lançamentos recentes.
* Implementar jogos mais bem avaliados.
* Integrar os dados da RAWG com Favoritos, Lista de Desejos e Avaliações.
* Preparar posteriormente a camada de front-end.

---

# Autor

**Isaque Costa da Cunha**
