# ☕ Java Café - Point of Sale (POS) System

> Sistema desktop de **Ponto de Venda (PDV)** desenvolvido em **Java** e **JavaFX** para gerenciamento de pedidos, estoque e relatórios de uma cafeteria.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge\&logo=java\&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge\&logo=apache-maven\&logoColor=white)

Este projeto foi desenvolvido como trabalho prático para a disciplina de **Programação Orientada a Objetos (POO)** do **Instituto de Ciências Matemáticas e de Computação (ICMC-USP)**.

---

# 📌 Visão Geral

O **Java Café** é uma aplicação desktop que simula um sistema de vendas para cafeteria, permitindo:

* registrar pedidos e pagamentos;
* controlar o catálogo e o estoque de produtos;
* gerar relatórios de vendas;
* persistir os dados da aplicação em arquivos `.csv`.

A aplicação foi construída com **JavaFX** para a interface gráfica e organizada com uma estrutura baseada em **MVC**, separando interface, regras de negócio e entidades do domínio.

---

# ✨ Funcionalidades

## 🛒 1. Módulo de Pedidos

Responsável pelo fluxo de venda no caixa.

* Seleção de produtos por meio da interface gráfica;
* montagem e atualização do carrinho de compras;
* cálculo automático de subtotal, total e valores da compra;
* escolha do método de pagamento;
* validação do pagamento;
* finalização do pedido e registro da venda.

## 📦 2. Módulo de Estoque

Responsável pelo gerenciamento dos produtos disponíveis na cafeteria.

* Visualização do catálogo de produtos;
* cadastro de novos produtos;
* edição de produtos existentes;
* remoção de produtos;
* atualização de quantidade em estoque;
* baixa automática no estoque após a conclusão de uma venda;
* organização dos produtos por categorias.

## 📊 3. Módulo de Relatórios

Responsável por consultar o histórico de vendas e apresentar métricas do sistema.

* Leitura do histórico de vendas armazenado em arquivo;
* resumo de vendas;
* cálculo de receita;
* análise de produtos mais vendidos;
* acompanhamento de dados gerais da operação da cafeteria.

---

# 🏗️ Arquitetura do Projeto

O sistema foi estruturado com base em uma separação de responsabilidades inspirada no padrão **MVC (Model-View-Controller)**, complementada por uma **camada de serviços** para concentrar a lógica de negócio.

## Camadas principais

### **Model**

Contém as classes do domínio da aplicação, como produtos, pedidos e demais entidades relacionadas ao funcionamento do sistema.

Também possui um subpacote `enums`, utilizado para armazenar enumerações ligadas ao modelo.

### **Controller**

Responsável por intermediar a comunicação entre a interface gráfica e a lógica da aplicação.

Os controllers recebem as ações do usuário na interface JavaFX e delegam o processamento para as classes de serviço.

### **Service**

Contém as regras de negócio do sistema, como:

* manipulação de pedidos;
* atualização de estoque;
* processamento de vendas;
* geração de relatórios;
* persistência e leitura de dados.

Também possui um subpacote `enums`, utilizado para enumerações específicas da camada de serviço, quando necessário.

### **Util**

Reúne classes utilitárias e auxiliares usadas por diferentes partes do sistema.

### **Exception**

Contém exceções personalizadas da aplicação, usadas para representar erros de negócio de forma mais clara e organizada.

### **Resources**

Armazena os arquivos usados pela interface JavaFX:

* `fxml/` → telas da aplicação;
* `css/` → estilos visuais;
* `images/` → imagens e ícones utilizados no sistema;
* `images/products/` → imagens dos produtos cadastrados.

---

# 📁 Estrutura de Diretórios

A estrutura atual do projeto está organizada da seguinte forma:

```text
JavaCafe/
├── .idea/
├── data/
├── src/
│   └── main/
│       ├── java/
│       │   ├── controller/
│       │   ├── exception/
│       │   ├── model/
│       │   │   └── enums/
│       │   ├── service/
│       │   │   └── enums/
│       │   └── util/
│       └── resources/
│           ├── css/
│           ├── fxml/
│           └── images/
│               └── products/
└── target/
    ├── classes/
    └── generated-sources/
```

