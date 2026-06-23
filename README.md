# ☕ Java Café - Point of Sale (POS) System

> Sistema Desktop de Ponto de Venda (PDV) desenvolvido em **Java** e **JavaFX** para gestão completa de uma cafeteria.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

Este projeto foi desenvolvido como trabalho prático para a disciplina de **Programação Orientada a Objetos (POO)** do Instituto de Ciências Matemáticas e de Computação (**ICMC-USP**).

---

## 📌 Funcionalidades

O sistema é dividido em três módulos principais, acessíveis através de uma interface gráfica amigável:

### 🛒 1. Entrada de Pedidos (Order Entry)
* Interface com botões rápidos para seleção de produtos.
* Atualização em tempo real do carrinho de compras (subtotal, taxas e total).
* Validação e processamento de pagamentos (Cartão, Pix e Dinheiro com cálculo de troco).
* Emissão de recibos e finalização de vendas.

### 📦 2. Gestão de Estoque (Inventory)
* Visualização completa do catálogo de produtos (Bebidas Quentes, Bebidas Frias e Comidas).
* Adição, edição e remoção de produtos do catálogo.
* Dedução automática de estoque após cada venda.
* Alertas visuais para produtos com **estoque baixo** (abaixo do limiar configurável).

### 📊 3. Relatórios (Sales Reports)
* Leitura do histórico de vendas e geração de métricas financeiras.
* Resumo de vendas diárias, semanais ou por hora.
* Identificação automática do **Top 3 produtos mais vendidos**.
* Divisão de receita por método de pagamento.

---

## 🏗️ Arquitetura e Decisões de Design

O projeto foi construído utilizando uma variação do padrão **MVC (Model-View-Controller)** com uma forte **Camada de Serviços (Service Layer)** para isolar as regras de negócio da interface gráfica.

* **View (`.fxml` / `.css`):** Interface construída puramente em JavaFX e estilizada com CSS.
* **Controller:** Classes responsáveis por capturar as interações do usuário e realizar a injeção de dependências dos serviços.
* **Model:** Entidades puras do domínio (`Product`, `Order`, `OrderItem`), protegidas via **Encapsulamento**.
* **Service:** Classes que concentram a lógica pesada (`CheckoutService`, `InventoryService`, `ReportsService`), aplicando conceitos como **Polimorfismo** (Padrão Strategy nos pagamentos).

### 💾 Persistência de Dados
O sistema **não** utiliza um banco de dados relacional. Toda a persistência é feita através de **File I/O** em arquivos `.csv` (armazenados na pasta `data/`). A aplicação é capaz de recuperar o estado anterior de estoque e os IDs incrementais das transações ao ser reiniciada.

### ⚠️ Tratamento de Exceções Customizadas
Foram implementadas exceções próprias para lidar com erros de negócio de forma elegante, tais como:
* `OutOfStockException`: Bloqueia vendas de itens sem estoque disponível.
* `InvalidPaymentException`: Bloqueia pagamentos em dinheiro com valores insuficientes.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* **Java Development Kit (JDK)** versão 17 ou superior.
* **Maven** instalado na máquina.
* IDE de sua preferência (IntelliJ IDEA, Eclipse, VS Code).

### Passo a Passo

1. **Clone este repositório:**
```bash
   git clone [https://github.com/seu-usuario/javacafe-project.git](https://github.com/seu-usuario/javacafe-project.git)