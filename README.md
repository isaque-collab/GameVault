# GameVault

O **GameVault** é uma aplicação web para descoberta, organização e avaliação de jogos. O projeto utiliza a API pública da **RAWG** como fonte externa do catálogo e mantém no banco de dados apenas os dados próprios da aplicação, como usuários, favoritos, wishlist e avaliações.

> **Status:** em desenvolvimento
> **Etapa atual:** Desenvolvimento Back-end — funcionalidades de Favoritos, Lista de Desejos e Avaliações concluídas e validadas ponta a ponta. Próximo foco: cadastro e gerenciamento de usuário.

## Funcionalidades planejadas para a V1

* Cadastro, autenticação e gerenciamento de conta.
* Perfil do usuário com foto.
* Busca de jogos.
* Listagem de jogos populares.
* Lançamentos recentes.
* Jogos mais bem avaliados.
* Favoritos.
* Lista de Desejos.
* Avaliação de jogos com notas de 1 a 5.
* Integração com a API RAWG.

## Tecnologias

### Back-end

* Java 21
* Spring Boot 4.1.1
* Spring Web
* Spring Data JPA
* Bean Validation
* Spring Security
* Maven

### Banco de dados e infraestrutura

* MySQL 8.4 LTS
* Docker
* Docker Compose
* Flyway
* DBeaver

### Testes

* JUnit 5
* Mockito
* MockMvc
* Spring Boot Test
* Spring Security Test

## Arquitetura

O projeto segue uma arquitetura de **monólito modular por funcionalidade**.

A estrutura implementada atualmente é organizada em módulos como:

```text
com.gamevault
├── avaliacao
├── favorito
├── listadesejos
├── shared
└── user
```

Cada funcionalidade evolui conforme a necessidade utilizando camadas como:

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

A integração com a RAWG será isolada em um cliente próprio, evitando acoplamento direto entre a API externa e as demais camadas da aplicação.

## Persistência

O catálogo de jogos **não é persistido localmente**. Os jogos são identificados pelo ID fornecido pela RAWG.

O banco de dados do GameVault contém atualmente:

```text
users
favorites
wishlist
reviews
```

O Flyway controla a evolução do schema por meio de migrations versionadas.

### Regras já implementadas no banco

* `username` único.
* `email` único.
* Um mesmo jogo não pode aparecer duas vezes nos favoritos do mesmo usuário.
* Um mesmo jogo não pode aparecer duas vezes na wishlist do mesmo usuário.
* Um usuário pode possuir apenas uma avaliação por jogo.
* Avaliações aceitam notas inteiras de 1 a 5.
* A exclusão de um usuário remove seus favoritos, itens da wishlist e avaliações por `ON DELETE CASCADE`.
* Um jogo pode estar simultaneamente nos favoritos e na wishlist do mesmo usuário.

## Estrutura de banco atual

A migration inicial está em:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

Ela cria as tabelas:

```text
users
favorites
wishlist
reviews
```

O Hibernate está configurado com:

```yaml
ddl-auto: validate
```

Assim, o schema é controlado pelo Flyway e o Hibernate apenas valida se as entidades correspondem ao banco.

## Favoritos

A funcionalidade de Favoritos foi concluída no back-end e possui atualmente:

```text
favorito
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

### Regras implementadas

* Adicionar um jogo aos favoritos de um usuário.
* Listar os favoritos de um usuário.
* Remover um jogo dos favoritos.
* Impedir que o mesmo jogo seja favoritado duas vezes pelo mesmo usuário.
* Retornar erro controlado quando o favorito não existe.
* Retornar erro controlado quando o usuário não existe.
* Manter a restrição de unicidade também no banco de dados como garantia de integridade.

### Endpoints atuais

| Método   | Endpoint                                           | Descrição                      | Resposta esperada |
| -------- | -------------------------------------------------- | ------------------------------ | ----------------- |
| `POST`   | `/api/usuarios/{usuarioId}/favoritos`              | Adiciona um jogo aos favoritos | `201 Created`     |
| `GET`    | `/api/usuarios/{usuarioId}/favoritos`              | Lista os favoritos do usuário  | `200 OK`          |
| `DELETE` | `/api/usuarios/{usuarioId}/favoritos/{rawgGameId}` | Remove um jogo dos favoritos   | `204 No Content`  |

Os erros da API são tratados de forma centralizada com `ProblemDetail`. Entre os cenários cobertos estão favorito duplicado (`409 Conflict`) e recursos inexistentes (`404 Not Found`).

> O vínculo entre o `usuarioId` recebido pela rota e o usuário autenticado ainda será reforçado quando a autenticação/autorização da aplicação for implementada.

## Lista de Desejos

A funcionalidade de Lista de Desejos foi concluída no back-end e possui atualmente:

```text
listadesejos
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

