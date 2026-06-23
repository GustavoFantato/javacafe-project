package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import model.Product;
import model.enums.Category;
import service.CheckoutService;
import service.InventoryService;
import service.ReportsService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static model.enums.Size.*;

public class Main extends Application {

    private static InventoryService inventoryService;
    private static CheckoutService checkoutService;
    private static ReportsService reportsService;
    private static Stage primaryStage;
    private static final String STORAGE_PATH = "./data/storage.csv";
    private static final String SALES_PATH = "./data/sales.csv";
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 1000;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            // Garante a existência do diretório de dados
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            // Abre a tela inicial padrão (Pedidos)
            changeScene("/fxml/order_entry.fxml");

            primaryStage.setTitle("Java Café POS");
            URL iconUrl = Main.class.getResource("/images/app-icon.png");
            if (iconUrl != null) {
                primaryStage.getIcons().setAll(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("CRITICAL: Falha ao carregar a interface gráfica do JavaFX.");
            e.printStackTrace();
        }
    }

    /**
     * Troca de cena injetando dinamicamente as dependências dos microsserviços
     */
    public static void changeScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            BorderPane root = loader.load();

            // Injeta as dependências necessárias no controller que acabou de ser inflado
            Object controller = loader.getController();
            if (controller instanceof OrderEntryController) {
                ((OrderEntryController) controller).setServices(inventoryService, checkoutService);
            } else if (controller instanceof InventoryController) {
                ((InventoryController) controller).setService(inventoryService);
            } else if (controller instanceof ReportsController) {
                ((ReportsController) controller).setReportsService(reportsService);
            }

            // Se for a primeira inicialização
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
                // Vincula o arquivo de estilo higienizado de forma segura
                scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                // Reaproveita a Scene alterando apenas o nó raiz (evita flickering e perda de foco)
                primaryStage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            System.err.println("Erro crítico ao navegar para a tela: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Evita quebras de parseamento decimal no CSV (força ponto ao invés de vírgula)
        Locale.setDefault(Locale.US);

        System.out.println("=== ☕ INICIANDO INFRAESTRUTURA DO JAVA CAFÉ ===");

        // Inicialização dos serviços globais
        inventoryService = new InventoryService(STORAGE_PATH);
        checkoutService = new CheckoutService(inventoryService, SALES_PATH);
        reportsService = new ReportsService(SALES_PATH);

        // Alimenta dados iniciais caso o estoque esteja completamente zerado
        if (inventoryService.getStorageList().isEmpty()) {
            System.out.println("[Menu] Estoque vazio detectado! Gerando itens iniciais de teste...");
            for (Product product : buildDefaultCatalog()) {
                inventoryService.addProductStorage(product);
            }
        }

        // ⚠️ REMOVIDO: O bloco antigo de simulação de venda foi retirado daqui
        // para impedir a gravação fantasma/duplicada no banco CSV ao iniciar o app.

        System.out.println("\n=== 🖥️ ABRINDO INTERFACE GRÁFICA JAVAFX ===");
        launch(args);
    }

    private static List<Product> buildDefaultCatalog() {
        return List.of(
                new Product(1, "Cappuccino Cremoso - P", 6.50, 18, "images/products/cappuccino.jpg",
                        P, Category.BEVERAGE, "Cafe espresso com leite vaporizado e espuma aveludada."),
                new Product(2, "Cappuccino Cremoso - M", 8.50, 22, "images/products/cappuccino.jpg",
                        M, Category.BEVERAGE, "Versao media do cappuccino da casa com canela."),
                new Product(3, "Cappuccino Cremoso - G", 10.50, 16, "images/products/cappuccino.jpg",
                        G, Category.BEVERAGE, "Versao grande para quem quer um cafe mais encorpado."),
                new Product(4, "Espresso Duplo", 5.00, 28, "images/products/espresso.jpg",
                        P, Category.BEVERAGE, "Dose dupla de espresso intenso e aromatico."),
                new Product(5, "Latte Baunilha", 9.50, 15, "images/products/latte.jpg",
                        M, Category.BEVERAGE, "Cafe com leite cremoso e toque suave de baunilha."),
                new Product(6, "Mocha Gelado", 12.00, 11, "images/products/mocha.jpg",
                        G, Category.BEVERAGE, "Bebida gelada de cafe com chocolate e chantilly."),
                new Product(7, "Croissant de Presunto", 11.00, 12, "images/products/croissant.jpg",
                        M, Category.FOOD, "Croissant amanteigado recheado com presunto e queijo."),
                new Product(8, "Pao de Queijo Recheado", 7.50, 24, "images/products/pao-de-queijo.jpg",
                        P, Category.FOOD, "Pao de queijo assado com recheio cremoso de catupiry."),
                new Product(9, "Sanduiche Natural", 13.50, 10, "images/products/sanduiche.jpg",
                        G, Category.FOOD, "Sanduiche fresco com frango, folhas e molho leve."),
                new Product(10, "Torta Integral de Frango", 12.50, 9, "images/products/sanduiche.jpg",
                        M, Category.FOOD, "Fatia de torta salgada com massa integral e recheio de frango."),
                new Product(11, "Brownie com Nozes", 9.00, 14, "images/products/brownie.jpg",
                        M, Category.DESSERTS, "Brownie de chocolate intenso com pedacos de noz."),
                new Product(12, "Cheesecake de Frutas Vermelhas", 14.00, 8, "images/products/cheesecake.jpg",
                        G, Category.DESSERTS, "Cheesecake cremoso coberto com calda de frutas vermelhas."),
                new Product(13, "Cookie Triplo Chocolate", 6.00, 20, "images/products/cookie.jpg",
                        P, Category.DESSERTS, "Cookie macio com gotas de chocolate ao leite, branco e meio amargo."),
                new Product(14, "Bolo de Cenoura com Calda", 8.50, 13, "images/products/brownie.jpg",
                        G, Category.DESSERTS, "Fatia generosa de bolo de cenoura com calda de chocolate.")
        );
    }
}