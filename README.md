# AutoBots - Automanager

Este projeto é a atividade prática ATVIII da disciplina de Desenvolvimento Web III, da FATEC São José dos Campos, sob orientação do Prof. Dr. Eng. Gerson Penha. Ele dá continuidade às atividades ATVI e ATVII, em que o micro-serviço automanager, construído com Java e Spring Boot, ganhou CRUDs e passou a atender os níveis de maturidade do Richardson Maturity Model, incluindo HATEOAS.

Nesta etapa foi feita a "atualização de base" pedida pelos investidores: o sistema deixou de registrar apenas clientes e passou a gerenciar empresas, usuários com perfis (cliente, fornecedor e funcionário), credenciais de acesso, veículos, mercadorias, serviços e vendas. Todas essas entidades possuem CRUD completo, status HTTP coerentes e links HATEOAS nas respostas.

## Versões utilizadas

- Java 17
- Spring Boot 2.6.7
- Spring HATEOAS
- Maven (gerenciador de dependências e build)
- Banco de dados H2 (em memória)

## Como rodar o projeto

### Opção 1: Spring Boot Dashboard (VS Code)

1. Instale a extensão **Spring Boot Dashboard** na aba de extensões do VS Code.
2. Abra a pasta `automanager` no VS Code.
3. Clique no ícone da folha do Spring Boot na barra lateral esquerda.
4. Localize o projeto `automanager` na lista e clique no botão de play ao lado dele.
5. Aguarde o log mostrar que a aplicação subiu na porta `8080`.

### Opção 2: Terminal

Na pasta `automanager`:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

Ao iniciar, a aplicação cadastra automaticamente uma empresa de exemplo com um funcionário, um fornecedor, um cliente, um veículo, mercadorias, serviços e duas vendas.

### Banco de dados

O projeto usa H2 em memória, então não é preciso instalar nada. Os dados voltam ao estado inicial sempre que a aplicação reinicia. O console do banco fica em `http://localhost:8080/h2-console`, com a URL JDBC `jdbc:h2:mem:automanager`, usuário `sa` e senha em branco.