### Regras implementadas

* Adicionar um jogo à lista de desejos de um usuário.
* Listar os jogos da lista de desejos.
* Remover um jogo da lista de desejos.
* Impedir que o mesmo jogo seja adicionado duas vezes pelo mesmo usuário.
* Retornar erro controlado quando o item não existe.
* Retornar erro controlado quando o usuário não existe.
* Manter a restrição de unicidade também no banco de dados.
* Permitir que um mesmo jogo esteja simultaneamente nos Favoritos e na Lista de Desejos.

### Endpoints atuais

| Método   | Endpoint                                               | Descrição                           | Resposta esperada |
| -------- | ------------------------------------------------------ | ----------------------------------- | ----------------- |
| `POST`   | `/api/usuarios/{usuarioId}/lista-desejos`              | Adiciona um jogo à lista de desejos | `201 Created`     |
| `GET`    | `/api/usuarios/{usuarioId}/lista-desejos`              | Lista os jogos da lista de desejos  | `200 OK`          |
| `DELETE` | `/api/usuarios/{usuarioId}/lista-desejos/{rawgGameId}` | Remove um jogo da lista de desejos  | `204 No Content`  |

Os erros da Lista de Desejos também são tratados pelo mecanismo global com `ProblemDetail`.

Entre os cenários cobertos estão:

* item duplicado (`409 Conflict`);
* item inexistente (`404 Not Found`);
* usuário inexistente (`404 Not Found`);
* requisição inválida (`400 Bad Request`).

> O vínculo entre o `usuarioId` recebido pela rota e o usuário autenticado ainda será reforçado quando a autenticação/autorização da aplicação for implementada.

## Avaliações

A funcionalidade de Avaliações foi concluída no back-end e possui atualmente:

