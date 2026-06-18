package controller;

import exception.InvalidPaymentException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import model.Order;
import model.OrderItem;
import model.Product;
import model.enums.Category;
import service.CheckoutService;
import service.InventoryService;
import service.enums.PaymentMethods;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static model.enums.Size.*;

public class Main extends Application {

    private static InventoryService inventoryService;
    private static CheckoutService checkoutService;
    private static Stage primaryStage;
    private static final String STORAGE_PATH = "./data/storage.csv";
    private static final String SALES_PATH = "./data/sales.csv";

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
                ((ReportsController) controller).setSalesFilePath(SALES_PATH);
            }

            // Se for a primeira inicialização
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, 1100, 700);
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

        // Alimenta dados iniciais caso o estoque esteja completamente zerado
        if (inventoryService.getStorageList().isEmpty()) {
            System.out.println("[Menu] Estoque vazio detectado! Gerando itens iniciais de teste...");

            Product cafeM = new Product(1, "Cappuccino Italiano", 8.50, 50, "cappuccino.png", M, Category.BEVERAGE, "Café espresso com leite vaporizado e canela.");
            Product cafeP = new Product(2, "Espresso Simples", 4.50, 4, "espresso.png", P, Category.BEVERAGE, "Café espresso curto e encorpado (Estoque Baixo).");
            Product bolo = new Product(3, "Bolo de Cenoura", 7.00, 15, "bolo.png", G, Category.FOOD, "Fatia de bolo artesanal com calda de chocolate.");

            inventoryService.addProductStorage(cafeM);
            inventoryService.addProductStorage(cafeP);
            inventoryService.addProductStorage(bolo);
        }

        // ⚠️ REMOVIDO: O bloco antigo de simulação de venda foi retirado daqui
        // para impedir a gravação fantasma/duplicada no banco CSV ao iniciar o app.

        System.out.println("\n=== 🖥️ ABRINDO INTERFACE GRÁFICA JAVAFX ===");
        launch(args);
    }
}