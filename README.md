# javacafe-project

```shell
 $ tree
    java-cafe-pos/
        │
        ├── data/                       # Pasta local para os arquivos de texto
        │   ├── estoque.csv
        │   └── vendas.csv
        │
        └── src/
        └── main/
        ├── java/                       # Código Java direto na raiz, sem com/javacafe/
        │   ├── controller.Main.java               # Ponto de entrada do sistema
        │   ├── module-info.java        # Configuração de módulos do JavaFX
        │   │
        │   ├── model/                  # Entidades (Classes puras)
        │   │   ├── Product.java
        │   │   ├── OrderItem.java
        │   │   └── Order.java
        │   │
        │   ├── service/                # Regras de negócio e arquivos
        │   │   ├── InventoryService.java
        │   │   └── CheckoutService.java
        │   │
        │   ├── exception/              # Exceções personalizadas
        │   │   ├── OutOfStockException.java
        │   │   └── InvalidPaymentException.java
        │   │
        │   └── controller/             # Controladores da Interface Gráfica
        │       ├── OrderEntryController.java
        │       ├── InventoryController.java
        │       └── ReportsController.java
        │
        └── resources/                  # Arquivos visuais organizados direto na raiz
        ├── fxml/
        │   ├── order_entry.fxml
        │   ├── inventory.fxml
        │   └── reports.fxml
        └── css/
            └── style.css
```