```text
avaliacao
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

### Regras implementadas

* Permitir que um usuário atribua uma nota de 1 a 5 a um jogo.
* Garantir apenas uma avaliação por usuário para cada jogo.
* Atualizar a avaliação existente quando o usuário atribui uma nova nota ao mesmo jogo.
* Manter o mesmo registro no banco durante a atualização, sem criar avaliações duplicadas.
* Consultar a avaliação atribuída pelo usuário a um jogo.
* Remover uma avaliação.
* Calcular a média das avaliações de um jogo.
* Contar a quantidade de avaliações recebidas por um jogo.
* Representar jogos sem avaliações com média ausente, em vez de utilizar nota `0`.
* Rejeitar notas fora do intervalo permitido.
* Retornar erro controlado quando uma avaliação não existe.
* Retornar erro controlado quando o usuário não existe.
* Manter as restrições de unicidade e intervalo de notas também no banco de dados.

### Endpoints atuais

| Método   | Endpoint                                            | Descrição                                        | Resposta esperada |
| -------- | --------------------------------------------------- | ------------------------------------------------ | ----------------- |
| `PUT`    | `/api/usuarios/{usuarioId}/avaliacoes/{rawgGameId}` | Cria ou atualiza a avaliação do usuário          | `200 OK`          |
| `GET`    | `/api/usuarios/{usuarioId}/avaliacoes/{rawgGameId}` | Consulta a avaliação do usuário para o jogo      | `200 OK`          |
| `DELETE` | `/api/usuarios/{usuarioId}/avaliacoes/{rawgGameId}` | Remove a avaliação                               | `204 No Content`  |
| `GET`    | `/api/jogos/{rawgGameId}/avaliacoes/resumo`         | Retorna média e quantidade de avaliações do jogo | `200 OK`          |

Um resumo de jogo sem avaliações é representado conceitualmente como:

```json
{
  "rawgGameId": 3498,
  "media": null,
  "quantidade": 0
}
```

Dessa forma, a ausência de avaliações não é confundida com uma avaliação de nota zero.

Os erros relacionados às Avaliações são tratados pelo mecanismo global com `ProblemDetail`.

Entre os cenários cobertos estão:

* avaliação inexistente (`404 Not Found`);
* usuário inexistente (`404 Not Found`);
* nota inválida (`400 Bad Request`);
* requisição inválida (`400 Bad Request`).

> O vínculo entre o `usuarioId` recebido pela rota e o usuário autenticado ainda será reforçado quando a autenticação/autorização da aplicação for implementada.

## Executando o banco com Docker

Crie um arquivo `.env` na raiz do projeto:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

> O arquivo `.env` não deve ser versionado.

Suba o MySQL:

```bash
docker compose up -d
```

Verifique se o container está em execução:

```bash
docker ps
```

Para interromper o container:

```bash
docker compose stop
```

Para iniciar novamente:

```bash
docker compose start
```

Para remover o container sem apagar o volume:

```bash
docker compose down
```

> `docker compose down -v` remove também o volume persistente e apaga os dados do banco.

## Configurando a aplicação

A aplicação carrega o arquivo `.env` localizado na raiz do projeto e reutiliza as credenciais configuradas para o MySQL.

Configure:

```env
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_USER=seu_usuario
MYSQL_PASSWORD=sua_senha
```

Por padrão, a aplicação utiliza:

```text
host: localhost
porta: 3306
database: gamevault
```

Caso seja necessário alterar o host ou a porta utilizados pela aplicação, podem ser definidas as variáveis:

```env
DB_HOST=localhost
DB_PORT=3306
```

A chave da RAWG será necessária quando a integração com a API for implementada:

```env
RAWG_API_KEY=sua_chave
```

> O arquivo `.env` contém informações sensíveis e não deve ser versionado.

## Executando os testes

Com o MySQL do Docker em execução e o arquivo `.env` configurado:

### Windows

```powershell
mvn test
```

O build deve terminar com:

```text
BUILD SUCCESS
```

## Estratégia de testes atual

O projeto possui testes em diferentes níveis para validar persistência, regras de negócio, camada HTTP e fluxos completos da aplicação.

### Persistência

Testes de integração dos repositories validam:

* persistência e consulta de usuários;
* relacionamento entre favoritos e usuários;
* bloqueio de favoritos duplicados;
* relacionamento entre wishlist e usuários;
* bloqueio de itens duplicados na wishlist;
* persistência de avaliações;
* bloqueio de avaliações duplicadas;
* rejeição de notas abaixo de 1;
* rejeição de notas acima de 5;
* consulta de avaliação por usuário e jogo;
* contagem de avaliações de um jogo;
* cálculo da média das avaliações;
* ausência de média quando um jogo ainda não possui avaliações.

### Regras de negócio de Favoritos

Testes unitários com Mockito validam o `FavoritoService`, incluindo:

* adição de favorito;
* prevenção de duplicidade;
* usuário inexistente;
* remoção de favorito;
* tentativa de remover favorito inexistente;
* listagem dos favoritos do usuário.

### Camada HTTP de Favoritos

Testes MVC com `MockMvc` validam:

* `POST` de favorito;
* `GET` da lista de favoritos;
* `DELETE` de favorito;
* retorno `409 Conflict` para duplicidade;
* retorno `404 Not Found` para recurso inexistente;
* retorno `400 Bad Request` para requisição inválida;
* comportamento de Spring Security e CSRF nos testes.

### Fluxo ponta a ponta de Favoritos

O fluxo de Favoritos possui teste de integração carregando o contexto completo do Spring e utilizando o MySQL real do ambiente de desenvolvimento.

O teste percorre:

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

São validados:

* adição de favorito;
* persistência no banco;
* listagem dos favoritos;
* remoção;
* prevenção de duplicidade;
* tratamento de usuário inexistente.

### Regras de negócio da Lista de Desejos

Testes unitários com Mockito validam o `ListaDesejosService`, incluindo:

* adição de jogo à lista de desejos;
* prevenção de duplicidade;
* usuário inexistente;
* remoção de item;
* tentativa de remover item inexistente;
* listagem dos jogos da lista de desejos.

### Camada HTTP da Lista de Desejos

Testes MVC com `MockMvc` validam:

* `POST` de item na lista de desejos;
* `GET` da lista de desejos;
* `DELETE` de item;
* retorno `409 Conflict` para duplicidade;
* retorno `404 Not Found` para recurso inexistente;
* retorno `400 Bad Request` para requisição inválida;
* comportamento de Spring Security e CSRF nos testes.

### Fluxo ponta a ponta da Lista de Desejos

A Lista de Desejos também possui teste de integração carregando o contexto completo do Spring e utilizando o MySQL real do ambiente de desenvolvimento.

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

São validados:

* adição de jogo à lista de desejos;
* persistência no banco;
* listagem;
* remoção;
* prevenção de duplicidade;
* tratamento de usuário inexistente;
* coexistência do mesmo jogo nos Favoritos e na Lista de Desejos.

A validação da coexistência garante uma regra importante do domínio: adicionar um jogo aos Favoritos não impede que o mesmo jogo também pertença à Lista de Desejos do usuário.

### Regras de negócio de Avaliações

Testes unitários com Mockito validam o `AvaliacaoService`, incluindo:

* criação de uma avaliação;
* atualização da nota de uma avaliação existente;
* validação de usuário inexistente;
* rejeição de nota abaixo do mínimo;
* rejeição de nota acima do máximo;
* rejeição de nota nula;
* remoção de avaliação;
* tentativa de remover avaliação inexistente;
* consulta da avaliação do usuário;
* cálculo da média das avaliações;
* contagem das avaliações de um jogo.

### Camada HTTP de Avaliações

Testes MVC com `MockMvc` validam:

* criação ou atualização de avaliação com `PUT`;
* consulta da avaliação do usuário;
* remoção da avaliação;
* retorno `404 Not Found` para avaliação inexistente;
* rejeição de notas abaixo de 1;
* rejeição de notas acima de 5;
* rejeição de requisição sem nota;
* retorno do resumo das avaliações;
* retorno de média ausente para jogos sem avaliações;
* comportamento de Spring Security e CSRF nos testes.

### Fluxo ponta a ponta de Avaliações

A funcionalidade de Avaliações possui teste de integração carregando o contexto completo do Spring e utilizando o MySQL real do ambiente de desenvolvimento.

O fluxo percorre:

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

São validados:

* estado de um jogo sem avaliações;
* criação de avaliação;
* persistência no banco;
* atualização da nota;
* manutenção do mesmo registro após a atualização;
* garantia de apenas uma avaliação por usuário e jogo;
* consulta da avaliação do usuário;
* cálculo da média das avaliações;
* contagem das avaliações;
* remoção da avaliação;
* retorno ao estado sem avaliações após a remoção;
* tratamento de usuário inexistente.

A validação da atualização garante uma regra central do domínio: ao alterar a nota atribuída a um jogo, a avaliação existente é atualizada em vez de uma segunda linha ser criada.

Os testes de persistência e integração utilizam transações quando aplicável para evitar resíduos de dados entre execuções.

## Estado atual do desenvolvimento

### Concluído

* Configuração inicial do projeto Spring Boot.
* Java 21 e Maven.
* MySQL 8.4 em Docker.
* Docker Compose.
* Flyway.
* Migration inicial.
* Entidades JPA:

  * `Usuario`
  * `Favorito`
  * `ItemListaDesejos`
  * `Avaliacao`
* Repositories Spring Data JPA.
* Testes de integração da camada de persistência.
* Padronização dos módulos e classes atuais do domínio.

#### Favoritos

* Regra de negócio de Favoritos.
* DTOs de Favoritos.
* API REST de Favoritos.
* Tratamento global das exceções utilizadas pela funcionalidade.
* Testes unitários do `FavoritoService`.
* Testes MVC do `FavoritoController`.
* Teste de integração do fluxo completo de Favoritos.

#### Lista de Desejos

* Regra de negócio da Lista de Desejos.
* DTOs da Lista de Desejos.
* API REST da Lista de Desejos.
* Tratamento global das exceções da Lista de Desejos.
* Testes unitários do `ListaDesejosService`.
* Testes MVC do `ListaDesejosController`.
* Teste de integração do fluxo completo da Lista de Desejos.
* Validação da independência entre Favoritos e Lista de Desejos.
* Validação da coexistência do mesmo jogo nas duas coleções.

#### Avaliações

* Consultas de avaliações por usuário e jogo.
* Consulta da média de avaliações.
* Contagem de avaliações por jogo.
* Regra de negócio de Avaliações.
* Atualização da avaliação existente sem duplicidade.
* Validação de notas entre 1 e 5.
* DTOs de Avaliações.
* API REST de Avaliações.
* Endpoint de resumo de avaliações por jogo.
* Tratamento global das exceções utilizadas pela funcionalidade.
* Testes unitários do `AvaliacaoService`.
* Testes MVC da API de Avaliações.
* Teste de integração do fluxo completo de Avaliações.
* Validação ponta a ponta da atualização de nota mantendo uma única avaliação.

#### Validação

* Suíte completa de testes validada com Maven.
* Build finalizado com `BUILD SUCCESS`.
* Favoritos validados ponta a ponta.
* Lista de Desejos validada ponta a ponta.
* Avaliações validadas ponta a ponta.

### Próximos passos

* Cadastro e gerenciamento de usuário.
* Autenticação e autorização com Spring Security.
* Proteção dos endpoints utilizando o usuário autenticado.
* Integração com a API RAWG.

## Autor

**Isaque Costa da Cunha**