---

# 💾 Persistência de Dados

O projeto **não utiliza banco de dados relacional**.
A persistência é feita por meio de **arquivos `.csv`** armazenados na pasta `data/`.

Essa abordagem permite:

* salvar produtos e/ou vendas entre execuções do programa;
* restaurar informações da aplicação ao iniciar novamente;
* manter o projeto mais simples e alinhado ao escopo acadêmico da disciplina.

---

# ⚠️ Tratamento de Exceções

O sistema utiliza **exceções customizadas** para tratar situações de erro de forma organizada e mais próxima das regras do negócio.

Exemplos de situações tratadas:

* tentativa de venda de produto sem estoque suficiente;
* pagamento inválido;
* entrada de dados inconsistentes;
* falhas em operações relacionadas ao catálogo ou ao pedido.

As exceções ficam centralizadas no pacote:

```text
src/main/java/exception
```

---

# 🧠 Conceitos de POO Aplicados

Durante o desenvolvimento do projeto, foram aplicados conceitos importantes de **Programação Orientada a Objetos**, como:

* **Encapsulamento**: proteção dos atributos e controle de acesso aos dados das entidades;
* **Abstração**: separação entre a regra de negócio e a interface gráfica;
* **Herança**: reaproveitamento e organização da estrutura entre classes, quando necessário;
* **Polimorfismo**: flexibilidade na modelagem de comportamentos e tratamento de diferentes tipos de objetos;
* **Responsabilidade única**: separação entre camadas (`controller`, `service`, `model`, `util` e `exception`).

---

# 🖥️ Tecnologias Utilizadas

* **Java 17+**
* **JavaFX**
* **Maven**
* **CSS** para estilização da interface
* **FXML** para definição das telas
* **CSV / File I/O** para persistência de dados

---

# 🚀 Como Executar o Projeto

## Pré-requisitos

Antes de executar o sistema, você precisa ter instalado:

* **JDK 17** ou superior
* **Maven**
* Uma IDE Java, como:

    * IntelliJ IDEA
    * Eclipse
    * VS Code

---

## 1. Clonar o repositório

```bash
git clone https://github.com/GustavoFantato/javacafe-project
cd javacafe-project
```

---

## 2. Compilar o projeto

No terminal, dentro da pasta do projeto, execute:

```bash
mvn clean install
```

Esse comando irá:

* baixar as dependências do projeto;
* compilar o código-fonte;
* gerar os arquivos compilados na pasta `target/`.

---

## 3. Executar a aplicação

Se o projeto estiver configurado com JavaFX via Maven, a execução pode ser feita com:

```bash
mvn javafx:run
```

Se preferir, também é possível abrir o projeto em uma IDE e executá-lo por lá, utilizando a classe principal da aplicação.

---

# 📂 Recursos da Interface

Os arquivos da interface gráfica ficam organizados em:

* `src/main/resources/fxml` → telas em JavaFX;
* `src/main/resources/css` → estilos visuais;
* `src/main/resources/images` → imagens gerais do sistema;
* `src/main/resources/images/products` → imagens de produtos.

Essa separação facilita a manutenção da interface e deixa o projeto mais organizado.

---

# 🎯 Objetivo Acadêmico

Este projeto foi desenvolvido com foco em praticar:

* modelagem orientada a objetos;
* organização de um projeto Java em camadas;
* construção de interfaces gráficas com JavaFX;
* manipulação de arquivos para persistência;
* aplicação prática de conceitos de POO em um sistema completo.

---

# 👨‍💻 Autores

Projeto desenvolvido para a disciplina de **Programação Orientada a Objetos** no **ICMC-USP**.

**Alunos:**

* Gustavo Fantato Fernandes
* Victor Kayky Zaneti Antunes
* Renan Silva Blasques 