Para usar MySQL, substitua o conteúdo de `src/main/resources/application.properties` por:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/base
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
```

## Rotas disponíveis

Todas as entidades seguem o mesmo padrão de rotas. Como os ids do seed podem variar entre execuções, consulte a listagem da entidade antes de usar um id.

| Entidade | Buscar por id | Listar | Cadastrar | Atualizar | Excluir |
|---|---|---|---|---|---|
| Empresa | `GET /empresa/empresa/{id}` | `GET /empresa/empresas` | `POST /empresa/cadastro` | `PUT /empresa/atualizar` | `DELETE /empresa/excluir` |
| Usuário | `GET /usuario/usuario/{id}` | `GET /usuario/usuarios` | `POST /usuario/cadastro/{idEmpresa}` | `PUT /usuario/atualizar` | `DELETE /usuario/excluir` |
| Credencial | `GET /credencial/credencial/{id}` | `GET /credencial/credenciais` | `POST /credencial/cadastro/{idUsuario}` | `PUT /credencial/atualizar` | `DELETE /credencial/excluir` |
| Veículo | `GET /veiculo/veiculo/{id}` | `GET /veiculo/veiculos` | `POST /veiculo/cadastro/{idProprietario}` | `PUT /veiculo/atualizar` | `DELETE /veiculo/excluir` |
| Mercadoria | `GET /mercadoria/mercadoria/{id}` | `GET /mercadoria/mercadorias` | `POST /mercadoria/cadastro/{idEmpresa}` | `PUT /mercadoria/atualizar` | `DELETE /mercadoria/excluir` |
| Serviço | `GET /servico/servico/{id}` | `GET /servico/servicos` | `POST /servico/cadastro/{idEmpresa}` | `PUT /servico/atualizar` | `DELETE /servico/excluir` |
| Venda | `GET /venda/venda/{id}` | `GET /venda/vendas` | `POST /venda/cadastro/{idEmpresa}` | `PUT /venda/atualizar` | `DELETE /venda/excluir` |

Regras gerais:

- No cadastro, o corpo não deve conter `id`, pois ele é gerado pelo banco.
- Em `PUT` e `DELETE`, o corpo precisa conter o `id` do registro.
- No `PUT`, apenas os campos enviados são alterados. Em `quantidade` e `valor`, só valores maiores que zero são aplicados.
- O cadastro de usuário vincula o usuário à empresa informada na rota e exige ao menos um perfil.
- Telefones, documentos, e-mails e endereço são cadastrados e atualizados junto com o usuário ou a empresa. No `PUT`, itens com `id` são atualizados e itens sem `id` são adicionados.
- A senha das credenciais nunca aparece nas respostas.

## Exemplos de corpo das requisições

### Cadastrar empresa

`POST /empresa/cadastro`

```json
{
  "razaoSocial": "Auto Center Volkswagen ltda",
  "nomeFantasia": "VW Auto Center",
  "endereco": {
    "estado": "São Paulo",
    "cidade": "São José dos Campos",
    "bairro": "Centro",
    "rua": "Rua Sete de Setembro",
    "numero": "100",
    "codigoPostal": "12245-000"
  },
  "telefones": [
    { "ddd": "012", "numero": "39001234" }
  ]
}
```

### Cadastrar usuário com credencial

`POST /usuario/cadastro/{idEmpresa}`

Perfis possíveis: `CLIENTE`, `FUNCIONARIO` e `FORNECEDOR`. Tipos de documento: `CPF`, `CNPJ`, `RG`, `CNH` e `PASSAPORTE`.

```json
{
  "nome": "Maria da Silva",
  "nomeSocial": "Maria",
  "perfis": ["CLIENTE"],
  "emails": [
    { "endereco": "maria@email.com" }
  ],
  "telefones": [
    { "ddd": "012", "numero": "988887777" }
  ],
  "documentos": [
    { "tipo": "CPF", "dataEmissao": "2020-05-10", "numero": "11122233344" }
  ],
  "credenciais": [
    { "tipo": "usuarioSenha", "nomeUsuario": "maria", "senha": "123456", "inativo": false }
  ]
}
```

### Cadastrar credencial

`POST /credencial/cadastro/{idUsuario}`

Usuário e senha:

```json
{ "tipo": "usuarioSenha", "nomeUsuario": "maria.loja", "senha": "abc123", "inativo": false }
```

Código de barras:

```json
{ "tipo": "codigoBarra", "codigo": 789456123, "inativo": false }
```

Para desativar uma credencial, use `PUT /credencial/atualizar` com `{ "tipo": "usuarioSenha", "id": 1, "inativo": true }`.

### Cadastrar veículo

`POST /veiculo/cadastro/{idProprietario}`

Tipos possíveis: `HATCH`, `SEDA`, `SUV`, `PICKUP` e `SW`.

```json
{ "tipo": "HATCH", "modelo": "polo", "placa": "XYZ-1234" }
```

### Cadastrar mercadoria

`POST /mercadoria/cadastro/{idEmpresa}`

Para vincular a mercadoria a um fornecedor, acrescente `?fornecedor={idUsuario}` na rota. O usuário precisa ter o perfil `FORNECEDOR`.

```json
{
  "nome": "Pastilha de freio",
  "descricao": "Jogo de pastilhas dianteiras",
  "fabricao": "2026-01-10",
  "validade": "2030-01-10",
  "quantidade": 20,
  "valor": 150.0
}
```

### Cadastrar serviço

`POST /servico/cadastro/{idEmpresa}`

```json
{ "nome": "Troca de óleo", "descricao": "Troca de óleo e filtro", "valor": 120.0 }
```

### Cadastrar venda

`POST /venda/cadastro/{idEmpresa}`

O cliente precisa ter o perfil `CLIENTE` e o funcionário, o perfil `FUNCIONARIO`. O veículo é opcional.

```json
{
  "identificacao": "VENDA-0001",
  "cliente": { "id": 3 },
  "funcionario": { "id": 1 },
  "veiculo": { "id": 1 },
  "mercadorias": [{ "id": 1 }],
  "servicos": [{ "id": 1 }, { "id": 2 }]
}
```

## Status de resposta

| Operação | Sucesso | Erro |
|---|---|---|
| Buscar por id | `302 Found` | `404 Not Found` (registro não existe) |
| Listar | `302 Found` | `404 Not Found` (nenhum registro) |
| Cadastrar | `201 Created` | `409 Conflict` (corpo com `id` ou identificação já usada) e `400 Bad Request` (registro vinculado não existe ou perfil incorreto) |
| Atualizar | `200 OK` | `400 Bad Request` (registro não existe) |
| Excluir | `200 OK` | `400 Bad Request` (registro não existe) |

Ao excluir um registro, o sistema remove antes os vínculos com os demais. Por exemplo, excluir um usuário desvincula ele da empresa, das vendas e dos veículos, sem apagar essas entidades.

## HATEOAS

As respostas das buscas trazem links para navegar pela API:

- Na listagem, cada item recebe um link `self` apontando para o próprio registro.
- Na busca por id, o registro recebe um link para a listagem completa da sua entidade.

## Testando a API

As buscas respondem com status `302 Found`. Desative a opção de seguir redirecionamentos na ferramenta de testes (no Insomnia: Preferences, General, desmarcar "Follow redirects").

Roteiro sugerido:

1. `GET /usuario/usuarios` para descobrir os ids do funcionário, do fornecedor e do cliente do seed.
2. `GET /mercadoria/mercadorias` e `GET /servico/servicos` para os ids das mercadorias e serviços.
3. `POST /usuario/cadastro/1` com o exemplo de usuário.
4. `POST /veiculo/cadastro/{idDoNovoUsuario}` com o exemplo de veículo.
5. `POST /venda/cadastro/1` usando os ids encontrados.
6. `GET /venda/vendas` para conferir a venda criada com seus links.
7. `PUT` e `DELETE` em qualquer entidade, informando o `id` no corpo.

## Troubleshooting

### `405 Method Not Allowed`

O método HTTP escolhido não corresponde ao da rota. Confira o método na tabela de rotas.

### `404 Not Found` em uma rota que deveria existir

As rotas de busca repetem o nome da entidade, como `/usuario/usuarios` e `/usuario/usuario/1`. Se a URL estiver correta, o `404` significa que o registro não existe ou que a lista está vazia.

### `400 Bad Request` ao cadastrar uma venda

Algum id informado não existe, o cliente não tem o perfil `CLIENTE` ou o funcionário não tem o perfil `FUNCIONARIO`. Confira os ids e perfis com `GET /usuario/usuarios`.

### `500 Internal Server Error` ao cadastrar

Algum campo obrigatório ficou de fora, como os campos do endereço, a `dataEmissao` do documento ou as datas da mercadoria, ou um valor único foi repetido, como o número de um documento.

### Erro ao cadastrar credencial

O campo `tipo` define o formato da credencial: `usuarioSenha` exige `nomeUsuario` e `senha`, e `codigoBarra` exige `codigo`. Sem o campo `tipo`, a credencial é tratada como `usuarioSenha`.

### Resposta vazia ou estranha nas buscas

A ferramenta está seguindo o redirecionamento do status `302`. Desative a opção "Follow redirects".

### `Port 8080 was already in use`

Já existe uma execução anterior do projeto na porta `8080`. Encerre-a e inicie novamente.

### Imports de `org.springframework.hateoas` não encontrados

As dependências do Maven não foram recarregadas. No VS Code, execute `Java: Clean Java Language Server Workspace` pela paleta de comandos, ou rode `./mvnw clean install`.

### Erros de getters e setters não encontrados

O Lombok não está ativo na IDE. No VS Code, instale o **Extension Pack for Java**, que já inclui o suporte.

### `release version 17 not supported`

O JDK instalado é anterior ao Java 17. Instale o JDK 17 ou superior e confira com `java -version`.
